package com.st.nicobot.internal.job;

import com.st.nicobot.bot.NicoBot;
import com.st.nicobot.job.HappyGeekTimeJob;
import com.st.nicobot.services.LeetGreetingService;
import com.st.nicobot.services.Messages;
import com.st.nicobot.services.UsernameService;
import com.st.nicobot.services.memory.GreetersRepositoryManager;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackUser;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    private GreetersRepositoryManager greetersRepositoryManager;

    @Autowired
    private UsernameService usernameService;

    @Override
    @Async
    @Scheduled(cron="0 37 13 * * *")
    //@Scheduled(fixedDelay = 11000, initialDelay = 5000)
    public void runJob() {
        greetingService.init();
        logger.info("Leet starting at "+ DateTime.now().toString());

        nicobot.getChannels().stream().forEach(channel -> nicobot.sendMessage(channel, null, messages.getMessage("hgt")));

        try {
            logger.info("Bot will now wait for 1 min to read mesages at "+ DateTime.now().toString());
            synchronized (this) {
                int secondsBeforeTimeOut = Seconds.secondsBetween(DateTime.now(), DateTime.now().withTime(13, 38, 0, 0)).getSeconds();
                logger.info("exact wait time (seconds): {}",secondsBeforeTimeOut);
                this.wait(secondsBeforeTimeOut*1000);
            }
            logger.info("Happy Geek Thread finished at "+ DateTime.now().toString());
        } catch (InterruptedException e) {
            logger.error("Error in waiting task", e);
        } finally {
            greetingService.finish();
            logger.info("Leet ended at " + DateTime.now().toString());
        }

        for(SlackChannel chan : nicobot.getChannels()) {
            Set<SlackUser> users = greetingService.getGreeters().get(chan);
            String message = buildMessageWithNames(users);

            nicobot.sendMessage(chan, null, message);

            if(users != null && !users.isEmpty()) {
                greetersRepositoryManager.addGreeters(chan, users);
            }

            nicobot.sendMessage(chan, null, buildTopUsers(greetersRepositoryManager.getWeeklyGreeters(chan)));
        }
    }

    public String buildTopUsers(Map<SlackUser, Integer> users) {
        StringBuilder message = new StringBuilder(messages.getMessage("weekTopHGT"));
        if(users != null && !users.isEmpty()) {
            for (Map.Entry<SlackUser, Integer> user : users.entrySet()) {
                message.append(usernameService.getNoHLName(user.getKey())).append(" (").append(user.getValue()).append("), ");
            }
            message.delete(message.lastIndexOf(","), message.length());
        } else {
            message.append(messages.getMessage("noOne"));
        }
        return message.toString();
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

        List<String> names = users.stream().map(SlackUser::getUserName).collect(Collectors.toList());

        return String.format(congratulationMessage, usernameService.getNoHLName(names, ", "));
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
