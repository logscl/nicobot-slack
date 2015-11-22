package com.st.nicobot.bot.cmd;

import com.st.nicobot.bot.NicoBot;
import com.st.nicobot.bot.utils.NicobotProperty;
import com.st.nicobot.bot.utils.Option;
import com.st.nicobot.services.Messages;
import com.st.nicobot.services.PropertiesService;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

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
        String searchUri = properties.get(NicobotProperty.YOUTUBE_QUERY_URI);
        String searchArguments = StringUtils.join(args, "+");

        MultivaluedMap<String,String> queryParams = new MultivaluedMapImpl();

        queryParams.putSingle("key", properties.get(NicobotProperty.SEARCH_API_KEY));
        queryParams.putSingle("q", searchArguments);

        try {
            WebResource resource = Client.create().resource(searchUri);

            resource = resource.queryParams(queryParams);

            JSONObject response = resource.type(MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);

            if(response.has("items")) {
                String videoId = response.getJSONArray("items").getJSONObject(0).getJSONObject("id").getString("videoId");

                nicobot.sendMessage(opts.message, properties.get(NicobotProperty.YOUTUBE_VIDEO_URI)+videoId);
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
