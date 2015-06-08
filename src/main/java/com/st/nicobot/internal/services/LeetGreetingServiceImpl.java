package com.st.nicobot.internal.services;

import com.st.nicobot.services.LeetGreetingService;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackMessage;
import com.ullink.slack.simpleslackapi.SlackUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class LeetGreetingServiceImpl implements LeetGreetingService {

	private static Logger logger = LoggerFactory.getLogger(LeetGreetingServiceImpl.class);

    private boolean leetHourActive = false;

    // clé=chan,list=users
    private Map<SlackChannel,Set<SlackUser>> leetGreeters = new HashMap<>();

    private static Pattern[] triggers = new Pattern[]{
            Pattern.compile(".*h+ ?g+ ?t+.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*Happy.*Geek.*Time.*", Pattern.CASE_INSENSITIVE)
    };

    @Override
    public void init() {
        leetHourActive = true;
        leetGreeters =  new HashMap<>();
    }

    public void finish() {
        leetHourActive = false;
    }

    @Override
    public void addGreeter(SlackMessage message) {
        if(!hasAlreadyGreeted(message)) {
            for(Pattern pattern : triggers) {
                if(pattern.matcher(message.getMessageContent()).matches()) {
                	logger.debug("Cha-ching ! trigger found");
                    leetGreeters.get(message.getChannel()).add(message.getSender());
                }
            }
        }
    }

    @Override
    public boolean isLeetHourActive() {
        return leetHourActive;
    }

    private boolean hasAlreadyGreeted(SlackMessage message) {
        if(leetGreeters.get(message.getChannel()) == null) {
            leetGreeters.put(message.getChannel(), new LinkedHashSet<>());
            return false;
        } else {
            return (leetGreeters.get(message.getChannel()).contains(message.getSender()));
        }
    }
    
    @Override
    public Map<SlackChannel,Set<SlackUser>> getGreeters() {
    	return leetGreeters;
    }
}
