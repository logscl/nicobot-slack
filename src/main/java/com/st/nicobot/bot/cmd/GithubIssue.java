package com.st.nicobot.bot.cmd;

import com.st.nicobot.bot.NicoBot;
import com.st.nicobot.bot.utils.NicobotProperty;
import com.st.nicobot.bot.utils.Option;
import com.st.nicobot.services.Messages;
import com.st.nicobot.services.PropertiesService;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Logs on 18-10-15.
 */
@Service
public class GithubIssue extends NiCommand {

    @Autowired
    private NicoBot nicobot;

    @Autowired
    private PropertiesService properties;

    @Autowired
    private Messages messages;

    private static Logger logger = LoggerFactory.getLogger(GithubIssue.class);

    private static final String COMMAND = "!github";
    private static final String FORMAT = "!github \"Titre du bug/feature\" [\"explications supplémentaires\"]";
    private static final String DESC = "Ajoute une feature / indique un bug sur nicobot.";


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
        return Arrays.asList("!jira", "!tp", "!gh");
    }

    @Override
    protected void doCommand(String command, String[] args, Option opts) {
        try {
            GithubArguments arguments = new GithubArguments(args);

            String issue = buildIssueStr(arguments, opts);

            WebResource resource = Client.create().resource(properties.get(NicobotProperty.GITHUB_ISSUE_ADD_URL));

            ClientResponse response = resource
                    .header("Authorization", "token "+properties.get(NicobotProperty.GITHUB_API_KEY))
                    .post(ClientResponse.class, issue);

            if(response.getClientResponseStatus() == ClientResponse.Status.CREATED) {
                nicobot.sendMessage(opts.message, messages.getMessage("githubAdded", getUrlWebFormat(response.getLocation().toString())));
            } else {
                logger.warn("Unable to add new request to GitHub ! Error: "+response.getClientResponseStatus());
                nicobot.sendMessage(opts.message, messages.getMessage("githubFailure"));
            }



        } catch (IllegalArgumentException ex) {
            nicobot.sendPrivateMessage(opts.message, ex.getMessage());
        } catch (JSONException ex) {
            logger.error("Unable to add issue to github !",ex);
        }
    }

    private String getUrlWebFormat(String apiUrl) {
        return apiUrl.replace("api.", "").replace("repos/", "");
    }

    private String buildIssueStr(GithubArguments arguments, Option opts) throws JSONException {
        JSONObject issue = new JSONObject();

        issue.put("title", arguments.issueTitle);

        String issueBody = arguments.issueBody != null ? arguments.issueBody : "";
        issueBody += "\r\n- Requested by " + opts.message.getSender().getUserName();

        issue.put("body", issueBody);

        return issue.toString();
    }

    private class GithubArguments {
        private String issueTitle, issueBody;

        public GithubArguments(String[] args) throws IllegalArgumentException {
            if(args != null && args.length > 0) {
                issueTitle = args[0];
                if(args.length == 2) {
                    issueBody = args[1];
                } else {
                    issueBody = null;
                    issueTitle = StringUtils.join(args, " ");
                }
            } else {
                throw new IllegalArgumentException("Too few arguments");
            }
        }
    }
}
