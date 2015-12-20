package com.st.nicobot.bot.cmd;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

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
    protected Map<String, String> getSpecificQueryArguments() {
        Map<String,String> map = new HashMap<>();
        map.put("searchType","image");
        map.put("fileType","gif");
        map.put("hq","animated");
        map.put("tbs","itp:animated");
        return map;
    }
}
