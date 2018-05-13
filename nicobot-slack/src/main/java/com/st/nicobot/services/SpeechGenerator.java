package com.st.nicobot.services;

public interface SpeechGenerator {

    void addSentence(String text);

    String generateRandomSpeech();
}
