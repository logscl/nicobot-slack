package be.zqsd.thirdparty;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.Collection;

import static java.util.Collections.emptyList;
import static java.util.List.of;
import static org.slf4j.LoggerFactory.getLogger;

@ApplicationScoped
public class YoutubeService {
    private static final Logger LOG = getLogger(YoutubeService.class);
    private static final String YOUTUBE_BASE_URL = "https://youtu.be/";
    private final String apiKey;
    private final YouTube youtube;

    @Inject
    public YoutubeService(@ConfigProperty(name = "search.api.key") String apiKey) {
        this.apiKey = apiKey;
        this.youtube = new YouTube.Builder(new NetHttpTransport(), new GsonFactory(), httpRequest -> {})
                .setApplicationName("youtube-search")
                .build();
    }

    public Collection<String> find(String query) {
        try {
            var search = prepareQuery(query);
            return search.execute().getItems()
                    .stream()
                    .map(video -> YOUTUBE_BASE_URL + video.getId().getVideoId())
                    .toList();
        } catch (IOException e) {
            LOG.error("Unable to make a Youtube search", e);
            return emptyList();
        }
    }

    private YouTube.Search.List prepareQuery(String query) throws IOException {
        var search = youtube.search().list(of("id"));
        search.setKey(apiKey);
        search.setQ(query);
        search.setType(of("video"));
        search.setFields("items(id/videoId)");
        search.setMaxResults(10L);

        return search;
    }
}
