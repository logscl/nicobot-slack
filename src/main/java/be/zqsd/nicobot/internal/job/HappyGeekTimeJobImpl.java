package be.zqsd.nicobot.internal.job;

import be.zqsd.nicobot.bot.NicoBot;
import be.zqsd.nicobot.job.HappyGeekTimeJob;
import be.zqsd.nicobot.services.*;
import be.zqsd.nicobot.utils.NicobotProperty;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.temporal.TemporalAdjuster;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoField.*;
import static java.time.temporal.ChronoUnit.SECONDS;

/**
 * Created by Logs on 22-03-15.
 */
@Service("happyGeekTimeJob")
public class HappyGeekTimeJobImpl implements HappyGeekTimeJob {


    private static Logger logger = LoggerFactory.getLogger(HappyGeekTimeJobImpl.class);

    @Autowired
    private NicoBot nicobot;

    @Autowired
    private Messages messages;

    @Autowired
    private LeetGreetingService greetingService;

    @Autowired
    private PersistenceService persistenceService;

    @Autowired
    private HappyGeekTimeService hgtService;

    @Autowired
    private PropertiesService properties;

    @Autowired
    private UsernameService usernameService;

    @Override
    @Async
    @Scheduled(cron="0 37 13 * * *")
    //@Scheduled(fixedDelay = 11000, initialDelay = 5000)
    public void runJob() {
        greetingService.init();
        logger.info("Leet starting at "+ now().toString());

        Predicate<SlackChannel> isGroupChannel = slackChannel -> slackChannel.getType() != SlackChannel.SlackChannelType.INSTANT_MESSAGING;
        Predicate<SlackChannel> isFeatured = slackChannel -> slackChannel.getName().equals(properties.get(NicobotProperty.FEATURED_CHANNEL));

        try {
            logger.info("Bot will now wait for 1 min to read mesages at "+ now().toString());
            synchronized (this) {

                long secondsBeforeTimeOut = SECONDS.between(now(), now().with(leetHour()));
                logger.info("exact wait time (seconds): {}",secondsBeforeTimeOut);
                this.wait(secondsBeforeTimeOut*1000);
            }
            logger.info("Happy Geek Thread finished at "+ now().toString());
        } catch (InterruptedException e) {
            logger.error("Error in waiting task", e);
        } finally {
            greetingService.finish();
            logger.info("Leet ended at " + now().toString());
        }

        nicobot.getSession().getChannels().stream().filter(isGroupChannel.and(isFeatured)).forEach(chan -> {
            Set<SlackUser> users = greetingService.getGreeters().get(chan);
            String message = buildMessageWithNames(users);

            nicobot.sendMessage(chan, null, messages.getMessage("hgt"));
            nicobot.sendMessage(chan, null, message);

            if (users != null && !users.isEmpty()) {
                try {
                    persistenceService.addHgtScores(chan.getId(), users.stream().map(SlackUser::getId).collect(Collectors.toList()));
                } catch (Exception e) {
                    logger.error("Unable to add HGT score", e);
                }
            }

            nicobot.sendMessage(chan, null, hgtService.getWeekTopUsers(chan.getId()));
        });
    }

    private TemporalAdjuster leetHour() {
        return (temporal) -> temporal
                .with(HOUR_OF_DAY, 13)
                .with(MINUTE_OF_HOUR, 37)
                .with(SECOND_OF_MINUTE, 0)
                .with(MILLI_OF_SECOND, 0);
    }

    /**
     * Construit un message suivant si {@code names} est null ou non.
     *
     * @param users
     *            Une collection de {@link SlackUser} pouvant etre vide
     * @return
     */
    public String buildMessageWithNames(Set<SlackUser> users) {
        String message = messages.getMessage("noHGT");

        if (users != null && users.size() > 0) {
            message = createCongratulationMessageWithNames(users);
        }

        return message;
    }

    /**
     * Retourne un message formaté diffirement si {@code names} contient 1 ou
     * plusieurs éléments
     *
     * @param users
     *            Un collection de {@link SlackUser} contenant 1 ou plusieurs
     *            éléments
     * @return
     */
    public String createCongratulationMessageWithNames(Set<SlackUser> users) {
        String congratulationMessage = retrieveCongratulationMessage(users.size() > 1);

        String names = users.stream().map(usernameService::getNoHLName).collect(Collectors.joining(", "));

        return String.format(congratulationMessage, names);
    }

    /**
     * Retourne un template de message suivant {@code isMoreThanOneGreeters}
     *
     * @param isMoreThanOneGreeters
     * @return
     */
    public String retrieveCongratulationMessage(boolean isMoreThanOneGreeters) {
        String congratMessage = messages.getMessage("congratSoloHGT");

        if (isMoreThanOneGreeters) {
            congratMessage = messages.getMessage("congratHGT");
        }

        return congratMessage;
    }
}
