package com.st.nicobot.api.domain.model.response;

import com.st.nicobot.api.domain.model.Score;

import java.util.List;

public class ScoreResponse implements UnmarshalledResponse {

    private List<Score> scores;

    public ScoreResponse() {
    }

    public List<Score> getScores() {
        return scores;
    }
}
