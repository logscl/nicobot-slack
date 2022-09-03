package be.zqsd.nicobot.handler.message;

import be.zqsd.nicobot.bot.ChannelService;
import be.zqsd.nicobot.bot.UserService;
import com.slack.api.model.event.MessageEvent;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import static org.slf4j.LoggerFactory.getLogger;

@ApplicationScoped
public class MessageLogger implements MessageHandler {
    private static final Logger LOG = getLogger(MessageLogger.class);

    private final UserService userService;
    private final ChannelService channelService;

    @Inject
    MessageLogger(UserService userService,
                  ChannelService channelService) {
        this.userService = userService;
        this.channelService = channelService;
    }

    @Override
    public void handle(MessageEvent event) {
        LOG.info("MESSAGE FROM {} IN CHANNEL {} RECEIVED: {}",
                userService.findUserName(event.getUser()).orElse(event.getUser()),
                channelService.findChannelName(event.getChannel()).orElse(event.getChannel()),
                event.getText());
    }
}
