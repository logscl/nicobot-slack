package be.zqsd.nicobot.internal.services;

import be.zqsd.nicobot.gommette.Gommette;
import be.zqsd.nicobot.gommette.GommetteScore;
import be.zqsd.nicobot.hgt.HgtScore;
import be.zqsd.nicobot.services.PersistenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collection;

import static java.util.Collections.emptyList;

@Service
@Profile({ "dev", "default" })
public class NoPersistenceServiceImpl implements PersistenceService {

    private static Logger logger = LoggerFactory.getLogger(NoPersistenceServiceImpl.class);

    @Override
    public void saveMessage(String username, String message) {
        logger.info("Save message omitted");
    }

    @Override
    public void addHgtScores(String channelId, Collection<String> userIds) {
        logger.info("Add score omitted");
    }

    @Override
    public Collection<HgtScore> getWeeklyHgtScores(String channelId) throws IOException {
        logger.info("Get weekly score omitted");
        return emptyList();
    }

    @Override
    public Collection<HgtScore> getYearlyHgtScores(String channelId) throws IOException {
        logger.info("Get yearly score omitted");
        return emptyList();
    }

    @Override
    public void addGommette(Gommette gommette) {
    logger.info("Add gommette omitted");
    }

    @Override
    public Collection<GommetteScore> getCurrentGommettesScore() throws IOException {
        logger.info("Get current year gommettes omitted");
        return emptyList();
    }

    @Override
    public Collection<GommetteScore> getYearlyGommettesScore(int year) throws IOException {
        logger.info("Get yearly gommettes omitted");
        return emptyList();
    }

    @Override
    public Collection<GommetteScore> getUserGommettesScore(int year, String userId) throws IOException {
        logger.info("Get user gommettes omitted");
        return emptyList();
    }
}
