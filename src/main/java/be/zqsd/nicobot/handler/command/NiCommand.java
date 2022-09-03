package be.zqsd.nicobot.handler.command;

import com.slack.api.model.event.MessageEvent;

import java.util.Collection;
import java.util.List;

public abstract class NiCommand {
	
	/**
	 * Any of these strings will trigger the command
	 */
	public abstract Collection<String> getCommandNames();

	/**
	 * Description help for the command
	 */
	public abstract String getDescription();
	
	/**
	 * Formatting help for the command
	 */
	public abstract String getFormat();

	/**
	 * Overide this method with the expected code of the command
	 */
	protected abstract void doCommand(String command, Collection<String> arguments, MessageEvent triggeringMessage);

	/**
	 * Handle the command from the triggering message
	 */
	public void handle(List<String> commandAndArguments, MessageEvent triggeringEvent) {
		this.doCommand(commandAndArguments.get(0), commandAndArguments.subList(1, commandAndArguments.size()), triggeringEvent);
	}
}
