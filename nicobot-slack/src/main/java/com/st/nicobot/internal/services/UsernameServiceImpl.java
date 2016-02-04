package com.st.nicobot.internal.services;

import com.st.nicobot.services.UsernameService;
import com.ullink.slack.simpleslackapi.SlackUser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Logs on 09-12-15.
 */
@Service
public class UsernameServiceImpl implements UsernameService {

    //private static String ZERO_WIDTH_SPACE = "\u200B"; Not working anymore. remaining untested: 200D/200E/200F/202F
    private static String ZERO_WIDTH_SPACE = "\u200C";

    @Override
    public String getNoHLName(String userName) {
        if(StringUtils.isNotBlank(userName)) {
            return userName.charAt(0) + ZERO_WIDTH_SPACE + userName.substring(1);
        } else {
            return userName;
        }
    }

    @Override
    public String getNoHLName(SlackUser user) {
        String userName = user.getUserName();
        return getNoHLName(userName);
    }

    @Override
    public String getNoHLName(List<String> names, String separator) {
        if(!CollectionUtils.isEmpty(names)) {
            ArrayList<String> tempNames = names.stream().map(this::getNoHLName).collect(Collectors.toCollection(ArrayList::new));
            return StringUtils.join(tempNames, separator);
        } else {
            return StringUtils.join(names, separator);
        }
    }

    @Override
    public String getNoHLNameList(List<SlackUser> names, String separator) {
        List<String> tempNames = names.stream().map(SlackUser::getUserName).collect(Collectors.toCollection(ArrayList::new));
        return getNoHLName(tempNames, separator);
    }
}
