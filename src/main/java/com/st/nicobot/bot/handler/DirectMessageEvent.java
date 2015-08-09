package com.st.nicobot.bot.handler;

import com.st.nicobot.bot.NicoBot;
import com.st.nicobot.services.Commands;
import com.ullink.slack.simpleslackapi.SlackMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Gere les messages de CHAN
 * @author Logan
 */
@Service
public class DirectMessageEvent extends AbstractMessageEvent {

    @Autowired
    private NicoBot nicoBot;

    @Autowired
    private Commands commands;

    @Override
    public void onMessage(SlackMessage message) {
        if(!nicoBot.isSelfMessage(message) && !nicoBot.getChannels().contains(message.getChannel())) {
            onEvent(message);
        }
    }

    @Override
    public void onEvent(SlackMessage message) {
        commands.handleCommandEvent(message);
    }
}
