package be.zqsd.nicobot.bot.handler;

import be.zqsd.nicobot.bot.utils.Chance;
import be.zqsd.nicobot.bot.utils.Option;
import be.zqsd.nicobot.bot.utils.Random;

/**
 * @author jlamby
 *
 */
public abstract class ConditionalMessageEvent extends AbstractMessageEvent implements Chance {

	/**
	 * L'implementation de la condition pour activer cet event. 
	 * 
	 * @param option
	 * 		Un objet contenant les differentes options sur lesquelles on peut faire le test.		
	 * @return 
	 * 		Doit retourner <code>true</code> si il faut activer cet event. <code>False</code> sinon.
	 */
	public abstract boolean testImpl(Option option);
	
	/**
	 * Le test.
	 * @param opt
	 * 		Les options
	 * @return
	 */
	final boolean testCondition(Option opt) {
		if (testImpl(opt)) {
			int chance = Random.MAX_CHANCE - Random.nextInt();
			
			if (chance < getChance()) {
				return true;
			}
		}
		
		return false;
	}

}
