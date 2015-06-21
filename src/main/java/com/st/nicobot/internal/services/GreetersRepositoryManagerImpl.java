package com.st.nicobot.internal.services;

import com.st.nicobot.api.domain.model.GreetersMemory;
import com.st.nicobot.bot.NicoBot;
import com.st.nicobot.services.GreetersRepositoryManager;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackUser;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.*;

/**
 * Created by Logs on 06-06-15.
 */
@Service
public class GreetersRepositoryManagerImpl implements GreetersRepositoryManager, Serializable {

    @Autowired
    private NicoBot nicoBot;

    private GreetersMemory memory;

    private static String MEMORY_FILE_NAME = "GreetersMemory.ser";

    private boolean memoryLoaded = false;

    @PostConstruct
    private boolean loadFile() {
        if (!memoryLoaded) {
            try {
                FileInputStream fin = new FileInputStream(MEMORY_FILE_NAME);
                ObjectInputStream ois = new ObjectInputStream(fin);
                memory = (GreetersMemory) ois.readObject();
                fin.close();
                memoryLoaded = true;
            } catch (FileNotFoundException fe) {
                memory = new GreetersMemory();
                memory.setCollectionWeek(DateTime.now().getWeekOfWeekyear());
                memory.setWeeklyGreeters(new HashMap<>());
                memory.setAllTimeGreeters(new HashMap<>());
                memoryLoaded = true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    private boolean writeFile() {
        try {
            FileOutputStream fout = new FileOutputStream(MEMORY_FILE_NAME);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(memory);
            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void addGreeters(SlackChannel channel, Set<SlackUser> users) {
        if (memory.getCollectionWeek() != DateTime.now().getWeekOfWeekyear()) {
            memory.setWeeklyGreeters(new HashMap<>());
            memory.setCollectionWeek(DateTime.now().getWeekOfWeekyear());
        }

        if (memory.getWeeklyGreeters().get(channel.getId()) == null) {
            memory.getWeeklyGreeters().put(channel.getId(), new HashMap<>());
        }

        if (memory.getAllTimeGreeters().get(channel.getId()) == null) {
            memory.getAllTimeGreeters().put(channel.getId(), new HashMap<>());
        }

        for (SlackUser user : users) {
            if (memory.getWeeklyGreeters().get(channel.getId()).get(user.getId()) == null) {
                memory.getWeeklyGreeters().get(channel.getId()).put(user.getId(), 0);
            }

            if (memory.getAllTimeGreeters().get(channel.getId()).get(user.getId()) == null) {
                memory.getAllTimeGreeters().get(channel.getId()).put(user.getId(), 0);
            }
            memory.getWeeklyGreeters().get(channel.getId()).put(user.getId(), memory.getWeeklyGreeters().get(channel.getId()).get(user.getId()) + 1);
            memory.getAllTimeGreeters().get(channel.getId()).put(user.getId(), memory.getAllTimeGreeters().get(channel.getId()).get(user.getId()) + 1);
        }
        writeFile();
    }

    @Override
    public Map<SlackUser, Integer> getWeeklyGreeters(SlackChannel channel) {
        Map<SlackUser, Integer> users = new HashMap<>();
        if (memory.getWeeklyGreeters() != null && !memory.getWeeklyGreeters().isEmpty() && memory.getWeeklyGreeters().get(channel.getId()) != null) {
            for (Map.Entry<String, Integer> entry : memory.getWeeklyGreeters().get(channel.getId()).entrySet()) {
                users.put(nicoBot.findUserById(entry.getKey()), entry.getValue());
            }
            users = entriesSortedByValues(users);
        }
        return users;
    }

    @Override
    public Map<SlackUser, Integer> getAllTimeGreeters(SlackChannel channel) {
        Map<SlackUser, Integer> users = new HashMap<>();
        if (memory.getAllTimeGreeters() != null && !memory.getAllTimeGreeters().isEmpty() && memory.getAllTimeGreeters().get(channel.getId()) != null) {
            for (Map.Entry<String, Integer> entry : memory.getAllTimeGreeters().get(channel.getId()).entrySet()) {
                users.put(nicoBot.findUserById(entry.getKey()), entry.getValue());
            }

            users = entriesSortedByValues(users);
        }

        return users;
    }

    private <K, V extends Comparable<? super V>> Map<K, V> entriesSortedByValues(Map<K, V> map) {
        SortedSet<Map.Entry<K, V>> sortedEntries = new TreeSet<>(
                (e1, e2) -> {
                    int res = e2.getValue().compareTo(e1.getValue());
                    return res != 0 ? res : 1;
                }
        );
        sortedEntries.addAll(map.entrySet());
        LinkedHashMap<K, V> outputMap = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : sortedEntries) {
            outputMap.put(entry.getKey(), entry.getValue());
        }
        return outputMap;
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
            for (Map.Entry<String, Integer> entry : impl.entriesSortedByValues(entry1.getValue()).entrySet()) {
                System.out.println("User ID : " + entry.getKey() + " , points: " + entry.getValue());
            }
        }

        System.out.println("\nAll Time Greeters :");
        for (Map.Entry<String, Map<String, Integer>> entry1 : impl.memory.getAllTimeGreeters().entrySet()) {
            System.out.println("Chan ID : " + entry1.getKey());
            for (Map.Entry<String, Integer> entry : impl.entriesSortedByValues(entry1.getValue()).entrySet()) {
                System.out.println("User ID : " + entry.getKey() + " , points: " + entry.getValue());
            }
        }
    }
}
