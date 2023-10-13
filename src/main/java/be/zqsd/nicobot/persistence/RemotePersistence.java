package be.zqsd.nicobot.persistence;

import io.quarkus.arc.profile.IfBuildProfile;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wtf.logs.nicobot.client.NicobotPersistence;
import wtf.logs.nicobot.gommette.Gommette;
import wtf.logs.nicobot.gommette.GommetteRequest;
import wtf.logs.nicobot.gommette.GommetteScore;
import wtf.logs.nicobot.hgt.HgtRequest;
import wtf.logs.nicobot.hgt.HgtScore;
import wtf.logs.nicobot.message.Message;
import wtf.logs.nicobot.message.MessageRequest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.Collection;

import static java.time.LocalDateTime.now;
import static java.util.Collections.singletonList;

@ApplicationScoped
@IfBuildProfile("prod")
public class RemotePersistence implements Persistence {

    private static final Logger LOG = LoggerFactory.getLogger(RemotePersistence.class);

    private final NicobotPersistence persistenceService;

    @Inject
    RemotePersistence(@ConfigProperty(name = "nicobot.persistence.api.uri") String apiUri,
                      @ConfigProperty(name = "nicobot.persistence.api.key") String apiKey) {
        persistenceService = NicobotPersistence.create(apiUri, apiKey);
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
