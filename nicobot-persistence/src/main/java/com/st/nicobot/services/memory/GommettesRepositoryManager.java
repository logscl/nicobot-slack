package com.st.nicobot.services.memory;

import com.st.nicobot.bot.utils.GommetteColor;

import java.util.Map;

/**
 * Created by Logs on 17-08-15.
 */
public interface GommettesRepositoryManager {

    void addGommette(String user, GommetteColor color);

    Map<GommetteColor, Integer> getGommettes(String user);

    Map<GommetteColor, Integer> getBestGommettes();

    Map<String, Integer> getGommettesTop();
}
