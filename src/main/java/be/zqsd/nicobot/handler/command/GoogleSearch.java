package be.zqsd.nicobot.handler.command;

import be.zqsd.nicobot.bot.Nicobot;
import com.google.api.services.customsearch.v1.model.Result;
import com.slack.api.model.event.MessageEvent;

import java.util.Collection;

public abstract class GoogleSearch implements NiCommand {

    private static final String NEXT_ARGUMENT = "next";

    // for a lack of a better solution
    private String lastQuery = "";
    private int searchIndex = 0;

    abstract Collection<Result> searchResult(String query);
    abstract Nicobot getBot();

    @Override
    public void doCommand(String command, Collection<String> arguments, MessageEvent triggeringMessage) {
        var query = buildSearchQuery(command, arguments);

        if (NEXT_ARGUMENT.equals(query)) {
            searchIndex++;
        } else {
            lastQuery = query;
            searchIndex = 0;
        }

        var results = searchResult(lastQuery);
        results.stream()
                .filter(search -> !search.getLink().contains("x-raw-image"))
                .skip(searchIndex)
                .findFirst()
                .map(this::createMessage)
                .ifPresentOrElse(message -> getBot().sendMessage(triggeringMessage, message),
                        () -> getBot().sendMessage(triggeringMessage, "J'ai rien trouvé :("));
    }

    private String buildSearchQuery(String command, Collection<String> arguments) {
        var query = String.join("+", arguments);

        if(shouldAppendCommandInQuery(command)) {
            return command.substring(1) + "+" + query;
        } else {
            return query;
        }
    }

    private boolean shouldAppendCommandInQuery(String command) {
        return getCommandNames().stream().findFirst().map(name -> !name.equals(command)).orElse(false);
    }

    private String createMessage(Result result) {
        return "<%s|%s (%s)>".formatted(
                result.getLink(),
                result.getDisplayLink(),
                lastQuery.replace("+", " "));
    }
}
