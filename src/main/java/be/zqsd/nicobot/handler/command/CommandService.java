package be.zqsd.nicobot.handler.command;

import com.slack.api.model.event.MessageEvent;
import io.quarkus.arc.All;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.*;
import java.util.regex.Pattern;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

@ApplicationScoped
public class CommandService {

    /** split a string into words (or phrases between quotes) */
    private static final Pattern STRING_INTO_ARGUMENTS_REGEX = Pattern.compile("[\"“]([^\"“”]*)[\"”]|[^ ]+");

    private final Collection<NiCommand> commands;
    private final Map<String, NiCommand> commandsPerName;

    @Inject
    public CommandService(@All List<NiCommand> commands) {
        this.commands = commands;
        var perName = new HashMap<String, NiCommand>();
        commands.forEach(command -> command.getCommandNames().forEach(name -> perName.put(name, command)));
        this.commandsPerName = perName;
    }

    public Optional<NiCommand> findCommandFor(String message) {
        var argument = toArguments(message)
                .stream()
                .findFirst();
        if (argument.isPresent()) {
            return findCommandByName(argument.get());
        } else {
            return empty();
        }
    }

    public void handle(NiCommand command, MessageEvent trigger) {
        var commandArguments = toArguments(trigger.getText());
        command.handle(commandArguments, trigger);
    }

    public Optional<NiCommand> findCommandByName(String commandName) {
        return ofNullable(commandsPerName.get(commandName));
    }

    public Collection<NiCommand> getCommands() {
        return commands;
    }

    private List<String> toArguments(String message) {
        var matcher = STRING_INTO_ARGUMENTS_REGEX.matcher(message);
        return matcher.results()
                .map(argument -> matcher.group(1) != null ? matcher.group(1) : matcher.group())
                .toList();
    }
}
