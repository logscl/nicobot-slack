package com.st.nicobot.services;

import com.st.nicobot.bot.cmd.NiCommand;
import com.ullink.slack.simpleslackapi.SlackMessage;

/**
 * @author Julien
 *
 */
public interface Commands {

	NiCommand getFirstLink();

	/**
	 * Gère le module des commandes
	 * @param message
	 * @return true si une commande a été gérée, false sinon
	 */
	boolean handleCommandEvent(SlackMessage message);
	
}
