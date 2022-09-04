package be.zqsd.nicobot.handler.command;

import be.zqsd.nicobot.bot.Nicobot;
import be.zqsd.thirdparty.GoogleService;
import com.google.api.services.customsearch.v1.model.Result;
import io.quarkus.cache.CacheResult;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.List.of;

@ApplicationScoped
public class ImageSearch extends GoogleSearch {

    private final Nicobot nicobot;

    private final GoogleService google;

    private static final String CACHE_NAME = "image-search";

    @Inject
    public ImageSearch(Nicobot nicobot,
                       GoogleService google) {
        this.nicobot = nicobot;
        this.google = google;
    }

    @Override
    public Collection<String> getCommandNames() {
        return of("!img", "!sexy", "!brazzer", "!brazzers");
    }

    @Override
    public String getDescription() {
        return "Recherche une image sur les internets et retourne le premier résultat. !img next pour le résultat suivant";
    }

    @Override
    public String getFormat() {
        return "!img query";
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
