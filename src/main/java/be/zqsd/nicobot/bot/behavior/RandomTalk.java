package be.zqsd.nicobot.bot.behavior;

import be.zqsd.nicobot.bot.NicoBot;
import be.zqsd.nicobot.bot.utils.Option;
import be.zqsd.nicobot.services.Messages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Julien
 *
 */
@Service
public class RandomTalk implements NiConduct {

	@Autowired
	private Messages messages;

	@Autowired
	private NicoBot nicobot;
	
	public RandomTalk() {	}
	
	@Override
	public int getChance() {
		return 30;
	}

	@Override
	public void behave(Option opts) {
		if(!nicobot.isSelfMessage(opts.message)) {
			nicobot.sendMessage(opts.message, getRandomTalk());
		}
	}
	
	protected String getRandomTalk() {
		return messages.getRandomSpeech();
	}
}