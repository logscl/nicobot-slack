package be.zqsd.slack.dispatcher;

import be.zqsd.nicobot.handler.command.CommandService;
import be.zqsd.nicobot.handler.message.MessageHandler;
import com.slack.api.app_backend.events.payload.EventsApiPayload;
import com.slack.api.bolt.context.builtin.EventContext;
import com.slack.api.bolt.handler.BoltEventHandler;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.event.MessageEvent;
import io.quarkus.arc.All;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

@ApplicationScoped
public class MessageEventDispatcher implements BoltEventHandler<MessageEvent> {

    private static final Logger LOG = getLogger(MessageEventDispatcher.class);

    private final Collection<MessageHandler> messageHandlers;
    private final CommandService commandService;

    @Inject
    public MessageEventDispatcher(@All List<MessageHandler> messageHandlers,
                                  CommandService commandService) {
        this.messageHandlers = messageHandlers;
        this.commandService = commandService;
    }

    @Override
    public Response apply(EventsApiPayload<MessageEvent> event, EventContext context) throws IOException, SlackApiException {
        var messageEvent = event.getEvent();
        commandService.findCommandFor(messageEvent.getText())
                .ifPresentOrElse(command -> commandService.handle(command, messageEvent), () -> {
                    LOG.debug("checking if Nicobot must react to {} message events", messageHandlers.size());
                    messageHandlers.forEach(action -> action.handle(messageEvent));
                });
        return context.ack();
    }
}
