package com.st.nicobot.internal.services;

import com.st.nicobot.services.UsernameService;
import com.ullink.slack.simpleslackapi.SlackUser;
import org.springframework.stereotype.Service;

/**
 * Created by Logs on 09-12-15.
 */
@Service
public class UsernameServiceImpl implements UsernameService {

    /**
     * Returns the raw name of the user
     */
    @Override
    public String getNoHLName(SlackUser user) {
        return user.getUserName();
    }

    /**
     * Returns the linked name of a user
     */
    @Override
    public String getHLName(SlackUser user) {
        String userQuery = "<@%s>";
        return String.format(userQuery, user.getId());
    }
}
