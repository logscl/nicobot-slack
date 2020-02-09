package be.zqsd.nicobot.services;

import be.zqsd.nicobot.bot.cmd.NiCommand;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;

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
	boolean handleCommandEvent(SlackMessagePosted message);

	/**
	 * Boucle sur les commandes actives connues et retourne vrai si le texte entré démarre comme une commande
	 * @param message
	 * @return
	 */
	boolean isProbableCommand(String message);
	
}
