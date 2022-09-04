package be.zqsd.thirdparty;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.customsearch.v1.CustomSearchAPI;
import com.google.api.services.customsearch.v1.model.Result;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.util.Collection;

import static java.util.Collections.emptyList;
import static org.slf4j.LoggerFactory.getLogger;

@ApplicationScoped
public class GoogleService {
    private static final Logger LOG = getLogger(GoogleService.class);

    private final String apiKey;
    private final String cxKey;
    private final CustomSearchAPI searchEngine;

    @Inject
    public GoogleService(@ConfigProperty(name = "search.api.key") String apiKey,
                         @ConfigProperty(name = "search.cx.key") String cxKey) {
        this.apiKey = apiKey;
        this.cxKey = cxKey;
        this.searchEngine = new CustomSearchAPI.Builder(new NetHttpTransport(), new GsonFactory(), httpRequest -> {
        }).setApplicationName("google-search").build();
    }

    public Collection<Result> webSearch(String query) {
        try {
            var search = prepareQuery(query);
            return search.execute().getItems();
        } catch (Exception e) {
            LOG.error("Unable to make a Google web search", e);
            return emptyList();
        }
    }

    public Collection<Result> imageSearch(String query) {
        try {
            var search = prepareQuery(query);
            search.setSearchType("image");
            return search.execute().getItems();
        } catch (Exception e) {
            LOG.error("Unable to make a Google image search", e);
            return emptyList();
        }
    }

    public Collection<Result> gifSearch(String query) {
        try {
            var search = prepareQuery(query);
            search.setSearchType("image");
            search.setFileType("gif");
            search.setHq("animated");
            return search.execute().getItems();
        } catch (Exception e) {
            LOG.error("Unable to make a Google gif search", e);
            return emptyList();
        }
    }

    private CustomSearchAPI.Cse.List prepareQuery(String query) throws IOException {
        var search = searchEngine.cse().list();

        search.setKey(apiKey);
        search.setCx(cxKey);
        search.setFields("items/link,items/displayLink");
        search.setQ(query);

        return search;
    }
}
