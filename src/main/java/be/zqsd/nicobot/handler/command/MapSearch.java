package be.zqsd.nicobot.handler.command;

import be.zqsd.nicobot.bot.Nicobot;
import be.zqsd.thirdparty.MapService;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.PlacesSearchResult;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.SectionBlock;
import com.slack.api.model.block.composition.MarkdownTextObject;
import com.slack.api.model.block.element.ImageElement;
import com.slack.api.model.event.MessageEvent;
import io.quarkus.cache.CacheResult;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.List.of;

@ApplicationScoped
public class MapSearch implements NiCommand {

    private static final String CACHE_NAME = "map-places";
    private static final String NEXT_ARGUMENT = "next";

    private final Nicobot nicobot;
    private final MapService maps;

    // for a lack of a better solution
    private String lastQuery = "";
    private int placesIndex = 0;

    @Inject
    public MapSearch(Nicobot nicobot,
                     MapService maps) {
        this.nicobot = nicobot;
        this.maps = maps;
    }

    @Override
    public Collection<String> getCommandNames() {
        return of("!map", "!maps");
    }

    @Override
    public String getDescription() {
        return "Recherche un emplacement sur Google Maps";
    }

    @Override
    public String getFormat() {
        return "!map query";
    }

    @Override
    public void doCommand(String command, Collection<String> arguments, MessageEvent triggeringMessage) {
        var query = String.join("+", arguments);

        if (NEXT_ARGUMENT.equals(query)) {
            placesIndex++;
        } else {
            lastQuery = query;
            placesIndex = 0;
        }

        var places = searchResult(lastQuery);
        places.stream()
                .skip(placesIndex)
                .findFirst()
                .map(this::createMessage)
                .ifPresentOrElse(blocks -> this.nicobot.sendBlocks(triggeringMessage, blocks, "Map search"),
                        () -> nicobot.sendMessage(triggeringMessage, "J'ai rien trouvé :("));
    }

    private List<LayoutBlock> createMessage(PlacesSearchResult place) {
        return maps.getPlaceDetails(place)
                .map(details -> createFromPlaceAndDetails(place, details))
                .map(Collections::singletonList)
                .orElse(emptyList());
    }

    private LayoutBlock createFromPlaceAndDetails(PlacesSearchResult place, PlaceDetails details) {
        return SectionBlock.builder()
                .text(MarkdownTextObject.builder().text("<%s|%s>%n%s".formatted(details.url.toExternalForm(), place.name, place.formattedAddress)).build())
                .accessory(ImageElement.builder().imageUrl(place.icon.toExternalForm()).altText("Place Icon").build())
                .build();
    }

    @CacheResult(cacheName = CACHE_NAME)
    protected Collection<PlacesSearchResult> searchResult(String query) {
        return maps.findPlaces(query);
    }
}
