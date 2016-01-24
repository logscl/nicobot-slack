package com.st.nicobot.bot.handler;

import com.st.nicobot.bot.NicoBot;
import com.st.nicobot.services.Commands;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
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
    public void onEvent(SlackMessagePosted message, SlackSession session) {
        if(!nicoBot.isSelfMessage(message) && !nicoBot.getChannels().contains(message.getChannel())) {
            onMessage(message);
        }
    }

    @Override
    public void onMessage(SlackMessagePosted message) {
        commands.handleCommandEvent(message);
    }
}
