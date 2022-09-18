package be.zqsd.nicobot.handler.command;

import be.zqsd.nicobot.bot.Nicobot;
import be.zqsd.nicobot.bot.UserService;
import be.zqsd.nicobot.persistence.Persistence;
import com.slack.api.model.event.MessageEvent;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

import static java.time.LocalDate.now;
import static java.util.Collections.singletonList;
import static org.slf4j.LoggerFactory.getLogger;

@ApplicationScoped
public class TopHgt implements NiCommand {
    private static final Logger LOG = getLogger(TopHgt.class);

    private final Nicobot nicobot;
    private final Persistence persistence;
    private final UserService userService;

    @Inject
    public TopHgt(Nicobot nicobot,
                  Persistence persistence,
                  UserService userService) {
        this.nicobot = nicobot;
        this.persistence = persistence;
        this.userService = userService;
    }

    @Override
    public Collection<String> getCommandNames() {
        return singletonList("!hgt");
    }

    @Override
    public String getDescription() {
        return "Donne le top score au HGT";
    }

    @Override
    public String getFormat() {
        return "!hgt";
    }

    @Override
    public void doCommand(String command, Collection<String> arguments, MessageEvent triggeringMessage) {
        try {
            var users = persistence.getYearlyHgtScores(triggeringMessage.getChannel());
            if (users.isEmpty()) {
                nicobot.sendMessage(triggeringMessage, "Personne ! Bande de clinches ! :(");
            } else {
                var scores = users.stream()
                        .map(score -> {
                            var name = userService.userNameWithoutHighlight(score.getUserId()).orElse("??");
                            return "%s (%s)".formatted(name, score.getScore());
                        })
                        .collect(Collectors.joining(", "));

                var message = "Les meilleurs en %s (%s jours cette année): %s".formatted(now().getYear(), now().getDayOfYear(), scores);
                nicobot.sendMessage(triggeringMessage, message);
            }
        } catch (IOException e) {
            LOG.error("Unable to call the persistence service", e);
        }
    }
}
