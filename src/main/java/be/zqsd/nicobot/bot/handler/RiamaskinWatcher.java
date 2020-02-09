package be.zqsd.nicobot.bot.handler;

import be.zqsd.nicobot.bot.NicoBot;
import be.zqsd.nicobot.bot.utils.Option;
import be.zqsd.nicobot.services.Messages;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
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
    public void onMessage(SlackMessagePosted message) {
    	Option o = new Option(message);
    	
    	if (testCondition(o)) {
    		nicobot.sendMessage(message, messages.getMessage(RIAMASKIN_STRING));
    	}
    }

}
