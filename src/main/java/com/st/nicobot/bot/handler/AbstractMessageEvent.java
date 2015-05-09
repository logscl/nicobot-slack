package com.st.nicobot.bot.handler;

import com.st.nicobot.bot.NicoBot;
import com.st.nicobot.bot.event.MessageEvent;
import com.ullink.slack.simpleslackapi.SlackMessage;
import com.ullink.slack.simpleslackapi.SlackSession;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author Logan
 */
public abstract class AbstractMessageEvent implements MessageEvent {

    @Override
    public void onSessionLoad(SlackSession session) {
        // do nothing for now
    }
}
