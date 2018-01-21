package com.st.nicobot.api.domain.model.response;

import com.st.nicobot.api.domain.model.Hgt;

import java.util.List;

public class HgtResponse implements UnmarshalledResponse {

    private List<Hgt> scores;

    public HgtResponse() {
    }

    public List<Hgt> getScores() {
        return scores;
    }
}
