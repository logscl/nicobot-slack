package be.zqsd.nicobot.handler.command;

import be.zqsd.nicobot.bot.Nicobot;
import com.slack.api.model.event.MessageEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.concurrent.ThreadLocalRandom.current;

@ApplicationScoped
public class Decide extends NiCommand {

    private final Nicobot nicobot;

    @Inject
    public Decide(Nicobot nicobot) {
        this.nicobot = nicobot;
    }

    @Override
    public Collection<String> getCommandNames() {
        return List.of(
                "!decide",
                "!décide"
        );
    }

    @Override
    public String getDescription() {
        return "Demande à Nicobot de prendre une décision sur un choix important";
    }

    @Override
    public String getFormat() {
        return "!decide choix 1, [choix2]... [ou choix 3]";
    }

    @Override
    protected void doCommand(String command, Collection<String> arguments, MessageEvent triggeringMessage) {
        if (arguments.size() < 2) {
            if(current().nextInt(2) == 1) {
                nicobot.sendMessage(triggeringMessage, "Oui !");
            } else {
                nicobot.sendMessage(triggeringMessage, "Non !");
            }
        } else {
            int index = current().nextInt(arguments.size());
            boolean none = current().nextInt(100) == 50;

            if(none) {
                nicobot.sendMessage(triggeringMessage, "Aucun des %s !".formatted(arguments.size()));
            } else {
                nicobot.sendMessage(triggeringMessage,  "%s !".formatted(new ArrayList<>(arguments).get(index)));
            }

        }
    }
}
