package edu.pugetsound.mathcs.nlp.kb.tweetykb.tests;

import edu.pugetsound.mathcs.nlp.kb.KnowledgeBase;
import edu.pugetsound.mathcs.nlp.kb.tweetykb.TweetyKnowledgeBase;

/**
 * 
 * @author jjenks
 * @version 12/03/18
 *
 */
public class TweetyKnowledgeBaseTest {
	public static void main(String[] args) {
		System.out.println("Tests for TweetyKnowledgeBase.java");
		KnowledgeBase kb = new TweetyKnowledgeBase();
		
		kb.makeConstant("me", "Person");
		kb.makeConstant("brother", "Person");
		kb.makeConstant("sister", "Person");
		kb.makeConstant("mom", "Person");
		kb.makeConstant("dad", "Person");
		
		kb.makeBinaryPredicate("parentOf", "Person", "Person");
		kb.makeBinaryPredicate("spouseOf", "Person", "Person");
		kb.makeBinaryPredicate("siblingOf", "Person", "Person");
		
		kb.assertFormula("parentOf(mom,me)");
		kb.assertFormula("spouseOf(dad,mom)");
		kb.assertFormula("siblingOf(sister,me)");
		kb.assertFormula("forall X: (forall Y: (spouseOf(X,Y) => spouseOf(Y,X)))");
		kb.assertFormula("forall X: (forall Y: (siblingOf(X,Y) => siblingOf(Y,X)))");
		// not necessarily true, but just as an example
		kb.assertFormula("forall X: (forall Y: (forall Z: ((spouseOf(X,Y) && parentOf(Y,Z)) => parentOf(X,Z))))");
		
		System.out.println(kb);
		
// 		SUCCESS!
//		returns true
//		but extremely, extremely slow
//		System.out.println("parentOf(dad,me)? > "+kb.query("parentOf(dad,me)"));
		
// 		SUCCESS!
//		System.out.println("attempting to save to a new file");
//		kb.saveKnowledgeBase("src/edu/pugetsound/mathcs/nlp/kb/tweetykb/tests/saveKBtest.fologic");
	
// 		SUCCESS!
//		System.out.println("attempting to overwrite an existing file");
//		kb.saveKnowledgeBase("src/edu/pugetsound/mathcs/nlp/kb/tweetykb/tests/saveKBtest.fologic");
		

// 		SUCCESS!
// 		loadKnowledgeBase does overwrite the existing knowledge base
		String beliefBaseFilepath = "src/edu/pugetsound/mathcs/nlp/kb/tweetykb/tests/examplebeliefbase.fologic";
		boolean loadKBWasSuccessful = kb.loadKnowledgeBase(beliefBaseFilepath);
		
		
		if (loadKBWasSuccessful) {
			System.out.println("knowledge base was loaded successfully");
			System.out.println(kb);
			
			System.out.println();
		
		} else {
			System.out.println("knowledge base was NOT loaded successfully");
		}
	}
}