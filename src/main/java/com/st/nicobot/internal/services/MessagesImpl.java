package com.st.nicobot.internal.services;

import com.st.nicobot.services.Messages;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Logs on 08-03-15.
 */
@Service
public class MessagesImpl implements Messages {

    /**
     * Les réactions aléatoires de nicobot
     */
    private List<String> randomSpeeches;

    @PostConstruct
    private void initRandomSpeeches() {
        randomSpeeches = new ArrayList<String>();
        randomSpeeches.add("Riverside motherfoker");
        randomSpeeches.add("On m'a volé mon vélooooo !!! Qui m'a volé mon vélooooo ???");
        randomSpeeches.add("TOPSIDE COMIC TROIS CENT QUATRE VING QUATORZE");
        randomSpeeches.add("Ouais BIATCH !");
        randomSpeeches.add("En somme.");
        randomSpeeches.add("ONE THIRTY TWO ONE THIRTY TWO.... REPONDEZ ONE THIRTY TWO !!! Papaaaaaaaaa~");
        randomSpeeches.add("C'est dur, mais c'est juste.");
        randomSpeeches.add("Chépatsé...");
        randomSpeeches.add("Staaaannnndard de merde olé oléééééé");
        randomSpeeches.add("WOUUUH WOUUUUUUHHH WOUUUUUUUUUUHHH WOUUUUUUUUUUUUUHHHHH");
        randomSpeeches.add("T'es qui ?");
        randomSpeeches.add("C'est une anecdote de MALADE ça !");
    }

    @Override
    public String getRandomSpeech() {
        return randomSpeeches.get(RandomUtils.nextInt(0, randomSpeeches.size()));
    }
}
