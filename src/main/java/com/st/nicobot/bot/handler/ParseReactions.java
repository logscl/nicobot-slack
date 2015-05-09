package com.st.nicobot.bot.handler;

import com.st.nicobot.bot.NicoBot;
import com.st.nicobot.bot.utils.Reaction;
import com.st.nicobot.services.LeetGreetingService;
import com.st.nicobot.services.Messages;
import com.ullink.slack.simpleslackapi.SlackMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ParseReactions extends AbstractMessageEvent {

	@Autowired
	private Messages messages;
	
	@Autowired
	private NicoBot nicobot;
	
	@Autowired
    private LeetGreetingService greetingService;
	
	@Override
	public void onMessage(SlackMessage message) {
		if(nicobot.isSelfMessage(message)) {
			return;
		}

		String content = message.getMessageContent();
		String response = null;
		
		for(Reaction reac : messages.getSentences()) {
			// reaction ok
			if(reac.match(content)) {
				// déjà dit ?
				if(reac.canSaySentence(message.getChannel())) {
					reac.addSpokenTime(message.getChannel());
					response = reac.getResponse();
				}
				break;
			}
		}
		
		if (response != null) {
			nicobot.sendMessage(message, response);
		}
		
		// Happy Hour handling
		if(greetingService.isLeetHourActive()) {
            greetingService.addGreeter(message);
        }

	}

}
