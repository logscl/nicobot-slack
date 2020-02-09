package com.st.nicobot.bot.cmd;

import com.st.nicobot.bot.NicoBot;
import com.st.nicobot.bot.utils.Option;
import com.st.nicobot.services.Messages;
import com.st.nicobot.services.PropertiesService;
import com.st.nicobot.utils.NicobotProperty;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.ullink.slack.simpleslackapi.SlackAttachment;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Arrays;
import java.util.List;

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
        String searchUri = properties.get(NicobotProperty.GOOGLE_MAPS_URI);
        String searchArguments = StringUtils.join(args, "+");

        MultivaluedMap<String,String> queryParams = new MultivaluedMapImpl();

        queryParams.putSingle("key", properties.get(NicobotProperty.SEARCH_API_KEY));
        queryParams.putSingle("query", searchArguments);

        try {
            WebResource resource = Client.create().resource(searchUri);

            resource = resource.queryParams(queryParams);

            JSONObject response = resource.type(MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);

            boolean hasResults = !"ZERO_RESULTS".equals(response.getString("status"));

            if(hasResults) {
                JSONObject mapResult = response.getJSONArray("results").getJSONObject(0);
                String address = mapResult.getString("formatted_address");
                String name = mapResult.getString("name");
                String iconUrl = mapResult.getString("icon");
                String placeId= mapResult.getString("place_id");

                String placeUri = properties.get(NicobotProperty.GOOGLE_MAPS_PLACE_URI);
                queryParams.remove("query");
                queryParams.putSingle("placeid", placeId);

                resource = Client.create().resource(placeUri);
                resource = resource.queryParams(queryParams);

                response = resource.type(MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);

                hasResults = !"NOT_FOUND".equals(response.getString("status"));

                if(hasResults) {
                    JSONObject placeResult = response.getJSONObject("result");
                    String placeUrl = placeResult.getString("url");

                    // attachment + send
                    SlackAttachment attachment = new SlackAttachment();
                    attachment.setFallback(placeUrl);
                    //attachment.setPretext(address);
                    attachment.setTitle(name);
                    attachment.setTitleLink(placeUrl);
                    attachment.setText(address);
                    attachment.addMiscField("thumb_url", iconUrl);

                    nicobot.sendMessage(opts.message, null, attachment);

                } else {
                    logger.info("Query for placeid [{}] has no results",placeId);
                    nicobot.sendMessage(opts.message, messages.getMessage("nothingFound"));
                }
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
