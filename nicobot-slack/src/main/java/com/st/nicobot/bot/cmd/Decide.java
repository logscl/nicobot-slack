package com.st.nicobot.bot.cmd;

import com.st.nicobot.bot.NicoBot;
import com.st.nicobot.bot.utils.Option;
import com.st.nicobot.services.Messages;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Logs on 31-01-16.
 */
@Service
public class Decide extends NiCommand {

    @Autowired
    private NicoBot nicobot;

    @Autowired
    private Messages messages;

    private static final String COMMAND = "!decide";
    private static final String FORMAT = "!decide choix 1, [choix2]... [ou choix 3]";
    private static final String DESC = "Demande à Nicobot de prendre une décision sur un choix important";

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

        try {
            DecideArguments arguments = new DecideArguments(args);

            if (arguments.choices.size() == 1) {
                if(RandomUtils.nextInt(0,2) == 1) {
                    nicobot.sendMessage(opts.message, messages.getMessage("yes"));
                } else {
                    nicobot.sendMessage(opts.message, messages.getMessage("no"));
                }
            } else {
                int index = RandomUtils.nextInt(0, arguments.choices.size());
                boolean none = RandomUtils.nextInt(0,100) == 50;

                if(none) {
                    nicobot.sendMessage(opts.message, messages.getMessage("noneOfThem", arguments.choices.size()));
                } else {
                    nicobot.sendMessage(opts.message, arguments.choices.get(index) + " !");
                }

            }
        } catch (IllegalArgumentException e) {
            nicobot.sendMessage(opts.message, "Malformed command, format : " + getFormat());
        }
    }


    private class DecideArguments {
        private List<String> choices = new ArrayList<>();

        public DecideArguments(String[] args) throws IllegalArgumentException {
            if(args != null && args.length > 0) {
                String fullArgs = StringUtils.join(args, " "); // Rebuild args to regex it
                String[] splits = fullArgs.split(",|ou");
                for(String str : splits) {
                    String cleanedString = StringUtils.trim(str);
                    cleanedString = StringUtils.trim(StringUtils.removeEnd(cleanedString, "?"));
                    choices.add(cleanedString);
                }
            } else {
                throw new IllegalArgumentException("Too few arguments");
            }
        }
    }


}
