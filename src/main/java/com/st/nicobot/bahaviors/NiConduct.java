package com.st.nicobot.bahaviors;

import com.ullink.slack.simpleslackapi.SlackMessage;
import com.ullink.slack.simpleslackapi.SlackMessageListener;

/**
 * Created by Logs on 08-03-15.
 */
public interface NiConduct extends SlackMessageListener {

    /**
     * Gestion écoute messages reçus
     * @param message
     */
    void handleMessage(SlackMessage message);

    int getChance();

    /**
     * Indique si le bot doit également appliquer la réaction sur ses messages
     * @return
     */
    boolean includeSelfMessages();

}
