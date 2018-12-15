package edu.pugetsound.mathcs.nlp.kb;

/**
 * This interface represents a predicate in first-order logic. 

 * @author alchambers and jjenks
 * @version 12/3/18
 *
 */
public interface MyPredicate {
	/**
	 * Returns the name of the predicate
	 * @return The name of the predicate
	 */
	public String getName();

	/**
	 * Sets the name of the predicate
	 * @param The name of the predicate
	 */
	public void setName(String name);
	
	/**
	 * Returns the arity of the predicate
	 * @return zero for a constant or a positive integer for a predicate 
	 */
	public int getArity();

	/**
	 * Returns a string representation of the predicate
	 */
	public String toString();

	/**
	 * Add an argument to the predicate
	 * @param arg Predicate argument
	 * @param index Position of the argument in the predicate
	 * @throws IllegalArgumentException if an illegal index is passed in
	 * 
	 * @deprecated
	 */
	@Deprecated
	public void addArgument(String arg, int index) throws IllegalArgumentException;
	
	/**
	 * Returns the specified argument of the predicate
	 * 
	 * @return The specified argument
	 * @throws IllegalStateException Calling method on a term
	 * @throws IllegalArgumentException Invalid index
	 */
	public String getArgument(int index) throws IllegalStateException, IllegalArgumentException;
	
	
	/**
	 * Returns entire argument list
	 * @return list of arguments for predicate or null if term
	 */
	public String[] getArguments();
	

}


