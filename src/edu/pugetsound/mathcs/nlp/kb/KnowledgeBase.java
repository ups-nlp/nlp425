package edu.pugetsound.mathcs.nlp.kb;


/**
 * Notes to self: For Tweety, this is going to need to store internally a signature that contains all constants,
 * sorts, and predicates. How big can a signature be before it slows down inference notably? 
 * 
 * For untyped predicates, pass an empty string as the sort(s).
 * @author alchambers
 *
 */
public interface KnowledgeBase {
	/**
	 * Creates a constant and adds it to the knowledge base. A constant is represented as
	 * a 0-ary predicate.	
	 * 
	 * @param constant A string containing the name of the constant to be added
	 * @param sort The type of the constant
	 * @return A 0-ary predicate representing the constant
	 */
	public MyPredicate makeConstant(String constant, String sort);
		
	/**
	 * Creates a binary predicate and adds it to the knowledge base
	 * @return A binary predicate
	 */
	public MyPredicate makeBinaryPredicate(String name, String sort1, String sort2);
	
	/**
	 * Creates a unary predicate and adds it to the knowledge base
	 * @return A unary predicate
	 */
	public MyPredicate makeUnaryPredicate(String name, String sort);
	
	/**
	 * Gets a constant, or returns null if no constant with that name exists
	 * @param name the name of the constant to retrieve
	 * @return the constant if it exists, or null otherwise
	 */
	public MyPredicate getConstant(String name);

	/**
	 * Gets a predicate, or returns null if no predicate with that name exists
	 * @param name the name of the predicate to retrieve
	 * @return the predicate if it exists, or null otherwise
	 */
	public MyPredicate getPredicate(String name);
	
	/**
	 * 
	 * @param formula the formula to assert
	 * @return True if the formula was successfully asserted in the knowledge base
	 */
	public boolean assertFormula(String formula);
	
	/**
	 * Queries the knowledge base to determine the truth value of the given formula
	 * @param formula A string representing a formula in first-order logic
	 * @return True if the formula evaluates to true under the knowledge base, false otherwise
	 */
	public boolean query(String formula);
	
	/**
	 * Loads a knowledge base from a file
	 * @param filepath the path to the file containing the knowledge base
	 * @return true if knowledge base is successfully loaded
	 */
	public boolean loadKnowledgeBase(String filepath);
	
	/**
	 * Saves a knowledge base to a file
	 * @param path and name of file to write to
	 * @return true if knowledge base is successfully saved
	 */
	public boolean saveKnowledgeBase(String filepath);

}
