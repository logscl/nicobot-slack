package com.st.nicobot.services;

import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;

import java.util.Map;
import java.util.Set;

public interface LeetGreetingService {

    void init();
    void finish();

    /**
     * Parse le message, et si nécessaire, ajoute le greeter dans la liste des greeters
     * @param message le message envoyé
     */
    void addGreeter(SlackMessagePosted message);

    boolean isLeetHourActive();
    Map<SlackChannel,Set<SlackUser>> getGreeters();
}
