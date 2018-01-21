package com.st.nicobot.api.domain.model;

public class Hgt {

    private String userId;

    private Integer score;

    public Hgt() {
    }

    public Hgt(String userId, Integer score) {
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
