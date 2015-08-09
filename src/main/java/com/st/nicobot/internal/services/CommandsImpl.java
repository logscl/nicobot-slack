package com.st.nicobot.internal.services;

import com.st.nicobot.bot.NicoBot;
import com.st.nicobot.bot.cmd.NiCommand;
import com.st.nicobot.bot.utils.Option;
import com.st.nicobot.services.Commands;
import com.ullink.slack.simpleslackapi.SlackMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Julien
 *
 */
@Service
public class CommandsImpl implements Commands {

	@Autowired
	private ApplicationContext ctx;

	@Autowired
	private NicoBot nicobot;
	
	private NiCommand firstLink;

	public CommandsImpl() {	}

	@PostConstruct
	private void postConstruct() {
		createChain();
	}
	
	@Override
	public NiCommand getFirstLink() {
		return firstLink;
	}
	
	/**
	 * Crée la chaine de commande et retourne le 1er maillon.
	 * @return
	 */
	private NiCommand createChain() {
		List<NiCommand> cmds = ctx.getBeansOfType(NiCommand.class).entrySet().stream().filter(entry -> !entry.getValue().getClass().isAnnotationPresent(Deprecated.class)).map(Map.Entry::getValue).collect(Collectors.toList());

		for (int i = 1; i < cmds.size(); i++) {
			final NiCommand prev = cmds.get(i-1);
			final NiCommand curr = cmds.get(i);
			
			prev.setNext(curr);
		}
		
		firstLink = cmds.get(0);
		
		return firstLink;
	}

	@Override
	public boolean handleCommandEvent(SlackMessage slackMessage) {
		String message = slackMessage.getMessageContent();
		//on extrait <cmd> <reste>
		String[] arguments = message.split(" ");

		boolean handled = false;

		if(arguments.length >= 1) {
			String[] commandArgs = null;

			if(arguments.length > 1) {
				// on extrait de la chaine uniquement la partie contenant les arguments
				String commandsString = message.substring(message.indexOf(arguments[1]));
				commandArgs = NiCommand.getArgs(commandsString);
			}

			handled = this.getFirstLink().handle(arguments[0], commandArgs, new Option(slackMessage));
		} else {
			nicobot.sendMessage(slackMessage, "T'es con ou quoi ? Une commande, c'est \"<commande> [params]\"");
		}

		return handled;
	}
}
