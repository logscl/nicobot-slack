package com.st.nicobot.bot.behavior;

import com.st.nicobot.bot.utils.Chance;
import com.st.nicobot.bot.utils.Option;

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
