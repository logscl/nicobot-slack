package com.st.nicobot.bot;

import com.st.nicobot.bot.utils.Emoji;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackMessageHandle;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;

import java.io.IOException;
import java.util.Collection;

/**
 * Created by Logs on 09-05-15.
 */
public interface NicoBot {

    void connect() throws IOException;

    SlackMessageHandle sendMessage(SlackChannel channel, SlackUser sender, String message);

    SlackMessageHandle sendMessage(SlackChannel channel, SlackUser sender, String message, Emoji emoji);

    SlackMessageHandle sendMessage(SlackMessagePosted originator, String message);

    SlackMessageHandle sendMessage(SlackMessagePosted originator, String message, Emoji emoji, boolean placeReactionOnBotMsg);

    boolean isSelfMessage(SlackMessagePosted message);

    String getBotName();

    Collection<SlackChannel> getChannels();

    SlackChannel findChannelByName(String channelName);

    SlackUser findUserByUserName(String userName);

    SlackUser findUserById(String userId);
}
