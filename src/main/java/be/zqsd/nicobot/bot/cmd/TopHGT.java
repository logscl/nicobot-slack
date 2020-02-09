package be.zqsd.nicobot.bot.cmd;

import be.zqsd.nicobot.bot.NicoBot;
import be.zqsd.nicobot.bot.utils.Option;
import be.zqsd.nicobot.services.HappyGeekTimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Logs on 21-07-15.
 */
@Service
public class TopHGT extends NiCommand {

    private static final String COMMAND = "!topHGT";
    private static final String FORMAT = "!topHGT";
    private static final String DESC = "Donne le top score au HGT";
    private static final String[] ALIASES = {"!hgt"};

    @Autowired
    private NicoBot nicobot;

    @Autowired
    private HappyGeekTimeService hgtService;

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
    public List<String> getAliases() {
        return Arrays.asList(ALIASES);
    }

    @Override
    protected void doCommand(String command, String[] args, Option opts) {
        nicobot.sendMessage(opts.message, hgtService.getAllTimeTopUsers(opts.message.getChannel().getId()));
    }
}
