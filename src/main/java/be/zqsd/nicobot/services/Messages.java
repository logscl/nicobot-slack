package be.zqsd.nicobot.services;

import be.zqsd.nicobot.bot.utils.Reaction;
import com.ullink.slack.simpleslackapi.SlackPersona;

import java.util.Set;

/**
 * @author Julien
 *
 */
public interface Messages {

	/**
	 * Retourne l'ensemble des messages auxquels nicobot va reagir
	 * @return
	 */
	Set<Reaction> getSentences();
	
	String getMessage(String key);

	/**
	 * Return a string formatted with {@link String#format(String, Object...)}
	 * @param key
	 * @param formatArgs
     * @return
     */
	String getMessage(String key, Object... formatArgs);
	
	/**
	 * Retourne un message de bienvenue aléatoire, ou conditionné par le nombre d'arrivées
	 * 
	 * @param nbr le nombre d'arrivées ajd du user
	 * @return un message
	 */
	String getWelcomeMessage(Integer nbr);

    String getRandomSpeech();

	void addPostInitMessages(SlackPersona bot);
}