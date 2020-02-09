package be.zqsd.nicobot.bot.cmd;

import com.google.api.services.customsearch.Customsearch;
import org.springframework.stereotype.Service;

/**
 * Created by Logs on 22-08-15.
 */
@Service
public class WebSearch extends AbstractSearch {

    private static final String COMMAND = "!search";
    private static final String FORMAT = "!search query";
    private static final String DESC = "Recherche un lien sur les internets et retourne le premier résultat. !search next pour le résultat suivant";

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
    protected void addSpecificQueryArguments(Customsearch.Cse.List search) {

    }
}
