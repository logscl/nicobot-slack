package com.st.nicobot.bot.cmd;

import com.st.nicobot.bot.NicoBot;
import com.st.nicobot.bot.utils.Option;
import com.st.nicobot.services.SpeechGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RandomSpeech extends NiCommand {

    private static final String COMMAND = "!speech";
    private static final String FORMAT = "!speech";
    private static final String DESC = "Markov Generated Speech";

    @Autowired
    private NicoBot nicobot;

    @Autowired
    private SpeechGenerator speechGenerator;

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
        nicobot.sendMessage(opts.message, speechGenerator.generateRandomSpeech());
    }
}
