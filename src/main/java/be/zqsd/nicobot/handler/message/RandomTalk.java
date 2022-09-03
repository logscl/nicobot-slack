package be.zqsd.nicobot.handler.message;

import be.zqsd.nicobot.bot.Nicobot;
import com.slack.api.model.event.MessageEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

import static java.util.List.of;
import static java.util.concurrent.ThreadLocalRandom.current;

@ApplicationScoped
public class RandomTalk extends ConditionalMessage {

    private final Nicobot nicobot;

    // TODO this will come from an API call
    private static final List<String> RANDOM_SPEECHES = of(
            "Riverside motherfoker",
            "On m'a volé mon vélooooo !!! Qui m'a volé mon vélooooo ???",
            "TOPSIDE COMIC TROIS CENT QUATRE VING QUATORZE",
            "Ouais BIATCH !",
            "En somme.",
            "ONE THIRTY TWO ONE THIRTY TWO.... REPONDEZ ONE THIRTY TWO !!! Papaaaaaaaaa~",
            "C'est dur, mais c'est juste.",
            "Chépatsé...",
            "Staaaannnndard de merde olé oléééééé",
            "WOUUUH WOUUUUUUHHH WOUUUUUUUUUUHHH WOUUUUUUUUUUUUUHHHHH",
            "T'es qui ?",
            "C'est une anecdote de MALADE ça !",
            "J'connais un mec en Guadeloupe...",
            "Tu pinailles un peu quand même !",
            "J'suis chaud ! J'suis chaud chaud chaud !!",
            "Un bijou !",
            "A méditer...",
            "La vie ne vaut rien, mais rien de vaut la vie.",
            "Ké Nouvelles ?!",
            "T'es pas le pingouin qui glisse le plus loin."
    );

    @Inject
    RandomTalk(Nicobot nicobot) {
        this.nicobot = nicobot;
    }

    @Override
    boolean conditionMet(MessageEvent event) {
        return true;
    }

    @Override
    int chance() {
        return 3;
    }

    @Override
    public void handleConditionalMessage(MessageEvent event) {
        nicobot.sendMessage(event, getRandomSpeech());
    }

    private String getRandomSpeech() {
        return RANDOM_SPEECHES.get(current().nextInt(RANDOM_SPEECHES.size()));
    }
}
