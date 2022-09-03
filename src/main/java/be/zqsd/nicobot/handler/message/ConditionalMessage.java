package be.zqsd.nicobot.handler.message;

import com.slack.api.model.event.MessageEvent;

import static java.util.concurrent.ThreadLocalRandom.current;

public abstract class ConditionalMessage implements MessageHandler {

    /**
     * For any incoming message (channel, pm, group), check if this message can trigger a response from Nicobot.
     * @param event
     * @return <code>true</code> if the message should (but not necessary will) trigger a response
     */
    abstract boolean conditionMet(MessageEvent event);

    /**
     * The activation rate (in percent) to trigger this response.
     * @return
     */
    abstract int chance();

    abstract void handleConditionalMessage(MessageEvent event);

    @Override
    public void handle(MessageEvent event) {
        if (shouldTrigger(event)) {
            handleConditionalMessage(event);
        }
    }

    private boolean shouldTrigger(MessageEvent event) {
        if (conditionMet(event)) {
            var chance = current().nextInt(100);
            return chance < chance();
        }
        return false;
    }
}
