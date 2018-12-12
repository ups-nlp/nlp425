package edu.pugetsound.mathcs.nlp.architecture_nlp.features;
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

/**
 * 
 * Contains a main method which takes 2 files, one full of words and their parts of speech, and another with 
 * sentences and their logical parses. Together it creates a new file with new sentences from the word file paired
 * with the logical parses from the other. 
 * REQUIRES A words.txt and test.ccg files to operate. 
 *  
 * @author NathanielLivingston
 */
// pull all code put in spf read in geo file push out fold push request keep verb the same!


public class FoldCreator{ 

	/**
	 * @param args
	 */
	public static void main(String [] args)
	{
		System.out.println(System.getProperty("user.dir"));
		try {
			
			String line = null;
			
			FileReader fileReader = new FileReader("src/edu/pugetsound/mathcs/nlp/architecture_nlp/features/words.txt"); // open the word file
			FileReader fileReader2 = new FileReader("src/edu/pugetsound/mathcs/nlp/architecture_nlp/features/test.ccg"); // open the fold file

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

            
            BufferedWriter writer = new BufferedWriter(new FileWriter("newTest")); // prepare to write to a file
	        
            for(int t = 0; t < atlas.size(); t=t+3) // for each sentence in the ccg data   
	            
	        {
	        	
	    		KBController KB = new KBController("."); // prepare to analyze
	    		TextAnalyzer TA = new TextAnalyzer(KB);
	    		Conversation talk = new Conversation();
	    		
	    		
	        	System.out.println("attempting to analyze sentance-> "+ atlas.get(t));
	        	
	    		Utterance utter = TA.analyze(atlas.get(t), talk); // analyze the quote
	    		talk.addUtterance(utter); // add it to the conversation		
	    		
	    		String[] peices = utter.toString().split("\n"); // take only the useful pieces
	    		String rawText = peices[1].substring(8);
	    		String[] parse = rawText.split("pos");
	    		
	    		
	    		String[] types = new String[parse.length-1]; //types is an array with the word types inside
	    		
	    		int iterator = -1; 
	    		
	    		for(String x : parse)
	    			{
	    				if (iterator!=-1) 
	    					{
	    						types[iterator]=(x.split(",")[0].substring(1)); // get the word types of the given sentence
	    					}
	    				iterator +=1;
	    			}
	        	
	        	
	        	
				List<String>[] outer = new List[types.length]; // create an array of arrayLists
				
				for (int i = 0; i < types.length; i++) {
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
	            
				ArrayList<String> newSentances = new ArrayList<String>(); // prepare for new sentences
				
				// if the first word is a verb, keep it and start new sentences with it
	        	if(types[0].equals("VB") || types[0].equals("VBD") || types[0].equals("VBG") || types[0].equals("VBN") || types[0].equals("VBP") || types[0].equals("VBZ") || types[0].equals("VH") || types[0].equals("VHD") || types[0].equals("VHG") || types[0].equals("VHN") || types[0].equals("VHP") || types[0].equals("VHZ") || types[0].equals("VV") || types[0].equals("VVD") ||types[0].equals("VVG") || types[0].equals("VVN") || types[0].equals("VVP") || types[0].equals("VVZ"))
	        	{
	        		newSentances.add(rawText.split("token=")[1].split(",")[0]);
	        	}
	        	
	        	// otherwise begin new possible sentences with corresponding words of the same type
	        	else {
	        		
					for (String thisWord : outer[0])
					{
						newSentances.add(thisWord);
					}
	        	}
	
				Random rand = new Random();
				
				for (int x = 1; x < types.length; x++) // for each word place 
	            {
	    			ArrayList<String> newList = new ArrayList<String>(); // make a temporary list
	
	    			// if it's a verb then keep it
	            	if(types[x].equals("VB") || types[x].equals("VBD") || types[x].equals("VBG") || types[x].equals("VBN") || types[x].equals("VBP") || types[x].equals("VBZ") || types[x].equals("VH") || types[x].equals("VHD") || types[x].equals("VHG") || types[x].equals("VHN") || types[x].equals("VHP") || types[x].equals("VHZ") || types[x].equals("VV") || types[x].equals("VVD") ||types[x].equals("VVG") || types[x].equals("VVN") || types[x].equals("VVP") || types[x].equals("VVZ"))
	            	{
	            		for(String sentance : newSentances) // for each sentence already created
		            	{
	            			newList.add(sentance + " " + rawText.split("token=")[x+1].split(",")[0]); // add to the sentences
		            	}
	            	}
	    			// if it isn't a verb
	            	else
	            	{
	            		
		            	for(String sentance : newSentances) // for each sentence already created
		            	{
		            		for (int y = 0; y < outer[x].size()/15 ; y++) // for each of the next possible words, taking a random subset
		                	{		            			
		            			int n = rand.nextInt(outer[x].size());
		            			newList.add(sentance + " " + outer[x].get(n)); // put them together 
		                	}
		            		
		            	}
	            	}
	            	newSentances = newList; // assign the new list
	            }
	            
				Collections.shuffle(newSentances); // shuffle for extra randomness
				
				System.out.println("attempting to adding lines " + t + ", " + (t+1) + ", " + (t+2));
					
				if(newSentances.size()!=0) // if it worked
				{
				writer.write(newSentances.get(0)+"\n"); // add to our new fold!
				writer.write(atlas.get(t+1)+"\n");
				writer.write(atlas.get(t+2)+"\n");
				System.out.println("Success!");
				}
				else // otherwise do nothing
				{
					System.out.println("failure.. :(");
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
		
		System.out.println("\nMission complete!");
		
	}	
	
}
