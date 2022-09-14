package be.zqsd.nicobot.handler.command;

import be.zqsd.nicobot.bot.Nicobot;
import be.zqsd.thirdparty.GoogleService;
import com.google.api.services.customsearch.v1.model.Result;
import io.quarkus.cache.CacheResult;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collection;

import static java.util.Collections.singletonList;

@ApplicationScoped
public class GifSearch extends GoogleSearch {

    private final Nicobot nicobot;

    private final GoogleService google;

    private static final String CACHE_NAME = "gif-search";

    @Inject
    public GifSearch(Nicobot nicobot,
                     GoogleService google) {
        this.nicobot = nicobot;
        this.google = google;
    }

    @Override
    public Collection<String> getCommandNames() {
        return singletonList("!gif");
    }

    @Override
    public String getDescription() {
        return "Recherche un gif sur les internets et retourne le premier résultat. !gif next pour le résultat suivant";
    }

    @Override
    public String getFormat() {
        return "!gif query";
    }

    @CacheResult(cacheName = CACHE_NAME)
    protected Collection<Result> searchResult(String query) {
        return google.imageSearch(query);
    }

    @Override
    protected Nicobot getBot() {
        return nicobot;
    }
}
