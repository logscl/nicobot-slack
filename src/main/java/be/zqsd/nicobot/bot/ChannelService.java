package be.zqsd.nicobot.bot;

import be.zqsd.slack.client.WebClient;
import com.slack.api.model.Conversation;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@ApplicationScoped
public class ChannelService {

    private static final String HIGHLIGHT_CHANNEL = "<#%s>";
    private final WebClient client;
    private Map<String, Conversation> channelsPerId;

    @Inject
    ChannelService(WebClient client) {
        this.client = client;
        refreshChannels();
    }

    public void refreshChannels() {
        channelsPerId = client.fetchChannels()
                .stream()
                .collect(toMap(Conversation::getId, identity()));
    }

    public Optional<String> findChannelName(String channelId) {
        return ofNullable(channelsPerId.get(channelId))
                .map(Conversation::getNameNormalized);
    }

    public Optional<String> findChannelId(String channelName) {
        return channelsPerId.values()
                .stream()
                .filter(channel -> channelName.equals(channel.getNameNormalized()))
                .findFirst()
                .map(Conversation::getId);
    }

    public String getChannelLink(String channelId) {
        return String.format(HIGHLIGHT_CHANNEL, channelId);
    }
}
