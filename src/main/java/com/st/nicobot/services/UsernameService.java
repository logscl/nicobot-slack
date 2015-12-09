package com.st.nicobot.services;

import com.ullink.slack.simpleslackapi.SlackUser;

import java.util.List;

/**
 * Created by Logs on 09-12-15.
 */
public interface UsernameService {

    String getNoHLName(String userName);
    String getNoHLName(SlackUser user);
    String getNoHLName(List<String> names, String separator);
    String getNoHLNameList(List<SlackUser> names, String separator);
}
