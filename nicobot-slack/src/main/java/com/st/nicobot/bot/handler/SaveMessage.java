package com.st.nicobot.bot.handler;

import com.st.nicobot.api.domain.model.Message;
import com.st.nicobot.api.services.APIMessageService;
import com.st.nicobot.bot.NicoBot;
import com.st.nicobot.services.PropertiesService;
import com.st.nicobot.utils.NicobotProperty;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Julien
 */
@Service
public class SaveMessage extends AbstractMessageEvent {

	@Autowired
	private APIMessageService apiMessageService;
	
	@Autowired
	private NicoBot nicoBot;

	@Autowired
	private PropertiesService properties;

	private Pattern nameIdPattern = Pattern.compile("<@([0-9A-Z]+)>");
	private Pattern nameIdWithStrPattern = Pattern.compile("<@([0-9A-Z]+)\\|(.*?)>");
	private Pattern channelIdPattern = Pattern.compile("<#([0-9A-Z]+)>");

	@Override
	public void onEvent(SlackMessagePosted message, SlackSession session) {
		if(message.getChannel().getName().equals(properties.get(NicobotProperty.FEATURED_CHANNEL))) {
			onMessage(message);
		}
	}

	@Override
	public void onMessage(final SlackMessagePosted message) {
		Thread t = new Thread() {
			public void run() {
				Message msg = new Message(new DateTime(), message.getSender().getUserName(), replaceTokens(message.getMessageContent()));
				apiMessageService.saveMessages(Collections.singletonList(msg));
			}
		};
		
		t.start();
	}

	private String replaceTokens(String message) {
		String returnMessage = message;
		Matcher nameIdMatcher = nameIdPattern.matcher(message);
		while(nameIdMatcher.find()) {
			String userID = nameIdMatcher.group(1);
			if(nicoBot.getSession().findUserById(userID) != null) {
				String username = nicoBot.getSession().findUserById(userID).getUserName();
				returnMessage = returnMessage.replaceAll("<@"+userID+">", username);
			}
		}

		Matcher nameIdWithStrMatcher = nameIdWithStrPattern.matcher(message);
		while(nameIdWithStrMatcher.find()) {
			String userID = nameIdWithStrMatcher.group(1);
			String username = nameIdWithStrMatcher.group(2);
			returnMessage = returnMessage.replaceAll("<@"+userID+"\\|"+username+">", username);
		}

		Matcher channelIdMatcher = channelIdPattern.matcher(message);
		while(channelIdMatcher.find()) {
			String chanID = channelIdMatcher.group(1);
			if(nicoBot.getSession().findChannelById(chanID) != null) {
				String username = nicoBot.getSession().findChannelById(chanID).getName();
				returnMessage = returnMessage.replaceAll("<#"+chanID+">", username);
			}
		}
		return returnMessage;
	}

	/**
	 * TODO:
	 * Remplacer <@BBBB> -> username <@([0-9A-Z]+)>
	 * Rempalcer <@BBB|xxx> -> xxx <@([0-9A-Z]+)\|(.*)>
	 * Remplacer <#BBB> -> nom du chan <#([0-9A-Z]+)>
	 */

}
