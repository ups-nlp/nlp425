package edu.pugetsound.mathcs.nlp.assignments;
import java.util.Scanner;
import java.util.List;
import edu.pugetsound.mathcs.nlp.lang.Conversation;
import edu.pugetsound.mathcs.nlp.lang.Utterance;
import edu.pugetsound.mathcs.nlp.architecture_nlp.features.stanford.StanfordSuite;
/**
* IanTextAnalyzer
* Class that continuously prompts the user to input text and gives an analysis of the conversation so far
**/
public class IanTextAnalyzer{
	public static void main(String[] args){
		Conversation conversation = new Conversation();
		StanfordSuite suite = new StanfordSuite();
		boolean quit = false;
		Scanner sc = new Scanner(System.in);
		String input= "";
		System.out.println("Welcome to Ian's text analyzer.");
		//continuously prompt user to type a sentence
		while(!quit){
			System.out.println("Type something to see it analyzed, type \"quit\" to quit.");
			input = sc.nextLine();
			if(input.equals("quit")){
				System.out.println("bye!");
				break;
			} else {
				Utterance utterance = new Utterance(input);
				suite.analyze(input, utterance);
				conversation.addUtterance(utterance);
			}
			for(Utterance utter : conversation.getConversation()) {
				System.out.println(utter + "\n");
			}
		}
	}
}


