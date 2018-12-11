/**
 * 
 */
package nlp425;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.Test;

import edu.pugetsound.mathcs.nlp.architecture_nlp.features.GeoAnalyzer;
import edu.pugetsound.mathcs.nlp.lang.Utterance;

/**
 * @author brian
 *
 */
public class GeoAnalyzerTest {
	
	
	@Test
	public void EnsureAnalyzeReturnsUtterance() {
		GeoAnalyzer geo_test = new GeoAnalyzer(".");
		assert(geo_test.geoAnalyze("This is a sentence") instanceof Utterance);
		String sentence1 = "The girl kicked the ball";
		String sentence2 = "The whale slept";
		String sentence3 = "I like jell-o";
		String sentence4 = "Texas is a state.";
		assert(geo_test.geoAnalyze(sentence1) instanceof Utterance);
		assert(geo_test.geoAnalyze(sentence2) instanceof Utterance);
		assert(geo_test.geoAnalyze(sentence3) instanceof Utterance);
		assert(geo_test.geoAnalyze(sentence4) instanceof Utterance);
	}
}
