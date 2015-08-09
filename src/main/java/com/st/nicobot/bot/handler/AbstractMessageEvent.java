package com.st.nicobot.bot.handler;

import com.st.nicobot.bot.NicoBot;
import com.st.nicobot.bot.event.MessageEvent;
import com.st.nicobot.services.Commands;
import com.ullink.slack.simpleslackapi.SlackMessage;
import com.ullink.slack.simpleslackapi.SlackSession;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Gere les messages de CHAN
 * @author Logan
 */
public abstract class AbstractMessageEvent implements MessageEvent {

    @Autowired
    private NicoBot nicoBot;

    @Autowired
    private Commands commands;

    @Override
    public void onSessionLoad(SlackSession session) {
        // do nothing for now
    }

    @Override
    public void onMessage(SlackMessage message) {
        if(!nicoBot.isSelfMessage(message)) {
            boolean isCommand = commands.handleCommandEvent(message);

            if(!isCommand && nicoBot.getChannels().contains(message.getChannel())) {
                onEvent(message);
            }
        }
    }
}
