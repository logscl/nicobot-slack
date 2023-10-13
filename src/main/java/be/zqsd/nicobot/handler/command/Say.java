package be.zqsd.nicobot.handler.command;

import be.zqsd.nicobot.bot.ChannelService;
import be.zqsd.nicobot.bot.Nicobot;
import be.zqsd.nicobot.bot.UserService;
import com.slack.api.model.event.MessageEvent;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Collection;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;

@ApplicationScoped
public class Say implements NiCommand {

    private final Nicobot nicobot;
    private final UserService userService;
    private final ChannelService channelService;

    @Inject
    public Say(Nicobot nicobot,
               UserService userService,
               ChannelService channelService) {
        this.nicobot = nicobot;
        this.userService = userService;
        this.channelService = channelService;
    }

    @Override
    public Collection<String> getCommandNames() {
        return singletonList("!say");
    }

    @Override
    public String getDescription() {
        return "Fait parler le bot. ADMIN ONLY :D";
    }

    @Override
    public String getFormat() {
        return "!say <channel> \"<message>\"";
    }

    @Override
    public void doCommand(String command, Collection<String> arguments, MessageEvent triggeringMessage) {
        if (userService.isAdmin(triggeringMessage.getUser())) {
            var channelId = arguments
                    .stream()
                    .findFirst()
                    .flatMap(channelService::findChannelId);
            var message = arguments
                    .stream()
                    .skip(1)
                    .collect(Collectors.joining(" "));

            if (channelId.isPresent() && !message.isBlank()) {
                nicobot.sendMessage(channelId.get(), null, message);
            } else {
                nicobot.sendEphemeralMessage(triggeringMessage, "Channel non trouvé ou message vide");
            }
        } else {
            nicobot.sendEphemeralMessage(triggeringMessage, "Nope, admin only !");
        }
    }
}
