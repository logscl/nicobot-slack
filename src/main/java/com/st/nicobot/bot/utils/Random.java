package com.st.nicobot.bot.utils;


/**
 * @author Julien
 *
 */
public class Random {

    public static final int MAX_CHANCE = 1001;

    private static java.util.Random instanceRandom;

    private Random() {	}

    private static java.util.Random getInstance() {
        if (instanceRandom == null) {
            instanceRandom = new java.util.Random(1337);
        }

        return instanceRandom;
    }

    /**
     * Retourne le 1er entier pseudo aléatoire compris dans [0;1000]
     * @return
     */
    public static int nextInt() {
        return getInstance().nextInt(MAX_CHANCE);
    }
    
    /**
     * Retourne le 1er entier pseudo aléatoire compris dans [0;max];
     * @param max
     * @return
     */
    public static int nextInt(int max) {
    	return getInstance().nextInt(max);
    }

}
