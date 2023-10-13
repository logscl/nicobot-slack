package be.zqsd.nicobot.handler.command;

import be.zqsd.nicobot.bot.Nicobot;
import be.zqsd.nicobot.leet.LeetService;
import com.slack.api.model.event.MessageEvent;
import org.slf4j.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Collection;

import static java.util.Collections.singletonList;
import static org.slf4j.LoggerFactory.getLogger;

@ApplicationScoped
public class TopHgt implements NiCommand {
    private static final Logger LOG = getLogger(TopHgt.class);

    private final Nicobot nicobot;
    private final LeetService leetService;

    @Inject
    public TopHgt(Nicobot nicobot,
                  LeetService leetService) {
        this.nicobot = nicobot;
        this.leetService = leetService;
    }

    @Override
    public Collection<String> getCommandNames() {
        return singletonList("!hgt");
    }

    @Override
    public String getDescription() {
        return "Donne le top score au HGT";
    }

    @Override
    public String getFormat() {
        return "!hgt";
    }

    @Override
    public void doCommand(String command, Collection<String> arguments, MessageEvent triggeringMessage) {
        leetService.buildTopYearMessage(triggeringMessage.getChannel()).ifPresent(message -> nicobot.sendMessage(triggeringMessage, message));
    }
}
