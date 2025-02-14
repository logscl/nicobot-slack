package be.zqsd.slack.client;

import be.zqsd.slack.dispatcher.ChannelEventDispatcher;
import be.zqsd.slack.dispatcher.MessageEventDispatcher;
import be.zqsd.slack.dispatcher.UserEventDispatcher;
import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;
import com.slack.api.bolt.jakarta_socket_mode.SocketModeApp;
import com.slack.api.model.event.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;

import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

@ApplicationScoped
public class WebsocketClient {
    private static final Logger LOG = getLogger(WebsocketClient.class);

    private final MessageEventDispatcher messageEventDispatcher;
    private final UserEventDispatcher userEventDispatcher;
    private final ChannelEventDispatcher channelEventDispatcher;

    private final App slackApp;
    private final SocketModeApp slackWebsocket;

    @Inject
    WebsocketClient(@ConfigProperty(name = "slack.api.key") String slackApiKey,
                    @ConfigProperty(name = "slack.websocket.key") String slackWebsocketKey,
                    MessageEventDispatcher messageEventDispatcher,
                    UserEventDispatcher userEventDispatcher,
                    ChannelEventDispatcher channelEventDispatcher) {
        this.messageEventDispatcher = messageEventDispatcher;
        this.userEventDispatcher = userEventDispatcher;
        this.channelEventDispatcher = channelEventDispatcher;

        try {
            var config = AppConfig.builder().singleTeamBotToken(slackApiKey).build();
            this.slackApp = new App(config);
            this.slackWebsocket = new SocketModeApp(
                    slackWebsocketKey,
                    slackApp
            );
            addHandlers();
        } catch (IOException e) {
            LOG.error("Issue when creating the websocket client", e);
            throw new SlackClientException(e);
        }
    }

    private void addHandlers() {
        slackApp.event(MessageEvent.class, messageEventDispatcher);
        slackApp.event(UserChangeEvent.class, userEventDispatcher.onUserChange());
        slackApp.event(ChannelRenameEvent.class, channelEventDispatcher.onChannelRename());
        slackApp.event(GroupRenameEvent.class, channelEventDispatcher.onGroupRename());
        slackApp.event(ChannelCreatedEvent.class, channelEventDispatcher.onChannelCreated());
        slackApp.event(ChannelDeletedEvent.class, channelEventDispatcher.onChannelDeleted());
        slackApp.event(GroupDeletedEvent.class, channelEventDispatcher.onGroupDeleted());

        // ignored events (for now) -> is there a way to ignore all events not handled ?
        slackApp.event(MessageChangedEvent.class, (payload, context) -> context.ack());
        slackApp.event(MessageThreadBroadcastEvent.class, (payload, context) -> context.ack());
        slackApp.event(MessageDeletedEvent.class, (payload, context) -> context.ack());
        slackApp.event(MessageFileShareEvent.class, (payload, context) -> context.ack());
        slackApp.event(MessageChannelLeaveEvent.class, (payload, context) -> context.ack());
        slackApp.event(MessageChannelJoinEvent.class, (payload, context) -> context.ack());

        // TODO - callback of blockactions
//        slackApp.blockAction(Pattern.compile("rps-.*"), (blockActionRequest, actionContext) -> {
//            return actionContext.ack();
//        });
    }

    public void connect() throws Exception {
        slackWebsocket.startAsync();
    }
}
