package be.zqsd.nicobot.message;

import be.zqsd.nicobot.bot.ChannelService;
import be.zqsd.nicobot.bot.UserService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@ApplicationScoped
public class MessageFormatter {

    private static final String RANDOM_NAME_PLACEHOLDER = "#u";
    private static final String CHANNEL_NAME_PLACEHOLDER = "#c";
    private static final String ORIGINATOR_PLACEHOLDER = "#p";
    private final ChannelService channelService;
    private final UserService userService;

    @Inject
    MessageFormatter(ChannelService channelService,
            UserService userService) {
        this.channelService = channelService;
        this.userService = userService;
    }

    public String formatMessage(String message, String originatorId, String channelId) {
        return ofNullable(message)
                .map(msg -> replaceOriginatorInMessage(msg, originatorId))
                .map(msg -> replaceChannelInMessage(msg, channelId))
                .map(this::replaceRandomNamesInMessage)
                .orElse(null);
    }

    private String replaceRandomNamesInMessage(String message) {
        return userService.randomUserNameWithoutHighlight()
                .map(name -> message.replace(RANDOM_NAME_PLACEHOLDER, name))
                .orElse(message);
    }

    private String replaceChannelInMessage(String message, String channelId) {
        return ofNullable(channelId)
                .map(channelService::findChannelName)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(name -> message.replace(CHANNEL_NAME_PLACEHOLDER, name))
                .orElse(message);
    }

    private String replaceOriginatorInMessage(String message, String originatorId) {
        return ofNullable(originatorId)
                .map(userService::userNameWithoutHighlight)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(name -> message.replace(ORIGINATOR_PLACEHOLDER, name))
                .orElse(message);
    }
}
