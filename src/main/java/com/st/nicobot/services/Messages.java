package com.st.nicobot.services;

import java.util.Set;

import com.st.nicobot.bot.utils.Reaction;

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
	
	String getOtherMessage(String key);
	
	/**
	 * Retourne un message de bienvenue aléatoire, ou conditionné par le nombre d'arrivées
	 * 
	 * @param nbr le nombre d'arrivées ajd du user
	 * @return un message
	 */
	String getWelcomeMessage(Integer nbr);

    String getRandomSpeech();
}