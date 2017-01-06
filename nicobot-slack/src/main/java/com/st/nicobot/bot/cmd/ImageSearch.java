package com.st.nicobot.bot.cmd;

import com.google.api.services.customsearch.Customsearch;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Logs on 22-08-15.
 */
@Service
public class ImageSearch extends AbstractSearch {

    private static final String COMMAND = "!img";
    private static final String FORMAT = "!img query";
    private static final String DESC = "Recherche une image sur les internets et retourne le premier résultat. !img next pour le résultat suivant";
    private static final String[] ALIASES = {"!sexy", "!brazzer", "!brazzers"};

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
    public List<String> getAliases() {
        return Arrays.asList(ALIASES);
    }

    @Override
    protected boolean needNSFWCheck() {
        return true;
    }

    @Override
    protected void addSpecificQueryArguments(Customsearch.Cse.List search) {
        search.setSearchType("image");
    }
}
