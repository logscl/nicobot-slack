package be.zqsd.nicobot.handler.command;

import be.zqsd.nicobot.bot.Nicobot;
import com.slack.api.model.event.MessageEvent;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Collection;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;

@ApplicationScoped
public class Help implements NiCommand {


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
        return "Retourne la liste des commandes disponibles, ou une aide détaillée pour la commande passée en paramètre.";
    }

    @Override
    public String getFormat() {
        return "!help [commandName]";
    }

    @Override
    public void doCommand(String command, Collection<String> arguments, MessageEvent triggeringMessage) {
        if (arguments.isEmpty()) {
            sendAllCommands(triggeringMessage);
        } else {
            sendCommandDescription(triggeringMessage, arguments.stream().findFirst().orElse(""));
        }
    }

    private void sendCommandDescription(MessageEvent triggeringMessage, String commandName) {
        var command = commandManager.findCommandByName(commandName);
        command.map(foundCommand -> foundCommand.getDescription() + "\n" + foundCommand.getFormat())
                .ifPresentOrElse(message ->
                    nicobot.sendEphemeralMessage(triggeringMessage, message)
                , () -> nicobot.sendEphemeralMessage(triggeringMessage, "Commande '" + commandName+ "' inconnue :("));
    }

    private void sendAllCommands(MessageEvent triggeringMessage) {
        var allCommands = commandManager.getCommands().stream()
                .map(command -> {
                    var commandName = command.getCommandNames().stream().findFirst().orElse("");
                    return commandName + " : " + command.getDescription();
                }).collect(joining("\n", "Voici les commandes disponibles: \n", ""));

        nicobot.sendEphemeralMessage(triggeringMessage, allCommands);
    }
}
