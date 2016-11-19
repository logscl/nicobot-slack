package com.st.nicobot.bot.cmd;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.st.nicobot.bot.NicoBot;
import com.st.nicobot.bot.utils.Option;
import com.st.nicobot.services.Messages;
import com.st.nicobot.services.PropertiesService;
import com.st.nicobot.utils.NicobotProperty;
import com.sun.jersey.api.client.UniformInterfaceException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Logs on 22-08-15.
 */
@Service
public class YoutubeSearch extends NiCommand {

    private static Logger logger = LoggerFactory.getLogger(YoutubeSearch.class);

    private static final String COMMAND = "!yt";
    private static final String FORMAT = "!yt query";
    private static final String DESC = "Recherche une vidéo sur YouTube";

    @Autowired
    private NicoBot nicobot;

    @Autowired
    private PropertiesService properties;

    @Autowired
    private Messages messages;

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
    protected void doCommand(String command, String[] args, Option opts) {
        String searchArguments = StringUtils.join(args, "+");

        try {
            YouTube youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), httpRequest -> {
            }).setApplicationName("youtube-search").build();

            YouTube.Search.List search = youtube.search().list("id");

            search.setKey(properties.get(NicobotProperty.SEARCH_API_KEY));
            search.setQ(searchArguments);

            search.setType("video");

            search.setFields("items(id/videoId)");
            search.setMaxResults(1L);

            SearchListResponse searchListResponse = search.execute();
            List<SearchResult> searchResults = searchListResponse.getItems();
            if(searchResults != null && !searchResults.isEmpty()) {
                nicobot.sendMessage(opts.message, properties.get(NicobotProperty.YOUTUBE_VIDEO_URI)+searchResults.get(0).getId().getVideoId());
            } else {
                logger.info("Query [{}] has no results",searchArguments);
                nicobot.sendMessage(opts.message, messages.getMessage("nothingFound"));
            }
        } catch (UniformInterfaceException e) {
            logger.error(e.getMessage(),e);
            logger.info("Additional Exception Info: "+e.getResponse().getEntity(String.class));
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
    }
}
