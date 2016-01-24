package com.st.nicobot.internal.services.memory;

import com.st.nicobot.api.domain.model.GreetersMemory;
import com.st.nicobot.services.memory.GreetersRepositoryManager;
import com.st.nicobot.utils.MapUtils;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Logs on 06-06-15.
 */
@Service
public class GreetersRepositoryManagerImpl extends AbstractRepositoryManager<GreetersMemory> implements GreetersRepositoryManager {

    private GreetersMemory memory;

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
    public void addGreeters(String channel, Set<String> users) {
        if (memory.getCollectionWeek() != DateTime.now().getWeekOfWeekyear()) {
            memory.setWeeklyGreeters(new HashMap<>());
            memory.setCollectionWeek(DateTime.now().getWeekOfWeekyear());
        }

        if (memory.getWeeklyGreeters().get(channel) == null) {
            memory.getWeeklyGreeters().put(channel, new HashMap<>());
        }

        if (memory.getAllTimeGreeters().get(channel) == null) {
            memory.getAllTimeGreeters().put(channel, new HashMap<>());
        }

        for (String user : users) {
            if (memory.getWeeklyGreeters().get(channel).get(user) == null) {
                memory.getWeeklyGreeters().get(channel).put(user, 0);
            }

            if (memory.getAllTimeGreeters().get(channel).get(user) == null) {
                memory.getAllTimeGreeters().get(channel).put(user, 0);
            }
            memory.getWeeklyGreeters().get(channel).put(user, memory.getWeeklyGreeters().get(channel).get(user) + 1);
            memory.getAllTimeGreeters().get(channel).put(user, memory.getAllTimeGreeters().get(channel).get(user) + 1);
        }
        writeFile();
    }

    @Override
    public Map<String, Integer> getWeeklyGreeters(String channel) {
        Map<String, Integer> users = new HashMap<>();
        if (memory.getWeeklyGreeters() != null && !memory.getWeeklyGreeters().isEmpty() && memory.getWeeklyGreeters().get(channel) != null) {
            for (Map.Entry<String, Integer> entry : memory.getWeeklyGreeters().get(channel).entrySet()) {
                users.put(entry.getKey(), entry.getValue());
            }
            users = MapUtils.entriesSortedByValues(users);
        }
        return users;
    }

    @Override
    public Map<String, Integer> getAllTimeGreeters(String channel) {
        Map<String, Integer> users = new HashMap<>();
        if (memory.getAllTimeGreeters() != null && !memory.getAllTimeGreeters().isEmpty() && memory.getAllTimeGreeters().get(channel) != null) {
            for (Map.Entry<String, Integer> entry : memory.getAllTimeGreeters().get(channel).entrySet()) {
                users.put(entry.getKey(), entry.getValue());
            }

            users = MapUtils.entriesSortedByValues(users);
        }

        return users;
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
