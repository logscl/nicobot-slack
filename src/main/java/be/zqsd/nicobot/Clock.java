package be.zqsd.nicobot;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.ZoneId;

@ApplicationScoped
public class Clock {

    private final ZoneId timezone;

    @Inject
    public Clock(@ConfigProperty(name = "nicobot.timezone.name") String timezoneName) {
        this.timezone = ZoneId.of(timezoneName);
    }

    public LocalDateTime now() {
        return LocalDateTime.now(timezone);
    }
}
