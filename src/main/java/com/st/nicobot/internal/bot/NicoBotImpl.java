package com.st.nicobot.internal.bot;

import com.st.nicobot.bot.NicoBot;
import com.st.nicobot.bot.event.MessageEvent;
import com.st.nicobot.bot.utils.NicobotProperty;
import com.st.nicobot.bot.utils.Option;
import com.st.nicobot.services.BehaviorsService;
import com.st.nicobot.services.PropertiesService;
import com.ullink.slack.simpleslackapi.*;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
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

    //@Autowired
    //private APIMessageService api;

    private SlackSession session;

    private SlackUser self;

    @PostConstruct
    private void postConstruct() {
        session = SlackSessionFactory.createWebSocketSlackSession(props.get(NicobotProperty.SLACK_API_KEY));
        Map<String, MessageEvent> eventMap = ctx.getBeansOfType(MessageEvent.class);

        session.addMessageListener(new SlackMessageListener() {
            @Override
            public void onSessionLoad(SlackSession session) {

            }

            @Override
            public void onMessage(SlackMessage message) {
                logger.info("MSG [{}] ON CHAN {} BY {} : {}", message.getSubType(), message.getChannel().getName(), message.getSender().getUserName(), message.getMessageContent());
                //Message apiMessage = new Message(new DateTime(), message.getSender().getUserName(), message.getMessageContent());
                //api.saveMessages(Arrays.asList(apiMessage));
                behaviors.randomBehave(new Option(message));
            }
        });

        for (Map.Entry<String, MessageEvent> entry : eventMap.entrySet()) {
            session.addMessageListener(entry.getValue());
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
            message = message.replaceAll("%p", sender.getUserName());
        }
        message = message.replaceAll("%c", channel.getName());

        if(sender != null && message.contains("%u")) {
            message = message.replaceAll("%u", getRandomUserFromChannel(channel).getUserName());
        }

        return message;
    }

    private SlackUser getRandomUserFromChannel(SlackChannel channel) {
        int randomInt = RandomUtils.nextInt(0, channel.getMembers().size());
        return new ArrayList<>(channel.getMembers()).get(randomInt);
    }

    @Override
    public void sendMessage(SlackChannel channel, SlackUser origin, String message) {
        session.sendMessage(channel, formatMessage(message, origin, channel), null);
    }

    @Override
    public void sendMessage(SlackMessage originator, String message) {
        sendMessage(originator.getChannel(), originator.getSender(), message);
    }

    @Override
    public void connect() throws IOException {
        session.connect();

        for(SlackUser user : session.getUsers()) {
            if (user.getUserName().equals(props.get(NicobotProperty.BOT_NAME))) {
                self = user;
                break;
            }
        }

        //session.sendMessage(devChan, "Yéop", null, null, null);
    }

    @Override
    public boolean isSelfMessage(SlackMessage message) {
        return message.getSender().equals(self);
    }

    @Override
    public Collection<SlackChannel> getChannels() {
        return session.getChannels();
    }

    @Override
    public SlackChannel findChannelByName(String channelName) {
        return session.findChannelByName(channelName);
    }

    @Override
    public SlackUser findUserByUserName(String userName) {
        return session.findUserByUserName(userName);
    }
}
