package com.st.nicobot.bot.cmd;

import com.st.nicobot.bot.NicoBot;
import com.st.nicobot.bot.utils.Option;
import com.st.nicobot.services.UsernameService;
import com.st.nicobot.services.memory.GreetersRepositoryManager;
import com.st.nicobot.services.Messages;
import com.ullink.slack.simpleslackapi.SlackUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by Logs on 21-07-15.
 */
@Service
public class TopHGT extends NiCommand {

    private static final String COMMAND = "!topHGT";
    private static final String FORMAT = "!topHGT";
    private static final String DESC = "Donne le top score au HGT";
    private static final String[] ALIASES = {"!hgt"};

    @Autowired
    private NicoBot nicobot;

    @Autowired
    private GreetersRepositoryManager greeters;

    @Autowired
    private Messages messages;

    @Autowired
    private UsernameService usernameService;

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
    public List<String> getAliases() {
        return Arrays.asList(ALIASES);
    }

    @Override
    protected void doCommand(String command, String[] args, Option opts) {
        nicobot.sendMessage(opts.message, buildTopUsers(greeters.getAllTimeGreeters(opts.message.getChannel())));
    }

    private String buildTopUsers(Map<SlackUser, Integer> users) {
        StringBuilder message = new StringBuilder(messages.getMessage("allTopHGT"));
        if(users != null && !users.isEmpty()) {
            for (Map.Entry<SlackUser, Integer> user : users.entrySet()) {
                message.append(usernameService.getNoHLName(user.getKey())).append(" (").append(user.getValue()).append("), ");
            }
            message.delete(message.lastIndexOf(","), message.length());
        } else {
            message.append(messages.getMessage("noOne"));
        }
        return message.toString();
    }
}
