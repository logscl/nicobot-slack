package com.st.nicobot.services.memory;

import com.st.nicobot.bot.utils.GommetteColor;
import com.ullink.slack.simpleslackapi.SlackUser;

import java.util.Map;

/**
 * Created by Logs on 17-08-15.
 */
public interface GommettesRepositoryManager {

    void addGommette(SlackUser user, GommetteColor color);

    Map<GommetteColor, Integer> getGommettes(SlackUser user);

    Map<GommetteColor, Integer> getBestGommettes();

    Map<SlackUser, Integer> getGommettesTop();
}
