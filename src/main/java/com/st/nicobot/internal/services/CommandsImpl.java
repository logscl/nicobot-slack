package com.st.nicobot.internal.services;

import com.st.nicobot.bot.cmd.NiCommand;
import com.st.nicobot.services.Commands;
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
}
