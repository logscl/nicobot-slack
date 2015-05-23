package com.st.nicobot.bot.cmd;

import com.st.nicobot.bot.NicoBot;
import com.st.nicobot.bot.utils.Option;
import com.st.nicobot.services.Commands;
import com.st.nicobot.services.Messages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Julien
 *
 */
@Service
public class Help extends NiCommand {

	private static final String COMMAND = "help";
	private static final String FORMAT = "help [commandName]";
	private static final String DESC = "Retourne la liste des commandes disponibles OU " +
			"une aide detaillée pour la commande passée en paramètre.";

	@Autowired
	private Messages messages;
	
	@Autowired
	private Commands commandsChain;

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
		HelpArguments arguments = new HelpArguments(args);
		
		if (arguments.commandName == null) {
			sendCommandList(nicobot, opts);
		}
		else {
			sendCommandHelp(nicobot, opts, arguments.commandName);
		}
		
	}	
	
	/**
	 * Envoie la liste de toutes les commandes + description 
	 * @param nicobot
	 * @param opts
	 */
	private void sendCommandList(NicoBot nicobot, Option opts) {
		NiCommand cmd = commandsChain.getFirstLink();
		
		nicobot.sendMessage(opts.message, messages.getOtherMessage("helpHeader"));
		
		while (cmd != null) {
			nicobot.sendMessage(opts.message, "    - " + cmd.getCommandName() + " : " + cmd.getDescription());
			cmd = cmd.nextCommand;
		}
	}
	
	/**
	 * Envoie la description de la commande + format
	 * @param nicobot
	 * @param opts
	 * @param commandName
	 */
	private void sendCommandHelp(NicoBot nicobot, Option opts, String commandName) {
		NiCommand cmd = commandsChain.getFirstLink();
		
		while(cmd != null && !cmd.getCommandName().equals(commandName)) {
			cmd = cmd.nextCommand;
		}
		
		if (cmd != null) {
			nicobot.sendMessage(opts.message, cmd.getDescription());
			nicobot.sendMessage(opts.message, cmd.getFormat());
		} else {
			nicobot.sendMessage(opts.message, messages.getOtherMessage("helpNotFound"));
		}
	}
	
	private class HelpArguments {
		private final String commandName;
		
		public HelpArguments(String[] args) {
			
			if (args != null && args.length != 0) {
				commandName = args[0];
			}
			else {
				commandName = null;
			}
		}
	}

}
