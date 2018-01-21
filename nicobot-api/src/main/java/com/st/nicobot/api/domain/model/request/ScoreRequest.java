package com.st.nicobot.api.domain.model.request;

import java.util.List;

public class ScoreRequest {

    private List<String> users;

    public ScoreRequest() {
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }
}
