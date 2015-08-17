package com.st.nicobot.bot.cmd;

import com.st.nicobot.bot.NicoBot;
import com.st.nicobot.bot.utils.Emoji;
import com.st.nicobot.bot.utils.GommetteColor;
import com.st.nicobot.bot.utils.Option;
import com.st.nicobot.services.memory.GommettesRepositoryManager;
import com.st.nicobot.services.Messages;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Logs on 17-08-15.
 */
@Service
public class Gommette extends NiCommand {

    private static Logger logger = LoggerFactory.getLogger(Gommette.class);

    private static final String COMMAND = "!gommette";
    private static final String FORMAT = "!gommette <rouge|verte> <nickname> [\"raison\"]";
    private static final String DESC = "Attribue une gommette rouge ou verte à l'utilisateur <nickname>.";

    private static final int VOTE_TIMER = 60;
    private static final int MIN_VOTES_TRIGGER = 3;

    @Autowired
    private NicoBot nicobot;

    @Autowired
    private Messages messages;

    @Autowired
    private GommettesRepositoryManager repositoryManager;

    private GommetteEventListener listener = null;

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
    @Async
    protected void doCommand(String command, String[] args, Option opts) {
        try {
            if(isPollAlreadyRunning()) {
                nicobot.sendMessage(opts.message, messages.getOtherMessage("gmPollRunning"));
                return;
            }

            GommetteArguments arguments = new GommetteArguments(args);
            if (arguments.gommette == null) {
                nicobot.sendMessage(opts.message, messages.getOtherMessage("gmMissingColor"));
            }

            if(arguments.user == null) {
                nicobot.sendMessage(opts.message, String.format(messages.getOtherMessage("gmUnknownUser"),args[1]));
            }

            if(StringUtils.isBlank(arguments.reason)) {
                nicobot.sendMessage(opts.message, String.format(messages.getOtherMessage("gmStartNoReason"),opts.message.getSender().getUserName(), arguments.gommette.getGommetteName(), arguments.user.getUserName(), VOTE_TIMER));
            } else {
                nicobot.sendMessage(opts.message, String.format(messages.getOtherMessage("gmStartReason"),opts.message.getSender().getUserName(), arguments.gommette.getGommetteName(), arguments.user.getUserName(), arguments.reason, VOTE_TIMER));
            }

            new Thread() {
                @Override
                public void run() {
                    try {
                        listener = new GommetteEventListener(nicobot, messages, arguments.user);
                        nicobot.addMessagePostedListener(listener);
                        synchronized (this) {
                            this.wait(VOTE_TIMER * 1000);
                        }

                        nicobot.removeMessagePostedListener(listener);

                        if(listener.getTotalVotes() >= MIN_VOTES_TRIGGER) {
                            if(listener.getVoteYesCount() > listener.getVoteNoCount()) {
                                Emoji emoji = arguments.gommette == GommetteColor.GREEN ? Emoji.GOMMETTE : Emoji.GOMMETTE_ROUGE;
                                nicobot.sendMessage(opts.message, String.format(messages.getOtherMessage("gmVoteValid"), listener.getVoteYesCount(), listener.getVoteNoCount(), arguments.user.getUserName(), arguments.gommette.getGommetteName()), emoji, true);
                                repositoryManager.addGommette(arguments.user, arguments.gommette);
                            } else if(listener.getVoteYesCount() == listener.getVoteNoCount()) {
                                boolean valid = RandomUtils.nextInt(0,2) == 1;
                                nicobot.sendMessage(opts.message, String.format(messages.getOtherMessage("gmVoteEquality"), valid ? "oui": "non"));
                                if(valid) {
                                    repositoryManager.addGommette(arguments.user, arguments.gommette);
                                }
                            } else {
                                nicobot.sendMessage(opts.message, String.format(messages.getOtherMessage("gmVoteInvalid"), opts.message.getSender().getUserName(), arguments.user.getUserName()));
                            }
                        } else {
                            nicobot.sendMessage(opts.message, messages.getOtherMessage("gmInsufficient"));
                        }
                    }  catch (InterruptedException e) {
                        logger.error("Error in waiting task", e);
                    } finally {
                        listener = null;
                    }
                }
            }.start();

        } catch (IllegalArgumentException e) {
            nicobot.sendMessage(opts.message, "Malformed command, format : " + getFormat());
        }
    }

    private class GommetteArguments {
        private GommetteColor gommette = null;
        private SlackUser user = null;
        private String reason = null;

        public GommetteArguments(String[] args) throws IllegalArgumentException {
            if(args != null && args.length > 1) {
                gommette = GommetteColor.getGommetteByName(args[0]);
                user = nicobot.findUserByUserName(args[1]);

                if(args.length > 2) {
                    reason = args[2];
                }
            } else {
                throw new IllegalArgumentException("Too few arguments");
            }
        }
    }

    private boolean isPollAlreadyRunning() {
        return listener != null;
    }

    private class GommetteEventListener implements SlackMessagePostedListener {

        private NicoBot nicobot;
        private Messages messages;
        private SlackUser target;
        private Map<SlackUser, GommetteVoteType> votes = new HashMap<>();

        public GommetteEventListener(NicoBot nicobot, Messages messages, SlackUser target) {
            this.nicobot = nicobot;
            this.messages = messages;
            this.target = target;
        }

        @Override
        public void onEvent(SlackMessagePosted event, SlackSession session) {
            String message = StringUtils.trim(event.getMessageContent());

            if(nicobot.isSelfMessage(event)) {
                return;
            }

            if(!isVote(message)) {
                return;
            }

            if(event.getSender().getId().equals(target.getId())) {
                nicobot.sendMessage(event, messages.getOtherMessage("gmNoVote"));
                return;
            }

            if(votes.get(event.getSender()) == null) {
                if (trollMessage()) {
                    nicobot.sendMessage(event, String.format(messages.getOtherMessage("gmTrollVote"), event.getSender().getUserName()));
                    votes.put(event.getSender(), GommetteVoteType.CANCELLED);
                } else if (getVoteYesStr().equals(message)) {
                    votes.put(event.getSender(), GommetteVoteType.YES);
                } else if (getVoteNoStr().equals(message)) {
                    votes.put(event.getSender(), GommetteVoteType.NO);
                }
            } else {
                nicobot.sendMessage(event, String.format(messages.getOtherMessage("gmVoteOnce"), event.getSender().getUserName()));
            }
        }

        public String getVoteYesStr() {
            return "!oui";
        }

        public String getVoteNoStr() {
            return "!non";
        }

        public boolean isVote(String message) {
            return message.equals(getVoteYesStr()) || message.equals(getVoteNoStr());
        }


        boolean trollMessage() {
            return RandomUtils.nextInt(0,100) == 50;
        }

        public int getVoteYesCount() {
            return getVoteCount(GommetteVoteType.YES);
        }

        public int getVoteNoCount() {
            return getVoteCount(GommetteVoteType.NO);
        }

        public int getVoteCount(GommetteVoteType type) {
            int count = 0;
            for(Map.Entry<SlackUser, GommetteVoteType> entry : votes.entrySet()) {
                if(entry.getValue() == type) {
                    count ++;
                }
            }
            return count;
        }

        public int getTotalVotes() {
            return votes.size();
        }
    }

    private enum GommetteVoteType {
        YES,
        NO,
        CANCELLED
    }

}
