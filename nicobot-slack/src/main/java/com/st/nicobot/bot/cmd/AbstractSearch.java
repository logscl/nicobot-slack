package com.st.nicobot.bot.cmd;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.customsearch.Customsearch;
import com.google.api.services.customsearch.model.Result;
import com.google.api.services.customsearch.model.Search;
import com.st.nicobot.bot.NicoBot;
import com.st.nicobot.bot.utils.Option;
import com.st.nicobot.services.Messages;
import com.st.nicobot.services.NudityDectionService;
import com.st.nicobot.services.PropertiesService;
import com.st.nicobot.utils.NicobotProperty;
import com.ullink.slack.simpleslackapi.SlackMessageHandle;
import com.ullink.slack.simpleslackapi.replies.SlackMessageReply;
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
            search.setFields("items/link");
            search.setQ(searchArguments);
            addSpecificQueryArguments(search);

            Search searchResponse = search.execute();
            List<Result> results = searchResponse.getItems();

            if (results != null && !results.isEmpty()) {
                lastSearchResult = results;
                searchAndSendLink(opts);
            } else {
                logger.info("Query [{}] has no results", searchArguments);
                nicobot.sendMessage(opts.message, messages.getMessage("nothingFound"));
                lastSearchResult = null;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }

    private void searchAndSendLink(Option opts) {
        String foundLink = lastSearchResult.get(searchIndex).getLink();
        while (foundLink.contains("x-raw-image")) {
            searchIndex++;
            foundLink = lastSearchResult.get(searchIndex).getLink();
        }

        SlackMessageHandle<SlackMessageReply> postedMessage = nicobot.sendMessage(opts.message, foundLink);
        if(needNSFWCheck()) {
            try {
                boolean hasNudity = nudityDectionService.hasNudity(foundLink);
                if(hasNudity) {
                    nicobot.addReactionToMessage(opts.message.getChannel(), postedMessage.getReply().getTimestamp(), ":nsfw:");
                }
            } catch (Exception e) {
                logger.error("Unable to parse nudity Service result",e);
            }
        }
    }


}
