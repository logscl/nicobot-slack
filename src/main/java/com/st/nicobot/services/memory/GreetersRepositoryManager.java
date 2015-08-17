package com.st.nicobot.services.memory;

import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackUser;

import java.util.Map;
import java.util.Set;

/**
 * Created by Logs on 06-06-15.
 */
public interface GreetersRepositoryManager {

    void addGreeters(SlackChannel channel, Set<SlackUser> users);

    Map<SlackUser, Integer> getWeeklyGreeters(SlackChannel channel);

    Map<SlackUser, Integer> getAllTimeGreeters(SlackChannel channel);
}
