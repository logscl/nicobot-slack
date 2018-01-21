package com.st.nicobot.api.services;

import com.st.nicobot.api.domain.model.Hgt;

import java.util.List;

public interface APIHgtService {

    List<Hgt> getWeeklyScores(String channelId);

    List<Hgt> getYearlyScores(String channelId, int year);

    void addScores(String channelId, List<String> userIds);
}
