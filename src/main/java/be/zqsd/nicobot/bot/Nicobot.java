package be.zqsd.nicobot.bot;

import be.zqsd.slack.client.WebClient;
import com.slack.api.methods.response.chat.ChatPostEphemeralResponse;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.methods.response.reactions.ReactionsAddResponse;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.event.MessageEvent;
import org.slf4j.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

@ApplicationScoped
public class Nicobot {
    private static final Logger LOG = getLogger(Nicobot.class);

    private final WebClient client;

    @Inject
    Nicobot(WebClient client) {
        this.client = client;
    }

    public Optional<ChatPostMessageResponse> sendMessage(String channelId, String threadTimestamp, String message) {
        LOG.debug("Sending message: {}", message);
        return client.sendMessage(channelId, threadTimestamp, message);
    }

    public Optional<ChatPostMessageResponse> sendMessage(MessageEvent event, String message) {
        return sendMessage(event.getChannel(), event.getThreadTs(), message);
    }

    public Optional<ChatPostMessageResponse> sendBlocks(MessageEvent event, List<LayoutBlock> blocks, String fallbackMessage) {
        return client.sendBlocks(event.getChannel(), event.getThreadTs(), blocks, fallbackMessage);
    }

    public Optional<ReactionsAddResponse> addReactionToMessage(String channelId, String messageTimeStamp, String emojiName) {
        return client.addReactionToMessage(channelId, messageTimeStamp, emojiName);
    }

    public Optional<ChatPostMessageResponse> sendPrivateMessage(String userId, String message) {
        LOG.debug("Sending private message: {}", message);
        return client.sendMessage(userId, null, message);
    }

    public Optional<ChatPostEphemeralResponse> sendEphemeralMessage(String channelId, String userId, String messageTimeStamp, String message) {
        LOG.debug("Sending ephemeral message to {}: {}", userId, message);
        return client.sendEphemeralMessage(channelId, userId, messageTimeStamp, message);
    }

    public Optional<ChatPostEphemeralResponse> sendEphemeralMessage(MessageEvent event, String message) {
        LOG.debug("Sending ephemeral message to {}: {}", event.getUser(), message);
        return client.sendEphemeralMessage(event.getChannel(), event.getThreadTs(), event.getUser(), message);
    }

    public Optional<ChatPostEphemeralResponse> sendEphemeralBlocks(MessageEvent event, List<LayoutBlock> blocks, String fallbackMessage) {
        return client.sendEphemeralBlocks(event.getChannel(), event.getThreadTs(), event.getUser(), blocks, fallbackMessage);
    }
}
