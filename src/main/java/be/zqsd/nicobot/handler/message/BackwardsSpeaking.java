package be.zqsd.nicobot.handler.message;

import be.zqsd.nicobot.bot.Nicobot;
import com.slack.api.model.event.MessageEvent;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static java.lang.Character.*;
import static java.util.Optional.of;
import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.joining;

@ApplicationScoped
public class BackwardsSpeaking extends ConditionalMessage {

    private static final int MAX_LENGTH_TO_REVERSE = 50;
    private static final Pattern URL_REGEX = compile("(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})([/\\w .-]*)*/?");
    private static final Pattern EMOJI_REGEX = compile(":[^: ]*:");
    private static final Pattern USERNAME_OR_CHANNEL_REGEX = compile(">[^<]*([@|#])<");

    private final Nicobot nicobot;

    @Inject
    BackwardsSpeaking(Nicobot nicobot) {
        this.nicobot = nicobot;
    }

    @Override
    boolean conditionMet(MessageEvent event) {
        var message = event.getText();
        return message.length() < MAX_LENGTH_TO_REVERSE && !URL_REGEX.matcher(message).find();
    }

    @Override
    int chance() {
        return 3;
    }

    @Override
    public void handleConditionalMessage(MessageEvent event) {
        nicobot.sendMessage(event, reverseMessage(event.getText()));
    }

    protected String reverseMessage(String message) {
        var reversed = new StringBuilder(message).reverse().toString();

        return of(reversed)
                .map(this::changeFirstAndLastLetterCaseIfNeeded)
                .map(this::correctEmojis)
                .map(this::correctUsernamesAndChannels)
                .orElse(null);
    }

    private String changeFirstAndLastLetterCaseIfNeeded(String originalMessage) {
        var isFirstLetterUppercase = isUpperCase(originalMessage.charAt(0));
        if (isFirstLetterUppercase) {
            return IntStream.range(0, originalMessage.length())
                    .mapToObj(index -> {
                        if (index == 0) {
                            return toUpperCase(originalMessage.charAt(index));
                        } else if (index == originalMessage.length() - 1) {
                            return toLowerCase(originalMessage.charAt(index));
                        } else {
                            return originalMessage.charAt(index);
                        }
                    })
                    .map(Object::toString)
                    .collect(joining());
        } else {
            return originalMessage;
        }
    }

    private String correctEmojis(String reversedMessage) {
        return EMOJI_REGEX.matcher(reversedMessage)
                .results()
                .map(MatchResult::group)
                .map(reversed -> (Function<String,String>) message -> reverseOccurrenceInMessage(reversed, message))
                .reduce(Function.identity(), Function::andThen)
                .apply(reversedMessage);
    }

    private String correctUsernamesAndChannels(String reversedMessage) {
        return USERNAME_OR_CHANNEL_REGEX.matcher(reversedMessage)
                .results()
                .map(MatchResult::group)
                .map(reversed -> (Function<String,String>) message -> reverseOccurrenceInMessage(reversed, message))
                .reduce(Function.identity(), Function::andThen)
                .apply(reversedMessage);
    }

    private String reverseOccurrenceInMessage(String occurrence, String message) {
        var fixed = new StringBuilder(occurrence).reverse().toString();
        return message.replace(occurrence, fixed);
    }
}
