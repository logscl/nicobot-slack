package be.zqsd.nicobot.bot.cmd;

import be.zqsd.nicobot.bot.NicoBot;
import be.zqsd.nicobot.bot.utils.Option;
import be.zqsd.nicobot.services.Messages;
import be.zqsd.nicobot.services.PropertiesService;
import be.zqsd.nicobot.utils.NicobotProperty;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult.Callback;
import com.google.maps.PlaceDetailsRequest;
import com.google.maps.PlacesApi;
import com.google.maps.TextSearchRequest;
import com.google.maps.errors.ApiException;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PlacesSearchResult;
import com.ullink.slack.simpleslackapi.SlackAttachment;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by Logs on 09-04-16.
 */
@Service
public class MapSearch extends NiCommand {

    private static Logger logger = LoggerFactory.getLogger(MapSearch.class);

    private static final String COMMAND = "!map";
    private static final String FORMAT = "!map query";
    private static final String DESC = "Recherche un emplacement sur Google Maps";
    private static final String[] ALIASES = {"!maps"};

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
    public List<String> getAliases() {
        return Arrays.asList(ALIASES);
    }

    @Override
    protected void doCommand(String command, String[] args, Option opts) {
        String searchArguments = StringUtils.join(args, "+");

        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey(properties.get(NicobotProperty.SEARCH_API_KEY))
                .build();

        TextSearchRequest textSearch = PlacesApi.textSearchQuery(context, searchArguments);

        try {
            PlacesSearchResult[] results = textSearch.await().results;
            if (results.length > 0) {
                PlacesSearchResult firstResult = results[0];
                PlaceDetails placeDetails = PlacesApi.placeDetails(context, firstResult.placeId).await();
                if (placeDetails != null) {
                    String placeUrl = placeDetails.url.toExternalForm();

                    // attachment + send
                    SlackAttachment attachment = new SlackAttachment();
                    attachment.setFallback(placeUrl);
                    attachment.setTitle(firstResult.name);
                    attachment.setTitleLink(placeUrl);
                    attachment.setText(firstResult.formattedAddress);
                    attachment.addMiscField("thumb_url", firstResult.icon.toExternalForm());

                    nicobot.sendMessage(opts.message, null, attachment);
                } else {
                    logger.info("Query for placeid [{}] has no results", firstResult.placeId);
                    nicobot.sendMessage(opts.message, messages.getMessage("nothingFound"));
                }

            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

}
