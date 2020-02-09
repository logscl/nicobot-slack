package com.st.nicobot.bot.cmd;

import com.st.nicobot.bot.NicoBot;
import com.st.nicobot.bot.utils.Option;
import com.ullink.slack.simpleslackapi.SlackChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Say extends NiCommand {
	
	private static final String COMMAND = "!say";
	private static final String FORMAT = "!say <channel> \"<message>\"";
	private static final String DESC = "Fait parler le bot. ADMIN ONLY :D";

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
		try {
			if(opts.message.getSender().isAdmin()) {
				SayArguments arguments = new SayArguments(args);
				String message = arguments.message;

				nicobot.sendMessage(arguments.channel, null, message);
			}
		}
		catch (IllegalArgumentException ex) {
			nicobot.sendPrivateMessage(opts.message, "Malformed command, format : " + getFormat());
		}
	}
	
	private class SayArguments {
		public final SlackChannel channel;
		public final String message;
		
		public SayArguments(String[] arguments) throws IllegalArgumentException {

			if (arguments == null || arguments.length < 2) {	
				throw new IllegalArgumentException(); 
			}
			
			channel = nicobot.getSession().findChannelByName(arguments[0]);
			message = arguments[1];
		}
	}

}
