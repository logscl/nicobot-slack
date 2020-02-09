package com.st.nicobot.bot.cmd;

import com.st.nicobot.bot.utils.Option;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author Julien
 *
 */
public abstract class NiCommand {

	/** Maillon suivant de la chaine */
	protected NiCommand nextCommand;
	
	/**
	 * Retourne la chaine de caractere à partir de laquelle nico doit etre commandé
	 */
	public abstract String getCommandName();

	/**
	 * Retourne une description pour la commande
	 * @return
	 */
	public abstract String getDescription();
	
	/**
	 * Retourne le format de la commande
	 */
	public abstract String getFormat();

	/**
	 * Retourne les alias de la commande
	 */
	public List<String> getAliases() {
		return Collections.emptyList();
	}
	

	/**
	 * Code à executer par la commande
	 * @param command
	 * 		La commande
	 * @param args
	 * 		Les arguments de la commande (peut etre null)
	 * @param opts
	 *		Les options {@link Option} 
	 */
	protected abstract void doCommand(String command, String[] args, Option opts);
	
	/**
	 * Ajoute un nouveau maillon à la chaine
	 * @param niCommand
	 */
	public void setNext(NiCommand niCommand){
		nextCommand = niCommand;
	}
	
	/**
	 * <p>Determine si l'implementation est capable de gérer la commande.</p>
	 * <p>Si oui, alors {@link NiCommand#doCommand(String, String[], Option)} est appelé.</p>
	 * <p>Si non, alors on passe le relais au maillon suivant ({@link NiCommand#nextCommand})</p>
	 * @param command
	 * 		La commande "brute" seule
	 * @param arguments
	 * 		Les arguments de la commande (sans le nom du chan, ni la commande)
	 * @param opts
	 * 		Les options {@link Option}
	 */
	public boolean handle(String command, String[] arguments, Option opts) {
		boolean handled = false;
		
		if (command.toLowerCase().equals(getCommandName().toLowerCase()) || getAliases().contains(command.toLowerCase())){
			this.doCommand(command, arguments, opts);
			return true;
		}

		if (nextCommand != null) {
			handled |= nextCommand.handle(command, arguments, opts);
		}

		return handled;
	}
	
	/** Regex pour trouver une chaine de caractere encadrée par " " */
	private static final Pattern REGEX_FIND_STRING = Pattern.compile("\"([^\"]*)\"");
	private static final String REPlACE_VALUE = "inputString";
	
	public static String[] getArgs(String arguments) {
		Matcher matcher = REGEX_FIND_STRING.matcher(arguments);
		
		List<String> inputStrings = new ArrayList<>();
		int j = 0, k = 0;
		if(matcher.find()) {
			inputStrings.add(matcher.group());
			j++;
			while (matcher.find()) {
				inputStrings.add(matcher.group());
				j++;
			}
			arguments = matcher.replaceAll(" "+REPlACE_VALUE);
		}
		
		String[] explodedArgs = arguments.trim().split(" +");
		boolean continueProcess = true;

		for (int i = 0; i < explodedArgs.length && continueProcess; i++) {
			if (explodedArgs[i].equals(REPlACE_VALUE)) {
				
				// on vire le " en debut et en fin de chaine
				explodedArgs[i] = StringUtils.substring(inputStrings.get(k), 1, -1);
				k++;
				if (j == k) {
					continueProcess = false;
				}
			}
		}
		
		return explodedArgs;
		
	}
}
