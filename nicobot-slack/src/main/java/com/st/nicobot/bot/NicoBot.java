package com.st.nicobot.bot;

import com.st.nicobot.bot.utils.Emoji;
import com.ullink.slack.simpleslackapi.*;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.replies.SlackMessageReply;

import java.io.IOException;

/**
 * Created by Logs on 09-05-15.
 */
public interface NicoBot {

    void connect() throws IOException;

    SlackMessageHandle<SlackMessageReply> sendMessage(SlackChannel channel, SlackUser sender, String message);

    SlackMessageHandle<SlackMessageReply> sendMessage(SlackChannel channel, SlackUser sender, String message, SlackAttachment attachment);

    SlackMessageHandle<SlackMessageReply> sendMessage(SlackChannel channel, SlackUser sender, String message, Emoji emoji);

    SlackMessageHandle<SlackMessageReply> sendMessage(SlackMessagePosted originator, String message);

    SlackMessageHandle<SlackMessageReply> sendMessage(SlackMessagePosted originator, String message, SlackAttachment attachment);

    SlackMessageHandle<SlackMessageReply> sendMessage(SlackMessagePosted originator, String message, Emoji emoji, boolean placeReactionOnBotMsg);

    SlackMessageHandle<SlackMessageReply> sendPrivateMessage(SlackMessagePosted originator, String message);

    SlackMessageHandle<SlackMessageReply> sendFile(SlackMessagePosted originator, byte[] fileBytes, String fileName);

    SlackUser findUser(String userQuery);

    boolean isSelfMessage(SlackMessagePosted message);

    SlackSession getSession();
}
