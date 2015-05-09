package com.st.nicobot.bot.handler;

import com.st.nicobot.bot.NicoBot;
import com.st.nicobot.bot.behavior.BackwardsSpeaking;
import com.st.nicobot.bot.utils.Option;
import com.ullink.slack.simpleslackapi.SlackMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Lorsque le message recu est un palindrome, il y a une chance que nicobot troll et le renvoi sans modif (simulation de 
 * {@link BackwardsSpeaking}.
 * 
 * @author Julien
 *
 */
@Service
public class TrollBackwardsSpeaking extends ConditionalMessageEvent {

	@Autowired
	private NicoBot nicobot;
	
    @Override
    public int getChance() {
        return 250;
    }

    @Override
    public void onMessage(SlackMessage message) {
        if(nicobot.isSelfMessage(message)) {
            return;
        }
    	Option opt = new Option(message);
    	
    	if (testCondition(opt)) {
        	nicobot.sendMessage(message, message.getMessageContent());
        }
    }
    
    @Override
    public boolean testImpl(Option o) {
    	return StringUtils.reverse(o.message.getMessageContent()).equals(o.message.getMessageContent());
    }
}
