package com.st.nicobot.bot.handler;

import com.st.nicobot.bot.NicoBot;
import com.st.nicobot.bot.utils.Option;
import com.st.nicobot.services.Messages;
import com.ullink.slack.simpleslackapi.SlackMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author jlamby
 *
 */
@Service
public class RiamaskinWatcher extends ConditionalMessageEvent {

    private static final String RIAMASKIN_STRING = "riamaskin";

    @Autowired
    private Messages messages;
    
    @Autowired
    private NicoBot nicobot;
    
    @Override
    public int getChance() {
        return 250;
    }

    @Override
    public boolean testImpl(Option option) {
    	return option.message.getMessageContent().equalsIgnoreCase(RIAMASKIN_STRING);
    }

    @Override
    public void onEvent(SlackMessage message) {
    	Option o = new Option(message);
    	
    	if (testCondition(o)) {
    		nicobot.sendMessage(message, messages.getOtherMessage(RIAMASKIN_STRING));
    	}
    }

}
