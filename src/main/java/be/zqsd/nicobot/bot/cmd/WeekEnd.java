package be.zqsd.nicobot.bot.cmd;

import be.zqsd.nicobot.bot.NicoBot;
import be.zqsd.nicobot.bot.utils.Option;
import be.zqsd.nicobot.services.Messages;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Duration;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Logs on 28-08-15.
 */
@Service
public class WeekEnd extends NiCommand {

    private static final String COMMAND = "!weekend";
    private static final String FORMAT = "!weekend";
    private static final String DESC = "C'est le weeeeekeeeend ! (Ven. 17h - Lun. 09h)";

    private static final int WEEKEND_START_DAY = DateTimeConstants.FRIDAY;
    private static final int WEEKEND_END_DAY = DateTimeConstants.MONDAY;

    private static final int WEEKEND_START_HOUR = 17;
    private static final int WEEKEND_END_HOUR = 9;

    private static final int HOUR_LIMIT = 48;
    private static final int MIN_LIMIT = 60;

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
        DateTime now = DateTime.now();
        DateTime nextWeekendStart = DateTime.now().withDayOfWeek(WEEKEND_START_DAY).withTime(WEEKEND_START_HOUR, 0, 0, 0);
        DateTime nextWeekendEnd =  DateTime.now().plusWeeks(1).withDayOfWeek(WEEKEND_END_DAY).withTime(WEEKEND_END_HOUR,0,0,0);

        if(nextWeekendStart.isAfter(now)) {
            // exception
            if(now.getDayOfWeek() == DateTimeConstants.MONDAY && now.getHourOfDay() < WEEKEND_END_HOUR) {
                nicobot.sendMessage(opts.message, messages.getMessage("weYesMinutes"));
            } else {
                nicobot.sendMessage(opts.message, buildRemainingTimeMessage(false, now, nextWeekendStart));
            }
        } else {
            nicobot.sendMessage(opts.message,buildRemainingTimeMessage(true, now, nextWeekendEnd));
        }
    }

    private String buildRemainingTimeMessage(boolean yesWeekEnd, DateTime d1, DateTime d2) {
        Duration duration = new Duration(d1,d2);
        if(duration.getStandardHours() > HOUR_LIMIT) {
            if(yesWeekEnd) {
                return messages.getMessage("weYesDays");
            }
            return messages.getMessage("weNoDays", duration.getStandardDays(), getRemainingTimeStr(duration));
        } else if(duration.getStandardMinutes() > MIN_LIMIT) {
            if(yesWeekEnd) {
                return messages.getMessage("weYesHours",duration.getStandardHours());
            }
            return messages.getMessage("weNoHours",duration.getStandardHours(), duration.getStandardHours() > 1 ? "s":"", getRemainingTimeStr(duration));
        } else {
            return messages.getMessage("weNoMinutes",duration.getStandardMinutes(), duration.getStandardMinutes() > 1 ? "s":"", getRemainingTimeStr(duration));
        }
    }

    private String getRemainingTimeStr(Duration duration) {
        PeriodFormatter fmt = new PeriodFormatterBuilder()
                .printZeroNever()
                .appendDays().appendSeparator("j ")
                .minimumPrintedDigits(2)
                .appendHours().appendSeparator("h ")
                .printZeroAlways()
                .appendMinutes().appendLiteral("m")
                .printZeroNever()
                .appendSeparatorIfFieldsAfter(" ").appendSeconds().appendSeparatorIfFieldsBefore("s")
                .toFormatter();

        return fmt.print(duration.toPeriod().normalizedStandard());
    }
}
