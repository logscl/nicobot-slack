package be.zqsd.nicobot.job;

import be.zqsd.nicobot.bot.ChannelService;
import be.zqsd.nicobot.bot.Nicobot;
import be.zqsd.nicobot.leet.LeetService;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.ScheduledExecution;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.ZoneId;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

@ApplicationScoped
public class LeetGreetingJob {
    private static final Logger LOG = getLogger(LeetGreetingJob.class);

    private final Nicobot nicobot;
    private final LeetService leetService;
    private final ChannelService channelService;

    @Inject
    public LeetGreetingJob(Nicobot nicobot,
                           LeetService leetService,
                           ChannelService channelService) {
        this.nicobot = nicobot;
        this.leetService = leetService;
        this.channelService = channelService;
    }

    @Scheduled(cron="59 37 * * * ? *", skipExecutionIf = ExecutionNotAtLeetHour.class)
    public void sendGreetings() {
        channelService.getFeaturedChannelId()
                .ifPresent(channelId -> nicobot.sendMessage(channelId, null, "!!§!!§§!!§ Happy Geek Time !!§!!§§!!§"));
    }

    @Scheduled(cron="01 38 * * * ? *", skipExecutionIf = ExecutionNotAtLeetHour.class)
    public void congratulateGreeters() {
        channelService.getFeaturedChannelId()
                .ifPresent(channelId -> {
                    nicobot.sendMessage(channelId, null, buildCongratulationMessage(leetService.getGreeters()));
                    leetService.persistGreeters();
                    // top of the week
                    leetService.buildTopWeekMessage(channelId).ifPresent(message -> nicobot.sendMessage(channelId, null, message));
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

    @ApplicationScoped
    static class ExecutionNotAtLeetHour implements Scheduled.SkipPredicate {
        private static final Logger LOG = getLogger(ExecutionNotAtLeetHour.class);

        private final ZoneId timezone;

        @Inject
        public ExecutionNotAtLeetHour(@ConfigProperty(name = "nicobot.timezone.name") String timezoneName) {
            this.timezone = ZoneId.of(timezoneName);
        }

        @Override
        public boolean test(ScheduledExecution execution) {
            var currentHour = execution.getFireTime().atZone(timezone).getHour();
            LOG.debug("Checking if this execution should trigger the Leet Greeting messages (hour {})...", currentHour);
            return currentHour != 13;
        }
    }

}
