package be.zqsd.nicobot.bot.cmd;

import com.google.api.services.customsearch.Customsearch;
import org.springframework.stereotype.Service;

/**
 * Created by Logs on 27-10-15.
 */
@Service
public class GifSearch extends AbstractSearch {

    private static final String COMMAND = "!gif";
    private static final String FORMAT = "!gif query";
    private static final String DESC = "Recherche un gif sur les internets et retourne le premier résultat";

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
        //map.put("tbs","itp:animated");
        search.setSearchType("image");
        search.setFileType("gif");
        search.setHq("animated");
    }
}
