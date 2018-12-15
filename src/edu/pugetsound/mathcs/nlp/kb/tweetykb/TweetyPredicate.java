package edu.pugetsound.mathcs.nlp.kb.tweetykb;

import java.util.List;

import edu.pugetsound.mathcs.nlp.kb.MyPredicate;

import net.sf.tweety.logics.commons.syntax.Constant;
import net.sf.tweety.logics.commons.syntax.Predicate;
import net.sf.tweety.logics.commons.syntax.Sort;

/**
 * Implements the MyPredicate interface using the Tweety library.
 * NOTE: A TweetyPredicate can be a constant (0-ary) or a predicate (n-ary)
 * depending on which constructor is used. Once this is decided,
 * a constant cannot become a predicate and vice-versa
 * @author jjenks
 * @version 12/03/18
 */
public class TweetyPredicate implements MyPredicate {
	private Predicate predicate;
	private Constant  constant;
	private boolean   isAConstant;

	/**
	 * Constructor for TweetyPredicate which
	 * creates a constant. aka a 0-ary predicate
	 * @param name the name of the constant
	 * @param sort the sort (type) of the constant
	 */
	public TweetyPredicate(String name, Sort sort) {
		this.constant = new Constant(name, sort);
		this.isAConstant = true;
	}
	
	/**
	 * Constructor for TweetyPredicate which
	 * creates an n-ary predicate for n > 0
	 * @param name the name of the predicate
	 * @param arguments the Sorts of the arguments
	 */
	public TweetyPredicate(String name, List<Sort> arguments) throws IllegalArgumentException {
		if (arguments.size() < 1) {
			throw new IllegalArgumentException(
					"A non-constant predicate must take at least one argument."
					+ "\nPlease ensure that your List is non-empty."
			);
		}
		this.predicate = new Predicate(name, arguments);
		this.isAConstant = false;
	}
	
	/**
	 * Gets the name of this constant or predicate
	 * @return the name of the constant or predicate
	 */
	@Override
	public String getName() {
		if (this.isAConstant) {
			return this.constant.toString();
		} else {
			return this.predicate.getName();
		}
	}
	
	/**
	 * Sets the name of the constant or predicate
	 * @param name the new name of the constant or predicate
	 */
	@Override
	public void setName(String name) {
		if (this.isAConstant) {
			this.constant.set(name);
		} else {
			this.predicate.setName(name);
		}
	}
	
	/**
	 * Gets the arity of the constant(0) or predicate
	 * @return the arity of the constant or predicate
	 */
	@Override
	public int getArity() {
		if (this.isAConstant) {
			return 0;
		} else {
			return this.predicate.getArity();
		}
	}
	
	public String toString() {
		if (this.isAConstant) {
			return this.constant.toString();
		} else {
			return this.predicate.toString();	
		}
	}
	
	@Override
	public void addArgument(String arg, int index) throws IllegalArgumentException {
		System.out.println("Please use the alternative addArgument method");	
		if (this.isAConstant) {
			throw new IllegalStateException("Cannot add arguments to constant.");
		}
	}

	/**
	 * Adds an argument to a predicate
	 * @param arg the new argument as a Sort
	 * 
	 * @throws IllegalStateException if this predicate is a constant
	 */
	public void addArgument(Sort arg) {
		if (this.isAConstant) {
			throw new IllegalStateException("Cannot add arguments to constant.");
		} else {
			this.predicate.addArgumentType(arg);
		}
	}

	
	/**
	 * Gets an argument for a predicate at a specified index
	 * @param index the index of the argument to retrieve
	 * 
	 * @return string representing the argument at the given index
	 * 
	 * @throws IllegalStateException if the TweetyPredicate is a constant
	 * @throws IllegalArgumentException if index < 0 or index >= arity
	 */
	@Override
	public String getArgument(int index) throws IllegalArgumentException {
		if (this.isAConstant) {
			throw new IllegalStateException("cannot get argument of constants.");
		} else if (index < 0 || index > this.predicate.getArity()-1) {
			throw new IllegalArgumentException("index must be less than arity and greater than 0");
		} else {
			return predicate.getArgumentTypes().get(index).toString();
		}	
	}
	
	/**
	 * Gets all arguments for a predicate
	 * @return an array of strings representing all arguments
	 * 
	 * @throws IllegalStateException if TweetyPredicate is a constant
	 */
	@Override
	public String[] getArguments() {
		if (this.isAConstant) {
			throw new IllegalStateException("cannot get arguments of constants");
		} else {
			List<Sort> args = predicate.getArgumentTypes();
			String[] argsToStrings = new String[args.size()];
			for (int i = 0; i<argsToStrings.length; i++) {
				argsToStrings[i] = args.get(i).toString();
			}
			return argsToStrings;
		}
	}
	
}