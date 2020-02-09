package be.zqsd.nicobot.bot.handler;

import be.zqsd.nicobot.bot.NicoBot;
import be.zqsd.nicobot.bot.utils.Option;
import be.zqsd.nicobot.bot.utils.Reaction;
import be.zqsd.nicobot.services.Commands;
import be.zqsd.nicobot.services.LeetGreetingService;
import be.zqsd.nicobot.services.Messages;
import be.zqsd.nicobot.services.PropertiesService;
import be.zqsd.nicobot.utils.NicobotProperty;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ParseReactions extends ConditionalMessageEvent {

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
	public int getChance() {
		return 500;
	}

	@Override
	public boolean testImpl(Option option) {
		// N'importe quel message peut trigger le random talk
		return true;
	}

	@Override
	public void onEvent(SlackMessagePosted message, SlackSession session) {
		if(!nicobot.isSelfMessage(message)) {
			if(nicobot.getSession().getChannels().contains(message.getChannel())) {
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
