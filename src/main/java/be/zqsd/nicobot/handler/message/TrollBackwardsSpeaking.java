package be.zqsd.nicobot.handler.message;

import be.zqsd.nicobot.bot.Nicobot;
import com.slack.api.model.event.MessageEvent;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import static org.slf4j.LoggerFactory.getLogger;

@ApplicationScoped
public class TrollBackwardsSpeaking extends ConditionalMessage {
    private static final Logger LOG = getLogger(TrollBackwardsSpeaking.class);
    private static final String EMOJI_TROLL = "troll";

    private final Nicobot nicobot;

    @Inject
    TrollBackwardsSpeaking(Nicobot nicobot) {
        this.nicobot = nicobot;
    }

    @Override
    boolean conditionMet(MessageEvent event) {
        var message = event.getText();
        var reversed = new StringBuilder(message).reverse().toString();
        return message.length() > 1 && message.equals(reversed);
    }

    @Override
    int chance() {
        return 15;
    }

    @Override
    public void handleConditionalMessage(MessageEvent event) {
        LOG.debug("Handler TrollBackwardsSpeaking triggered");
        var postedMessage = nicobot.sendMessage(event, event.getText());
        postedMessage.ifPresent(posted -> nicobot.addReactionToMessage(posted.getChannel(), posted.getTs(), EMOJI_TROLL));
    }
}
