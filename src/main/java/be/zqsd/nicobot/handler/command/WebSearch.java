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
public class WebSearch extends GoogleSearch {

    private final Nicobot nicobot;

    private final GoogleService google;

    private static final String CACHE_NAME = "web-search";

    @Inject
    public WebSearch(Nicobot nicobot,
                     GoogleService google) {
        this.nicobot = nicobot;
        this.google = google;
    }

    @Override
    public Collection<String> getCommandNames() {
        return singletonList("!search");
    }

    @Override
    public String getDescription() {
        return "Recherche un lien sur les internets et retourne le premier résultat. !search next pour le résultat suivant";
    }

    @Override
    public String getFormat() {
        return "!search query";
    }

    @CacheResult(cacheName = CACHE_NAME)
    protected Collection<Result> searchResult(String query) {
        return google.webSearch(query);
    }

    @Override
    protected Nicobot getBot() {
        return nicobot;
    }
}
