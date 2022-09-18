package be.zqsd.nicobot.bot;

import be.zqsd.slack.client.WebClient;
import com.slack.api.model.Conversation;
import org.eclipse.microprofile.config.inject.ConfigProperty;

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
    private final String featuredChannelId;
    private Map<String, Conversation> channelsPerId;

    @Inject
    ChannelService(WebClient client,
                   @ConfigProperty(name = "nicobot.featured.channel") String featuredChannelName) {
        this.client = client;
        refreshChannels();
        this.featuredChannelId = findChannelId(featuredChannelName).orElse(null);
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

    public boolean isFeaturedChannel(String channelId) {
        return getFeaturedChannelId().map(chanId -> chanId.equals(channelId)).orElse(false);
    }

    public Optional<String> getFeaturedChannelId() {
        return ofNullable(featuredChannelId);
    }
}
