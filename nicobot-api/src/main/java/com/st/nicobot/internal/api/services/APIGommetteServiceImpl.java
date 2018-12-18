package com.st.nicobot.internal.api.services;

import com.st.nicobot.api.domain.model.Gommette;
import com.st.nicobot.api.domain.model.GommetteScore;
import com.st.nicobot.api.domain.model.request.GommetteRequest;
import com.st.nicobot.api.domain.model.response.GommetteScoreResponse;
import com.st.nicobot.api.services.APIGommetteService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class APIGommetteServiceImpl extends APIBaseService<GommetteScoreResponse> implements APIGommetteService {

    public final String SERVICE_NAME = "scores/gommettes";

    private static final GommetteScoreResponse responseInstance = new GommetteScoreResponse();

    @Override
    public GommetteScoreResponse getResponseInstance() {
        return responseInstance;
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }


    @Override
    public List<GommetteScore> getCurrentYearScores() {
        GommetteScoreResponse response = sendGetRequest(null);

        return response.getScores();
    }

    @Override
    public List<GommetteScore> getYearlyScores(Integer year) {
        GommetteScoreResponse response = sendGetRequest(year.toString(), null);

        return response.getScores();
    }

    @Override
    public List<GommetteScore> getUserScore(Integer year, String userId) {
        String context = String.format("%s/%s", year, userId);
        GommetteScoreResponse response = sendGetRequest(context, null);

        return response.getScores();
    }

    @Override
    public void addGommette(Gommette gommette) {
        GommetteRequest request = new GommetteRequest();
        request.setGommette(gommette);

        sendPostRequest(request);
    }
}
