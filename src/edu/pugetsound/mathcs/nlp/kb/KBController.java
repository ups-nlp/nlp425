package edu.pugetsound.mathcs.nlp.kb;

import edu.pugetsound.mathcs.nlp.kb.tweetykb.TweetyKnowledgeBase;

/**
 * Manages access to all knowledge bases
 * 
 * In the future, this class should log statistics about how many hits/misses we've
 * gotten from the current knowledge base, control which knowledge bases to query (perhaps
 * have a hierarchical set of knowledge bases) and shift the current knowledge base depending
 * upon the focus of the conversation (and the hits/misses)
 *  
 * @author alchambers
 */
public class KBController{
	public static final String ISA = "isA";
	public static final String AGENT = "agent";
	public static final String THEME = "theme";
	public static final String EVENT = "event";
	public static final String ENTITY = "entity";
	public static final String POSSESSION = "possessedBy";	
	public static final String PROPERTY = "property";
	
	private KnowledgeBase current;
	private String dir;
	private int varCounter;

	/**
	 * Constructs a knowledge base controller. All knowledge bases will be read from and 
	 * written to the given directory.
	 * 
	 * @param The directory where knowledge bases are to be written/read
	 */
	public KBController(String dir){
		this.dir = dir;
		varCounter = 0;
		current = new TweetyKnowledgeBase();
	}

	/**
	 * Creates and returns a variable with a unique name. All variables
	 * are of the format "XNN" where "NN" is a unique number.
	 * 
	 * NOTE: Should we kick variables back to the KnowledgeBase Interface?
	 */
	public String makeVariable() {
		String var = "X" + varCounter;
		varCounter++;
		return var;
	}

	/**
	 * Creates a constant and adds it to the knowledge base
	 * 
	 * @param constant A string containing the name of the constant to be added
	 * @param sort The sort (type) of the constant. Pass an empty string for default sort
	 * @return True if the constant is successfully added, false otherwise
	 */
	public MyPredicate makeConstant(String constant, String sort) {
		return current.makeConstant(constant, sort);
	}

	/**
	 * Creates a binary predicate and adds it to the knowledge base
	 * @param name the name of the binary predicate
	 * @param sort1 the sort (type) of the first argument. Pass an empty string for default sort
	 * @param sort2 the sort (type) of the second argument. Pass an empty string for default sort
	 * @return A binary predicate
	 */
	public MyPredicate makeBinaryPredicate(String name, String sort1, String sort2) {
		MyPredicate predicate = current.makeBinaryPredicate(name, sort1, sort2);
		return predicate;
	}

	/**
	 * Creates a unary predicate and adds it to the knowledge base
	 * @param name the name of the unary predicate
	 * @param sort the sort (type) of the argument. Pass an empty string for default sort
	 * @return A unary predicate
	 */
	public MyPredicate makeUnaryPredicate(String name, String sort) {
		MyPredicate predicate = current.makeUnaryPredicate(name, sort);
		return predicate;
	}

	/**
	 * Checks if the entity exists in the current knowledge base. All entities are
	 * represented as constants.
	 * 
	 * @param name The name of the entity to be checked
	 * @return The constant if it exists, null otherwise
	 * 
	 */
	public MyPredicate constantExists(String name) {
		return current.getConstant(name);
	}
	
	/**
	 * Queries the knowledge base to determine the truth value of the given formula
	 * @param formula A string representing a formula in first-order logic
	 * @return True if the formula evaluates to true under the knowledge base, false otherwise
	 */
	public boolean query(String formula) {
		return current.query(formula);
	}

	/**
	 * Determines the truth value of the predicate in the current context 
	 * @param predicate A predicate
	 * @return True if the predicate evaluates to true, false otherwise
	 */
	public boolean query(MyPredicate predicate){  
		return current.query(predicate.toString());
	}

	/**
	 * Asserts a given formula in the current knowledge base
	 * @param formula the formula to assert
	 * 
	 * @return true if formula was successfully asserted
	 */
	public boolean assertFormula(String formula) {
		return current.assertFormula(formula);
	}
	
	/**
	 * Loads a knowledge base into the current knowledge base from a file
	 * @param filename the name of the file to load
	 * NOTE: the filename is prepended with the path passed to the constructor
	 * @return true if the knowledge base was successfully loaded
	 */
	public boolean loadKnowledgeBase(String filename) {
		return current.loadKnowledgeBase(this.dir + filename);
	}
	
	/**
	 * Saves the current knowledge base to a given filename
	 * @param filename the name of the file to create or overwrite
	 * NOTE: the filename is prepended with the path passed to the constructor
	 * @return true if the knowledge base was successfully saved
	 */
	public boolean saveKnowledgeBase(String filename) {
		return current.saveKnowledgeBase(this.dir + filename);
	}

}
