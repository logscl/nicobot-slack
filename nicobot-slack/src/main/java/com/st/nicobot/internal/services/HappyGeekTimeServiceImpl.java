package com.st.nicobot.internal.services;

import com.st.nicobot.api.domain.model.Hgt;
import com.st.nicobot.api.services.APIHgtService;
import com.st.nicobot.bot.NicoBot;
import com.st.nicobot.services.HappyGeekTimeService;
import com.st.nicobot.services.Messages;
import com.st.nicobot.services.UsernameService;
import com.ullink.slack.simpleslackapi.SlackUser;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Logs on 24-01-16.
 */
@Service
public class HappyGeekTimeServiceImpl implements HappyGeekTimeService {

    @Autowired
    private NicoBot nicobot;

    @Autowired
    private UsernameService usernameService;

    @Autowired
    private APIHgtService greeters;

    @Autowired
    private Messages messages;


    @Override
    public String getAllTimeTopUsers(String channel) {
        String startSentence = messages.getMessage("allTopHGT", DateTime.now().getYear(), DateTime.now().getDayOfYear());
        String noOne = messages.getMessage("noOne");
        List<Hgt> users = greeters.getYearlyScores(channel, DateTime.now().getYear());
        return buildUserList(startSentence,noOne,users);
    }

    @Override
    public String getWeekTopUsers(String channel) {
        String startSentence = messages.getMessage("weekTopHGT");
        String noOne = messages.getMessage("noOne");
        List<Hgt> users = greeters.getWeeklyScores(channel);
        return buildUserList(startSentence,noOne,users);
    }

    private String buildUserList(String startSentence, String noOneSentence, List<Hgt> users) {
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
