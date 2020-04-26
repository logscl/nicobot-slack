package be.zqsd.nicobot.bot.cmd;

import be.zqsd.nicobot.bot.NicoBot;
import be.zqsd.nicobot.bot.utils.Option;
import be.zqsd.nicobot.services.Messages;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;

import static java.time.Duration.between;
import static java.time.LocalDateTime.now;
import static java.time.LocalDateTime.of;

/**
 * Created by Logs on 2020-04-01.
 */
@Service
public class Containment extends NiCommand {

    private static final String COMMAND = "!confinement";
    private static final String FORMAT = "!confinement";
    private static final String DESC = "Reste chez toi !";

    @Autowired
    private Clock clock;

    @Autowired
    private Messages messages;

    @Autowired
    private NicoBot nicobot;

    @Override
    public String getCommandName() {
        return COMMAND;
    }

    @Override
    public String getDescription() {
        return DESC;
    }

    @Override
    public String getFormat() {
        return FORMAT;
    }

    @Override
    protected void doCommand(String command, String[] args, Option opts) {
        LocalDateTime now = now(clock);
        LocalDateTime containmentEnd = of(2020, 5, 18, 0, 0);

        if(containmentEnd.isAfter(now)) {
            // exception
            Duration duration = between(now, containmentEnd);
            nicobot.sendMessage(opts.message, messages.getMessage("contNo", getRemainingTimeStr(duration)));
        } else {
            nicobot.sendMessage(opts.message, messages.getMessage("contYes"));
        }
    }

    private String getRemainingTimeStr(Duration duration) {
        return DurationFormatUtils.formatDuration(duration.toMillis(), "d'j' HH'h' mm'm' ss's'");
    }
}
