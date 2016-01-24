package com.st.nicobot.bot.handler;

import com.st.nicobot.bot.NicoBot;
import com.st.nicobot.utils.NicobotProperty;
import com.st.nicobot.bot.utils.Reaction;
import com.st.nicobot.services.Commands;
import com.st.nicobot.services.LeetGreetingService;
import com.st.nicobot.services.Messages;
import com.st.nicobot.services.PropertiesService;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
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

	@Autowired
	private Commands commands;

	@Autowired
	private PropertiesService properties;

	@Override
	public void onEvent(SlackMessagePosted message, SlackSession session) {
		if(!nicobot.isSelfMessage(message)) {
			if(nicobot.getChannels().contains(message.getChannel())) {
				onMessage(message);
			}
		}
	}
	
	@Override
	public void onMessage(SlackMessagePosted message) {

		boolean isCommand = commands.handleCommandEvent(message);

		if(isCommand) {
			return;
		}

		if(message.getChannel().getName().equals(properties.get(NicobotProperty.FEATURED_CHANNEL))) {


			String content = message.getMessageContent();
			String response = null;

			for (Reaction reac : messages.getSentences()) {
				// reaction ok
				if (reac.match(content)) {
					// déjà dit ?
					if (reac.canSaySentence(message.getChannel())) {
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
			if (greetingService.isLeetHourActive()) {
				greetingService.addGreeter(message);
			}

		}

	}

}
