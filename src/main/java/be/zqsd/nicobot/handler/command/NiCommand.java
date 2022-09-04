package be.zqsd.nicobot.handler.command;

import com.slack.api.model.event.MessageEvent;

import java.util.Collection;
import java.util.List;

public interface NiCommand {
	
	/**
	 * Any of these strings will trigger the command
	 */
	Collection<String> getCommandNames();

	/**
	 * Description help for the command
	 */
	String getDescription();
	
	/**
	 * Formatting help for the command
	 */
	String getFormat();

	/**
	 * Overide this method with the expected code of the command
	 */
	void doCommand(String command, Collection<String> arguments, MessageEvent triggeringMessage);

	/**
	 * Handle the command from the triggering message
	 */
	default void handle(List<String> commandAndArguments, MessageEvent triggeringEvent) {
		this.doCommand(commandAndArguments.get(0), commandAndArguments.subList(1, commandAndArguments.size()), triggeringEvent);
	}
}
