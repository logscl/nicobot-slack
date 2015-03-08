package com.st.nicobot.internal.behaviors;

import com.st.nicobot.bahaviors.NiConduct;
import com.st.nicobot.services.Nicobot;
import com.ullink.slack.simpleslackapi.SlackMessage;
import com.ullink.slack.simpleslackapi.SlackMessageListener;
import com.ullink.slack.simpleslackapi.SlackSession;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Logs on 08-03-15.
 */
public abstract class AbstractBehavior implements NiConduct {

    private static int MAX_CHANCE = 101;

    @Autowired
    private Nicobot nicobot;

    @Override
    public void onSessionLoad(SlackSession session) {

    }

    @Override
    public void onMessage(SlackMessage message) {
        if(includeSelfMessages() || !nicobot.isSelfMessage(message)) {
            if(RandomUtils.nextInt(0, MAX_CHANCE) > getChance()) {
                handleMessage(message);
            }
        }
    }
}
