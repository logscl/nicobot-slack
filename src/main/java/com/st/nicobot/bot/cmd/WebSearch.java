package com.st.nicobot.bot.cmd;

import org.springframework.stereotype.Service;

/**
 * Created by Logs on 22-08-15.
 */
@Service
public class WebSearch extends AbstractSearch {

    private static final String COMMAND = "!search";
    private static final String FORMAT = "!search query";
    private static final String DESC = "Recherche un lien sur les internets et retourne le premier résultat";

    @Override
    public String getCommandName() {
        return COMMAND;
    }

    @Override
    public String getDescription() {
        return DESC;
    }

    @Override
    public String getFormat() {
        return FORMAT;
    }

    @Override
    protected boolean imageSearch() {
        return false;
    }
}
