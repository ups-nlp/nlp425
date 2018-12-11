/**
 * 
 */
package nlp425;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.Test;

import edu.pugetsound.mathcs.nlp.lang.Utterance;
import edu.pugetsound.nlp.geoanalyzer.GeoAnalyzer;

/**
 * @author brian
 *
 */
public class geo_analyzer {
	
	
	@Test
	public void EnsureAnalyzeReturnsUtterance() {
		GeoAnalyzer geo_test = new GeoAnalyzer(".");
		assert(geo_test.geoAnalyze("This is a sentance") instanceof Utterance);
	}
	
	@Test
	public void TestAnalysisForExceptions() {
		GeoAnalyzer geo_test = new GeoAnalyzer(".");
		String sentence1 = "The girl kicked the ball";
		String sentence2 = "The whale slept";
		String sentence3 = "I like jell-o";
		assert(geo_test.geoAnalyze(sentence1) instanceof Utterance);
		assert(geo_test.geoAnalyze(sentence2) instanceof Utterance);
		assert(geo_test.geoAnalyze(sentence3) instanceof Utterance);
	}
}
