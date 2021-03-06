package be.zqsd.nicobot.services;

import wtf.logs.nicobot.gommette.Gommette;
import wtf.logs.nicobot.gommette.GommetteScore;
import wtf.logs.nicobot.hgt.HgtScore;

import java.io.IOException;
import java.util.Collection;

public interface PersistenceService {

    void saveMessage(String username, String message) throws IOException;
    void addHgtScores(String channelId, Collection<String> userIds) throws IOException;
    Collection<HgtScore> getWeeklyHgtScores(String channelId) throws IOException;
    Collection<HgtScore> getYearlyHgtScores(String channelId) throws IOException;
    void addGommette(Gommette gommette) throws IOException;
    Collection<GommetteScore> getCurrentGommettesScore() throws IOException;
    Collection<GommetteScore> getYearlyGommettesScore(int year) throws IOException;
    Collection<GommetteScore> getUserGommettesScore(int year, String userId) throws IOException;
}
