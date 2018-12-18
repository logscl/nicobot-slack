package com.st.nicobot.api.domain.model.response;

import com.st.nicobot.api.domain.model.GommetteScore;

import java.util.List;

public class GommetteScoreResponse implements UnmarshalledResponse {

    private List<GommetteScore> scores;

    public GommetteScoreResponse() {

    }

    public List<GommetteScore> getScores() {
        return scores;
    }
}
