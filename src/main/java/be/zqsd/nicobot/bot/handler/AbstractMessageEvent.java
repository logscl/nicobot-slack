package be.zqsd.nicobot.bot.handler;

import be.zqsd.nicobot.bot.NicoBot;
import be.zqsd.nicobot.bot.event.MessageEvent;
import be.zqsd.nicobot.services.Commands;
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

    @Autowired
    private Commands commands;

    @Override
    public void onEvent(SlackMessagePosted message, SlackSession session) {
        if (!nicoBot.isSelfMessage(message) && !commands.isProbableCommand(message.getMessageContent())) {
            if (nicoBot.getSession().getChannels().contains(message.getChannel())) {
                onMessage(message);
            }
        }
    }
}
