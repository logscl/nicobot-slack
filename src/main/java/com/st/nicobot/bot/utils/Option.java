package com.st.nicobot.bot.utils;

import com.ullink.slack.simpleslackapi.SlackMessage;

/**
 * @author Julien
 *
 */
public class Option {

	public final SlackMessage message;
	
	public Option(SlackMessage message) {
		this.message = message;
	}
	
}
