package be.zqsd.nicobot.bot.behavior;

import be.zqsd.nicobot.bot.utils.Chance;
import be.zqsd.nicobot.bot.utils.Option;

/**
 * @author Julien
 *
 */
public interface NiConduct extends Chance {

    /**
     * Le comportement que nicobot doit adopter pour ce {@code NiConduct}.
     * @param opts
     * 		Les options
     */
    void behave(Option opts);
}
