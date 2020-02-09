package be.zqsd.nicobot.internal.bot;

import be.zqsd.nicobot.bot.NicoBot;
import be.zqsd.nicobot.bot.event.MessageEvent;
import be.zqsd.nicobot.bot.utils.Emoji;
import be.zqsd.nicobot.bot.utils.Option;
import be.zqsd.nicobot.services.BehaviorsService;
import be.zqsd.nicobot.services.Messages;
import be.zqsd.nicobot.services.PropertiesService;
import be.zqsd.nicobot.services.UsernameService;
import be.zqsd.nicobot.utils.NicobotProperty;
import com.ullink.slack.simpleslackapi.*;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import com.ullink.slack.simpleslackapi.replies.SlackMessageReply;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private static Pattern userIDPattern = Pattern.compile("<@(U[0-9A-Z]+)>");

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
     * Format une chaine de caractere en remplacant les "#p" par {@code sender} et les "#c" par {@code channel}.
     * @param message
     * @param sender
     * @param channel
     * @return
     */
    private String formatMessage(String message, SlackUser sender, SlackChannel channel) {
        if(message != null) {
            if (sender != null) {
                message = message.replaceAll("#p", usernameService.getNoHLName(sender));
            }
            message = message.replaceAll("#c", channel.getName());

            if (sender != null && message.contains("#u")) {
                message = message.replaceAll("#u", usernameService.getNoHLName(getRandomUserFromChannel(channel)));
            }
        }

        return message;
    }

    private SlackUser getRandomUserFromChannel(SlackChannel channel) {
        int randomInt = RandomUtils.nextInt(0, channel.getMembers().size());
        return new ArrayList<>(channel.getMembers()).get(randomInt);
    }

    @Override
    public SlackMessageHandle<SlackMessageReply> sendMessage(SlackChannel channel, SlackUser origin, String message) {
        return session.sendMessage(channel, formatMessage(message, origin, channel));
    }

    @Override
    public SlackMessageHandle<SlackMessageReply> sendMessage(SlackChannel channel, SlackUser origin, String message, SlackAttachment attachment) {
        return session.sendMessage(channel, formatMessage(message, origin, channel), attachment);
    }

    @Override
    public SlackMessageHandle<SlackMessageReply> sendMessage(SlackChannel channel, SlackUser sender, String message, Emoji emoji) {
        SlackMessageHandle<SlackMessageReply> handle = sendMessage(channel, sender, message);

        session.addReactionToMessage(channel, handle.getReply().getTimestamp(), emoji.getEmojiName());

        return handle;
    }

    @Override
    public SlackMessageHandle<SlackMessageReply> sendMessage(SlackMessagePosted originator, String message) {
        SlackPreparedMessage preparedMessage = new SlackPreparedMessage.Builder()
                .withMessage(formatMessage(message, originator.getSender(), originator.getChannel()))
                .withUnfurl(true)
                .withThreadTimestamp(originator.getThreadTimestamp())
                .build();
        return session.sendMessage(originator.getChannel(), preparedMessage);
    }

    @Override
    public SlackMessageHandle<SlackMessageReply> sendMessage(SlackMessagePosted originator, String message, SlackAttachment attachment) {
        return sendMessage(originator.getChannel(), originator.getSender(), message, attachment);
    }

    @Override
    public SlackMessageHandle<SlackMessageReply> sendMessage(SlackMessagePosted originator, String message, Emoji emoji, boolean placeReactionOnBotMsg) {
        SlackMessageHandle<SlackMessageReply> handle = sendMessage(originator, message);
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
    public SlackMessageHandle<SlackMessageReply> sendFile(SlackMessagePosted originator, byte[] fileBytes, String fileName) {
        return session.sendFile(originator.getChannel(), fileBytes, fileName);
    }

    @Override
    public SlackMessageHandle<SlackMessageReply> addReactionToMessage(SlackChannel channel, String messageTimeStamp, String emojiCode) {
        return session.addReactionToMessage(channel, messageTimeStamp, emojiCode);
    }

    @Override
    public SlackUser findUser(String userQuery) {
        Matcher m = userIDPattern.matcher(userQuery);
        if(m.matches()) {
            String id = m.group(1);
            return session.findUserById(id);
        } else {
            return session.findUserByUserName(userQuery);
        }
    }

    @Override
    public void connect() throws IOException {
        session.connect();

        messages.addPostInitMessages(session.sessionPersona());
    }

    @Override
    public boolean isSelfMessage(SlackMessagePosted message) {
        return message.getSender().getId().equals(session.sessionPersona().getId());
    }

    @Override
    public SlackSession getSession() {
        return session;
    }
}
