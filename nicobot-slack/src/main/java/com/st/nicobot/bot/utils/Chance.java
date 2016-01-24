package com.st.nicobot.bot.utils;

/**
 * 
 * @author jlamby
 *
 */
public interface Chance {

    /*
     * En utilisant les ‰ on beneficie d'une granularité d'activation plus fine tout en evitant de travailler avec de vils double/float !
     * 100%     => 1        1000‰   => 100%
     * 10%      => 0.1      100‰    => 10%
     * 0.1%     => 0.001    1‰      => 0.1%
     */
    /**
     * <p>Retourne le taux d'activation pour ce comportement (en ‰).</p>
     * @return
     */
    int getChance();

}
