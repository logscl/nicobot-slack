package be.zqsd.nicobot.handler.command;

import be.zqsd.nicobot.bot.Nicobot;
import com.slack.api.model.event.MessageEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collection;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;

@ApplicationScoped
public class Help extends NiCommand {


    private final Nicobot nicobot;

    private final CommandService commandManager;

    @Inject
    public Help(Nicobot nicobot,
                CommandService commandManager) {
        this.nicobot = nicobot;
        this.commandManager = commandManager;
    }


    @Override
    public Collection<String> getCommandNames() {
        return singletonList("!help");
    }

    @Override
    public String getDescription() {
        return """
                Retourne la liste des commandes disponibles, ou
                une aide détaillée pour la commande passée en paramètre.
                """;
    }

    @Override
    public String getFormat() {
        return "!help [commandName]";
    }

    @Override
    protected void doCommand(String command, Collection<String> arguments, MessageEvent triggeringMessage) {
        if (arguments.isEmpty()) {
            sendAllCommands(triggeringMessage.getUser());
        } else {
            sendCommandDescription(triggeringMessage.getUser(), arguments.stream().findFirst().orElse(""));
        }
    }

    private void sendCommandDescription(String userId, String commandName) {
        var command = commandManager.findCommandByName(commandName);
        command.map(foundCommand -> foundCommand.getDescription() + "\n" + foundCommand.getFormat())
                .ifPresentOrElse(message ->
                    nicobot.sendPrivateMessage(userId, message)
                , () -> nicobot.sendPrivateMessage(userId, "Commande '" + commandName+ "' inconnue :("));
    }

    private void sendAllCommands(String userId) {
        var allCommands = commandManager.getCommands().stream()
                .map(command -> {
                    var commandName = command.getCommandNames().stream().findFirst().orElse("");
                    return commandName + " : " + command.getDescription();
                }).collect(joining("\n", "Voici les commandes disponibles: \n", ""));

        nicobot.sendPrivateMessage(userId, allCommands);
    }
}
