package com.st.nicobot.bot.handler;

import com.st.nicobot.bot.NicoBot;
import com.st.nicobot.bot.event.MessageEvent;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Gere les messages de CHAN
 * @author Logan
 */
public abstract class AbstractMessageEvent implements MessageEvent {

    @Autowired
    private NicoBot nicoBot;

    @Override
    public void onEvent(SlackMessagePosted message, SlackSession session) {
        if(!nicoBot.isSelfMessage(message) && nicoBot.getChannels().contains(message.getChannel())) {
            onMessage(message);
        }
    }

}
