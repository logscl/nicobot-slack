package com.st.nicobot.services;

import com.ullink.slack.simpleslackapi.SlackUser;

/**
 * Created by Logs on 09-12-15.
 */
public interface UsernameService {

    String getNoHLName(SlackUser user);

    String getHLName(SlackUser user);
}
