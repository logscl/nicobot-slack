package be.zqsd.nicobot.bot.cmd;

import be.zqsd.nicobot.bot.NicoBot;
import be.zqsd.nicobot.bot.utils.Option;
import be.zqsd.nicobot.services.Messages;
import be.zqsd.nicobot.services.PropertiesService;
import be.zqsd.nicobot.utils.NicobotProperty;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static be.zqsd.nicobot.utils.NicobotProperty.GITHUB_REPOSITORY_NAME;
import static be.zqsd.nicobot.utils.NicobotProperty.GITHUB_REPOSITORY_USERNAME;

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

            GitHubClient client = new GitHubClient();
            client.setOAuth2Token(properties.get(NicobotProperty.GITHUB_API_KEY));

            IssueService issueService = new IssueService(client);

            Issue issue = new Issue()
                    .setTitle(arguments.issueTitle)
                    .setBody(buildIssueText(arguments, opts));

            Issue savedIssue = issueService.createIssue(properties.get(GITHUB_REPOSITORY_USERNAME), properties.get(GITHUB_REPOSITORY_NAME), issue);
            nicobot.sendMessage(opts.message, messages.getMessage("githubAdded", savedIssue.getHtmlUrl()));
        } catch (IOException e) {
            nicobot.sendMessage(opts.message, messages.getMessage("githubFailure"));
        } catch (IllegalArgumentException ex) {
            nicobot.sendPrivateMessage(opts.message, ex.getMessage());
        }
    }

    private String buildIssueText(GithubArguments arguments, Option opts) {
        String issueText = arguments.issueBody != null ? arguments.issueBody : "";
        issueText += "\r\n- Requested by " + opts.message.getSender().getUserName();
        return issueText;
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
