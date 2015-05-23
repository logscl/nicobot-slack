package com.st.nicobot.bot;

import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackMessage;
import com.ullink.slack.simpleslackapi.SlackUser;

import java.io.IOException;
import java.util.Collection;

/**
 * Created by Logs on 09-05-15.
 */
public interface NicoBot {

    void connect() throws IOException;

    void sendMessage(SlackChannel channel, SlackUser sender, String message);

    void sendMessage(SlackMessage originator, String message);

    boolean isSelfMessage(SlackMessage message);

    Collection<SlackChannel> getChannels();

    SlackChannel findChannelByName(String channelName);

    SlackUser findUserByUserName(String userName);
}
