package com.st.nicobot.bot.handler;

import com.st.nicobot.services.SpeechGenerator;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SaveSentences extends AbstractMessageEvent {

    @Autowired
    private SpeechGenerator speechGenerator;

    @Override
    public void onMessage(SlackMessagePosted message) {
        speechGenerator.addSentence(message.getMessageContent());
    }
}
