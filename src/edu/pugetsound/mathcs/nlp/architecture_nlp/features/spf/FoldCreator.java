package edu.pugetsound.mathcs.nlp.architecture_nlp.features.spf;
import java.io.*;

import java.util.Random;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

import edu.pugetsound.mathcs.nlp.lang.Conversation;
import edu.pugetsound.mathcs.nlp.lang.Utterance;
import edu.pugetsound.mathcs.nlp.kb.KBController;
import edu.pugetsound.mathcs.nlp.architecture_nlp.features.TextAnalyzer;
import edu.pugetsound.mathcs.nlp.util.PathFormat;
import edu.pugetsound.mathcs.nlp.util.Logger;


/**
 * 
 * Contains a main method which takes 2 files, one full of words and their parts of speech, and another with 
 * sentences and their logical parses. Together it creates a new file with new sentences from the word file paired
 * with the logical parses from the other. 
 * REQUIRES A words.txt and test.ccg files to operate. 
 *  
 * @author NathanielLivingston
 */



public class FoldCreator{ 

	private static final int DATA_LOCATION = 1; 
	private static final int BEGINNING_FLUFF=8; 
	/**
	 * @param array the array of word types of a sentence
	 * @param index the index of the word you'd like to check
	 */
	private static boolean checkForVerb(String[] array, int index)
	{
		if (array[index].equals("VB") || array[index].equals("VBD") || array[index].equals("VBG") || array[index].equals("VBN") || array[index].equals("VBP") || array[index].equals("VBZ") || array[index].equals("VH") || array[index].equals("VHD") || array[index].equals("VHG") || array[index].equals("VHN") || array[index].equals("VHP") || array[index].equals("VHZ") || array[index].equals("VV") || array[index].equals("VVD") || array[index].equals("VVG") || array[index].equals("VVN") || array[index].equals("VVP") || array[index].equals("VVZ"))
		{
			return true;
		}
		
		return false;
	}
	
	/**
	 * @param args
	 */
	public static void main(String [] args)
	{
		
		try {
			
			PathFormat pf = new PathFormat();
			String testLoc = pf.absolutePathFromRoot("resources/SpfResources/test.ccg");
			String wordsLoc = pf.absolutePathFromRoot("resources/SpfResources/words.txt");
			
			String line = null;
			
			FileReader fileReader = new FileReader(wordsLoc); // open the word file
			FileReader fileReader2 = new FileReader(testLoc); // open the fold file

			BufferedReader bufferedReader = new BufferedReader(fileReader);
			BufferedReader bufferedReader2 = new BufferedReader(fileReader2);

			
			ArrayList<String> newWords = new ArrayList<String>(); // create arrayLists for both
			ArrayList<String> atlas = new ArrayList<String>();

			
            while((line = bufferedReader.readLine()) != null) // fill the wordlist
            	{
	            	newWords.add(line); 
            	} 
            
            while((line = bufferedReader2.readLine()) != null) // fill the atlas full of the ccg data
	        	{
	            	atlas.add(line);
	        	} 

            
            BufferedWriter writer = new BufferedWriter(new FileWriter("resources\\SpfResources\\fabricatedFold")); // prepare to write to a file
	        
    		KBController KB = new KBController("."); // prepare to analyze
    		TextAnalyzer TA = new TextAnalyzer(KB);
    		Conversation talk = new Conversation();
			Random rand = new Random();
            
            for(int t = 0; t < atlas.size(); t=t+3) // for each sentence in the ccg data   
	            
	        {
	    		
	    			        	
	    		Utterance utter = TA.analyze(atlas.get(t), talk); // analyze the quote
	    		talk.addUtterance(utter); // add it to the conversation		
	    		
	    		String[] pieces = utter.toString().split("\n"); // split the utterance by line
	    		String rawText = pieces[DATA_LOCATION].substring(BEGINNING_FLUFF); // the first line of pieces is the sentence itself, which we don't need. The second line has the POS tags, but it starts with "Tokens: [" so the substring(8) removes that
	    		String[] parse = rawText.split("pos"); // split by the part of speech indicator
	    		
	    		
	    		String[] types = new String[parse.length-1]; //types is an array with the word types inside
	    		
	    		int iterator = -1; 
	    		
	    		for(String x : parse)
    			{
    				if (iterator!=-1) 
    					{
    						types[iterator]=(x.split(",")[0].substring(1)); // get the word types of the given sentence, but remove the comma that starts it
    					}
    				iterator +=1;
    			}
	        	
	        	
	        	
				List<String>[] outer = new List[types.length]; // create an array of arrayLists
				
				for (int i = 0; i < types.length; i++) 
				{
				    outer[i] = new ArrayList<>(); // fill it
				}
				
				int iterator2 = 0;
				
	            for (String x : types) // fill the array of arrayLists with words of the corresponding type in the corresponding arrays
	            {
	            
	        
	            	for (String y : newWords)
	            	{
	            		
	            		if(y.split("\\.")[1].equals(x))
	            		{
	            			outer[iterator2].add(y.split("\\.")[0]);
	            		}
	            	}
	            	iterator2 +=1;
	            	
	            	
	            }
	            
				ArrayList<String> newSentences = new ArrayList<String>(); // prepare for new sentences
				
				// if the first word is a verb, keep it and start new sentences with it
	        	if(checkForVerb(types, 0))
	        	{
	        		newSentences.add(rawText.split("token=")[1].split(",")[0]);
	        	}
	        	
	        	// otherwise begin new possible sentences with corresponding words of the same type
	        	else 
	        	{	
					for (String thisWord : outer[0])
					{
						newSentences.add(thisWord);
					}
	        	}
	

				
				for (int x = 1; x < types.length; x++) // for each word place 
	            {
	    			ArrayList<String> newList = new ArrayList<String>(); // make a temporary list
	
	    			// if it's a verb then keep it
	            	if(checkForVerb(types, x))
	            	{
	            		for(String sentence : newSentences) // for each sentence already created
		            	{
	            			newList.add(sentence + " " + rawText.split("token=")[x+1].split(",")[0]); // add to the sentences
		            	}
	            	}
	    			// if it isn't a verb
	            	else
	            	{
	            		
		            	for(String sentence : newSentences) // for each sentence already created
		            	{
		            		for (int y = 0; y < outer[x].size()/15 ; y++) // for each of the next possible words, taking a random subset that is 1/15 the size of total
		                	{		            			
		            			int n = rand.nextInt(outer[x].size());
		            			newList.add(sentence + " " + outer[x].get(n)); // put them together 
		                	}
		            		
		            	}
	            	}
	            	newSentences = newList; // assign the new list
	            }
	            
				Collections.shuffle(newSentences); // shuffle for extra randomness
				
					
				if(newSentences.size()!=0) // if it worked
				{
				writer.write(newSentences.get(0)+"\n"); // add to our new fold
				writer.write(atlas.get(t+1)+"\n");
				writer.write(atlas.get(t+2)+"\n");
				}
				
				else
				{
					if(Logger.debug())
					{
						System.out.println("Unable to analyze the sentence succesfully, no action taken towards the new fold.");
					}
				}
				
	        }
            
            // close it all up
            bufferedReader.close();
            bufferedReader2.close();
            writer.close();
		} 
		
		catch (FileNotFoundException e) {
			// catch block
			e.printStackTrace();
		} catch (IOException e) {
			// catch block
			e.printStackTrace();
		}
		
		
	}	
	
}
