package com.st.nicobot.api.services;

import com.st.nicobot.api.domain.model.Gommette;
import com.st.nicobot.api.domain.model.GommetteScore;

import java.util.List;

public interface APIGommetteService {

    List<GommetteScore> getCurrentYearScores();

    List<GommetteScore> getYearlyScores(Integer year);

    List<GommetteScore> getUserScore(Integer year, String userId);

    void addGommette(Gommette gommette);
}
