package edu.pugetsound.mathcs.nlp.architecture_nlp.features;

import edu.pugetsound.mathcs.nlp.kb.KBController;
import edu.pugetsound.mathcs.nlp.lang.Conversation;
import edu.pugetsound.mathcs.nlp.lang.Utterance;
/*
 * Wrapper for the TextAnalyzer Class. Use for any SPF 
 * related analysis.
 * 
 * @Author Brian Dague
 * @Version 1.0
 */

public class GeoAnalyzer {
	
	private TextAnalyzer analyzer;
	private KBController knowledge;
	
	/*
	 * Constructor for the GeoAnalyzer class
	 * @param directory Directory for this class to store a knowledge base.
	 */
	public GeoAnalyzer(String directory) {
		this.knowledge = new KBController(directory);
		this.analyzer = new TextAnalyzer(knowledge);
	};
	
	/*
	 * Special analyze method for all SPF related activites. 
	 * @param sentence The string to be analyzed.
	 * @returns Utterance object, containing the parse tree.
	 */
	public Utterance geoAnalyze(String sentence) {
		
		Utterance parsed = this.analyzer.analyze(sentence, new Conversation());
		
		return parsed;
	}
}
