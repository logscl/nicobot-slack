package be.zqsd.nicobot.internal.services;

import be.zqsd.nicobot.gommette.GommetteScore;
import be.zqsd.nicobot.bot.NicoBot;
import be.zqsd.nicobot.services.Messages;
import be.zqsd.nicobot.services.UsernameService;
import be.zqsd.nicobot.services.GommetteService;
import be.zqsd.nicobot.services.PersistenceService;
import com.ullink.slack.simpleslackapi.SlackUser;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

import static java.util.Optional.ofNullable;

@Service
public class GommetteServiceImpl implements GommetteService {

    private static Logger logger = LoggerFactory.getLogger(GommetteServiceImpl.class);

    @Autowired
    private NicoBot nicobot;

    @Autowired
    private UsernameService usernameService;

    @Autowired
    private PersistenceService persistenceService;

    @Autowired
    private Messages messages;


    @Override
    public String getCurrentYearScore() {
        try {
            Collection<GommetteScore> scores = persistenceService
                    .getCurrentGommettesScore();
            return buildTopUsers(scores);
        } catch (Exception e) {
            logger.error("unable to get current year scores", e);
            return "Oops, error :(";
        }
    }

    @Override
    public String getUserScore(SlackUser user) {
        try {
            Collection<GommetteScore> gommettes = persistenceService
                    .getUserGommettesScore(DateTime.now().getYear(), user.getId());

            return gommettes
                    .stream().findFirst().map(this::buildUserScore)
                    .orElse(messages.getMessage("gmScoreEmpty", usernameService.getNoHLName(user)));
        } catch (Exception e) {
            logger.error("unable to get user '" + user.getId() + "' score", e);
            return "Oops, error :(";
        }
    }

    private SlackUser getById(String userId) {
        return nicobot.getSession().findUserById(userId);
    }

    private String buildTopUsers(Collection<GommetteScore> scores) {
        StringBuilder message = new StringBuilder(messages.getMessage("gmTopUsers"));
        if (!scores.isEmpty()) {
            for (GommetteScore score : scores) {
                SlackUser username = getById(score.getUserId());
                message.append(usernameService.getNoHLName(username)).append(" (*").append(score.getScore()).append("* [").append(score.getGreenCount()).append("|").append(score.getRedCount()).append("]), ");
            }
            message.delete(message.lastIndexOf(","), message.length());
        } else {
            message.append(messages.getMessage("noOne"));
        }
        return message.toString();
    }

    private String buildUserScore(GommetteScore score) {
        int greenCount = ofNullable(score.getGreenCount()).orElse(0);
        int redCount = ofNullable(score.getRedCount()).orElse(0);

        String greenPlural = greenCount > 1 ? "s" : "";
        String redPlural = redCount > 1 ? "s" : "";

        return messages.getMessage("gmScore", usernameService.getNoHLName(getById(score.getUserId())), greenCount, greenPlural, greenPlural, redCount, redPlural, redPlural);
    }
}
