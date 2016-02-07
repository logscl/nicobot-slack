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
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Map;

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

    private JSONObject lastSearchResult = null;
    private int searchIndex = 0;

    protected abstract Map<String, String> getSpecificQueryArguments();


    @Override
    protected void doCommand(String command, String[] args, Option opts) {
        String searchUri = properties.get(NicobotProperty.SEARCH_URI);
        String searchArguments = StringUtils.join(args, "+");

        if ("next".equals(searchArguments) && lastSearchResult != null) {
            searchIndex++;
            searchAndSendLink(opts);
            return;
        } else {
            searchIndex=0;
        }

        if(command.compareToIgnoreCase(getCommandName()) != 0) {
            searchArguments = command.substring(1) + "+" + searchArguments;
        }

        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();

        if (getSpecificQueryArguments() != null && getSpecificQueryArguments().size() > 0) {
            for (Map.Entry<String, String> entry : getSpecificQueryArguments().entrySet()) {
                queryParams.putSingle(entry.getKey(), entry.getValue());
            }
        }

        queryParams.putSingle("cx", properties.get(NicobotProperty.SEARCH_CX_KEY));
        queryParams.putSingle("key", properties.get(NicobotProperty.SEARCH_API_KEY));
        queryParams.putSingle("q", searchArguments);
        queryParams.putSingle("fields", "items/link");

        try {
            WebResource resource = Client.create().resource(searchUri);

            resource = resource.queryParams(queryParams);

            JSONObject response = resource.type(MediaType.APPLICATION_JSON_TYPE).get(JSONObject.class);

            if(response.has("items")) {
                lastSearchResult = response;
                searchAndSendLink(opts);
            } else {
                logger.info("Query [{}] has no results",searchArguments);
                nicobot.sendMessage(opts.message, messages.getMessage("nothingFound"));
                lastSearchResult = null;
            }
        } catch (UniformInterfaceException e) {
            logger.error(e.getMessage(),e);
            logger.info("Additional Exception Info: "+e.getResponse().getEntity(String.class));
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }

    }

    private void searchAndSendLink(Option opts) {
        try {
            String foundLink = lastSearchResult.getJSONArray("items").getJSONObject(searchIndex).getString("link");
            nicobot.sendMessage(opts.message, foundLink);
        } catch (JSONException e) {
            logger.error(e.getMessage(),e);
        }
    }


}
