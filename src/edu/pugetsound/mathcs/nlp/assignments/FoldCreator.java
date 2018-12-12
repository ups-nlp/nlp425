package edu.pugetsound.mathcs.nlp.assignments;

import java.util.List;
import java.util.Scanner; 
import edu.pugetsound.mathcs.nlp.lang.Conversation;
import edu.pugetsound.mathcs.nlp.lang.Utterance;
import edu.pugetsound.mathcs.nlp.architecture_nlp.features.TextAnalyzer;

/**
 * The text analyzer for assignment 5
 * @author NathanielLivingston
 */
public class NathanielTextAnalyzer{

	/**
	 * Main method that analyzes input strings and displays information about them. 
	 * @param args
	 */
	public static void main(String [] args)
	{
		
		TextAnalyzer TA = new TextAnalyzer(null);
		Conversation talk = new Conversation();
		Scanner scan = new Scanner(System.in);

		
		
		boolean proceed = true;
		
		while(proceed) // start the loop
		{
			System.out.println("Type text (or q to quit)");
			String quote = scan.nextLine(); // get input
			if (quote.equals("q")) // check if user wants to exit
			{
				proceed = false; // if so end loop
			}
			else
			{
				Utterance utter = TA.analyze(quote, talk); // analyze the quote
				talk.addUtterance(utter); // add it to the conversation
			}
				
			
		}
		
		System.out.println("Here is an analysis of what you typed:");
		List<Utterance> sentenceList = talk.getConversation(); // get the list of utterances
		for (Utterance n : sentenceList) // for each
		{
			System.out.println(n); // print the info
			System.out.println("");

		}
		

	}	
		
}
