package com.st.nicobot.internal.services;

import com.st.nicobot.services.SpeechGenerator;
import org.springframework.stereotype.Service;
import rita.RiMarkov;

import javax.annotation.PostConstruct;

@Service
public class SpeechGeneratorImpl implements SpeechGenerator {

    private RiMarkov markov;

    @PostConstruct
    private void postConstruct() {
        markov = new RiMarkov(3);
    }

    @Override
    public void addSentence(String text) {
        markov = markov.loadText(text);
    }

    @Override
    public String generateRandomSpeech() {
        return markov.generateSentence();
    }
}
