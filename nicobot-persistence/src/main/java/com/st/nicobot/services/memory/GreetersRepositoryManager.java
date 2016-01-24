package com.st.nicobot.services.memory;

import java.util.Map;
import java.util.Set;

/**
 * Created by Logs on 06-06-15.
 */
public interface GreetersRepositoryManager {

    void addGreeters(String channel, Set<String> users);

    Map<String, Integer> getWeeklyGreeters(String channel);

    Map<String, Integer> getAllTimeGreeters(String channel);
}
