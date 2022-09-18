package be.zqsd.nicobot.job;

import be.zqsd.nicobot.bot.ChannelService;
import be.zqsd.nicobot.bot.Nicobot;
import be.zqsd.nicobot.bot.UserService;
import be.zqsd.nicobot.leet.LeetService;
import be.zqsd.nicobot.persistence.Persistence;
import io.quarkus.scheduler.Scheduled;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

@ApplicationScoped
public class LeetGreetingJob {
    private static final Logger LOG = getLogger(LeetGreetingJob.class);

    private final Nicobot nicobot;
    private final LeetService leetService;
    private final ChannelService channelService;
    private final Persistence persistence;
    private final UserService userService;

    @Inject
    public LeetGreetingJob(Nicobot nicobot,
                           LeetService leetService,
                           ChannelService channelService,
                           Persistence persistence,
                           UserService userService) {
        this.nicobot = nicobot;
        this.leetService = leetService;
        this.channelService = channelService;
        this.persistence = persistence;
        this.userService = userService;
    }

    @Scheduled(cron="59 37 13 * * ? *")
    public void sendGreetings() {
        channelService.getFeaturedChannelId()
                .ifPresent(channelId -> nicobot.sendMessage(channelId, null, "!!§!!§§!!§ Happy Geek Time !!§!!§§!!§"));
    }

    @Scheduled(cron="01 38 13 * * ? *")
    public void congratulateGreeters() {
        channelService.getFeaturedChannelId()
                .ifPresent(channelId -> {
                    nicobot.sendMessage(channelId, null, buildCongratulationMessage(leetService.getGreeters()));
                    leetService.persistGreeters();
                    // top of the week
                    sendTopWeekMessage(channelId);
                });
    }

    private String buildCongratulationMessage(List<String> usernames) {
        if (usernames.isEmpty()) {
            return "Vous me souhaitez pas un Happy Geek Time à moi ? Ingrats ! :(";
        } else if (usernames.size() == 1) {
            return "Bravo %s ! Au moins toi tu y as pensé <3 !".formatted(usernames.get(0));
        } else {
            return "Félicitations à %s ! Propre sur vous !".formatted(String.join(", ", usernames));
        }
    }

    private void sendTopWeekMessage(String channelId) {
        // Le top de la semaine:
        try {
            var users = persistence.getWeeklyHgtScores(channelId);
            if (users.isEmpty()) {
                nicobot.sendMessage(channelId, null, "Personne ! Bande de clinches ! :(");
            } else {
                var weeklyGreeters = users
                        .stream()
                        .map(score -> {
                            var name = userService.userNameWithoutHighlight(score.getUserId()).orElse("??");
                            return "%s (%s)".formatted(name, score.getScore());
                        })
                        .collect(Collectors.joining(", "));
                var message = "Le top de la semaine: %s".formatted(weeklyGreeters);
                nicobot.sendMessage(channelId, null, message);
            }
        } catch (IOException e) {
            LOG.error("Unable to call the persistence service", e);
        }
    }
}
