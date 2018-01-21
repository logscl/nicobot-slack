package com.st.nicobot.api.services;

import com.st.nicobot.api.domain.model.Score;

import java.util.List;

public interface APIScoreService {

    List<Score> getWeeklyScores(String channelId);

    List<Score> getYearlyScores(String channelId, int year);

    void addScores(String channelId, List<String> userIds);
}
