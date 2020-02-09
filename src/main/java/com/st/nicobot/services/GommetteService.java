package com.st.nicobot.services;

import com.ullink.slack.simpleslackapi.SlackUser;

public interface GommetteService {

    String getCurrentYearScore();

    String getUserScore(SlackUser user);
}
