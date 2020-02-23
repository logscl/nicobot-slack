package be.zqsd.nicobot.bot.cmd;

import be.zqsd.nicobot.bot.NicoBot;
import be.zqsd.nicobot.bot.utils.Option;
import be.zqsd.nicobot.services.Messages;
import be.zqsd.nicobot.services.PropertiesService;
import be.zqsd.nicobot.utils.NicobotProperty;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
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

    private List<SearchResult> lastSearchResult = null;
    private int searchIndex = 0;

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

        if ("next".equals(searchArguments) && lastSearchResult != null) {
            searchIndex++;
            nicobot.sendMessage(opts.message, properties.get(NicobotProperty.YOUTUBE_VIDEO_URI)+lastSearchResult.get(searchIndex).getId().getVideoId());
            return;
        } else {
            searchIndex = 0;
        }

        try {
            YouTube youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), httpRequest -> {
            }).setApplicationName("youtube-search").build();

            YouTube.Search.List search = youtube.search().list("id");

            search.setKey(properties.get(NicobotProperty.SEARCH_API_KEY));
            search.setQ(searchArguments);

            search.setType("video");

            search.setFields("items(id/videoId)");
            search.setMaxResults(10L);

            SearchListResponse searchListResponse = search.execute();
            List<SearchResult> searchResults = searchListResponse.getItems();
            if(searchResults != null && !searchResults.isEmpty()) {
                lastSearchResult = searchResults;
                nicobot.sendMessage(opts.message, properties.get(NicobotProperty.YOUTUBE_VIDEO_URI)+searchResults.get(0).getId().getVideoId());
            } else {
                logger.info("Query [{}] has no results",searchArguments);
                nicobot.sendMessage(opts.message, messages.getMessage("nothingFound"));
            }
        } catch (IOException e) {
            logger.error(e.getMessage(),e);
        }
    }
}
