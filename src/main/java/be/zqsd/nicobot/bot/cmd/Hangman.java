package be.zqsd.nicobot.bot.cmd;

import be.zqsd.nicobot.bot.NicoBot;
import be.zqsd.nicobot.bot.utils.Option;
import be.zqsd.nicobot.services.Messages;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Minutes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by Logs on 18-06-17.
 */
@Service
public class Hangman extends NiCommand {

    private static Logger logger = LoggerFactory.getLogger(Hangman.class);

    private static final String COMMAND = "!pendu";
    private static final String FORMAT = "!pendu channel mot(s)";
    private static final String DESC = "Démarre un pendu. \"!pendu general mon pendu\" nicobot démarre un pendu avec \"mon pendu\" à trouver (à envoyer en pv)";

    private static final int MAX_GAME_TIME_MIN = 5;

    @Autowired
    private NicoBot nicobot;

    @Autowired
    private Messages messages;

    private HangmanEventListener listener = null;
    private CountDownLatch latch = null;

    private DateTime lastHangman = null;

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
        if (isGameRunning()) {
            nicobot.sendMessage(opts.message, messages.getMessage("hmRunning"));
            return;
        }

        try {
            HangmanArguments arguments = new HangmanArguments(args);
            String hangmanQuery;
            SlackChannel channel;

            if(lastHangman != null && Minutes.minutesBetween(lastHangman, DateTime.now(DateTimeZone.UTC)).getMinutes() < 15) {
                nicobot.sendMessage(opts.message, "Essaye plus tard...");
                return;
            }

            if(arguments.hangmanQuery != null) {
                hangmanQuery = arguments.hangmanQuery;
            } else {
                throw new IllegalArgumentException("Missing Query");
            }

            if(arguments.hangmanQuery.length() > 50) {
                throw new IllegalArgumentException("Too long query (max 50 chars)");
            }

            if(arguments.channel != null) {
                channel = arguments.channel;
            } else {
                channel = opts.message.getChannel();
            }

            nicobot.sendMessage(channel, opts.message.getSender(), messages.getMessage("hmStart"));
            new Thread("Hangman-Inner-Thread") {
                @Override
                public void run() {
                    try {
                        latch = new CountDownLatch(1);
                        listener = new HangmanEventListener(nicobot, messages, channel, hangmanQuery);
                        nicobot.getSession().addMessagePostedListener(listener);

                        latch.await(MAX_GAME_TIME_MIN, TimeUnit.MINUTES);
                        manageEndGame(channel, hangmanQuery);
                    } catch (InterruptedException e) {
                        logger.info("Game ended");
                        manageEndGame(channel, hangmanQuery);
                    } catch (Exception e) {
                        logger.error("Unexpected error in game task", e);
                    } finally {
                        nicobot.getSession().removeMessagePostedListener(listener);
                        listener = null;
                        lastHangman = DateTime.now(DateTimeZone.UTC);
                    }
                }
            }.start();

        } catch (IllegalArgumentException e) {
            nicobot.sendMessage(opts.message, e.getMessage() + "! Malformed command, format : " + getFormat());
        } catch (UnknownChannelException e) {
            nicobot.sendMessage(opts.message, String.format(messages.getMessage("hmUnknownChannel"), e.getMessage()));
        }

    }

    private void manageEndGame(SlackChannel channel, String hangmanQuery) {
        if(listener.getWinner() != null) {
            nicobot.sendMessage(channel, listener.getWinner(), messages.getMessage("hmWinner", hangmanQuery));
        } else {
            nicobot.sendMessage(channel, listener.getWinner(), messages.getMessage("hmLost", hangmanQuery));
        }
    }

    private boolean isGameRunning() {
        return listener != null;
    }

    private class HangmanEventListener implements SlackMessagePostedListener {

        private NicoBot nicobot;
        private Messages messages;
        private SlackChannel channel;
        private SlackUser winner;

        private String query;
        private Set<Character> lettersToFind = new HashSet<>();
        private List<Character> foundLetters = new ArrayList<>();
        private List<Character> wronglyGuessedLetters = new ArrayList<>();

        public HangmanEventListener(NicoBot nicobot, Messages messages, SlackChannel channel, String query) {
            this.nicobot = nicobot;
            this.messages = messages;
            this.channel = channel;
            this.query = query;

            for(Character ch : query.toCharArray()) {
                if(StringUtils.isAlpha(ch.toString())) {
                    lettersToFind.add(ch);
                }
            }

            nicobot.sendMessage(channel, null, getFormattedOutput());
        }


        @Override
        public void onEvent(SlackMessagePosted slackMessagePosted, SlackSession slackSession) {
            if(isValidInput(slackMessagePosted.getMessageContent())) {
                Character input = slackMessagePosted.getMessageContent().toUpperCase().charAt(0);
                if(!foundLetters.contains(input) && !wronglyGuessedLetters.contains(input)) {
                    if(lettersToFind.contains(input)) {
                        foundLetters.add(input);
                    } else {
                        wronglyGuessedLetters.add(input);
                    }
                    nicobot.sendMessage(channel, null, getFormattedOutput());

                    if(foundLetters.size() == lettersToFind.size()) {
                        winner = slackMessagePosted.getSender();
                        latch.countDown();
                    }

                    if(wronglyGuessedLetters.size() == 5) {
                        latch.countDown();
                    }
                }
            } else {
                String messagePosted = slackMessagePosted.getMessageContent();
                messagePosted = StringUtils.stripAccents(messagePosted);
                messagePosted = StringUtils.upperCase(messagePosted);
                logger.info("Query: {} - Input : {} - Jaro Winkler Distance: {}", query, messagePosted, StringUtils.getJaroWinklerDistance(messagePosted, query));
                if(StringUtils.getJaroWinklerDistance(messagePosted, query) >= .99) {
                    winner = slackMessagePosted.getSender();
                    latch.countDown();
                }
            }
        }

        private String getFormattedOutput() {
            StringBuilder builder = new StringBuilder("`");
            for(Character ch : query.toCharArray()) {
                if(foundLetters.contains(ch)) {
                    builder.append(ch).append(" ");
                } else {
                    if(ch.equals(' ')) {
                        builder.append("  ");
                    } else if(StringUtils.isAlpha(ch.toString())) {
                        builder.append("_ ");
                    } else {
                        builder.append(ch).append(" ");
                    }
                }
            }

            if(builder.lastIndexOf(" ") == builder.length()-1) {
                builder.delete(builder.lastIndexOf(" "), builder.length());
            }

            return builder.append("` :pendu").append(wronglyGuessedLetters.size()).append(":").toString();
        }

        public boolean isSuccess() {
            return wronglyGuessedLetters.size() < 5;
        }

        public SlackUser getWinner() {
            return winner;
        }

        private boolean isValidInput(String message) {
            return message.length() == 1 && message.matches("[a-zA-Z]");
        }
    }

    private class HangmanArguments {
        private SlackChannel channel;
        private String hangmanQuery;

        public HangmanArguments(String[] args) throws IllegalArgumentException, UnknownChannelException {
            if (args != null && args.length > 0) {
                if (args.length > 1) {
                    String arg1 = args[0];
                    channel = nicobot.getSession().findChannelByName(arg1.replaceFirst("#", ""));
                    if (channel == null) {
                        throw new UnknownChannelException(arg1);
                    }

                    hangmanQuery =
                            Arrays.stream(args)
                                    .skip(1)
                                    .map(StringUtils::stripAccents)
                                    .map(StringUtils::upperCase)
                                    .collect(Collectors.joining(" "));

                } else {
                    throw new IllegalArgumentException("Too few arguments");
                }
            }
        }
    }

    private class UnknownChannelException extends Exception {
        UnknownChannelException(String message) {
            super(message);
        }
    }
}
