package be.zqsd.nicobot;

import be.zqsd.nicobot.bot.Nicobot;
import be.zqsd.slack.client.WebsocketClient;
import io.quarkus.runtime.LaunchMode;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.event.Observes;
import org.slf4j.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import static io.quarkus.runtime.LaunchMode.TEST;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * This "main" class starts the bot and registers listeners.
 * To call methods on the Slack API, use {@link Nicobot}
 */
@ApplicationScoped
public class StartBot {
    private static final Logger LOG = getLogger(StartBot.class);

    private final WebsocketClient client;
    private final LaunchMode launchMode;

    @Inject
    StartBot(WebsocketClient client,
             LaunchMode launchMode) {
        this.client = client;
        this.launchMode = launchMode;
    }

    void startup(@Observes StartupEvent event) throws Exception {
        if (launchMode == TEST) {
            LOG.debug("Connection to slack disabled");
        } else {
            client.connect();
        }
    }
}
