package be.zqsd.nicobot.bot.cmd;

import be.zqsd.nicobot.bot.NicoBot;
import be.zqsd.nicobot.bot.utils.Option;
import be.zqsd.nicobot.services.Messages;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjuster;

import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.Duration.between;
import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoField.*;

/**
 * Created by Logs on 28-08-15.
 */
@Service
public class WeekEnd extends NiCommand {

    private static final String COMMAND = "!weekend";
    private static final String FORMAT = "!weekend";
    private static final String DESC = "C'est le weeeeekeeeend ! (Ven. 17h - Lun. 09h)";

    private static final DayOfWeek WEEKEND_START_DAY = FRIDAY;
    private static final DayOfWeek WEEKEND_END_DAY = MONDAY;

    private static final int WEEKEND_START_HOUR = 17;
    private static final int WEEKEND_END_HOUR = 9;

    private static final int HOUR_LIMIT = 48;
    private static final int MIN_LIMIT = 60;

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
        LocalDateTime nextWeekendStart = now(clock).with(weekendStart());
        LocalDateTime nextWeekendEnd =  now(clock).plusWeeks(1).with(weekendEnd());

        if(nextWeekendStart.isAfter(now)) {
            // exception
            if(now.getDayOfWeek() == MONDAY && now.getHour() < WEEKEND_END_HOUR) {
                nicobot.sendMessage(opts.message, messages.getMessage("weYesMinutes"));
            } else {
                nicobot.sendMessage(opts.message, buildRemainingTimeMessage(false, now, nextWeekendStart));
            }
        } else {
            nicobot.sendMessage(opts.message,buildRemainingTimeMessage(true, now, nextWeekendEnd));
        }
    }

    private TemporalAdjuster weekendStart() {
        return (temporal) ->
                temporal
                        .with(DAY_OF_WEEK, WEEKEND_START_DAY.getValue())
                        .with(HOUR_OF_DAY, WEEKEND_START_HOUR)
                        .with(MINUTE_OF_HOUR, 0)
                        .with(SECOND_OF_MINUTE, 0)
                        .with(MILLI_OF_SECOND, 0);
    }

    private TemporalAdjuster weekendEnd() {
        return (temporal) ->
                temporal
                        .with(DAY_OF_WEEK, WEEKEND_END_DAY.getValue())
                        .with(HOUR_OF_DAY, WEEKEND_END_HOUR)
                        .with(MINUTE_OF_HOUR, 0)
                        .with(SECOND_OF_MINUTE, 0)
                        .with(MILLI_OF_SECOND, 0);
    }

    private String buildRemainingTimeMessage(boolean yesWeekEnd, LocalDateTime d1, LocalDateTime d2) {
        Duration duration = between(d1, d2);
        if(duration.toHours() > HOUR_LIMIT) {
            if(yesWeekEnd) {
                return messages.getMessage("weYesDays");
            }
            return messages.getMessage("weNoDays", duration.toDays(), getRemainingTimeStr(duration));
        } else if(duration.toMinutes() > MIN_LIMIT) {
            if(yesWeekEnd) {
                return messages.getMessage("weYesHours",duration.toHours());
            }
            return messages.getMessage("weNoHours",duration.toHours(), duration.toHours() > 1 ? "s":"", getRemainingTimeStr(duration));
        } else {
            return messages.getMessage("weNoMinutes",duration.toMinutes(), duration.toMinutes() > 1 ? "s":"", getRemainingTimeStr(duration));
        }
    }

    private String getRemainingTimeStr(Duration duration) {
        return DurationFormatUtils.formatDuration(duration.toMillis(), "d'j' HH'h' mm'm' ss's'");
    }
}
