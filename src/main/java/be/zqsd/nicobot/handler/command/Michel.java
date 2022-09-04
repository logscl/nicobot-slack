package be.zqsd.nicobot.handler.command;

import be.zqsd.nicobot.bot.Nicobot;
import com.slack.api.model.event.MessageEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static java.util.concurrent.ThreadLocalRandom.current;

@ApplicationScoped
public class Michel implements NiCommand {

    private final Nicobot nicobot;

    private static final List<String> FRAGMENTS = List.of("ok", "michel", "bisous", "merci", "michel smits", "bise", "appel moi", "gsm", "belle claire", "claire", "j'aime ca", "sva", "sva toi", "grand fére", "parles gsm", "plus belle toi", "talme", "la plus belle", "ma grande soeur", "bise a elle", "toi", "joile pied");

    @Inject
    public Michel(Nicobot nicobot) {
        this.nicobot = nicobot;
    }


    @Override
    public Collection<String> getCommandNames() {
        return singletonList("!michel");
    }

    @Override
    public String getDescription() {
        return "bisous michel ok";
    }

    @Override
    public String getFormat() {
        return "!michel";
    }

    @Override
    public void doCommand(String command, Collection<String> arguments, MessageEvent triggeringMessage) {
        var endOfSentence = current().nextBoolean() ? " ok" : "";
        var sentence = current().ints(0, FRAGMENTS.size())
                .distinct()
                .limit(current().nextInt(2, 8))
                .map(idx -> current().nextInt(FRAGMENTS.size()))
                .mapToObj(FRAGMENTS::get)
                .collect(Collectors.joining(" ", "", endOfSentence));

        nicobot.sendMessage(triggeringMessage, sentence);
    }
}
