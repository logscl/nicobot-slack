package be.zqsd.nicobot.bot.utils;

import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;

/**
 * @author Julien
 *
 */
public class Option {

	public final SlackMessagePosted message;
	
	public Option(SlackMessagePosted message) {
		this.message = message;
	}
	
}
