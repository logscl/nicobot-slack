package com.st.nicobot.bot.cmd;

import com.st.nicobot.bot.NicoBot;
import com.st.nicobot.bot.utils.Emoji;
import com.st.nicobot.bot.utils.GommetteColor;
import com.st.nicobot.bot.utils.Option;
import com.st.nicobot.services.Messages;
import com.st.nicobot.services.UsernameService;
import com.st.nicobot.services.memory.GommettesRepositoryManager;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Logs on 17-08-15.
 */
@Service
public class Gommette extends NiCommand {

    private static Logger logger = LoggerFactory.getLogger(Gommette.class);

    private static final String COMMAND = "!gommette";
    private static final String FORMAT = "!gommette <rouge|verte|score"/*+"|best"*/+"|top> <nickname> [\"raison\"]";
    private static final String DESC = "Attribue une gommette rouge ou verte à l'utilisateur <nickname>.";

    private static final int VOTE_TIMER_MINUTES = 5;
    private static final int TICK_DURATION_SECONDS = 10;
    private static final int TICKS = VOTE_TIMER_MINUTES * 60 / TICK_DURATION_SECONDS;
    private static final int GRACE_TICKS = 2 * 60 / TICK_DURATION_SECONDS;

    private static final int MIN_VOTES_TRIGGER = 3;

    @Autowired
    private NicoBot nicobot;

    @Autowired
    private Messages messages;

    @Autowired
    private GommettesRepositoryManager repositoryManager;

    @Autowired
    private UsernameService usernameService;

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
    public List<String> getAliases() {
        return Collections.singletonList("!gom");
    }

    @Override
    @Async
    protected void doCommand(String command, String[] args, Option opts) {
        try {
            GommetteArguments arguments = new GommetteArguments(args);

            if(arguments.subCmd != null) {
                switch (arguments.subCmd) {
                    case GREEN:
                    case RED:
                        doPollingMode(arguments, args, opts);
                        break;
                    //case BEST:
                        //doBestUserMode(arguments, args, opts);
                        //break;
                    case SCORE:
                        doScoreMode(arguments, args, opts);
                        break;
                    case TOP:
                        doTopMode(arguments, args, opts);
                        break;
                }
            } else {
                nicobot.sendMessage(opts.message,messages.getMessage("gmWrongArgs"));
            }

        } catch (IllegalArgumentException e) {
            nicobot.sendMessage(opts.message, "Malformed command, format : " + getFormat());
        }
    }

    private void doPollingMode(GommetteArguments arguments, String[] args, Option opts) {
        if(isPollAlreadyRunning()) {
            nicobot.sendMessage(opts.message, messages.getMessage("gmPollRunning"));
            return;
        }

        if(arguments.user == null) {
            nicobot.sendMessage(opts.message, messages.getMessage("gmUnknownUser",args[1]));
            return;
        }

        if(StringUtils.isBlank(arguments.reason)) {
            nicobot.sendMessage(opts.message, messages.getMessage("gmStartNoReason",opts.message.getSender().getUserName(), arguments.gommette.getGommetteName(), arguments.user.getUserName(), VOTE_TIMER_MINUTES));
        } else {
            nicobot.sendMessage(opts.message, messages.getMessage("gmStartReason",opts.message.getSender().getUserName(), arguments.gommette.getGommetteName(), arguments.user.getUserName(), arguments.reason, VOTE_TIMER_MINUTES));
        }

        new Thread() {
            @Override
            public void run() {
                try {
                    listener = new GommetteEventListener(nicobot, messages, arguments.user);
                    listener.addInitiatorVote(opts.message.getSender());
                    nicobot.getSession().addMessagePostedListener(listener);
                    synchronized (this) {
                        int i = 0;
                        int warns = 0;
                        while(i < TICKS) {
                            if(i> GRACE_TICKS) {
                                // pas de vote depuis 1 min
                                if(listener.getLastVoteDate().isBefore(DateTime.now().minusMinutes(1))) {
                                    warns++;
                                } else {
                                    warns = 0;
                                }

                                if(warns == 2) {
                                    nicobot.sendMessage(opts.message, messages.getMessage("gmCloseSoon"));
                                } else if(warns == 3) {
                                    nicobot.sendMessage(opts.message, messages.getMessage("gmPollClosed"));
                                    break;
                                }
                            }

                            this.wait(TICK_DURATION_SECONDS * 1000);
                            i++;
                        }
                    }

                    nicobot.getSession().removeMessagePostedListener(listener);

                    if(listener.getTotalVotes() >= MIN_VOTES_TRIGGER) {
                        if(listener.getVoteYesCount() > listener.getVoteNoCount()) {
                            Emoji emoji = arguments.gommette == GommetteColor.GREEN ? Emoji.GOMMETTE : Emoji.GOMMETTE_ROUGE;
                            nicobot.sendMessage(opts.message, messages.getMessage("gmVoteValid", listener.getVoteYesCount(), listener.getVoteNoCount(), arguments.user.getUserName(), arguments.gommette.getGommetteName()), emoji, true);
                            repositoryManager.addGommette(arguments.user.getId(), arguments.gommette);
                        } else if(listener.getVoteYesCount() == listener.getVoteNoCount()) {
                            boolean valid = RandomUtils.nextInt(0,2) == 1;
                            nicobot.sendMessage(opts.message,messages.getMessage("gmVoteEquality", valid ? "oui": "non"));
                            if(valid) {
                                repositoryManager.addGommette(arguments.user.getId(), arguments.gommette);
                            }
                        } else {
                            nicobot.sendMessage(opts.message, messages.getMessage("gmVoteInvalid", opts.message.getSender().getUserName(), arguments.user.getUserName()));
                        }
                    } else {
                        nicobot.sendMessage(opts.message, messages.getMessage("gmInsufficient"));
                    }
                }  catch (InterruptedException e) {
                    logger.error("Error in waiting task", e);
                } finally {
                    listener = null;
                }
            }
        }.start();
    }

    private void doBestUserMode(GommetteArguments arguments, String[] args, Option opts) {
        Map<GommetteColor, Integer> gomm = repositoryManager.getBestGommettes();

        if(gomm != null)  {
            sendGreetings(gomm, opts, opts.message.getSender());
        } else {
            nicobot.sendMessage(opts.message, messages.getMessage("gmNoBest"));
        }
    }

    private void doScoreMode(GommetteArguments arguments, String[] args, Option opts) {
        SlackUser target = arguments.user != null ? arguments.user : opts.message.getSender();
        Map<GommetteColor, Integer> gomm = repositoryManager.getGommettes(target.getId());

        if (gomm == null) {
            nicobot.sendMessage(opts.message, messages.getMessage("gmScoreEmpty", usernameService.getNoHLName(target)));
        } else {
            sendGreetings(gomm,opts,target);
        }
    }

    private void doTopMode(GommetteArguments arguments, String[] args, Option opts) {
        nicobot.sendMessage(opts.message, buildTopUsers(repositoryManager.getGommettesTop()));
    }

    private String buildTopUsers(Map<String, Integer> users) {
        StringBuilder message = new StringBuilder(messages.getMessage("gmTopUsers"));
        if(users != null && !users.isEmpty()) {
            for (Map.Entry<String, Integer> user : users.entrySet()) {
                SlackUser username = nicobot.getSession().findUserById(user.getKey());
                Map<GommetteColor, Integer> gomm = repositoryManager.getGommettes(username.getId());
                int green = gomm.get(GommetteColor.GREEN) != null ? gomm.get(GommetteColor.GREEN) : 0;
                int red = gomm.get(GommetteColor.RED) != null ? gomm.get(GommetteColor.RED) : 0;
                message.append(usernameService.getNoHLName(username)).append(" (*").append(user.getValue()).append("* [").append(green).append("|").append(red).append("]), ");
            }
            message.delete(message.lastIndexOf(","), message.length());
        } else {
            message.append(messages.getMessage("noOne"));
        }
        return message.toString();
    }

    private void sendGreetings(Map<GommetteColor, Integer> gomm, Option opts, SlackUser target) {
        int green = gomm.get(GommetteColor.GREEN) != null ? gomm.get(GommetteColor.GREEN) : 0;
        int red = gomm.get(GommetteColor.RED) != null ? gomm.get(GommetteColor.RED) : 0;

        String greenPlural = green > 1 ? "s" : "";
        String redPlural = red > 1 ? "s" : "";

        nicobot.sendMessage(opts.message, messages.getMessage("gmScore", usernameService.getNoHLName(target), green, greenPlural, greenPlural, red, redPlural, redPlural));
    }

    private class GommetteArguments {
        private GommetteColor gommette = null;
        private GommetteSubCmd subCmd = null;
        private SlackUser user = null;
        private String reason = null;

        public GommetteArguments(String[] args) throws IllegalArgumentException {
            if(args != null && args.length > 0) {
                subCmd = GommetteSubCmd.getSubCmdByArgStr(args[0]);
                if(subCmd != null) {
                    if(subCmd.color != null) {
                        gommette = subCmd.color;
                        user = nicobot.getSession().findUserByUserName(args[1]);
                        if(args.length > 2) {
                            reason = args[2];
                        }
                    } else if(subCmd == GommetteSubCmd.SCORE) {
                        if(args.length > 1) {
                            user = nicobot.getSession().findUserByUserName(args[1]);
                        }
                    }
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
        private DateTime lastVoteDate;

        public GommetteEventListener(NicoBot nicobot, Messages messages, SlackUser target) {
            this.nicobot = nicobot;
            this.messages = messages;
            this.target = target;
            this.lastVoteDate = DateTime.now();
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
                nicobot.sendMessage(event, messages.getMessage("gmNoVote"));
                return;
            }

            if(votes.get(event.getSender()) == null) {
                if (trollMessage()) {
                    nicobot.sendMessage(event, messages.getMessage("gmTrollVote", event.getSender().getUserName()));
                    votes.put(event.getSender(), GommetteVoteType.CANCELLED);
                } else if (getVoteYesStr().equals(message)) {
                    votes.put(event.getSender(), GommetteVoteType.YES);
                } else if (getVoteNoStr().equals(message)) {
                    votes.put(event.getSender(), GommetteVoteType.NO);
                }
                lastVoteDate = DateTime.now();
            } else {
                nicobot.sendMessage(event, messages.getMessage("gmVoteOnce", event.getSender().getUserName()));
            }
        }

        public void addInitiatorVote(SlackUser initiator) {
            this.votes.put(initiator, GommetteVoteType.YES);
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

        public DateTime getLastVoteDate() {
            return lastVoteDate;
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

    private enum GommetteSubCmd {
        GREEN("verte", GommetteColor.GREEN),
        RED("rouge", GommetteColor.RED),
        SCORE("score"),
        BEST("best"),
        TOP("top");

        private String txtArg;
        private GommetteColor color;

        GommetteSubCmd(String txtArg, GommetteColor color) {
            this.txtArg = txtArg;
            this.color = color;
        }

        GommetteSubCmd(String txtArg) {
            this(txtArg,null);
        }

        private static GommetteSubCmd getSubCmdByArgStr(String txtArg) {
            for(GommetteSubCmd subcmd : GommetteSubCmd.values()) {
                if(txtArg.toLowerCase().equals(subcmd.txtArg)) {
                    return subcmd;
                }
            }
            return null;
        }
    }

    private enum GommetteVoteType {
        YES,
        NO,
        CANCELLED
    }

}
