package be.zqsd.nicobot.handler.message;

import be.zqsd.nicobot.bot.ChannelService;
import be.zqsd.nicobot.leet.LeetService;
import com.slack.api.model.event.MessageEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static java.util.List.of;
import static java.util.regex.Pattern.compile;
import static org.slf4j.LoggerFactory.getLogger;

@ApplicationScoped
public class LeetWatcher implements MessageHandler {

    private static final Logger LOG = getLogger(LeetWatcher.class);

    private static final List<Pattern> TRIGGERS = of(
            compile(".*h+ ?g+ ?t+.*", Pattern.CASE_INSENSITIVE),
            compile(".*Happy.*Geek.*Time.*", Pattern.CASE_INSENSITIVE)
    );

    private final ChannelService channelService;
    private final LeetService leetService;
    private final ZoneId timezone;

    @Inject
    public LeetWatcher(ChannelService channelService,
                       LeetService leetService,
                       @ConfigProperty(name = "nicobot.timezone.name") String timezoneName) {
        this.channelService = channelService;
        this.leetService = leetService;
        this.timezone = ZoneId.of(timezoneName);
    }

    @Override
    public void handle(MessageEvent event) {
        if (channelService.isFeaturedChannel(event.getChannel())) {
            var isSentAtLeetHour = extractSeconds(event.getTs())
                    .map(Instant::ofEpochSecond)
                    .map(epoch -> ZonedDateTime.ofInstant(epoch, timezone))
                    .map(this::isLeetHour)
                    .orElse(false);
            if (isSentAtLeetHour &&  containsTrigger(event.getText())) {
                leetService.addGreeter(event.getUser());
            }
        }
    }

    private Optional<Long> extractSeconds(String timestampWithNanos) {
        return Optional.of(timestampWithNanos)
                .map(ts -> ts.split("\\."))
                .map(epochAndNanos -> epochAndNanos[0])
                .map(Long::valueOf);
    }

    private boolean isLeetHour(ZonedDateTime dateTime) {
        return dateTime.getHour() == 13 && dateTime.getMinute() == 37;
    }

    private boolean containsTrigger(String message) {
        return TRIGGERS.stream().anyMatch(trigger -> trigger.matcher(message).find());
    }
}
