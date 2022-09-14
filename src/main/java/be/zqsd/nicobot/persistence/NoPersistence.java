package be.zqsd.nicobot.persistence;

import io.quarkus.arc.DefaultBean;
import org.slf4j.Logger;
import wtf.logs.nicobot.gommette.Gommette;
import wtf.logs.nicobot.gommette.GommetteScore;
import wtf.logs.nicobot.hgt.HgtScore;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.util.Collection;

import static java.util.Collections.emptyList;
import static org.slf4j.LoggerFactory.getLogger;

@ApplicationScoped
@DefaultBean
public class NoPersistence implements Persistence {

    private static final Logger LOG = getLogger(NoPersistence.class);

    @Override
    public void saveMessage(String username, String message) {
        LOG.info("Save message omitted");
    }

    @Override
    public void addHgtScores(String channelId, Collection<String> userIds) {
        LOG.info("Add score omitted");
    }

    @Override
    public Collection<HgtScore> getWeeklyHgtScores(String channelId) throws IOException {
        LOG.info("Get weekly score omitted");
        return emptyList();
    }

    @Override
    public Collection<HgtScore> getYearlyHgtScores(String channelId) throws IOException {
        LOG.info("Get yearly score omitted");
        return emptyList();
    }

    @Override
    public void addGommette(Gommette gommette) {
    LOG.info("Add gommette omitted");
    }

    @Override
    public Collection<GommetteScore> getCurrentGommettesScore() throws IOException {
        LOG.info("Get current year gommettes omitted");
        return emptyList();
    }

    @Override
    public Collection<GommetteScore> getYearlyGommettesScore(int year) throws IOException {
        LOG.info("Get yearly gommettes omitted");
        return emptyList();
    }

    @Override
    public Collection<GommetteScore> getUserGommettesScore(int year, String userId) throws IOException {
        LOG.info("Get user gommettes omitted");
        return emptyList();
    }
}
