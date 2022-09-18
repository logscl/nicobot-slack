package be.zqsd.slack.client;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.request.chat.ChatPostEphemeralRequest;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.auth.AuthTestResponse;
import com.slack.api.methods.response.chat.ChatPostEphemeralResponse;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.methods.response.reactions.ReactionsAddResponse;
import com.slack.api.model.Conversation;
import com.slack.api.model.User;
import com.slack.api.model.block.LayoutBlock;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

import static com.slack.api.model.ConversationType.*;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.slf4j.LoggerFactory.getLogger;

@ApplicationScoped
public class WebClient {
    private static final Logger LOG = getLogger(WebClient.class);

    private final MethodsClient methods;
    private final AuthTestResponse identity;

    @Inject
    WebClient(@ConfigProperty(name = "slack.api.key") String slackApiKey) {
        var slack = Slack.getInstance();
        methods = slack.methods(slackApiKey);
        identity = fetchIdentity();
    }

    private AuthTestResponse fetchIdentity() {
        try {
            LOG.debug("Fetching bot identity...");
            var self = methods.authTest(builder -> builder);
            LOG.debug("Identity found: ID: {} - Name: {}", self.getUserId(), self.getUser());
            return self;
        } catch (Exception e) {
            LOG.error("Issue when creating the websocket client", e);
            throw new SlackClientException(e);
        }
    }

    public List<User> fetchUsers() {
        try {
            var response = methods.usersList(req -> req);
            return response.getMembers();
        } catch (Exception e) {
            LOG.error("Unable to fetch users", e);
        }
        return emptyList();
    }

    public List<Conversation> fetchChannels() {
        try {
            var response = methods.conversationsList(req ->
                    req
                            .excludeArchived(true)
                            .types(List.of(PUBLIC_CHANNEL, PRIVATE_CHANNEL, IM, MPIM)));
            return response.getChannels();
        } catch (Exception e) {
            LOG.error("Unable to fetch conversations", e);
        }
        return emptyList();
    }

    public Optional<ChatPostMessageResponse> sendMessage(String channelId, String threadTimestamp, String message) {
        try {
            var request = ChatPostMessageRequest.builder()
                    .channel(channelId)
                    .threadTs(threadTimestamp)
                    .text(message)
                    .unfurlLinks(true)
                    .unfurlMedia(true)
                    .build();

            var response = methods.chatPostMessage(request);
            LOG.debug("response posted: {}", response);
            return of(response);
        } catch (Exception e) {
            LOG.error("Unable to send message at this time", e);
        }
        return empty();
    }

    public Optional<ChatPostMessageResponse> sendBlocks(String channelId, String threadTimestamp, List<LayoutBlock> blocks, String fallbackMessage) {
        try {
            var request = ChatPostMessageRequest.builder()
                    .channel(channelId)
                    .threadTs(threadTimestamp)
                    .blocks(blocks)
                    .text(fallbackMessage)
                    .unfurlLinks(false)
                    .unfurlMedia(false)
                    .build();

            var response = methods.chatPostMessage(request);
            LOG.debug("response posted: {}", response);
            return of(response);
        } catch (Exception e) {
            LOG.error("Unable to send message at this time", e);
        }
        return empty();
    }

    public Optional<ChatPostEphemeralResponse> sendEphemeralMessage(String channelId, String threadTimestamp, String userId, String message) {
        try {
            var request = ChatPostEphemeralRequest.builder()
                    .channel(channelId)
                    .threadTs(threadTimestamp)
                    .user(userId)
                    .text(message)
                    .build();

            var response = methods.chatPostEphemeral(request);
            LOG.debug("Ephemeral response posted: {}", response);
            return of(response);
        } catch (Exception e) {
            LOG.error("Unable to send ephemeral message at this time", e);
        }
        return empty();
    }

    public Optional<ChatPostEphemeralResponse> sendEphemeralBlocks(String channelId, String threadTimestamp, String userId, List<LayoutBlock> blocks, String fallbackMessage) {
        try {
            var request = ChatPostEphemeralRequest.builder()
                    .channel(channelId)
                    .threadTs(threadTimestamp)
                    .user(userId)
                    .blocks(blocks)
                    .text(fallbackMessage)
                    .build();

            var response = methods.chatPostEphemeral(request);
            LOG.debug("ephemeral response posted: {}", response);
            return of(response);
        } catch (Exception e) {
            LOG.error("Unable to send ephemeral message at this time", e);
        }
        return empty();
    }

    public Optional<ReactionsAddResponse> addReactionToMessage(String channelId, String messageTimeStamp, String emojiName) {
        try {
            var reactionResponse = methods.reactionsAdd(builder -> builder
                    .channel(channelId)
                    .timestamp(messageTimeStamp)
                    .name(emojiName)
            );
            LOG.debug("Reaction added, {}", reactionResponse);
            return of(reactionResponse);
        } catch (Exception e) {
            LOG.error("Unable to add reaction to message at this time", e);
        }
        return empty();
    }

    public String botId() {
        return identity.getUserId();
    }

    public String botName() {
        return identity.getUser();
    }
}
