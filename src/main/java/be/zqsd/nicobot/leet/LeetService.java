package be.zqsd.nicobot.leet;

import be.zqsd.nicobot.bot.ChannelService;
import be.zqsd.nicobot.bot.UserService;
import be.zqsd.nicobot.persistence.Persistence;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.emptyList;
import static org.slf4j.LoggerFactory.getLogger;

@ApplicationScoped
public class LeetService {
    private static final Logger LOG = getLogger(LeetService.class);

    private final Persistence persistence;
    private final ChannelService channelService;
    private final UserService userService;

    private Set<String> greeterIds;

    public LeetService(Persistence persistence,
                       ChannelService channelService,
                       UserService userService) {
        this.persistence = persistence;
        this.channelService = channelService;
        this.userService = userService;
    }

    public void addGreeter(String greeterId) {
        greeterIds.add(greeterId);
    }

    public void persistGreeters() {
        var channel = channelService.getFeaturedChannelId();
        if (channel.isPresent()) {
            try {
                persistence.addHgtScores(channel.get(), greeterIds.stream().toList());
            } catch (IOException e) {
                LOG.error("Unable to persist scores !", e);
            }
        }
        greeterIds.clear();
    }

    public List<String> getGreeters() {
        return greeterIds.stream()
                .map(userService::userNameWithoutHighlight)
                .flatMap(Optional::stream)
                .toList();
    }
}
