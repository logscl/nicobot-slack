package com.st.nicobot.services.memory;

import com.st.nicobot.bot.utils.GommetteColor;
import com.st.nicobot.db.tables.records.GommetteRecord;
import com.st.nicobot.internal.services.memory.GommettesRepositoryManagerImpl.GommetteUserScore;

import java.util.List;
import java.util.Map;

/**
 * Created by Logs on 17-08-15.
 */
public interface GommettesRepositoryManager {

    void addGommette(GommetteRecord record);

    Map<GommetteColor, Integer> getGommettes(String user);

    Map<GommetteColor, Integer> getBestGommettes();

    List<GommetteUserScore> getGommettesTop();

    String getGommettesFormatted();
}
