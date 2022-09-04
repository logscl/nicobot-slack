package be.zqsd.nicobot.handler.command;

import be.zqsd.nicobot.bot.Nicobot;
import be.zqsd.thirdparty.YoutubeService;
import com.slack.api.model.event.MessageEvent;
import io.quarkus.cache.CacheResult;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collection;

import static java.util.Collections.singletonList;
import static org.slf4j.LoggerFactory.getLogger;

@ApplicationScoped
public class YoutubeSearch implements NiCommand {

    private static final Logger LOG = getLogger(YoutubeSearch.class);
    private static final String CACHE_NAME = "youtube-videos";

    private static String NEXT_ARGUMENT = "next";

    private final Nicobot nicobot;
    private final YoutubeService youtube;

    // for a lack of a better solution
    private String lastQuery = "";
    private int videoIndex = 0;

    @Inject
    public YoutubeSearch(Nicobot nicobot,
                         YoutubeService youtube) {
        this.nicobot = nicobot;
        this.youtube = youtube;
    }

    @Override
    public Collection<String> getCommandNames() {
        return singletonList("!yt");
    }

    @Override
    public String getDescription() {
        return "Recherche une vidéo sur YouTube";
    }

    @Override
    public String getFormat() {
        return "!yt query";
    }

    @Override
    public void doCommand(String command, Collection<String> arguments, MessageEvent triggeringMessage) {
        var query = String.join("+", arguments);

        if (NEXT_ARGUMENT.equals(query)) {
            videoIndex++;
        } else {
            lastQuery = query;
            videoIndex = 0;
        }
        var videos = searchResult(lastQuery);
        videos.stream()
                .skip(videoIndex)
                .findFirst()
                .ifPresentOrElse(video -> nicobot.sendMessage(triggeringMessage, "<%s>".formatted(video)),
                        () -> nicobot.sendMessage(triggeringMessage, "J'ai rien trouvé :("));
    }

    @CacheResult(cacheName = CACHE_NAME)
    protected Collection<String> searchResult(String query) {
        return youtube.find(query);
    }
}
