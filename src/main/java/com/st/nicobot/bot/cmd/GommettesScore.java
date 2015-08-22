package com.st.nicobot.bot.cmd;

import com.st.nicobot.bot.NicoBot;
import com.st.nicobot.bot.utils.GommetteColor;
import com.st.nicobot.bot.utils.Option;
import com.st.nicobot.services.memory.GommettesRepositoryManager;
import com.st.nicobot.services.Messages;
import com.ullink.slack.simpleslackapi.SlackUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Created by Logs on 17-08-15.
 */
@Service
public class GommettesScore extends NiCommand {

    private static final String COMMAND = "!gommettesScore";
    private static final String FORMAT = "!gommettesScore [nickName|BEST|TOP]";
    private static final String DESC = "Donne la liste des gommettes de l'utilisateur (BEST pour le meilleur, TOP pour le classement) (ou de soi si pas de paramètre passé)";

    @Autowired
    private NicoBot nicobot;

    @Autowired
    private Messages messages;

    @Autowired
    private GommettesRepositoryManager repositoryManager;

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
        GommettesScoreArguments arguments = new GommettesScoreArguments(args);

        SlackUser target = opts.message.getSender();

        if(arguments.user != null) {
            target = arguments.user;
        }

        Map<GommetteColor, Integer> gomm;
        if(arguments.bestUser) {
            gomm = repositoryManager.getBestGommettes();

            if(gomm != null)  {
                sendGreetings(gomm, opts, target);
            } else {
                nicobot.sendMessage(opts.message, messages.getOtherMessage("gmNoBest"));
            }

        } else if(arguments.topUsers) {
            nicobot.sendMessage(opts.message, buildTopUsers(repositoryManager.getGommettesTop()));
        } else {
            gomm = repositoryManager.getGommettes(target);

            if (gomm == null) {
                nicobot.sendMessage(opts.message, String.format(messages.getOtherMessage("gmScoreEmpty"), target.getUserName()));
            } else {
                sendGreetings(gomm,opts,target);
            }
        }
    }

    private String buildTopUsers(Map<SlackUser, Integer> users) {
        StringBuilder message = new StringBuilder(messages.getOtherMessage("gmTopUsers"));
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

    private void sendGreetings(Map<GommetteColor, Integer> gomm, Option opts, SlackUser target) {
        int green = gomm.get(GommetteColor.GREEN) != null ? gomm.get(GommetteColor.GREEN) : 0;
        int red = gomm.get(GommetteColor.RED) != null ? gomm.get(GommetteColor.RED) : 0;

        String greenPlural = green > 1 ? "s" : "";
        String redPlural = red > 1 ? "s" : "";

        nicobot.sendMessage(opts.message, String.format(messages.getOtherMessage("gmScore"), target.getUserName(), green, greenPlural, greenPlural, red, redPlural, redPlural));
    }

    private class GommettesScoreArguments {

        SlackUser user;
        boolean bestUser = false;
        boolean topUsers = false;

        public GommettesScoreArguments(String[] args) {
            if(args != null && args.length > 0) {
                if(args[0].toLowerCase().equals("best")) {
                    bestUser = true;
                } else if(args[0].toLowerCase().equals("top")) {
                    topUsers = true;
                } else {
                    user = nicobot.findUserByUserName(args[0]);
                }
            }
        }
    }
}
