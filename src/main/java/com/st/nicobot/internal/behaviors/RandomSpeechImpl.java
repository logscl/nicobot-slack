package com.st.nicobot.internal.behaviors;

import com.st.nicobot.bahaviors.NiConduct;
import com.st.nicobot.services.Messages;
import com.st.nicobot.services.Nicobot;
import com.ullink.slack.simpleslackapi.SlackMessage;
import com.ullink.slack.simpleslackapi.SlackSession;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Logs on 08-03-15.
 */
@Service("randomSpeech")
public class RandomSpeechImpl extends AbstractBehavior {

    @Autowired
    private Messages messages;

    @Autowired
    private Nicobot session;

    @Override
    public void handleMessage(SlackMessage message) {
        session.sendMessage(message.getChannel(), messages.getRandomSpeech());
    }

    @Override
    public int getChance() {
        return 50;
    }

    @Override
    public boolean includeSelfMessages() {
        return false;
    }
}
