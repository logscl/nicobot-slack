package be.zqsd.nicobot.message;

import java.util.List;
import java.util.regex.Pattern;

import static java.util.List.of;
import static java.util.regex.Pattern.compile;

public class Reaction {
    private static final int DEFAULT_COOLDOWN_TIMER = 60;

    private final String trigger;
    private final boolean caseInsensitive;
    private final List<String> replies;
    private final int cooldownInSeconds;

    Reaction(String trigger, boolean caseInsensitive, List<String> replies, int cooldownInSeconds) {
        this.trigger = trigger;
        this.caseInsensitive = caseInsensitive;
        this.replies = replies;
        this.cooldownInSeconds = cooldownInSeconds;
    }

    public static Reaction react(String trigger, String... responses) {
        return new Reaction(trigger, false, of(responses), DEFAULT_COOLDOWN_TIMER);
    }

    public static Reaction react(String trigger, int cooldownInSeconds, String... responses) {
        return new Reaction(trigger, false, of(responses), cooldownInSeconds);
    }

    public static Reaction react(String trigger, boolean caseInsensitive, int cooldownInSeconds, String... responses) {
        return new Reaction(trigger, caseInsensitive, of(responses), cooldownInSeconds);
    }

    public Pattern buildPattern(String botId, String botName) {
        var botNameAndId = String.format("(%s|<@%s>)", botName, botId);
        return compile(trigger.replace("#b", botNameAndId), (caseInsensitive) ? Pattern.CASE_INSENSITIVE : 0);
    }

    public List<String> getReplies() {
        return replies;
    }

    public int getCooldownInSeconds() {
        return cooldownInSeconds;
    }
}
