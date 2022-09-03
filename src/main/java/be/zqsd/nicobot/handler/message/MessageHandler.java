package be.zqsd.nicobot.handler.message;

import com.slack.api.model.event.MessageEvent;

public interface MessageHandler {

    void handle(MessageEvent event);
}
