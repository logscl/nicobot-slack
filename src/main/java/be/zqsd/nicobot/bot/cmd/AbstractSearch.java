package be.zqsd.nicobot.bot.cmd;

import be.zqsd.nicobot.bot.NicoBot;
import be.zqsd.nicobot.bot.utils.Option;
import be.zqsd.nicobot.services.Messages;
import be.zqsd.nicobot.services.NudityDectionService;
import be.zqsd.nicobot.services.PropertiesService;
import be.zqsd.nicobot.utils.NicobotProperty;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.customsearch.Customsearch;
import com.google.api.services.customsearch.model.Result;
import com.google.api.services.customsearch.model.Search;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by Logs on 22-08-15.
 */
public abstract class AbstractSearch extends NiCommand {

    private static Logger logger = LoggerFactory.getLogger(AbstractSearch.class);

    @Autowired
    private NicoBot nicobot;

    @Autowired
    private PropertiesService properties;

    @Autowired
    private Messages messages;

    @Autowired
    private NudityDectionService nudityDectionService;

    private List<Result> lastSearchResult = null;
    private String lastSearchQuery = null;
    private int searchIndex = 0;

    protected abstract void addSpecificQueryArguments(Customsearch.Cse.List search);

    protected boolean needNSFWCheck() {
        return false;
    }


    @Override
    protected void doCommand(String command, String[] args, Option opts) {
        String searchArguments = StringUtils.join(args, "+");

        if ("next".equals(searchArguments) && lastSearchResult != null) {
            searchIndex++;
            searchAndSendLink(opts);
            return;
        } else {
            searchIndex = 0;
        }

        if (command.compareToIgnoreCase(getCommandName()) != 0) {
            searchArguments = command.substring(1) + "+" + searchArguments;
        }

        try {
            Customsearch customsearch = new Customsearch.Builder(new NetHttpTransport(), new JacksonFactory(), httpRequest -> {
            }).setApplicationName("google-search").build();

            Customsearch.Cse.List search = customsearch.cse().list("id");

            search.setKey(properties.get(NicobotProperty.SEARCH_API_KEY));
            search.setCx(properties.get(NicobotProperty.SEARCH_CX_KEY));
            search.setFields("items/link,items/displayLink");
            search.setQ(searchArguments);
            addSpecificQueryArguments(search);

            Search searchResponse = search.execute();
            List<Result> results = searchResponse.getItems();

            if (results != null && !results.isEmpty()) {
                lastSearchResult = results;
                lastSearchQuery = String.join(" ", args);
                searchAndSendLink(opts);
            } else {
                logger.info("Query [{}] has no results", searchArguments);
                nicobot.sendMessage(opts.message, messages.getMessage("nothingFound"));
                lastSearchResult = null;
                lastSearchQuery = null;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }

    private void searchAndSendLink(Option opts) {
        Result foundResult = lastSearchResult.get(searchIndex);
        while (foundResult.getLink().contains("x-raw-image")) {
            searchIndex++;
            foundResult = lastSearchResult.get(searchIndex);
        }

        if(needNSFWCheck()) {
            logger.info("No service for nudity detection yet :(");
        } else {
            nicobot.sendMessage(opts.message, formatMessage(foundResult.getLink(), foundResult.getDisplayLink()));
        }
    }

    private String formatMessage(String link, String displayLink) {
        return String.format("<%s|%s (%s)>", link, lastSearchQuery, displayLink);
    }


}
