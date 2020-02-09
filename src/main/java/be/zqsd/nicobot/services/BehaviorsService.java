package be.zqsd.nicobot.services;

import be.zqsd.nicobot.bot.utils.Option;

/**
 * @author Julien
 *
 */
public interface BehaviorsService {

	/**
	 * Genere un nombre aleatoire qui va etre la probablité de déclencher un {@code NiConduct}.
	 * @param opts
	 * 		Les options
	 */
	void randomBehave(Option opts);

}
