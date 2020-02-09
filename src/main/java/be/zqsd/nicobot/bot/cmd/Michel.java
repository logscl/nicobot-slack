package be.zqsd.nicobot.bot.cmd;

import be.zqsd.nicobot.bot.NicoBot;
import be.zqsd.nicobot.bot.utils.Option;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Logs on 06-01-16.
 */
@Service
public class Michel extends NiCommand {

    private static final String COMMAND = "!michel";
    private static final String FORMAT = "!michel";
    private static final String DESC = "bisous michel ok";

    private static List<String> FRAGMENTS = Arrays.asList("ok", "michel", "bisous", "merci", "michel smits", "bise", "appel moi", "gsm", "belle claire", "claire", "j'aime ca", "sva", "sva toi", "grand fére", "parles gsm", "plus belle toi", "talme", "la plus belle", "ma grande soeur", "bise a elle", "toi", "joile pied");

    @Autowired
    private NicoBot nicobot;

    @Override
    public String getCommandName() {
        return COMMAND;
    }

    @Override
    public String getDescription() {
        return DESC;
    }

    @Override
    public String getFormat() {
        return FORMAT;
    }

    @Override
    protected void doCommand(String command, String[] args, Option opts) {
        int wordCount = RandomUtils.nextInt(2, 8);

        StringBuilder phrase = new StringBuilder();

        List<String> possibleWords = new ArrayList<>(FRAGMENTS);

        for (int i = 0; i < wordCount; i++) {
            int index = RandomUtils.nextInt(0, possibleWords.size());
            String word = possibleWords.get(index);
            phrase.append(word).append(" ");
            possibleWords.remove(index);
        }

        if(RandomUtils.nextInt(0,2) == 1) {
            phrase.append("ok");
        }

        nicobot.sendMessage(opts.message, phrase.toString());
    }
}
