package com.st.nicobot.internal.services;

import com.st.nicobot.bot.NicoBot;
import com.st.nicobot.services.HappyGeekTimeService;
import com.st.nicobot.services.Messages;
import com.st.nicobot.services.UsernameService;
import com.st.nicobot.services.memory.GreetersRepositoryManager;
import com.ullink.slack.simpleslackapi.SlackUser;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Map;
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
    private GreetersRepositoryManager greeters;

    @Autowired
    private Messages messages;


    @Override
    public String getAllTimeTopUsers(String channel) {
        String startSentence = messages.getMessage("allTopHGT", DateTime.now().getYear(), DateTime.now().getDayOfYear());
        String noOne = messages.getMessage("noOne");
        Map<String, Integer> users = greeters.getAllTimeGreeters(channel);
        return buildUserList(startSentence,noOne,users);
    }

    @Override
    public String getWeekTopUsers(String channel) {
        String startSentence = messages.getMessage("weekTopHGT");
        String noOne = messages.getMessage("noOne");
        Map<String, Integer> users = greeters.getWeeklyGreeters(channel);
        return buildUserList(startSentence,noOne,users);
    }

    private String buildUserList(String startSentence, String noOneSentence, Map<String, Integer> users) {
        StringBuilder message = new StringBuilder(startSentence);
        int daysOfYear = DateTime.now().getDayOfYear();

        if (CollectionUtils.isEmpty(users)) {
            message.append(noOneSentence);
        } else {
            String usersString = users.entrySet().stream()
                    .map(entry -> {
                        SlackUser user = nicobot.getSession().findUserById(entry.getKey());
                        String noHlName = usernameService.getNoHLName(user);

                        //double percentDone = (entry.getValue() / (double)daysOfYear) * 100;
                        //return String.format("%s (%d-_%.0f%%_)",noHlName, entry.getValue(), percentDone);

                        return noHlName + " (" + entry.getValue() + ")";
                    })
                    .collect(Collectors.joining(", "));

            message.append(usersString);
        }
        return message.toString();
    }
}
