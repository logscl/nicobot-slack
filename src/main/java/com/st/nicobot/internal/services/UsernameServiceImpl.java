package com.st.nicobot.internal.services;

import com.st.nicobot.services.UsernameService;
import com.ullink.slack.simpleslackapi.SlackUser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * Created by Logs on 09-12-15.
 */
@Service
public class UsernameServiceImpl implements UsernameService {

    //private static String ZERO_WIDTH_SPACE = "\u200B"; Not working anymore. remaining untested: 200D/200E/200F/202F
    private static String ZERO_WIDTH_SPACE = "\u200C";

    /**
     * Returns the name of the user with a blank char in the middle
     */
    @Override
    public String getNoHLName(SlackUser user) {
        String userName = user.getUserName();
        if(StringUtils.isNotBlank(userName)) {
            return userName.charAt(0) + ZERO_WIDTH_SPACE + userName.substring(1);
        } else {
            return userName;
        }
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
