package edu.pugetsound.mathcs.nlp.kb.tweetykb;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import java.lang.StringBuilder;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;

import edu.pugetsound.mathcs.nlp.kb.KnowledgeBase;
import edu.pugetsound.mathcs.nlp.kb.MyPredicate;
import edu.pugetsound.mathcs.nlp.kb.tweetykb.TweetyPredicate;

import net.sf.tweety.commons.ParserException;

import net.sf.tweety.logics.commons.syntax.Constant;
import net.sf.tweety.logics.commons.syntax.Predicate;
import net.sf.tweety.logics.commons.syntax.Sort;

import net.sf.tweety.logics.fol.parser.FolParser;
import net.sf.tweety.logics.fol.reasoner.FolReasoner;
import net.sf.tweety.logics.fol.reasoner.NaiveFolReasoner;
import net.sf.tweety.logics.fol.syntax.FolBeliefSet;
import net.sf.tweety.logics.fol.syntax.FolFormula;
import net.sf.tweety.logics.fol.syntax.FolSignature;

/**
 * Implementation of the KnowledgeBase interface using the Tweety library
 * @author jjenks
 * @version 12/03/18
 */
public class TweetyKnowledgeBase implements KnowledgeBase {
	private FolSignature signature;
	private FolBeliefSet beliefSet;
	private Map<String, MyPredicate> allPredicates;
	private FolParser parser;
	private FolReasoner prover;

	/**
	 * Constructor for the Tweety knowledge base
	 */
	public TweetyKnowledgeBase() {
		// the boolean passed to the FOLSignature constructor simply means we want to
		// include the equality predicate "==" by default
		this.signature = new FolSignature(true);
		this.beliefSet = new FolBeliefSet();
		this.allPredicates = new HashMap<String, MyPredicate>();
		this.parser = new FolParser();
		this.parser.setSignature(signature);
		
		FolReasoner.setDefaultReasoner(new NaiveFolReasoner());
		this.prover = FolReasoner.getDefaultReasoner();
	}

	/**
	 * Creates a new TweetyPredicate
	 * @param name the name of the constant
	 * @param sort the sort of the constant. Pass an empty string for default sort THING
	 * 
	 * @return a 0-ary TweetyPredicate
	 * 
	 * @throws IllegalArgumentException if the given name is an empty string
	 * @throws IllegalStateException if a constant or predicate with the given name already exists
	 */
	@Override
	public MyPredicate makeConstant(String name, String sort) throws IllegalArgumentException {
		if (name.equals("")) {
			throw new IllegalArgumentException("Constant must be given a name. "
					+ "You passed an empty string");
		} else if (this.signature.containsConstant(name)) {
			throw new IllegalStateException("Cannot make constant " + name
					+ ". A constant with this name already exists");
		} else if (this.signature.containsPredicate(name)) {
			throw new IllegalStateException("Cannot make constant " + name
					+ ". A predicate with this name already exists");
		}else {
			// create constant, add to signature, and return
			Sort constantSort = this.createRetrieveOrGetDefaultSort(sort, true);
			
			Constant signatureConstant = new Constant(name, constantSort);
			this.signature.add(signatureConstant);
			
			MyPredicate newConstant = new TweetyPredicate(name, constantSort);
			this.allPredicates.put(name, newConstant);

			return newConstant;
		}
	}

	/**
	 * @param name the name of the binary predicate
	 * @param sort1 the sort of the first argument. Pass an empty string for default sort ANY
	 * @param sort2 the sort of the second argument. Pass an empty string for default sort ANY
	 * 
	 * @return a binary TweetyPredicate
	 * 
	 * @throws IllegalArgumentException if the given name is an empty string
	 * @throws IllegalStateException if a constant or predicate with the given name already exists
	 */
	@Override
	public MyPredicate makeBinaryPredicate(String name, String sort1, String sort2) throws IllegalArgumentException {
		if (name.equals("")) {
			throw new IllegalArgumentException("Binary predicates must be given a name. "
					+ "You passed an empty string");
		} else if (this.signature.containsConstant(name)) {
			throw new IllegalStateException("Cannot make binary predicate " + name
					+ ". A constant with this name already exists");
		} else if (this.signature.containsPredicate(name)) {
			throw new IllegalStateException("Cannot make binary predicate " + name
					+ ". A predicate with this name already exists");
		}else {
			
			// create constant, add to signature, and return
			Sort binaryPredicateArg1Sort = this.createRetrieveOrGetDefaultSort(sort1, false);
			Sort binaryPredicateArg2Sort = this.createRetrieveOrGetDefaultSort(sort2, false);
			
			List<Sort> binaryPredicateArguments = new ArrayList<Sort>(2);
			binaryPredicateArguments.add(binaryPredicateArg1Sort);
			binaryPredicateArguments.add(binaryPredicateArg2Sort);

			Predicate signatureBinaryPredicate = new Predicate(name, binaryPredicateArguments);
			this.signature.add(signatureBinaryPredicate);

			MyPredicate newBinaryPredicate = new TweetyPredicate(name, binaryPredicateArguments);
			this.allPredicates.put(name, newBinaryPredicate);
			
			return newBinaryPredicate;
		}
	}

	/**
	 * @param name the name of the unary predicate
	 * @param sort the sort of the argument. Pass an empty string for default sort ANY
	 * 
	 * @return a unary TweetyPredicate
	 * 
	 * @throws IllegalArgumentException if the given name is an empty string
	 * @throws IllegalStateException if a constant or predicate with the given name already exists
	 */
	@Override
	public MyPredicate makeUnaryPredicate(String name, String sort) throws IllegalArgumentException{
		if (name.equals("")) {
			throw new IllegalArgumentException("Unary predicates must be given a name. "
					+ "You passed an empty string");
		} else if (this.signature.containsConstant(name)) {
			throw new IllegalStateException("Cannot make unary predicate " + name
					+ ". A constant with this name already exists");
		} else if (this.signature.containsPredicate(name)) {
			throw new IllegalStateException("Cannot make unary predicate " + name
					+ ". A predicate with this name already exists");
		}else {
			Sort unaryPredicateArgSort = this.createRetrieveOrGetDefaultSort(sort, false);
			
			List<Sort> unaryPredicateArgument = new ArrayList<Sort>(1);
			unaryPredicateArgument.add(unaryPredicateArgSort);
			
			Predicate signatureUnaryPredicate = new Predicate(name, unaryPredicateArgument);
			this.signature.add(signatureUnaryPredicate);
			
			MyPredicate newUnaryPredicate = new TweetyPredicate(name, unaryPredicateArgument);
			this.allPredicates.put(name, newUnaryPredicate);
			
			return newUnaryPredicate;
		}
	}
	
	@Override
	public MyPredicate getConstant(String name) {
		if (this.signature.containsConstant(name)) {
			return this.allPredicates.get(name);
		} else {
			return null;
		}
	}
	
	@Override
	public MyPredicate getPredicate(String name) {
		if (this.signature.containsPredicate(name)) {
			return this.allPredicates.get(name);
		} else {
			return null;
		}
	}
	
	/**
	 * Asserts the given formula in the knowledge base
	 * assumes that the formula is in a format that can be parsed by Tweety
	 * 
	 * @param formula the formula to assert
	 * 
	 * @return true if the the formula was successfully asserted in the knowledge base
	 */
	@Override
	public boolean assertFormula(String formula) {
		try {
			this.beliefSet.add((FolFormula)this.parser.parseFormula(formula));
			return true;
		} catch(IOException e) {
			System.err.printf("Encountered error when asserting formula -> %s\n", formula);
		} catch(ParserException e) {
			System.err.printf("Encountered error when parsing formula -> %s\n", formula);
			System.err.println(e);
		}
		return false;
	}
	
	/**
	 * Queries the knowledge base.
	 * Assumes that the formula is in a format that can be parsed by Tweety.
	 * 
	 * NOTE: this currently uses the built-in "NaiveFolReasoner" which is quite slow
	 * In the future, should add functionality to use an external reasoner
	 * see "net.sf.tweety.logics.fol.reasoner" in the documentation
	 * here http://tweetyproject.org/api/1.11/index.html
	 * 
	 * @param formula the formula to query
	 * 
	 * @return the result of the query, or false if there was an error
	 */
	@Override
	public boolean query(String formula) {
		try {
			return this.prover.query(this.beliefSet, (FolFormula)parser.parseFormula(formula));
		} catch(IOException e) {
			System.err.printf("Encountered error when querying formula -> %s\n", formula);
		} catch(ParserException e) {
			System.err.printf("Encountered error while parsing formula -> %s\n", formula);
			System.err.println(e);
		}
		return false;
	}
	
	/**
	 * Loads a knowledge base, or a Tweety belief base from a given file
	 * Note: this will overwrite the existing belief set,
	 * so this should be called before asserting any formulae
	 * 
	 * @param filepath the file to load a knowledge base from
	 * 
	 * @return true if the knowledge base was successfully loaded from the given file
	 */
	@Override
	public boolean loadKnowledgeBase(String filepath) {
		try{
			this.beliefSet = parser.parseBeliefBaseFromFile(filepath);
			return true;
		} catch (FileNotFoundException e){
			System.err.printf("could not find file -> %s\n", filepath);
		} catch (IOException e) {
			System.err.printf("Encountered IO errors for file -> %s\n", filepath);
		} catch (ParserException e) {
			System.err.printf("Encountered an error "
					+ "while parsing a formula in file -> %s\n", filepath);
			System.err.println(e);
		}
		return false;
	}
	
	/**
	 * Saves the knowledge base to a new or existing file
	 * 
	 * @param filepath the name of the file to create or overwrite
	 * 
	 * @return true if the knowledge base was successfully saved to the given file
	 */
	@Override
	public boolean saveKnowledgeBase(String filepath) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(filepath));
			writer.write(this.formatForFile());
			writer.close();
			return true;
		} catch (IOException e) {
			System.err.println("Encountered error when saving knowledge base to file");
		}
		return false;
	}
	
	/**
	 * A private method for formatting the belief base and signature for writing to a file
	 * according to the grammar described in the Tweety documentation
	 * see "net.sf.tweety.logics.fol.parser"
	 * http://tweetyproject.org/api/1.11/index.html
	 * 
	 * @return a properly formatted String
	 */
	private String formatForFile() {
		StringBuilder output = new StringBuilder();
		
		// currently this does not seem very memory efficient
		HashMap<String, List<String>> sortToConsts = new HashMap<String, List<String>>();
		for (Constant c : this.signature.getConstants()) {
			String sort = c.getSort().toString();
			if (!sortToConsts.containsKey(sort)) {
				sortToConsts.put(sort, new ArrayList<String>());
			}
			sortToConsts.get(sort).add(c.toString());
		}

		// sort declarations
		for (Map.Entry<String, List<String>> e : sortToConsts.entrySet()) {
			output.append(e.getKey() + "={");
			List<String> consts = e.getValue();
			for (int i = 0; i<consts.size(); i++) {
				output.append(consts.get(i) + (i == consts.size()-1 ? "" : ", "));
			}
			output.append("}\n");
		}
		output.append("\n");
		
		// predicate declarations
		for (Predicate p : this.signature.getPredicates()) {
			output.append("type("+p.toString()+")\n");
		}
		output.append("\n");
		
		// formulas
		Iterator<FolFormula> bsIterator = this.beliefSet.iterator();
		while (bsIterator.hasNext()) {
			output.append(bsIterator.next().toString());
			output.append("\n");
		}
		
		return output.toString();
	}
	
	public String toString() {
		StringBuilder output = new StringBuilder();
		Iterator<FolFormula> bsIterator = this.beliefSet.iterator();
		output.append("belief set\n");
		while (bsIterator.hasNext()) {
			output.append("\t");
			output.append(bsIterator.next().toString());
			output.append("\n");
		}
		output.append("\n");
		
		output.append("signature\n\tsorts\n\t\t");
		output.append(this.signature.getSorts().toString());
		
		output.append("\n\tconstants\n\t\t");
		output.append(this.signature.getConstants().toString());

		output.append("\n\tpredicates\n\t\t");
		output.append(this.signature.getPredicates().toString());

//		the NaiveFolReasoner does NOT allow functors
//		hopefully this can be changed if an external theorem prover is used
		
//		output.append("\n\tfunctors\n\t\t");
//		output.append(this.signature.getFunctors().toString());
//		output.append("\n");
		
		return output.toString();
	}
	
	/**
	 * A private method to decide a sort
	 * If the given sort is an empty string,
	 * return default sort, otherwise retrieve or create and add to signature
	 * 
	 * @param sort the sort to create or retrieve.
	 * @param forConstant if true, returns the default Sort.THING, otherwise Sort.ANY
	 * 
	 * @return the correct Sort
	 */
	private Sort createRetrieveOrGetDefaultSort(String sort, boolean forConstant) {
		// get default Sort
		if (sort.equals("")) {
			return forConstant ? Sort.THING : Sort.ANY;
		} else {
			// retrieve
			if (this.signature.containsSort(sort)) {
				return this.signature.getSort(sort);
			// create
			} else {
				Sort createSort = new Sort(sort);
				this.signature.add(createSort);
				return createSort;
			}
		}
	}
}