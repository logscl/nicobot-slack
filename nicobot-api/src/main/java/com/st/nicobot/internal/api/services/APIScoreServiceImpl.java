package com.st.nicobot.internal.api.services;

import com.st.nicobot.api.domain.model.Score;
import com.st.nicobot.api.domain.model.request.ScoreRequest;
import com.st.nicobot.api.domain.model.response.ScoreResponse;
import com.st.nicobot.api.services.APIScoreService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class APIScoreServiceImpl extends APIBaseService<ScoreResponse> implements APIScoreService {

    public final String SERVICE_NAME = "scores/hgt";

    private static final ScoreResponse reponseInstance = new ScoreResponse();

    @Override
    public ScoreResponse getResponseInstance() {
        return reponseInstance;
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    public List<Score> getWeeklyScores(String channelId) {
        ScoreResponse response = sendGetRequest(channelId, null);

        return response.getScores();
    }

    @Override
    public List<Score> getYearlyScores(String channelId, int year) {
        String context = channelId+"/"+year;
        ScoreResponse response = sendGetRequest(context, null);

        return response.getScores();
    }

    @Override
    public void addScores(String channelId, List<String> userIds) {
        ScoreRequest request = new ScoreRequest();
        request.setUsers(userIds);

        sendPostRequest(channelId, request);
    }
}
