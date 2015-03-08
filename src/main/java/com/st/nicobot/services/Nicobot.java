package com.st.nicobot.services;

import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackMessage;
import com.ullink.slack.simpleslackapi.SlackSession;

/**
 * Created by Logs on 08-03-15.
 */
public interface Nicobot {

    void connect();

    void sendMessage(SlackChannel channel, String message);

    boolean isSelfMessage(SlackMessage message);
}
