package com.st.nicobot.api.domain.model;

public class Score {

    private String userId;

    private Integer score;

    public Score() {
    }

    public Score(String userId, Integer score) {
        this.userId = userId;
        this.score = score;
    }

    public String getUserId() {
        return userId;
    }

    public Integer getScore() {
        return score;
    }
}
