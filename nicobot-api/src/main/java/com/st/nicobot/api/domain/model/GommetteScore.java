package com.st.nicobot.api.domain.model;

public class GommetteScore {

    private String userId;
    private Integer redCount;
    private Integer greenCount;
    private Integer score;

    public GommetteScore() {
    }

    public GommetteScore(String userId, Integer redCount, Integer greenCount, Integer score) {
        this.userId = userId;
        this.redCount = redCount;
        this.greenCount = greenCount;
        this.score = score;
    }

    public String getUserId() {
        return userId;
    }

    public Integer getRedCount() {
        return redCount;
    }

    public Integer getGreenCount() {
        return greenCount;
    }

    public Integer getScore() {
        return score;
    }
}
