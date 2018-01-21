package com.st.nicobot.internal.api.services;

import com.st.nicobot.api.domain.model.Hgt;
import com.st.nicobot.api.domain.model.request.HgtRequest;
import com.st.nicobot.api.domain.model.response.HgtResponse;
import com.st.nicobot.api.services.APIHgtService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class APIHgtServiceImpl extends APIBaseService<HgtResponse> implements APIHgtService {

    public final String SERVICE_NAME = "scores/hgt";

    private static final HgtResponse reponseInstance = new HgtResponse();

    @Override
    public HgtResponse getResponseInstance() {
        return reponseInstance;
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    public List<Hgt> getWeeklyScores(String channelId) {
        HgtResponse response = sendGetRequest(channelId, null);

        return response.getScores();
    }

    @Override
    public List<Hgt> getYearlyScores(String channelId, int year) {
        String context = channelId+"/"+year;
        HgtResponse response = sendGetRequest(context, null);

        return response.getScores();
    }

    @Override
    public void addScores(String channelId, List<String> userIds) {
        HgtRequest request = new HgtRequest();
        request.setUsers(userIds);

        sendPostRequest(channelId, request);
    }
}
