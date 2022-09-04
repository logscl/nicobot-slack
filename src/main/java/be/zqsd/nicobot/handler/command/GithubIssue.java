package be.zqsd.nicobot.handler.command;

import be.zqsd.nicobot.bot.Nicobot;
import be.zqsd.nicobot.bot.UserService;
import be.zqsd.thirdparty.GithubService;
import com.slack.api.model.event.MessageEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.of;

@ApplicationScoped
public class GithubIssue implements NiCommand {

    private final Nicobot nicobot;
    private final UserService userService;
    private final GithubService github;

    @Inject
    public GithubIssue(Nicobot nicobot,
                       UserService userService,
                       GithubService github) {
        this.nicobot = nicobot;
        this.userService = userService;
        this.github = github;
    }

    @Override
    public Collection<String> getCommandNames() {
        return List.of("!github", "!jira", "!tp", "!gh");
    }

    @Override
    public String getDescription() {
        return "Ajoute une feature / indique un bug sur nicobot.";
    }

    @Override
    public String getFormat() {
        return "!github \"Titre du bug/feature\" [\"explications supplémentaires\"]";
    }

    @Override
    public void doCommand(String command, Collection<String> arguments, MessageEvent triggeringMessage) {
        var title = findTitleFromArguments(arguments);
        if (title.isPresent()) {
            var issue = github.createIssue(title.get(), descriptionBody(arguments, triggeringMessage.getUser()));
            issue.ifPresentOrElse(
                    createdIssue -> nicobot.sendMessage(triggeringMessage, "Ok. J'y penserai : <%s|#%s>".formatted(createdIssue.getHtmlUrl(), createdIssue.getNumber())),
                    () -> nicobot.sendMessage(triggeringMessage, "Désolé, j'ai pas su créer l'issue :(")
            );
        } else {
            nicobot.sendEphemeral(triggeringMessage, "Mauvais format: !github \"issue\" \"description\"");
        }
    }

    private Optional<String> findTitleFromArguments(Collection<String> arguments) {
        return arguments.size() > 2 ? of(String.join(" ", arguments)) : arguments.stream().findFirst();
    }

    private String descriptionBody(Collection<String> arguments, String requesterId) {
        var content =  arguments.size() == 2 ? arguments.stream().skip(1).findFirst().orElse("") : "";
        var requesterName = userService.findUserName(requesterId).orElse(requesterId);
        return "%s%n- Requested by %s".formatted(content, requesterName);
    }
}
