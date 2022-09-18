package be.zqsd.nicobot.handler.message;

import be.zqsd.nicobot.bot.ChannelService;
import be.zqsd.nicobot.bot.UserService;
import be.zqsd.nicobot.persistence.Persistence;
import com.slack.api.model.event.MessageEvent;
import io.smallrye.mutiny.Uni;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.util.regex.Pattern;

import static org.slf4j.LoggerFactory.getLogger;

@ApplicationScoped
public class SaveMessage implements MessageHandler {

    private static final Logger LOG = getLogger(SaveMessage.class);

    private static final Pattern NAME_ID_PATTERN = Pattern.compile("<@([0-9A-Z]+)>");
    private static final Pattern NAME_ID_WITH_STR_PATTERN = Pattern.compile("<@([0-9A-Z]+)\\|(.*?)>");
    private static final Pattern CHANNEL_ID_PATTERN = Pattern.compile("<#([0-9A-Z]+)>");

    private static final Pattern CHANNEL_ID_WITH_STR_PATTERN = Pattern.compile("<#([0-9A-Z]+)\\|(.*?)>");

    private final Persistence persistence;
    private final ChannelService channelService;
    private final UserService userService;

    @Inject
    public SaveMessage(Persistence persistence,
                       ChannelService channelService,
                       UserService userService) {
        this.persistence = persistence;
        this.channelService = channelService;
        this.userService = userService;
    }

    @Override
    public void handle(MessageEvent event) {
        if (channelService.isFeaturedChannel(event.getChannel())) {
            var user = userService.findUserName(event.getUser()).orElse("?");
            var message = replaceTokens(event.getText());
            LOG.debug("Will log message from {}: '{}'", user, message);
            Uni.createFrom()
                    .voidItem()
                    .onItem()
                    .invoke(() -> {
                        try {
                            persistence.saveMessage(user, message);
                        } catch (IOException e) {
                            LOG.error("Unable to save message", e);
                        }
                    })
                    .subscribe()
                    .with(unused -> LOG.debug("Message logged"));
        }
    }

    private String replaceTokens(String message) {
        var nameIdMatcher = NAME_ID_PATTERN.matcher(message);
        var cleanedMessage = nameIdMatcher.replaceAll(match -> userService.findUserName(match.group(1)).orElse(match.group(1)));

        var nameIdWithStrMatcher = NAME_ID_WITH_STR_PATTERN.matcher(cleanedMessage);
        cleanedMessage = nameIdWithStrMatcher.replaceAll(match -> match.group(2));

        var channelIdMatcher = CHANNEL_ID_PATTERN.matcher(cleanedMessage);
        cleanedMessage = channelIdMatcher.replaceAll(match -> channelService.findChannelName(match.group(1)).orElse(match.group(1)));

        var channelIdWithStrMatcher = CHANNEL_ID_WITH_STR_PATTERN.matcher(cleanedMessage);
        cleanedMessage = channelIdWithStrMatcher.replaceAll(match -> match.group(2));

        return cleanedMessage;
    }
}
