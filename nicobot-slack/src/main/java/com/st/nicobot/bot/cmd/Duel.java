package com.st.nicobot.bot.cmd;

import com.st.nicobot.bot.NicoBot;
import com.st.nicobot.bot.utils.Option;
import com.st.nicobot.services.Messages;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by Logs on 22-11-15.
 */
@Service
public class Duel extends NiCommand {

    private static Logger logger = LoggerFactory.getLogger(Duel.class);

    private static final String COMMAND = "!duel";
    private static final String FORMAT = "!duel <nickname>...";
    private static final String DESC = "Lance un duel avec un ou plusieurs autres membres. C'est moi le juge !";

    private static final int MAX_POLL_TIME_SEC = 60;
    private static final int NUMERIC_POLL_MIN_VALUE = 1;
    private static final int NUMERIC_POLL_MAX_VALUE = 1337;

    @Autowired
    private NicoBot nicobot;

    @Autowired
    private Messages messages;

    private DuelEventListener listener = null;
    private Thread thread = null;
    private CountDownLatch latch = null;

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
        if (isDuelAlreadyRunning()) {
            nicobot.sendMessage(opts.message, messages.getMessage("duelPollRunning"));
            return;
        }

        try {
            DuelArguments arguments = new DuelArguments(args);

            List<SlackUser> users = arguments.users;
            Iterator<SlackUser> iter = users.iterator();

            while (iter.hasNext()) {
                SlackUser user = iter.next();
                if (user.isBot() || user.getId().equals(nicobot.getSession().sessionPersona().getId()) || user.getUserName().equals("slackbot")) {
                    iter.remove();
                }
            }

            if (!users.contains(opts.message.getSender())) {
                users.add(opts.message.getSender());
            }

            if (users.size() > 1) {
                nicobot.sendMessage(opts.message, messages.getMessage("duelStart", opts.message.getSender().getUserName()));
                thread = new Thread("Duel-Inner-Thread") {
                    @Override
                    public void run() {
                        try {
                            latch = new CountDownLatch(users.size());
                            listener = new DuelEventListener(nicobot, messages, users, opts);
                            nicobot.getSession().addMessagePostedListener(listener);

                            latch.await(MAX_POLL_TIME_SEC, TimeUnit.SECONDS);
                            manageEndPoll(opts);
                        } catch (InterruptedException e) {
                            logger.info("Poll unexpectedly ended, but the bot will try to handle the votes anyway...");
                            manageEndPoll(opts);
                        } catch (Exception e) {
                            logger.error("Unexpected error in poll task", e);
                        } finally {
                            nicobot.getSession().removeMessagePostedListener(listener);
                            listener = null;
                        }
                    }
                };

                thread.start();
            }


        } catch (IllegalArgumentException e) {
            nicobot.sendMessage(opts.message, "Malformed command, format : " + getFormat());
        } catch (UnknwownUserException e) {
            nicobot.sendMessage(opts.message, String.format(messages.getMessage("gmUnknownUser"), e.getMessage()));
        }
    }

    private void manageEndPoll(Option opts) {
        List<SlackUser> winners = new ArrayList<>();
        SlackUser winner = null;
        if (listener.pollType == PollType.RPC) {
            if (listener.missingVotes()) {
                nicobot.sendMessage(opts.message, messages.getMessage("duelNoVotes"));
            } else {
                Vote voter1 = listener.votes.get(0);
                Vote voter2 = listener.votes.get(1);
                if (voter1.vote == VoteValue.ROCK) {
                    if (voter2.vote == VoteValue.PAPER) {
                        winner = voter2.user;
                    } else if (voter2.vote == VoteValue.CISSORS) {
                        winner = voter1.user;
                    }
                } else if (voter1.vote == VoteValue.PAPER) {
                    if (voter2.vote == VoteValue.CISSORS) {
                        winner = voter2.user;
                    } else if (voter2.vote == VoteValue.ROCK) {
                        winner = voter1.user;
                    }
                } else {
                    if (voter2.vote == VoteValue.ROCK) {
                        winner = voter2.user;
                    } else if (voter2.vote == VoteValue.PAPER) {
                        winner = voter1.user;
                    }
                }
                nicobot.sendMessage(opts.message, messages.getMessage("duelPRCResult", voter1.user.getUserName(), voter1.vote.getStr(), voter2.user.getUserName(), voter2.vote.getStr()));
                if (winner != null) {
                    nicobot.sendMessage(opts.message, messages.getMessage("duelPRCWinner", winner.getUserName()));
                } else {
                    nicobot.sendMessage(opts.message, messages.getMessage("duelPRCDraw"));
                }
            }
        } else {
            int correctNumber = RandomUtils.nextInt(NUMERIC_POLL_MIN_VALUE, NUMERIC_POLL_MAX_VALUE);
            int diff = NUMERIC_POLL_MAX_VALUE + 1;
            int nearestValue = NUMERIC_POLL_MAX_VALUE + 1;
            for (Vote vote : listener.votes) {
                int currentDiff = Math.abs(correctNumber - vote.voteNumber);
                if (currentDiff < diff) {
                    winners.clear();
                    winners.add(vote.user);
                    diff = currentDiff;
                    nearestValue = vote.voteNumber;
                } else if (currentDiff == diff) {
                    winners.add(vote.user);
                }
            }

            if (!winners.isEmpty()) {
                nicobot.sendMessage(opts.message, messages.getMessage("duelRNResult", correctNumber));
                if (winners.size() == 1) {
                    nicobot.sendMessage(opts.message, messages.getMessage("duelRNWinner", winners.get(0).getUserName(), nearestValue));
                } else {
                    List<String> userNames = winners.stream().map(SlackUser::getUserName).collect(Collectors.toList());
                    nicobot.sendMessage(opts.message, messages.getMessage("duelRNWinnerPl", StringUtils.join(userNames, ", "), nearestValue));
                }
            }
        }
    }

    private class DuelArguments {
        private List<SlackUser> users = new ArrayList<>();

        public DuelArguments(String[] args) throws IllegalArgumentException, UnknwownUserException {
            if (args != null && args.length > 0) {
                for (String arg : args) {
                    SlackUser user = nicobot.getSession().findUserByUserName(arg);
                    if (user != null) {
                        users.add(user);
                    } else {
                        throw new UnknwownUserException(arg);
                    }
                }
            } else {
                throw new IllegalArgumentException("Too few arguments");
            }
        }
    }

    private class UnknwownUserException extends Exception {
        UnknwownUserException(String message) {
            super(message);
        }
    }

    private class DuelEventListener implements SlackMessagePostedListener {

        private NicoBot nicobot;
        private Messages messages;
        private List<SlackUser> remainingContestants;
        private PollType pollType;
        private List<Vote> votes = new ArrayList<>();

        public DuelEventListener(NicoBot nicobot, Messages messages, List<SlackUser> contestants, Option opts) {
            this.nicobot = nicobot;
            this.messages = messages;
            this.pollType = contestants.size() > 2 ? PollType.NUMERIC : PollType.RPC;
            this.remainingContestants = contestants;

            List<String> userNames = remainingContestants.stream().map(SlackUser::getUserName).collect(Collectors.toList());
            if (pollType == PollType.RPC) {
                nicobot.sendMessage(opts.message, messages.getMessage("duelPRCStart", StringUtils.join(userNames, ", ")));
            } else {
                nicobot.sendMessage(opts.message, messages.getMessage("duelRNStart", StringUtils.join(userNames, ", "), NUMERIC_POLL_MIN_VALUE, NUMERIC_POLL_MAX_VALUE));
            }
        }


        @Override
        public void onEvent(SlackMessagePosted event, SlackSession slackSession) {
            if (event.getChannel().isDirect() && remainingContestants.contains(event.getSender())) {
                String message = event.getMessageContent().trim();
                if (pollType == PollType.NUMERIC) {
                    if (NumberUtils.isNumber(message) && Integer.parseInt(message) >= NUMERIC_POLL_MIN_VALUE && Integer.parseInt(message) <= NUMERIC_POLL_MAX_VALUE) {
                        addVoteAndDecrement(new Vote(event.getSender(), Integer.parseInt(message)));
                    } else {
                        nicobot.sendMessage(event, messages.getMessage("duelRNError", NUMERIC_POLL_MIN_VALUE, NUMERIC_POLL_MAX_VALUE));
                    }
                } else {
                    VoteValue vote = VoteValue.getVoteByStr(message);
                    if (vote != null) {
                        addVoteAndDecrement(new Vote(event.getSender(), vote));
                    } else {
                        nicobot.sendMessage(event, messages.getMessage("duelPRCError"));
                    }
                }
            }
        }

        public void addVoteAndDecrement(Vote vote) {
            votes.add(vote);
            remainingContestants.remove(vote.user);
            logger.info("{} vode added, {} remaining", votes.size(), remainingContestants.size());
            latch.countDown();
        }

        public boolean missingVotes() {
            return remainingContestants.size() > 0;
        }
    }

    private boolean isDuelAlreadyRunning() {
        return listener != null;
    }

    private enum PollType {
        RPC,
        NUMERIC
    }

    private enum VoteValue {
        ROCK("pierre", "caillou", "rock"),
        PAPER("papier", "feuille", "paper"),
        CISSORS("ciseaux", "ciseau", "cissors");

        private String[] acceptedValues;

        VoteValue(String... acceptedValues) {
            this.acceptedValues = acceptedValues;
        }

        public static VoteValue getVoteByStr(String voteArg) {
            for (VoteValue voteValue : VoteValue.values()) {
                for (String str : voteValue.acceptedValues) {
                    if (str.equalsIgnoreCase(voteArg)) {
                        return voteValue;
                    }
                }
            }
            return null;
        }

        private String getStr() {
            return acceptedValues[0];
        }
    }

    private class Vote {
        SlackUser user;
        VoteValue vote;
        int voteNumber;

        public Vote(SlackUser user, VoteValue vote) {
            this.user = user;
            this.vote = vote;
        }

        public Vote(SlackUser user, int voteNumber) {
            this.user = user;
            this.voteNumber = voteNumber;
        }
    }
}
