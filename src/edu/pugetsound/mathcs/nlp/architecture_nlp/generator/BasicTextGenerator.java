package edu.pugetsound.mathcs.nlp.architecture_nlp.generator;

import edu.pugetsound.mathcs.nlp.architecture_nlp.brain.Action;
import edu.pugetsound.mathcs.nlp.kb.KBController;
import edu.pugetsound.mathcs.nlp.lang.Conversation;
import edu.pugetsound.mathcs.nlp.lang.Utterance;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * This class connects the java codebase to a trained python model with a python script intermediary to
 * create a natural language response from an utterance input.
 *
 * @author kmramos
 * @version 12/3/2018
 */
public class BasicTextGenerator implements Generator {
	/**
	 * Gives the model an utterance and returns the generated response.
	 * 
	 * @param input		an utterance to feed to the model
	 * @return String	a String representation of the response from the model or null if nothing worked
	 * @exception IOException  if error occurs while receiving response
	 */
	public String generateResponse(Conversation conversation, Action action, KBController kb) {
		
		final String OS = System.getProperty("os.name");
		final String input = conversation.getLastUtterance().utterance;
		String output = null;
		String command = "";
		
		//Storing project path on current machine
		Path currentRelativePath = Paths.get("");
		String path = currentRelativePath.toAbsolutePath().toString();	
			 
		//Checking OS to determine which slashes to use in file path to script
		//This file is a test file that only works on my (kmramos) machine right now
		//This will change once I know where the script will be placed in the project file structure
		if (OS.contains("Windows")) {
			command = "python " + "\""+ path + "\\scripts\\generator\\responseGenerator.py" + "\"";
		}
		else {
			command = "python " + "\""+ path + "/scripts/generator/responseGenerator.py" + "\"";
		}

		try {
			//Run the command in the command line with input as a command line argument
			Process process = Runtime.getRuntime().exec(command + " " + "\"" + input + "\"");
				 	
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));

			BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

			// read the output from the command line (should only be reading one line if working correctly)
			while ((output = stdInput.readLine()) != null) {
			    return output;
			}
			            
			// read any errors from the attempted command
			// prints python errors if there are any
			while ((output = stdError.readLine()) != null) {
				System.out.println(output);
			}
			
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		return output;
	}
	
	/**
	 * Used for testing purposes using hard-coded objects
	 * @param args	Not used
	 */
	public static void main(String[] args) {
		//Unused right now
		Action act = null;
		KBController kb = null;
		
		BasicTextGenerator gen = new BasicTextGenerator();
		Conversation convo = new Conversation();
		
		//Hardcode utterances
		convo.addUtterance(new Utterance( "I am an utterance"));
		convo.addUtterance(new Utterance( "Hello, I am talking to you"));
		
		System.out.println(gen.generateResponse(convo, act, kb));
	}
}


