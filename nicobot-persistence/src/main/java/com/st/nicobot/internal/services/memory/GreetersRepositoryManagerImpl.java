package com.st.nicobot.internal.services.memory;

import com.st.nicobot.api.domain.model.GreetersMemory;
import com.st.nicobot.db.tables.records.GreeterRecord;
import com.st.nicobot.services.memory.GreetersRepositoryManager;
import com.st.nicobot.utils.MapUtils;
import org.joda.time.DateTime;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.InsertValuesStep4;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.st.nicobot.db.Tables.GREETER;
import static org.jooq.impl.DSL.count;
import static org.jooq.impl.DSL.trueCondition;

/**
 * Created by Logs on 06-06-15.
 */
@Service
public class GreetersRepositoryManagerImpl extends AbstractRepositoryManager<GreetersMemory> implements GreetersRepositoryManager {

    private GreetersMemory memory;

    @Autowired
    private DSLContext create;

    @Override
    protected String getMemoryFileName() {
        return "GreetersMemory.ser";
    }

    @Override
    public GreetersMemory getMemory() {
        return memory;
    }

    @Override
    protected void setMemory(Object memory) {
        this.memory = (GreetersMemory) memory;
    }

    @Override
    protected void initMemory() {
        this.memory = new GreetersMemory();
        this.memory.setCollectionWeek(DateTime.now().getWeekOfWeekyear());
        this.memory.setWeeklyGreeters(new HashMap<>());
        this.memory.setAllTimeGreeters(new HashMap<>());
    }

    @Override
    @Transactional
    public void addGreeters(String channel, Set<String> users) {
        if(!users.isEmpty()) {
            InsertValuesStep4<GreeterRecord, String, Integer, Integer, String> insertinto = create.insertInto(GREETER, GREETER.USER_ID, GREETER.YEAR, GREETER.WEEK, GREETER.CHANNEL_ID);
            for (String user : users) {
                insertinto = insertinto.values(user, DateTime.now().getYear(), DateTime.now().getWeekOfWeekyear(), channel);
            }
            insertinto.execute();
        }

        if (memory.getCollectionWeek() != DateTime.now().getWeekOfWeekyear()) {
            memory.setWeeklyGreeters(new HashMap<>());
            memory.setCollectionWeek(DateTime.now().getWeekOfWeekyear());
        }

        memory.getWeeklyGreeters().putIfAbsent(channel, new HashMap<>());

        memory.getAllTimeGreeters().putIfAbsent(channel, new HashMap<>());

        for (String user : users) {
            memory.getWeeklyGreeters().get(channel).putIfAbsent(user, 0);
            memory.getAllTimeGreeters().get(channel).putIfAbsent(user, 0);

            memory.getWeeklyGreeters().get(channel).put(user, memory.getWeeklyGreeters().get(channel).get(user) + 1);
            memory.getAllTimeGreeters().get(channel).put(user, memory.getAllTimeGreeters().get(channel).get(user) + 1);
        }
        writeFile();
    }

    @Override
    public Map<String, Integer> getWeeklyGreeters(String channel) {
        return getGreeters(channel, DateTime.now().getYear(), DateTime.now().getWeekOfWeekyear(), false);
    }

    @Override
    public Map<String, Integer> getAllTimeGreeters(String channel) {
        return getGreeters(channel, DateTime.now().getYear(), DateTime.now().getWeekOfWeekyear(), true);
    }

    private Map<String, Integer> getGreeters(String channel, int year, int week, boolean allTime) {
        Condition condition = trueCondition();
        condition = condition
                .and(GREETER.CHANNEL_ID.equal(channel))
                .and(GREETER.YEAR.equal(year));
        if(!allTime) {
            condition = condition.and(GREETER.WEEK.equal(week));
        }
        return create
                .select(GREETER.USER_ID, count(GREETER.USER_ID).as("countbyweek"))
                .from(GREETER)
                .where(condition)
                .groupBy(GREETER.USER_ID)
                .orderBy(count(GREETER.USER_ID).desc())
                .fetch()
                .stream().collect(MapUtils.toLinkedMap(r -> r.get(GREETER.USER_ID),r -> r.get("countbyweek", Integer.class)));
    }

    public static void main(String[] args) {
        GreetersRepositoryManagerImpl impl = new GreetersRepositoryManagerImpl();
        impl.loadFile();

        System.out.print("Collection Week : " + impl.memory.getCollectionWeek());
        if (impl.memory.getCollectionWeek() == DateTime.now().getWeekOfWeekyear()) {
            System.out.print(" (This week)");
        } else {
            System.out.println(" (This week is " + DateTime.now().getWeekOfWeekyear() + ")");
        }

        System.out.println("\nWeekly Greeters :");
        for (Map.Entry<String, Map<String, Integer>> entry1 : impl.memory.getWeeklyGreeters().entrySet()) {
            System.out.println("Chan ID : " + entry1.getKey());
            for (Map.Entry<String, Integer> entry : MapUtils.entriesSortedByValues(entry1.getValue()).entrySet()) {
                System.out.println("User ID : " + entry.getKey() + " , points: " + entry.getValue());
            }
        }

        System.out.println("\nAll Time Greeters :");
        for (Map.Entry<String, Map<String, Integer>> entry1 : impl.memory.getAllTimeGreeters().entrySet()) {
            System.out.println("Chan ID : " + entry1.getKey());
            for (Map.Entry<String, Integer> entry : MapUtils.entriesSortedByValues(entry1.getValue()).entrySet()) {
                System.out.println("User ID : " + entry.getKey() + " , points: " + entry.getValue());
            }
        }
    }
}
