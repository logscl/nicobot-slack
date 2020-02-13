package be.zqsd.nicobot.internal.services;

import be.zqsd.nicobot.hgt.HgtScore;
import be.zqsd.nicobot.bot.NicoBot;
import be.zqsd.nicobot.services.HappyGeekTimeService;
import be.zqsd.nicobot.services.Messages;
import be.zqsd.nicobot.services.UsernameService;
import be.zqsd.nicobot.services.PersistenceService;
import com.ullink.slack.simpleslackapi.SlackUser;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Created by Logs on 24-01-16.
 */
@Service
public class HappyGeekTimeServiceImpl implements HappyGeekTimeService {

    private static Logger logger = LoggerFactory.getLogger(HappyGeekTimeServiceImpl.class);

    @Autowired
    private NicoBot nicobot;

    @Autowired
    private UsernameService usernameService;

    @Autowired
    private PersistenceService persistenceService;

    @Autowired
    private Messages messages;


    @Override
    public String getAllTimeTopUsers(String channel) {
        String startSentence = messages.getMessage("allTopHGT", DateTime.now().getYear(), DateTime.now().getDayOfYear());
        String noOne = messages.getMessage("noOne");
        try {
            Collection<HgtScore> scores = persistenceService.getYearlyHgtScores(channel);

            return buildUserList(startSentence, noOne, scores);
        } catch (Exception e) {
            logger.error("Unable to get user scores", e);
            return "Oops error :(";
        }
    }

    @Override
    public String getWeekTopUsers(String channel) {
        String startSentence = messages.getMessage("weekTopHGT");
        String noOne = messages.getMessage("noOne");
        try {
            Collection<HgtScore> scores = persistenceService.getWeeklyHgtScores(channel);

            return buildUserList(startSentence, noOne, scores);
        } catch (Exception e) {
            logger.error("Unable to get user scores", e);
            return "Oops error :(";
        }
    }

    private String buildUserList(String startSentence, String noOneSentence, Collection<HgtScore> users) {
        StringBuilder message = new StringBuilder(startSentence);

        if (CollectionUtils.isEmpty(users)) {
            message.append(noOneSentence);
        } else {
            String usersString = users.stream()
                    .map(hgt -> {
                        SlackUser user = nicobot.getSession().findUserById(hgt.getUserId());
                        String noHlName = usernameService.getNoHLName(user);

                        return noHlName + " (" + hgt.getScore() + ")";
                    })
                    .collect(Collectors.joining(", "));

            message.append(usersString);
        }
        return message.toString();
    }
}
