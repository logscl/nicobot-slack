package com.st.nicobot.bot.handler;

import com.st.nicobot.bot.NicoBot;
import com.st.nicobot.bot.cmd.NiCommand;
import com.st.nicobot.bot.utils.Option;
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
    public void onMessage(SlackMessagePosted slackMessage) {
        String message = slackMessage.getMessageContent();
        //on extrait <cmd> <reste>
        String[] arguments = message.split(" ");

        if(arguments.length >= 1) {
            String[] commandArgs = null;

            if(arguments.length > 1) {
                // on extrait de la chaine uniquement la partie contenant les arguments
                String commandsString = message.substring(message.indexOf(arguments[1]));
                commandArgs = NiCommand.getArgs(commandsString);
            }

            commands.getFirstLink().handle(arguments[0], commandArgs, new Option(slackMessage));
        } else {
            nicoBot.sendMessage(slackMessage, "T'es con ou quoi ? Une commande, c'est \"<commande> [params]\"");
        }
    }
}
