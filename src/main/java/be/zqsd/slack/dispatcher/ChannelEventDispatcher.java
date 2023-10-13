package be.zqsd.slack.dispatcher;

import be.zqsd.nicobot.bot.ChannelService;
import com.slack.api.bolt.handler.BoltEventHandler;
import com.slack.api.model.event.*;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ChannelEventDispatcher {

    private final ChannelService channelService;

    @Inject
    ChannelEventDispatcher(ChannelService channelService) {
        this.channelService = channelService;
    }

    public BoltEventHandler<ChannelRenameEvent> onChannelRename() {
        return (event, context) -> {
            channelService.refreshChannels();
            return context.ack();
        };
    }

    public BoltEventHandler<ChannelCreatedEvent> onChannelCreated() {
        return (event, context) -> {
            channelService.refreshChannels();
            return context.ack();
        };
    }

    public BoltEventHandler<ChannelDeletedEvent> onChannelDeleted() {
        return (event, context) -> {
            channelService.refreshChannels();
            return context.ack();
        };
    }

    public BoltEventHandler<GroupRenameEvent> onGroupRename() {
        return (event, context) -> {
            channelService.refreshChannels();
            return context.ack();
        };
    }

    public BoltEventHandler<GroupDeletedEvent> onGroupDeleted() {
        return (event, context) -> {
            channelService.refreshChannels();
            return context.ack();
        };
    }
}
