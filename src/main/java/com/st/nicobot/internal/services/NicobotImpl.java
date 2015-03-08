package com.st.nicobot.internal.services;

import com.st.nicobot.bahaviors.NiConduct;
import com.st.nicobot.services.Nicobot;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackMessage;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * Created by Logs on 08-03-15.
 */
@Service("nicobotSession")
public class NicobotImpl implements Nicobot {

    private static Logger logger = LoggerFactory.getLogger(NicobotImpl.class);

    @Autowired
    private ApplicationContext ctx;

    @Autowired
    private Environment environment;

    private SlackSession session;

    private SlackUser self;

    private SlackChannel devChan;

    @PostConstruct
    private void postConstruct() {
        session = SlackSessionFactory.createWebSocketSlackSession(environment.getRequiredProperty("slack.api.key"));
        Map<String, NiConduct> behaviors = ctx.getBeansOfType(NiConduct.class);

        for (Map.Entry<String, NiConduct> entry : behaviors.entrySet()) {
            session.addMessageListener(entry.getValue());
        }


    }

    @Override
    public void sendMessage(SlackChannel channel, String message) {
        Boolean devMode = environment.getProperty("bot.dev.mode", Boolean.class, Boolean.FALSE);
        if(!devMode || (devMode && channel.getId().equals(devChan.getId()))) {
                session.sendMessage(channel, message, null, null, null);
        }
    }

    @Override
    public void connect() {
        session.connect();

        for(SlackUser user : session.getUsers()) {
            if(user.getUserName().equals("nicobot")) {
                self = user;
                break;
            }
        }

        devChan = null;
        for(SlackChannel chan : session.getChannels()) {
            if(chan.getName().equals("dev")) {
                devChan = chan;
                break;
            }
        }

        session.sendMessage(devChan,"Yop !", null, null, null);
    }

    @Override
    public boolean isSelfMessage(SlackMessage message) {
        return message.getSender().equals(self);
    }
}
