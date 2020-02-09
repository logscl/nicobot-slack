package be.zqsd.nicobot.bot.cmd;

import be.zqsd.gommette.GommetteType;
import be.zqsd.nicobot.bot.NicoBot;
import be.zqsd.nicobot.bot.utils.Emoji;
import be.zqsd.nicobot.bot.utils.Option;
import be.zqsd.nicobot.services.GommetteService;
import be.zqsd.nicobot.services.Messages;
import be.zqsd.nicobot.services.PersistenceService;
import be.zqsd.nicobot.services.UsernameService;
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

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static be.zqsd.gommette.GommetteType.GREEN;

/**
 * Created by Logs on 17-08-15.
 */
@Service
public class Gommette extends NiCommand {

    private static Logger logger = LoggerFactory.getLogger(Gommette.class);

    private static final String COMMAND = "!gommette";
    private static final String FORMAT = "!gommette <rouge|verte|score"/*+"|best"*/ + "|top> <nickname> [\"raison\"]";
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
    private GommetteService gommetteService;

    @Autowired
    private UsernameService usernameService;

    @Autowired
    private PersistenceService persistenceService;

    private GommetteEventListener listener = null;

    public Gommette() {
    }

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

            if (arguments.subCmd != null) {
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
                    //case CSV:
                    //doCsvMode(arguments, args, opts);
                    //break;
                }
            } else {
                nicobot.sendMessage(opts.message, messages.getMessage("gmWrongArgs"));
            }

        } catch (IllegalArgumentException e) {
            nicobot.sendMessage(opts.message, "Malformed command, format : " + getFormat());
        }
    }

    //private void doCsvMode(GommetteArguments arguments, String[] args, Option opts) {
    //String gomCSV = repositoryManager.getGommettesFormatted();
    //nicobot.sendFile(opts.message, gomCSV.getBytes(),"gommettes.csv");
    //nicobot.sendMessage(opts.message, "```"+repositoryManager.getGommettesFormatted()+"```");
    //}

    private void doPollingMode(GommetteArguments arguments, String[] args, Option opts) {
        if (isPollAlreadyRunning()) {
            nicobot.sendMessage(opts.message, messages.getMessage("gmPollRunning"));
            return;
        }

        if (arguments.user == null) {
            nicobot.sendMessage(opts.message, messages.getMessage("gmUnknownUser", args[1]));
            return;
        }

        if (StringUtils.isBlank(arguments.reason)) {
            nicobot.sendMessage(opts.message, messages.getMessage("gmStartNoReason", usernameService.getHLName(opts.message.getSender()), getGommetteName(arguments.gommette), usernameService.getHLName(arguments.user), VOTE_TIMER_MINUTES));
        } else {
            nicobot.sendMessage(opts.message, messages.getMessage("gmStartReason", usernameService.getHLName(opts.message.getSender()), getGommetteName(arguments.gommette), usernameService.getHLName(arguments.user), arguments.reason, VOTE_TIMER_MINUTES));
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
                        while (i < TICKS) {
                            if (i > GRACE_TICKS) {
                                // pas de vote depuis 1 min
                                if (listener.getLastVoteDate().isBefore(DateTime.now().minusMinutes(1))) {
                                    warns++;
                                } else {
                                    warns = 0;
                                }

                                if (warns == 2) {
                                    nicobot.sendMessage(opts.message, messages.getMessage("gmCloseSoon"));
                                } else if (warns == 3) {
                                    nicobot.sendMessage(opts.message, messages.getMessage("gmPollClosed"));
                                    break;
                                }
                            }

                            this.wait(TICK_DURATION_SECONDS * 1000);
                            i++;
                        }
                    }

                    nicobot.getSession().removeMessagePostedListener(listener);

                    if (listener.getTotalVotes() >= MIN_VOTES_TRIGGER) {
                        boolean valid = false;
                        if (listener.getVoteYesCount() > listener.getVoteNoCount()) {
                            Emoji emoji = arguments.gommette == GREEN ? Emoji.GOMMETTE : Emoji.GOMMETTE_ROUGE;
                            nicobot.sendMessage(opts.message, messages.getMessage("gmVoteValid", listener.getVoteYesCount(), listener.getVoteNoCount(), usernameService.getHLName(arguments.user), getGommetteName(arguments.gommette)), emoji, true);
                            valid = true;
                        } else if (listener.getVoteYesCount() == listener.getVoteNoCount()) {
                            valid = RandomUtils.nextInt(0, 2) == 1;
                            nicobot.sendMessage(opts.message, messages.getMessage("gmVoteEquality", valid ? "oui" : "non"));
                        } else {
                            nicobot.sendMessage(opts.message, messages.getMessage("gmVoteInvalid", usernameService.getHLName(opts.message.getSender()), usernameService.getHLName(arguments.user)));
                        }
                        addGommette(arguments, opts, valid);
                    } else {
                        nicobot.sendMessage(opts.message, messages.getMessage("gmInsufficient"));
                    }
                } catch (InterruptedException e) {
                    logger.error("Error in waiting task", e);
                } finally {
                    listener = null;
                }
            }
        }.start();
    }

    private void addGommette(GommetteArguments arguments, Option opts, boolean valid) {
        be.zqsd.gommette.Gommette gommette = new be.zqsd.gommette.Gommette(
                arguments.user.getId(),
                opts.message.getSender().getId(),
                arguments.reason,
                arguments.gommette,
                listener.getVoteYesCount(),
                listener.getVoteNoCount(),
                OffsetDateTime.now(),
                valid
        );
        try {
            persistenceService.addGommette(gommette);
        } catch (Exception e) {
            logger.error("Unable to save gommette", e);
        }
    }

//    private void doBestUserMode(GommetteArguments arguments, String[] args, Option opts) {
//        Map<GommetteColor, Integer> gomm = repositoryManager.getBestGommettes();
//
//        if(gomm != null)  {
//            sendGreetings(gomm, opts, opts.message.getSender());
//        } else {
//            nicobot.sendMessage(opts.message, messages.getMessage("gmNoBest"));
//        }
//    }

    private void doScoreMode(GommetteArguments arguments, String[] args, Option opts) {
        SlackUser target = arguments.user != null ? arguments.user : opts.message.getSender();
        String scoreMessage = gommetteService.getUserScore(target);


        nicobot.sendMessage(opts.message, scoreMessage);
    }

    private void doTopMode(GommetteArguments arguments, String[] args, Option opts) {
        nicobot.sendMessage(opts.message, gommetteService.getCurrentYearScore());
    }

    private class GommetteArguments {
        private GommetteType gommette = null;
        private GommetteSubCmd subCmd = null;
        private SlackUser user = null;
        private String reason = null;

        public GommetteArguments(String[] args) throws IllegalArgumentException {
            if (args != null && args.length > 0) {
                subCmd = GommetteSubCmd.getSubCmdByArgStr(args[0]);
                if (subCmd != null) {
                    if (subCmd.type != null) {
                        gommette = subCmd.type;
                        user = nicobot.findUser(args[1]);
                        if (args.length > 2) {
                            reason = args[2];
                        }
                    } else if (subCmd == GommetteSubCmd.SCORE) {
                        if (args.length > 1) {
                            user = nicobot.findUser(args[1]);
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

            if (nicobot.isSelfMessage(event)) {
                return;
            }

            if (!isVote(message)) {
                return;
            }

            if (event.getSender().getId().equals(target.getId())) {
                nicobot.sendMessage(event, messages.getMessage("gmNoVote"));
                return;
            }

            if (votes.get(event.getSender()) == null) {
                if (trollMessage()) {
                    nicobot.sendMessage(event, messages.getMessage("gmTrollVote", event.getSender().getUserName()));
                    votes.put(event.getSender(), GommetteVoteType.CANCELLED);
                } else if (getVoteYesStr().contains(message)) {
                    votes.put(event.getSender(), GommetteVoteType.YES);
                } else if (getVoteNoStr().contains(message)) {
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
            return RandomUtils.nextInt(0, 100) == 50;
        }

        public int getVoteYesCount() {
            return getVoteCount(GommetteVoteType.YES);
        }

        public int getVoteNoCount() {
            return getVoteCount(GommetteVoteType.NO);
        }

        public int getVoteCount(GommetteVoteType type) {
            int count = 0;
            for (Map.Entry<SlackUser, GommetteVoteType> entry : votes.entrySet()) {
                if (entry.getValue() == type) {
                    count++;
                }
            }
            return count;
        }

        public int getTotalVotes() {
            return votes.size();
        }
    }

    private enum GommetteSubCmd {
        GREEN("verte", GommetteType.GREEN),
        RED("rouge", GommetteType.RED),
        SCORE("score"),
        BEST("best"),
        TOP("top"),
        CSV("csv");

        private String txtArg;
        private GommetteType type;

        GommetteSubCmd(String txtArg, GommetteType color) {
            this.txtArg = txtArg;
            this.type = color;
        }

        GommetteSubCmd(String txtArg) {
            this(txtArg, null);
        }

        private static GommetteSubCmd getSubCmdByArgStr(String txtArg) {
            for (GommetteSubCmd subcmd : GommetteSubCmd.values()) {
                if (txtArg.toLowerCase().equals(subcmd.txtArg)) {
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

    private static String getGommetteName(GommetteType type) {
        return type == GREEN ? "verte" : "rouge";
    }

}
