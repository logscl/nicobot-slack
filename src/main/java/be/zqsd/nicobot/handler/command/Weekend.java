package be.zqsd.nicobot.handler.command;

import be.zqsd.nicobot.bot.Nicobot;
import com.slack.api.model.event.MessageEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjuster;
import java.util.Collection;

import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.Duration.between;
import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoField.*;
import static java.util.Collections.singletonList;

@ApplicationScoped
public class Weekend implements NiCommand {

    private final Nicobot nicobot;
    private final Clock clock;

    private static final DayOfWeek WEEKEND_START_DAY = FRIDAY;
    private static final DayOfWeek WEEKEND_END_DAY = MONDAY;

    private static final int WEEKEND_START_HOUR = 17;
    private static final int WEEKEND_END_HOUR = 9;

    private static final int HOUR_LIMIT = 48;
    private static final int MIN_LIMIT = 60;

    @Inject
    public Weekend(Nicobot nicobot, Clock clock) {
        this.nicobot = nicobot;
        this.clock = clock;
    }

    @Override
    public Collection<String> getCommandNames() {
        return singletonList("!weekend");
    }

    @Override
    public String getDescription() {
        return "C'est le weeeeekeeeend ! (Ven. 17h - Lun. 09h)";
    }

    @Override
    public String getFormat() {
        return "!weekend";
    }

    @Override
    public void doCommand(String command, Collection<String> arguments, MessageEvent triggeringMessage) {
        LocalDateTime now = now(clock);
        LocalDateTime nextWeekendStart = now(clock).with(weekendStart());
        LocalDateTime nextWeekendEnd =  now(clock).plusWeeks(1).with(weekendEnd());

        if(nextWeekendStart.isAfter(now)) {
            // exception
            if(now.getDayOfWeek() == MONDAY && now.getHour() < WEEKEND_END_HOUR) {
                nicobot.sendMessage(triggeringMessage, "Oui, mais c'est bientôt terminé...");
            } else {
                nicobot.sendMessage(triggeringMessage, buildRemainingTimeMessage(false, now, nextWeekendStart));
            }
        } else {
            nicobot.sendMessage(triggeringMessage, buildRemainingTimeMessage(true, now, nextWeekendEnd));
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
                return "C'est le WEEEEKEEEEEND \\o/ !";
            } else {
                return "Non, encore %d jours :( (%s)".formatted(duration.toDays(), getRemainingTimeStr(duration));
            }
        } else if(duration.toMinutes() > MIN_LIMIT) {
            if(yesWeekEnd) {
                return "Oui \\o/ Pour encore %d heures !".formatted(duration.toHours());
            } else {
                return "C'est pour bientôt ! Encore %d heure%s ! (%s)".formatted(duration.toHours(), duration.toHours() > 1 ? "s":"", getRemainingTimeStr(duration));
            }
        } else {
            return "Ouvre les bières ! C'est dans %d minute%s ! (%s)".formatted(duration.toMinutes(), duration.toMinutes() > 1 ? "s":"", getRemainingTimeStr(duration));
        }
    }

    private String getRemainingTimeStr(Duration duration) {
        return "%sj %sh %sm %ss".formatted(
                duration.toDaysPart(),
                duration.toHoursPart(),
                duration.toMinutesPart(),
                duration.toSecondsPart());
    }
}
