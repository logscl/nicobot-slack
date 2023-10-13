package be.zqsd.thirdparty;

import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.PlacesSearchResult;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.slf4j.LoggerFactory.getLogger;

@ApplicationScoped
public class MapService {

    private static final Logger LOG = getLogger(MapService.class);

    private final GeoApiContext maps;

    @Inject
    public MapService(@ConfigProperty(name = "search.api.key") String apiKey) {
        this.maps = new GeoApiContext.Builder()
                .apiKey(apiKey)
                .build();
    }

    public Collection<PlacesSearchResult> findPlaces(String query) {
        var search = PlacesApi.textSearchQuery(maps, query);
        try {
            return Arrays.stream(search.await().results).toList();
        } catch (Exception e) {
            LOG.error("Unable to search for places", e);
            return emptyList();
        }
    }

    public Optional<PlaceDetails> getPlaceDetails(PlacesSearchResult result) {
        try {
            return of(PlacesApi.placeDetails(maps, result.placeId).await());
        } catch (Exception e) {
            LOG.error("Unable to get details for place id {}", result.placeId, e);
            return empty();
        }
    }
}
