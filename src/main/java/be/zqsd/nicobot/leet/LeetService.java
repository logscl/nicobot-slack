package be.zqsd.nicobot.leet;

import be.zqsd.nicobot.bot.ChannelService;
import be.zqsd.nicobot.bot.UserService;
import be.zqsd.nicobot.persistence.Persistence;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.time.LocalDate.now;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.slf4j.LoggerFactory.getLogger;

@ApplicationScoped
public class LeetService {
    private static final Logger LOG = getLogger(LeetService.class);

    private final Persistence persistence;
    private final ChannelService channelService;
    private final UserService userService;

    private final Set<String> greeterIds = new LinkedHashSet<>();

    public LeetService(Persistence persistence,
                       ChannelService channelService,
                       UserService userService) {
        this.persistence = persistence;
        this.channelService = channelService;
        this.userService = userService;
    }

    public void addGreeter(String greeterId) {
        greeterIds.add(greeterId);
    }

    public void persistGreeters() {
        var channel = channelService.getFeaturedChannelId();
        if (channel.isPresent()) {
            try {
                persistence.addHgtScores(channel.get(), greeterIds.stream().toList());
            } catch (IOException e) {
                LOG.error("Unable to persist scores !", e);
            }
        }
        greeterIds.clear();
    }

    public List<String> getGreeters() {
        return greeterIds.stream()
                .map(userService::userNameWithoutHighlight)
                .flatMap(Optional::stream)
                .toList();
    }

    public Optional<String> buildTopWeekMessage(String channelId) {
        try {
            var users = persistence.getWeeklyHgtScores(channelId);
            if (users.isEmpty()) {
                return of("Personne ! Bande de clinches ! :(");
            } else {
                var weeklyGreeters = users
                        .stream()
                        .map(score -> {
                            var name = userService.userNameWithoutHighlight(score.getUserId()).orElse("??");
                            return "%s (%s)".formatted(name, score.getScore());
                        })
                        .collect(Collectors.joining(", "));
                return of("Le top de la semaine: %s".formatted(weeklyGreeters));
            }
        } catch (IOException e) {
            LOG.error("Unable to call the persistence service", e);
        }
        return empty();
    }

    public Optional<String> buildTopYearMessage(String channelId) {
        try {
            var users = persistence.getYearlyHgtScores(channelId);
            if (users.isEmpty()) {
                return of("Personne ! Bande de clinches ! :(");
            } else {
                var scores = users.stream()
                        .map(score -> {
                            var name = userService.userNameWithoutHighlight(score.getUserId()).orElse("??");
                            return "%s (%s)".formatted(name, score.getScore());
                        })
                        .collect(Collectors.joining(", "));

                return of("Les meilleurs en %s (%s jours cette année): %s".formatted(now().getYear(), now().getDayOfYear(), scores));
            }
        } catch (IOException e) {
            LOG.error("Unable to call the persistence service", e);
        }
        return empty();
    }
}
