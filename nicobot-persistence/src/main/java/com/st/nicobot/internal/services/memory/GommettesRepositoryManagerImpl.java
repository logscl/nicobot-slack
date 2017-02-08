package com.st.nicobot.internal.services.memory;

import com.st.nicobot.api.domain.model.GommettesMemory;
import com.st.nicobot.bot.utils.GommetteColor;
import com.st.nicobot.db.tables.records.GommetteRecord;
import com.st.nicobot.services.memory.GommettesRepositoryManager;
import org.joda.time.DateTime;
import org.jooq.DSLContext;
import org.jooq.Record4;
import org.jooq.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

import static com.st.nicobot.db.Tables.GOMMETTE;
import static org.jooq.impl.DSL.*;

/**
 * Created by Logs on 17-08-15.
 */
@Service
public class GommettesRepositoryManagerImpl extends AbstractRepositoryManager<GommettesMemory> implements GommettesRepositoryManager {

    @Autowired
    private DSLContext create;

    private GommettesMemory memory;

    private static final int GREEN_COEFF = 2;
    private static final int RED_COEFF = -1;

    @Override
    protected String getMemoryFileName() {
        return "GommettesMemory.ser";
    }

    @Override
    public GommettesMemory getMemory() {
        return memory;
    }

    @Override
    protected void setMemory(Object memory) {
        this.memory = (GommettesMemory) memory;
    }

    @Override
    protected void initMemory() {
        this.memory = new GommettesMemory();
        this.memory.setGommettes(new HashMap<>());
    }

    @Override
    @Transactional
    public void addGommette(GommetteRecord record) {
        create.executeInsert(record);

        if(record.getValid()) {
            if (memory.getGommettes() == null) {
                memory.setGommettes(new HashMap<>());
            }

            String user = record.getUserId();
            GommetteColor color = GommetteColor.values()[record.getType()];

            memory.getGommettes().putIfAbsent(user, new HashMap<>());

            if (memory.getGommettes().get(user).get(color) == null) {
                memory.getGommettes().get(user).put(color, 1);
            } else {
                memory.getGommettes().get(user).put(color, memory.getGommettes().get(user).get(color) + 1);
            }
            writeFile();
        }
    }

    @Override
    public Map<GommetteColor, Integer> getGommettes(String user) {
        Map<GommetteColor, Integer> map = new HashMap<>();
        create
                .select(GOMMETTE.TYPE, count().as("count"))
                .from(GOMMETTE)
                .where(GOMMETTE.USER_ID.equal(user))
                .and(GOMMETTE.VALID.isTrue())
                .and(year(GOMMETTE.DATE).equal(DateTime.now().getYear()))
                .groupBy(GOMMETTE.TYPE)
                .fetch()
                .forEach(r -> {
                    map.put(GommetteColor.values()[r.get(GOMMETTE.TYPE)], r.get("count", Integer.class));
                });
        return map;
        //return memory.getGommettes().get(user);
    }

    @Override
    public Map<GommetteColor, Integer> getBestGommettes() {
        Map<GommetteColor, Integer> tmp = null;
        int totalTmp = 0;
        boolean exaeqo = false;
        for (Map.Entry<String, Map<GommetteColor, Integer>> entry1 : memory.getGommettes().entrySet()) {
            int count =  (entry1.getValue().get(GommetteColor.GREEN) != null ?  entry1.getValue().get(GommetteColor.GREEN) : 0) + (entry1.getValue().get(GommetteColor.RED) != null ? entry1.getValue().get(GommetteColor.RED) : 0);
            if(count > totalTmp) {
                tmp = entry1.getValue();
                totalTmp = count;
                exaeqo = false;
            } else if(count == totalTmp) {
                exaeqo = true;
            }
        }
        if(!exaeqo) {
            return tmp;
        }
        return null;
    }

    @Override
    public List<GommetteUserScore> getGommettesTop() {
        List<GommetteUserScore> list = new ArrayList<>();
        String previousUserId = null;
        GommetteUserScore currentUserScore = new GommetteUserScore();
        Result<Record4<String, Integer, Integer, BigDecimal>> records =  create
                .select(GOMMETTE.USER_ID, GOMMETTE.TYPE, count().as("count"), sum(GOMMETTE.COEFFICIENT).as("sum"))
                .from(GOMMETTE)
                .where(year(GOMMETTE.DATE).equal(DateTime.now().getYear()))
                .and(GOMMETTE.VALID.isTrue())
                .groupBy(GOMMETTE.USER_ID, GOMMETTE.TYPE)
                .orderBy(GOMMETTE.USER_ID.asc(), GOMMETTE.TYPE.desc())
                .fetch();

        for(Record4<String, Integer, Integer, BigDecimal> record : records) {
            String userId = record.get(GOMMETTE.USER_ID);
            if(previousUserId == null || previousUserId.equals(userId)) {
                previousUserId = userId;
                fillGommetteUserScore(currentUserScore, record);
            } else {
                list.add(currentUserScore);
                currentUserScore = new GommetteUserScore();
                fillGommetteUserScore(currentUserScore, record);
            }
        }
        list.add(currentUserScore);
        Collections.sort(list);
        return list;
    }

    private void fillGommetteUserScore(GommetteUserScore currentUserScore, Record4<String, Integer, Integer, BigDecimal> record) {
        currentUserScore.setUserId(record.get(GOMMETTE.USER_ID));
        GommetteColor color = GommetteColor.values()[record.get(GOMMETTE.TYPE)];
        if(color == GommetteColor.RED) {
            currentUserScore.setRedCount(record.get("count", Integer.class));
            currentUserScore.setRedScore(record.get("sum", BigDecimal.class).intValue());
        } else {
            currentUserScore.setGreenCount(record.get("count", Integer.class));
            currentUserScore.setGreenScore(record.get("sum", BigDecimal.class).intValue());
        }
    }

    @Override
    public String getGommettesFormatted() {
        return create.selectFrom(GOMMETTE).fetch().format();
    }

    public static void main(String[] args) {
        GommettesRepositoryManagerImpl impl = new GommettesRepositoryManagerImpl();
        impl.loadFile();

        if(impl.memory.getGommettes() != null) {
            System.out.println("\nGommettes:");
            for (Map.Entry<String, Map<GommetteColor, Integer>> entry1 : impl.memory.getGommettes().entrySet()) {
                System.out.println("User ID: " + entry1.getKey() + " - " + entry1.getValue().get(GommetteColor.GREEN) + " vertes, " + entry1.getValue().get(GommetteColor.RED) + " rouges.");
            }
        }
    }

    public static class GommetteUserScore implements Comparable<GommetteUserScore> {
        private String userId;
        private int greenCount;
        private int greenScore;
        private int redCount;
        private int redScore;

        public GommetteUserScore() {

        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public int getGreenCount() {
            return greenCount;
        }

        public void setGreenCount(int greenCount) {
            this.greenCount = greenCount;
        }

        public int getGreenScore() {
            return greenScore;
        }

        public void setGreenScore(int greenScore) {
            this.greenScore = greenScore;
        }

        public int getRedCount() {
            return redCount;
        }

        public void setRedCount(int redCount) {
            this.redCount = redCount;
        }

        public int getRedScore() {
            return redScore;
        }

        public void setRedScore(int redScore) {
            this.redScore = redScore;
        }

        public int getTotalScore() {
            return greenScore + redScore;
        }

        @Override
        public int compareTo(GommetteUserScore o) {
            return o.getTotalScore() == this.getTotalScore() ? 0 :
                    o.getTotalScore() > this.getTotalScore() ? 1 : -1;
        }
    }
}
