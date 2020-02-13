package be.zqsd.nicobot.internal.services;

import be.zqsd.nicobot.client.NicobotPersistence;
import be.zqsd.nicobot.gommette.Gommette;
import be.zqsd.nicobot.gommette.GommetteRequest;
import be.zqsd.nicobot.gommette.GommetteScore;
import be.zqsd.nicobot.hgt.HgtRequest;
import be.zqsd.nicobot.hgt.HgtScore;
import be.zqsd.nicobot.message.Message;
import be.zqsd.nicobot.message.MessageRequest;
import be.zqsd.nicobot.utils.NicobotProperty;
import be.zqsd.nicobot.services.PersistenceService;
import be.zqsd.nicobot.services.PropertiesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Collection;

import static java.time.LocalDateTime.now;
import static java.util.Collections.singletonList;

@Service
@Profile("master")
public class RemotePersistenceServiceImpl implements PersistenceService {

    private static Logger logger = LoggerFactory.getLogger(RemotePersistenceServiceImpl.class);

    private NicobotPersistence persistenceService;

    @Autowired
    private PropertiesService properties;

    @PostConstruct
    private void init() {
        persistenceService = NicobotPersistence.create(properties.get(NicobotProperty.API_URI), properties.get(NicobotProperty.API_KEY));
    }

    @Override
    public void saveMessage(String username, String content) throws IOException {
        Message message = new Message(now(), username, content);
        persistenceService
                .messageService()
                .saveMessages(new MessageRequest(singletonList(message)))
                .execute();
    }

    @Override
    public void addHgtScores(String channelId, Collection<String> userIds) throws IOException {
        persistenceService
                .hgtService()
                .addScore(channelId, new HgtRequest(userIds))
                .execute();
    }

    @Override
    public Collection<HgtScore> getWeeklyHgtScores(String channelId) throws IOException {
        return persistenceService
                .hgtService()
                .currentWeekScores(channelId)
                .execute()
                .body()
                .getScores();
    }

    @Override
    public Collection<HgtScore> getYearlyHgtScores(String channelId) throws IOException {
        return persistenceService
                .hgtService()
                .scoresByYear(channelId, now().getYear())
                .execute()
                .body()
                .getScores();
    }

    @Override
    public void addGommette(Gommette gommette) throws IOException {
        persistenceService
                .gommetteService()
                .addGommette(new GommetteRequest(gommette))
                .execute();
    }

    @Override
    public Collection<GommetteScore> getCurrentGommettesScore() throws IOException {
        return persistenceService
                .gommetteService()
                .currentYearGommettes()
                .execute()
                .body()
                .getScores();
    }

    @Override
    public Collection<GommetteScore> getYearlyGommettesScore(int year) throws IOException {
        return persistenceService
                .gommetteService()
                .gommettesByYear(year)
                .execute()
                .body()
                .getScores();
    }

    @Override
    public Collection<GommetteScore> getUserGommettesScore(int year, String userId) throws IOException {
        return persistenceService
                .gommetteService()
                .gommettesByYearForUser(year, userId)
                .execute()
                .body()
                .getScores();
    }
}
