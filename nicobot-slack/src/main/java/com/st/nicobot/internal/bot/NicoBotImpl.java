package com.st.nicobot.internal.bot;

import com.st.nicobot.bot.NicoBot;
import com.st.nicobot.bot.event.MessageEvent;
import com.st.nicobot.bot.utils.Emoji;
import com.st.nicobot.utils.NicobotProperty;
import com.st.nicobot.bot.utils.Option;
import com.st.nicobot.services.BehaviorsService;
import com.st.nicobot.services.Messages;
import com.st.nicobot.services.PropertiesService;
import com.st.nicobot.services.UsernameService;
import com.ullink.slack.simpleslackapi.*;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.impl.SlackChatConfiguration;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import com.ullink.slack.simpleslackapi.listeners.*;
import com.ullink.slack.simpleslackapi.replies.GenericSlackReply;
import com.ullink.slack.simpleslackapi.replies.SlackChannelReply;
import com.ullink.slack.simpleslackapi.replies.SlackMessageReply;
import com.ullink.slack.simpleslackapi.replies.SlackReply;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Logs on 09-05-15.
 */
@Service
public class NicoBotImpl implements NicoBot {

    private static Logger logger = LoggerFactory.getLogger(NicoBotImpl.class);

    @Autowired
    private ApplicationContext ctx;

    @Autowired
    private PropertiesService props;

    @Autowired
    private BehaviorsService behaviors;

    @Autowired
    private UsernameService usernameService;

    @Autowired
    private Messages messages;

    private SlackSession session;

    private SlackPersona self;

    @PostConstruct
    private void postConstruct() {
        session = SlackSessionFactory.createWebSocketSlackSession(props.get(NicobotProperty.SLACK_API_KEY));
        Map<String, MessageEvent> eventMap = ctx.getBeansOfType(MessageEvent.class);

        session.addMessagePostedListener((message, session1) -> {
            logger.info("MSG [{}] ON CHAN {} BY {} : {}", message.getEventType(), message.getChannel().getName(), message.getSender().getUserName(), message.getMessageContent());
            //Message apiMessage = new Message(new DateTime(), message.getSender().getUserName(), message.getMessageContent());
            //api.saveMessages(Arrays.asList(apiMessage));
            if(message.getChannel().getName().equals(props.get(NicobotProperty.FEATURED_CHANNEL))) {
                behaviors.randomBehave(new Option(message));
            }
        });

        for (Map.Entry<String, MessageEvent> entry : eventMap.entrySet()) {
            session.addMessagePostedListener(entry.getValue());
            logger.info("{} loaded", entry.getValue().getClass().getSimpleName());
        }

    }

    /**
     * Format une chaine de caractere en remplacant les "%p" par {@code sender} et les "%c" par {@code channel}.
     * @param message
     * @param sender
     * @param channel
     * @return
     */
    private String formatMessage(String message, SlackUser sender, SlackChannel channel) {
        if(sender != null) {
            message = message.replaceAll("%p", usernameService.getNoHLName(sender));
        }
        message = message.replaceAll("%c", channel.getName());

        if(sender != null && message.contains("%u")) {
            message = message.replaceAll("%u", usernameService.getNoHLName(getRandomUserFromChannel(channel)));
        }

        return message;
    }

    private SlackUser getRandomUserFromChannel(SlackChannel channel) {
        int randomInt = RandomUtils.nextInt(0, channel.getMembers().size());
        return new ArrayList<>(channel.getMembers()).get(randomInt);
    }

    @Override
    public SlackMessageHandle<SlackMessageReply> sendMessage(SlackChannel channel, SlackUser origin, String message) {
        return session.sendMessage(channel, formatMessage(message, origin, channel), null);
    }

    @Override
    public SlackMessageHandle<SlackMessageReply> sendMessage(SlackChannel channel, SlackUser sender, String message, Emoji emoji) {
        SlackMessageHandle<SlackMessageReply> handle = sendMessage(channel, sender, message);

        session.addReactionToMessage(channel, handle.getReply().getTimestamp(), emoji.getEmojiName());

        return handle;
    }

    @Override
    public SlackMessageHandle<SlackMessageReply> sendMessage(SlackMessagePosted originator, String message) {
        return sendMessage(originator.getChannel(), originator.getSender(), message);
    }

    @Override
    public SlackMessageHandle<SlackMessageReply> sendMessage(SlackMessagePosted originator, String message, Emoji emoji, boolean placeReactionOnBotMsg) {
        SlackMessageHandle<SlackMessageReply> handle = sendMessage(originator.getChannel(), originator.getSender(), message);
        String tstamp = originator.getTimeStamp();
        if(placeReactionOnBotMsg) {
            tstamp = handle.getReply().getTimestamp();
        }
        session.addReactionToMessage(originator.getChannel(), tstamp, emoji.getEmojiName());
        return handle;
    }

    @Override
    public SlackMessageHandle<SlackMessageReply> sendPrivateMessage(SlackMessagePosted originator, String message) {
        return session.sendMessageToUser(originator.getSender(), message, null);
    }

    @Override
    public SlackMessageHandle<SlackMessageReply> sendMessageToUser(SlackUser user, String message, SlackAttachment attachment) {
        return session.sendMessageToUser(user, message, attachment);
    }

    @Override
    public SlackMessageHandle<SlackMessageReply> sendMessageToUser(String userName, String message, SlackAttachment attachment) {
        return session.sendMessageToUser(userName, message, attachment);
    }

    @Override
    public SlackMessageHandle<SlackChannelReply> openDirectMessageChannel(SlackUser user) {
        return session.openDirectMessageChannel(user);
    }

    @Override
    public SlackMessageHandle<SlackChannelReply> openMultipartyDirectMessageChannel(SlackUser... users) {
        return session.openMultipartyDirectMessageChannel(users);
    }

    @Override
    public void connect() throws IOException {
        session.connect();
        self = session.sessionPersona();

        messages.addPostInitMessages(self.getUserName());
    }

    @Override
    public void disconnect() throws IOException {
        session.disconnect();
    }

    @Override
    public boolean isConnected() {
        return session.isConnected();
    }

    @Override
    public SlackMessageHandle deleteMessage(String s, SlackChannel slackChannel) {
        return null;
    }

    @Override
    public SlackMessageHandle sendMessage(SlackChannel slackChannel, String s, SlackAttachment slackAttachment, SlackChatConfiguration slackChatConfiguration) {
        throw new UnsupportedOperationException("Please use the other methods !");
    }

    @Override
    public SlackMessageHandle sendMessage(SlackChannel slackChannel, String s, SlackAttachment slackAttachment) {
        throw new UnsupportedOperationException("Please use the other methods !");
    }

    @Override
    public SlackMessageHandle updateMessage(String s, SlackChannel slackChannel, String s1) {
        return null;
    }

    @Override
    public SlackMessageHandle sendMessageOverWebSocket(SlackChannel slackChannel, String s, SlackAttachment slackAttachment) {
        return null;
    }

    @Override
    public SlackMessageHandle addReactionToMessage(SlackChannel slackChannel, String s, String s1) {
        return null;
    }

    @Override
    public SlackMessageHandle joinChannel(String s) {
        return null;
    }

    @Override
    public SlackMessageHandle leaveChannel(SlackChannel slackChannel) {
        return null;
    }

    @Override
    public SlackMessageHandle<GenericSlackReply> inviteUser(String email, String firstName, boolean setActive) {
        return null;
    }

    @Override
    public SlackMessageHandle<SlackChannelReply> inviteToChannel(SlackChannel channel, SlackUser user) {
        return null;
    }

    @Override
    public SlackPersona.SlackPresence getPresence(SlackPersona slackPersona) {
        return session.getPresence(slackPersona);
    }

    @Override
    public void addchannelArchivedListener(SlackChannelArchivedListener slackChannelArchivedListener) {

    }

    @Override
    public void removeChannelArchivedListener(SlackChannelArchivedListener slackChannelArchivedListener) {

    }

    @Override
    public void addchannelCreatedListener(SlackChannelCreatedListener slackChannelCreatedListener) {

    }

    @Override
    public void removeChannelCreatedListener(SlackChannelCreatedListener slackChannelCreatedListener) {

    }

    @Override
    public void addchannelDeletedListener(SlackChannelDeletedListener slackChannelDeletedListener) {

    }

    @Override
    public void removeChannelDeletedListener(SlackChannelDeletedListener slackChannelDeletedListener) {

    }

    @Override
    public void addChannelRenamedListener(SlackChannelRenamedListener slackChannelRenamedListener) {

    }

    @Override
    public void removeChannelRenamedListener(SlackChannelRenamedListener slackChannelRenamedListener) {

    }

    @Override
    public void addChannelUnarchivedListener(SlackChannelUnarchivedListener slackChannelUnarchivedListener) {

    }

    @Override
    public void removeChannelUnarchivedListener(SlackChannelUnarchivedListener slackChannelUnarchivedListener) {

    }

    @Override
    public void addMessageDeletedListener(SlackMessageDeletedListener slackMessageDeletedListener) {
        session.addMessageDeletedListener(slackMessageDeletedListener);
    }

    @Override
    public void removeMessageDeletedListener(SlackMessageDeletedListener slackMessageDeletedListener) {
        session.removeMessageDeletedListener(slackMessageDeletedListener);
    }

    @Override
    public void addReactionAddedListener(ReactionAddedListener reactionAddedListener) {

    }

    @Override
    public void removeReactionAddedListener(ReactionAddedListener reactionAddedListener) {

    }

    @Override
    public void addReactionRemovedListener(ReactionRemovedListener reactionRemovedListener) {

    }

    @Override
    public void removeReactionRemovedListener(ReactionRemovedListener reactionRemovedListener) {

    }

    @Override
    public void addSlackConnectedListener(SlackConnectedListener slackConnectedListener) {

    }

    @Override
    public void removeSlackConnectedListener(SlackConnectedListener slackConnectedListener) {

    }

    @Override
    public SlackMessageHandle<GenericSlackReply> postGenericSlackCommand(Map<String, String> map, String s) {
        return session.postGenericSlackCommand(map, s);
    }

    @Override
    public SlackMessageHandle<SlackReply> archiveChannel(SlackChannel slackChannel) {
        return session.archiveChannel(slackChannel);
    }

    @Override
    public boolean isSelfMessage(SlackMessagePosted message) {
        return message.getSender().getId().equals(self.getId());
    }

    @Override
    public String getBotName() {
        return self.getUserName();
    }

    @Override
    public Collection<SlackChannel> getChannels() {
        return session.getChannels().stream().filter(chan -> !chan.isDirect()).collect(Collectors.toList());
    }

    @Override
    public Collection<SlackUser> getUsers() {
        return session.getUsers();
    }

    @Override
    public Collection<SlackBot> getBots() {
        return session.getBots();
    }

    @Override
    public SlackChannel findChannelByName(String channelName) {
        return session.findChannelByName(channelName);
    }

    @Override
    public SlackChannel findChannelById(String s) {
        return session.findChannelById(s);
    }

    @Override
    public SlackUser findUserByUserName(String userName) {
        return session.findUserByUserName(userName);
    }

    @Override
    public SlackUser findUserByEmail(String s) {
        return session.findUserByEmail(s);
    }

    @Override
    public SlackPersona sessionPersona() {
        return session.sessionPersona();
    }

    @Override
    public SlackBot findBotById(String s) {
        return session.findBotById(s);
    }

    @Override
    public SlackUser findUserById(String userId) {
        return session.findUserById(userId);
    }

    @Override
    public void addMessagePostedListener(SlackMessagePostedListener event) {
        session.addMessagePostedListener(event);
    }

    @Override
    public void removeMessagePostedListener(SlackMessagePostedListener event) {
        session.removeMessagePostedListener(event);
    }

    @Override
    public void addMessageUpdatedListener(SlackMessageUpdatedListener slackMessageUpdatedListener) {
        session.addMessageUpdatedListener(slackMessageUpdatedListener);
    }

    @Override
    public void removeMessageUpdatedListener(SlackMessageUpdatedListener slackMessageUpdatedListener) {
        session.removeMessageUpdatedListener(slackMessageUpdatedListener);
    }

    @Override
    public void addGroupJoinedListener(SlackGroupJoinedListener slackGroupJoinedListener) {
        session.addGroupJoinedListener(slackGroupJoinedListener);
    }

    @Override
    public void removeGroupJoinedListener(SlackGroupJoinedListener slackGroupJoinedListener) {
        session.removeGroupJoinedListener(slackGroupJoinedListener);
    }
}
