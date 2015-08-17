package com.st.nicobot.bot.cmd;

import com.st.nicobot.bot.NicoBot;
import com.st.nicobot.bot.utils.Option;
import com.st.nicobot.services.GreetersRepositoryManager;
import com.st.nicobot.services.Messages;
import com.ullink.slack.simpleslackapi.SlackUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Created by Logs on 21-07-15.
 */
@Service
public class TopHGT extends NiCommand {

    private static final String COMMAND = "!topHGT";
    private static final String FORMAT = "!topHGT";
    private static final String DESC = "Donne le top score au HGT";

    @Autowired
    private NicoBot nicobot;

    @Autowired
    private GreetersRepositoryManager greeters;

    @Autowired
    private Messages messages;

    @Override
    public String getCommandName() {
        return COMMAND;
    }

    @Override
    public String getDescription() {
        return DESC;
    }

    @Override
    public String getFormat() {
        return FORMAT;
    }

    @Override
    protected void doCommand(String command, String[] args, Option opts) {
        nicobot.sendMessage(opts.message, buildTopUsers(greeters.getAllTimeGreeters(opts.message.getChannel())));
    }

    private String buildTopUsers(Map<SlackUser, Integer> users) {
        StringBuilder message = new StringBuilder(messages.getOtherMessage("allTopHGT"));
        if(users != null && !users.isEmpty()) {
            for (Map.Entry<SlackUser, Integer> user : users.entrySet()) {
                message.append(user.getKey().getUserName()).append(" (").append(user.getValue()).append("), ");
            }
            message.delete(message.lastIndexOf(","), message.length());
        } else {
            message.append(messages.getOtherMessage("noOne"));
        }
        return message.toString();
    }
}
