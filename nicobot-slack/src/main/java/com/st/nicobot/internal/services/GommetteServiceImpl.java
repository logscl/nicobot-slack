package com.st.nicobot.internal.services;

import com.google.common.base.Objects;
import com.st.nicobot.api.domain.model.GommetteScore;
import com.st.nicobot.api.services.APIGommetteService;
import com.st.nicobot.bot.NicoBot;
import com.st.nicobot.services.GommetteService;
import com.st.nicobot.services.Messages;
import com.st.nicobot.services.UsernameService;
import com.ullink.slack.simpleslackapi.SlackUser;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class GommetteServiceImpl implements GommetteService {

    @Autowired
    private NicoBot nicobot;

    @Autowired
    private UsernameService usernameService;

    @Autowired
    private APIGommetteService gommetteService;

    @Autowired
    private Messages messages;


    @Override
    public String getCurrentYearScore() {
        return buildTopUsers(gommetteService.getCurrentYearScores());
    }

    @Override
    public String getUserScore(SlackUser user) {
        List<GommetteScore> scores = gommetteService.getUserScore(DateTime.now().getYear(), user.getId());
        if (!CollectionUtils.isEmpty(scores)) {
            return buildUserScore(scores.get(0));
        } else {
            return messages.getMessage("gmScoreEmpty", usernameService.getNoHLName(user));
        }
    }

    private SlackUser getById(String userId) {
        return nicobot.getSession().findUserById(userId);
    }

    private String buildTopUsers(List<GommetteScore> scores) {
        StringBuilder message = new StringBuilder(messages.getMessage("gmTopUsers"));
        if (!CollectionUtils.isEmpty(scores)) {
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
        int greenCount = Objects.firstNonNull(score.getGreenCount(), 0);
        int redCount = Objects.firstNonNull(score.getRedCount(), 0);

        String greenPlural = greenCount > 1 ? "s" : "";
        String redPlural = redCount > 1 ? "s" : "";

        return messages.getMessage("gmScore", usernameService.getNoHLName(getById(score.getUserId())), greenCount, greenPlural, greenPlural, redCount, redPlural, redPlural);
    }
}
