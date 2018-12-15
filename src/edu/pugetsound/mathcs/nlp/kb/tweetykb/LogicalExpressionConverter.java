package edu.pugetsound.mathcs.nlp.kb.tweetykb;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.io.File;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.lang.StringBuilder;
import java.util.Arrays;

import edu.cornell.cs.nlp.spf.mr.lambda.LogicalExpression;
import edu.cornell.cs.nlp.spf.base.string.IStringFilter;
import edu.cornell.cs.nlp.spf.base.string.StubStringFilter;
import edu.cornell.cs.nlp.spf.data.sentence.Sentence;
import edu.cornell.cs.nlp.spf.mr.lambda.Lambda;
import edu.cornell.cs.nlp.spf.mr.lambda.Literal;
import edu.cornell.cs.nlp.spf.mr.lambda.Term;
import edu.cornell.cs.nlp.spf.mr.lambda.Variable;
import edu.cornell.cs.nlp.spf.mr.language.type.TypeRepository;
import edu.cornell.cs.nlp.spf.mr.language.type.Type;
import edu.cornell.cs.nlp.spf.parser.IDerivation;
import edu.cornell.cs.nlp.spf.mr.lambda.LogicalConstant;

import edu.cornell.cs.nlp.spf.base.string.IStringFilter;
import edu.cornell.cs.nlp.spf.base.string.StubStringFilter;
import edu.cornell.cs.nlp.spf.ccg.lexicon.LexicalEntry;
import edu.cornell.cs.nlp.spf.data.sentence.Sentence;
import edu.cornell.cs.nlp.spf.mr.lambda.LogicalExpression;
import edu.cornell.cs.nlp.spf.parser.IDerivation;
import edu.pugetsound.mathcs.nlp.architecture_nlp.features.spf.GenerateInteractor;
import edu.pugetsound.mathcs.nlp.architecture_nlp.features.spf.Interactor;
import edu.pugetsound.mathcs.nlp.architecture_nlp.features.SemanticAnalyzer;
import edu.pugetsound.mathcs.nlp.lang.Conversation;
import edu.pugetsound.mathcs.nlp.lang.Utterance;

/**
 * Converts a LogicalExpression from the Cornell Semantic Parsing Framework (SPF) to a String
 * which can be interpreted by Tweety
 * 
 * currently does NOT work on all cases
 * 
 * Right now there are issues with pointers and modifying elements of an ArrayList
 * while iterating over it. Both
 * 		(Object x : myList) {
 * 			modifyX(x);
 * 		}
 * and 
 * 		modifyX(myList.get(0));
 * 
 * will NOT work because the pointers to objects are not affected, as I had assumed.
 * 
 * 
 * eventually this should be converted from combinatory logic (lambda calculus with no free variables)
 * directly into a first-order logic formula with classes from the Tweety library,
 * instead of a String or the Node class here.
 * However this depends on whether this is even theoretically possible...
 * 
 * The basic approach is to repeatedly "beta-reduce" the given expression 
 * until everything is at most a first-order expression
 * then do some other processing to get rid of all lambda expressions
 * by converting them into existentially quantified statements
 * 
 * In pseudocode:
 * 
 * Input: An Object S representing the input sentence
 * while S contains lambda expressions {
 * 		for each lambda expression L not containing any other lambda expression {
 * 			if possible, try to remove the lambda expression
 * 			otherwise return an error
 * 		}
 * }
 * @author jjenks
 * @version 12/14/18
 */
public class LogicalExpressionConverter {
	private static final int MAX_DEPTH = 30;
	
	private static final boolean DEBUG = false;
	private static final boolean DEBUG_SPF_TRAVERSAL = false;
	
	private static final boolean DEBUG_MEASURE_TIME = true;
	
	private static final boolean DEBUG_CONVERSION = false;
	// DEBUG_CONVERSION must be true for any of the following flags to work
	// the suffices refer to the methods debug statements will come from
	private static final boolean DEBUG_CONVERSION_APPLY = false;
	private static final boolean DEBUG_CONVERSION_DUPLICATE = false;
	private static final boolean DEBUG_CONVERSION_REDUCE = false;
	// similarly DEBUG_CONVERSION_REDUCE must be true for DEBUG_CONVERSION_REDUCE_VERBOSE to work
	private static final boolean DEBUG_CONVERSION_REDUCE_VERBOSE = false;

	
	/**
	 * A private Node class for storing relevant parse-tree information in a tree structure
	 * @author jjenks
	 * @version 12/14/18
	 *
	 */
	private static class Node {
		public static final byte ROOT = -1;
		public static final byte LAMBDA = 0;
		public static final byte VARIABLE = 1;
		public static final byte PREDICATE = 2;
		public static final byte TERM = 3;
		public static final byte LOGICAL_CONSTANT = 4;
		
		private static final Map<String, String> infixMap = new HashMap<>();
		static {
			infixMap.put("and", "&&");
			infixMap.put("or", "||");
			infixMap.put("<", "<");
			infixMap.put(">", ">");
			infixMap.put("<=", "<=");
			infixMap.put(">=", ">=");
			infixMap.put("=", "==");
		}
		
		private String value;
		private byte type;
		private Node parent;
		private ArrayList<Node> children;
		private boolean isQuantifier;
		private boolean isQuantifierVariable;
		private boolean isInfixPredicate;
		private boolean isLeafNode;
		private byte hasLambdaDescendants;
		
		public Node(String expression, byte type) {
			this.value = expression;
			this.type = type;
			if (this.type == Node.LAMBDA) {
				this.isQuantifier = true;
			} else {
				this.isQuantifier = false;
			}
			// this.depth = 0;
			this.isQuantifierVariable = false;
			this.isInfixPredicate = Node.infixMap.containsKey(this.value);
			this.isLeafNode = true;
			this.hasLambdaDescendants = -1;
		}
		
		public void setAsQuantifier() {
			this.isQuantifier = true;
		}
		
		public void setAsQuantifierVariable() {
			this.isQuantifierVariable = true;
		}
		
		public void setParent(Node p) {
			this.parent = p;
		}
		
		public Node getParent() {
			return this.parent;
		}
		
		public boolean isLambda() {
			return this.type == Node.LAMBDA;
		}
		
		public void addChild(Node child) {
			if (this.children == null) {
				this.children = new ArrayList<>();
				this.isLeafNode = false;
			}
			this.children.add(child);
			// child.depth = this.depth+1;
		}
		
		public Node getChild(int index) {
			if (this.children == null) {
				return null;
			} else {
				return this.children.get(index);
			}
		}
		
		public void replaceChild(int index, Node replacement) {
			if (this.hasChildren()) {
				this.children.remove(index);
				this.children.add(index, replacement);
			}
		}
		
		public void replaceChild(Node old, Node replacement) {
			if (this.hasChildren()) {
				int index = 0;
				for (Node child : this.children) {
					if (child.equals(old)) {
						this.children.remove(index);
						this.children.add(index, replacement);
						return;
					}
					index++;
				}
			}
		}
		
		public int whichChild(Node c) {
			if (this.hasChildren()) {
				int count = 0;
				for (Node child : this.children) {
					if (child.equals(c)) {
						return count;
					}
				count++;
				}
			}
			return -1;
		}
		
		public List<Node> getChildren() {
			return this.children;
		}
		
		public boolean isLeafNode() {
			return this.isLeafNode;
		}
		
		public int numLambdaChildren() {
			int numLambdaChildren = 0;
			for (Node child : this.children) {
				if (child.isLambda()) {
					numLambdaChildren++;
				}
			}
			return numLambdaChildren;
		}
		
		public boolean hasChildren() {
			return this.children != null;
		}
		
		public boolean hasLambdaChildren() {
			if (this.children == null) {
				this.hasLambdaDescendants = 0;
				return false;
			}
			for (Node child : this.children) {
				if (child.type == Node.LAMBDA) {
					this.hasLambdaDescendants = 1;
					return true;
				}
			}
			return false;
		}
		
		public boolean hasLambdaDescendants() {
			if (this.hasLambdaDescendants > -1) {
				return this.hasLambdaDescendants == 1;
			}
			if (this.hasLambdaChildren()) {
				this.hasLambdaDescendants = 1;
				return true;
			}
			if (this.hasChildren()) {
				for (Node child : this.children) {
					if (child.hasLambdaDescendants()) {
						this.hasLambdaDescendants = 1;
						return true;
					}
				}
			}
			this.hasLambdaDescendants = 0;
			return false;
		}
		
		public void setAsNoLambdaDescendants() {
			this.hasLambdaDescendants = 0;
		}
		
		public String getValue() {
			return this.value;
		}
		
		public int getType() {
			return this.type;
		}
		
		public String toTreeString(String pad) {
			String output = pad + this.value+"\n";
			
			if (this.children != null) {
				for (Node child : this.children) {
					output += child.toTreeString(pad + " - ");
				}
			}
			return output;
		}
		
		public boolean equals(Node other) {
			return this.value.equals(other.getValue()) && this.type == other.getType();
		}
		
		public boolean valueEquals(String val) {
			return this.value.equals(val);
		}
		
		public Node shallowDuplicate() {
			Node dup = new Node(this.value, this.type);
			dup.isQuantifier = this.isQuantifier;
			dup.isQuantifierVariable = this.isQuantifierVariable;
			dup.isLeafNode = this.isLeafNode;
			dup.hasLambdaDescendants = this.hasLambdaDescendants;
			dup.isInfixPredicate = this.isInfixPredicate;
//			dup.parent = this.parent;
			return dup;
		}
		
		public String toString() {
			if (this.type == Node.ROOT) {
				return this.children.get(0).toString();
			}
			
			StringBuilder output = new StringBuilder();
			
			if (this.isQuantifierVariable) {
				output.append(this.value + ": ");
			} else if (!this.isInfixPredicate) {
				output.append(this.value);
			}
			
			
			if (this.children != null && !this.children.isEmpty()) {
				if (this.isInfixPredicate) {
					int numChildren = this.children.size();
					String predicate = Node.infixMap.get(this.value);
					for (int i = 0; i<numChildren; i++) {
						output.append(this.children.get(i).toString());
						if (i < numChildren-1) {
							output.append(" "+predicate+" ");
						}
					}
					
				} else if (this.isQuantifier) {
					for (Node child : this.children) {
						if (child.isQuantifierVariable) {
							output.append(" " + child.toString());
						} else {
							output.append("("+child.toString()+")");
						}
					}
				} else if (this.type == Node.PREDICATE) {
					int numChildren = this.children.size();
					output.append("(");
					for (int i = 0; i<numChildren; i++) {
						output.append(this.children.get(i).toString());
						if (i < numChildren-1) {
							output.append(", ");
						}
					}
					output.append(")");
				} else {
					for (Node child : this.children) {
						if (child.isQuantifierVariable) {
							output.append(" " + child.toString());
						} else {
							output.append("("+child.toString()+")");
						}
					}
				}
			}
			return output.toString();
		}
	}

	public static void main(String[] args) {
		
//*
		GenerateInteractor translate = new GenerateInteractor();
		Interactor<Sentence,LogicalExpression,Sentence> interactor = translate.generate();
		
		Utterance utt = 
//				new Utterance("does california have the smallest river");
//				new Utterance("does california have rivers");
//				new Utterance("what rivers run through the state with the lowest point in the usa");
//				
//				new Utterance("what is the largest state with a river in it");
//				new Utterance("what is the population of california and the population of california");
//				new Utterance("what states border the state with the lowest population");
				new Utterance("what rivers run through the state with the lowest population");
//				
//				new Utterance("what state is bigger than any other state");
		
		long startTime;
		if (DEBUG_MEASURE_TIME) {
			startTime= System.currentTimeMillis();
		}
		
		
		// similar to the Interactor class
		IStringFilter textFilter = new StubStringFilter();
		final String currentSentence = textFilter.filter(utt.utterance);
		Sentence sentence = new Sentence(currentSentence);
		Sentence dataItem = new Sentence(sentence);
		IDerivation<LogicalExpression> parse = interactor.interact(dataItem);

		long endTime;
		if (DEBUG_MEASURE_TIME) {
			endTime = System.currentTimeMillis();
			System.out.println("SPF parsing took " + (endTime - startTime) + " milliseconds");
		}

		if (parse == null) {
			System.out.println("parse was null");
		} else {	
			System.out.println(parse.getSemantics());
		
			if (DEBUG_MEASURE_TIME) {
				startTime = System.currentTimeMillis();
			}
			
			System.out.println(getConvertedExpression(parse.getSemantics()));
			
			if (DEBUG_MEASURE_TIME) {
				endTime = System.currentTimeMillis();
				System.out.println("Conversion took " + (endTime - startTime) + " milliseconds");
			}
		}

/*
		// It may be possible to test by building up your own lambda expressions, but this has proved a bit tricky


		String dir = System.getProperty("user.dir");
		TypeRepository myTypes = new TypeRepository(new File(dir+"/resources/SpfResources/resources/geo.types"));
		System.out.println(myTypes);
		System.out.println(myTypes.getEntityType());
		Type t0 = myTypes.getEntityType();
		Variable v0 = new Variable(t0);
		
		
		Lambda l = new Lambda(v0, );
		System.out.println(getConvertedExpression(l));
*/
	}
	
	
	/**
	 * gets the converted, Tweety ready string from the LogicalExpression parse output by the SPF library
	 * 
	 * This is the method which DOES NOT WORK
	 * @param parse
	 * @return
	 */
	private static String getConvertedExpression(LogicalExpression parse) {
		// if the expression is not a predicate
		if (!parse.getType().getRange().toString().equals("t")) {
			return "Currently cannot convert to Tweety format : \t->\t"+parse.toString()+"\n\tsentence is Not a predicate";
		} else {
			Node root = new Node("S", Node.ROOT);
			HashSet variables =  new HashSet<Variable>();
			// traverse and copy SPF structure into the private Node class
			recursivelyTraverseExpression(parse, 0, variables, root);
			getLeavesAndParents(root);
			return root.toTreeString("");
		}
	}
	
	/**
	 * prints the attempted reductions of found lambda expressions
	 * @param root
	 */
	private static void getLeavesAndParents(Node root) {
		List<Node> parents = new ArrayList<Node>();
		recursivelyGetLeavesAndParents(root, parents);
		
		System.out.println("printing parents");
		for (Node parent : parents) {
			System.out.println(parent);
		}
		System.out.println("---");
		System.out.println("reduced to");
		System.out.println("---");
		
		reduceParents(parents);
		
		for (Node parent : parents) {
			System.out.println(parent);
		}
		
		System.out.println("done printing parents");
	}
	
	/**
	 * finds "lowest" lambda expressions in tree
	 * aka the lambda expressions which contain no other lambda expressions
	 * @param current
	 * @param leafParents
	 */
	private static void recursivelyGetLeavesAndParents(Node current, List<Node> leafParents) {
		if (!current.hasChildren()) {
			return;
		}
		for (Node c : current.getChildren()) {
			if (c.isLambda() && !c.hasLambdaDescendants()) {
				leafParents.add(current);
				break;
			} else {
				recursivelyGetLeavesAndParents(c, leafParents);
			}
		}
	}
	
	/**
	 * reduces a list of nodes containing the "lowest" lambda expressions.
	 * aka the lambda expressions which contain no other lambda expressions
	 * 
	 * NOTE: this is where the pointer issues lie
	 * @param parents
	 */
	private static void reduceParents(List<Node> parents) {
		for (int i = 0; i<parents.size(); i++) {
			parents.set(i, reduceSubtree(parents.get(i)));
		}
	}
	
	/**
	 * deprecated function application method. See apply method below
	 * @param lambdaExpression
	 * @param variable
	 * @param term
	 * @return
	 */
	private static String apply(String lambdaExpression, String variable, String term) {
		return lambdaExpression.replaceAll(variable, term);
	}
	
	/*
	 * finds all nodes in a subtree with value equal to given variableName
	 * and replaces that node with a copy of the given subtree
	 * 
	 * should be useful for beta-reduction
	 * (lambda x: (f(x)))(bob) => f(bob)
	 * currently seems quite memory inefficient
	 */
	private static Node apply(Node subtreeRoot, String variableName, Node replacementTree) {
		Node subtreeRootCopy = subtreeRoot.shallowDuplicate();//duplicateNode(subtreeRoot);
		
		if (DEBUG_CONVERSION && DEBUG_CONVERSION_APPLY) {
			System.out.println("apply:\t"+subtreeRoot.getValue()+"["+variableName+":="+replacementTree+"]");
		}

		if (subtreeRoot.hasChildren()) {
			for (Node child : subtreeRoot.getChildren()) {
				if (child.valueEquals(variableName)) {
					if (DEBUG_CONVERSION && DEBUG_CONVERSION_APPLY) {
						System.out.println("\tchild = "+variableName+", replacing with "+replacementTree);
					}
					subtreeRootCopy.addChild(duplicateNode(replacementTree));
				} else {
					subtreeRootCopy.addChild(apply(child, variableName, replacementTree));
				}
				
			}
		}
		if (DEBUG_CONVERSION && DEBUG_CONVERSION_APPLY) {
			System.out.println("result of apply on "+subtreeRoot+" => "+subtreeRootCopy);
		}
		return subtreeRootCopy;
	}
	
	private static Node duplicateNode(Node root) {
		if (DEBUG_CONVERSION && DEBUG_CONVERSION_DUPLICATE) {
			System.out.println("duplicating "+root);
		}
		
		Node rootCopy = root.shallowDuplicate();
		if (root.hasChildren()) {
			for (Node child : root.getChildren()) {
				rootCopy.addChild(duplicateNode(child));
			}
		}
		if (DEBUG_CONVERSION && DEBUG_CONVERSION_DUPLICATE) {
			System.out.println("duplicate of "+root+" = ");
			System.out.println(rootCopy);
		}
		return rootCopy;
	}

	// 
	/**
	 * Attempts to remove all lambda expressions in a given subtree
	 * assumes given root has a lambda expression as a direct descendant and
	 * contains no other lambda expressions as descendants
	 * @param subtreeRoot
	 */
	private static Node reduceSubtree(Node subtreeRoot) {
		String nodeValue = subtreeRoot.getValue();
		if (nodeValue.equals("exists") || nodeValue.equals("forall")) {
			Node subtreeRootCopy = duplicateNode(subtreeRoot);
			
			Node lambda = subtreeRoot.getChild(0);
			Node variable = lambda.getChild(0);
			
			
//			subtreeRoot.replaceChild(0, variable);
//			subtreeRoot.setAsQuantifier();
//			subtreeRoot.setAsNoLambdaDescendants();
			
			
			subtreeRootCopy.replaceChild(0, variable);
			subtreeRootCopy.setAsQuantifier();
			subtreeRootCopy.setAsNoLambdaDescendants();
			
			return subtreeRootCopy;
		} else if (nodeValue.equals("argmin") || nodeValue.equals("argmax")) {
/*
			The closest FOL expression I could think of to the argmin / argmax function as described in the paper
			outerPred
			- 	argmin
			-	- lambda
			-	- 	- Xi
			-	-	-	- pred
			-	-	-	-	... - Xi ...
			-	-	lambda
			-	-	- Xj
			-	-	-	- func
			-	-	-	-	... - Xj ...

 			outerPred( ..., argmin (lambda Xi: (pred(... Xi ...)), lambda Xj: (func(... Xj ...))), ...)
			 					||
			 					\/
			exists Y0: (pred(... Y0 ...) && forall Y1: (func(... Y0 ...) <= func(... Y1 ...)) && outerPred(...,Y1,...));


			exists
			- Y0
			-	- and                               <- In SPF and in NOT a binary predicate. I was shocked as well.
			-	-	- pred                             It simply 'ands' all of its children together.
			-	-	-	- ... Y0
			-	-	- forall
			-	-	-	- Y1
			-	-	-	-	<(=)
			-	-	-	-	-	func
			-	-	-	-	-	-	... - Y0 ...
			-	-	-	-	-	func
			-	-	-	-	-	-	... - Y1 ...
			-	- 	- outerPred
			-	-	-	- ... - Y0 ...
*/
			

			boolean isMin = nodeValue.equals("argmin");

			// only instances of "Literal" (from SPF) will have a non-null parent
			Node outerPred = subtreeRoot.getParent();

			Node arg         = subtreeRoot;

			Node lambda1       = subtreeRoot.getChild(0);
			Node xi              = lambda1.getChild(0);
			Node pred              = xi.getChild(0);

			Node lambda2       = subtreeRoot.getChild(1);
			Node xj              = lambda2.getChild(0);
			Node func              = xj.getChild(0);

			
			// need to figure out how to get correct variable count
			// however the scoping should mean that it won't matter
			// unless conversion occurs multiple times,
			// then simply setting as Y0 and Y1 is a problem

			int replacementIndex = outerPred.whichChild(arg);
			
			Node exists = new Node("exists", Node.PREDICATE);
			Node y0       = new Node("Y0", Node.VARIABLE);
			Node and        = new Node("and", Node.PREDICATE);
			Node newPred      = apply(pred, xi.getValue(), y0);
			Node forall       = new Node("forall", Node.PREDICATE);
			Node y1             = new Node("Y1", Node.VARIABLE);
			Node leqOrGreq        = isMin ? new Node("<=", Node.PREDICATE) : new Node(">=", Node.PREDICATE); // is <= valid tweety predicate?
			Node newFunc0           = apply(func, xj.getValue(), y0);
			Node newFunc1           = apply(func, xj.getValue(), y1);
			Node newOuterPred = duplicateNode(outerPred);
			
			// need to duplicate variable y0 or we have a recursion loop 
			newOuterPred.replaceChild(replacementIndex, duplicateNode(y0));

			
			if (DEBUG_CONVERSION_REDUCE) {
				System.out.println("pred => newPred");
				
				System.out.println(pred);
				if (DEBUG_CONVERSION_REDUCE_VERBOSE) {
					System.out.println(pred.toTreeString(""));
				}
				
				System.out.println(newPred);
				if (DEBUG_CONVERSION_REDUCE_VERBOSE) {
					System.out.println(newPred.toTreeString(""));
				}
			
			
				System.out.println();
				
				System.out.println("func => newFunc0");
				
				System.out.println(func);
				if (DEBUG_CONVERSION_REDUCE_VERBOSE) {
					System.out.println(func.toTreeString(""));
				}
				
				System.out.println(newFunc0);
				if (DEBUG_CONVERSION_REDUCE_VERBOSE) {
					System.out.println(newFunc0.toTreeString(""));
				}
				
				System.out.println();
				
				System.out.println("func => newFunc1");
				
				System.out.println(func);
				if (DEBUG_CONVERSION_REDUCE_VERBOSE) {
					System.out.println(func.toTreeString(""));
				}
				
				System.out.println(newFunc1);
				if (DEBUG_CONVERSION_REDUCE_VERBOSE) {
					System.out.println(newFunc1.toTreeString(""));
				}
			}
			
			
			// order matters when adding child nodes
			
			leqOrGreq.addChild(newFunc0);
			leqOrGreq.addChild(newFunc1);
			
			
			if (DEBUG_CONVERSION_REDUCE) {
				System.out.println("leqOrGreq");
				System.out.println(leqOrGreq);
				if (DEBUG_CONVERSION_REDUCE_VERBOSE) {
					System.out.println(leqOrGreq.toTreeString(""));
				}
				
				System.out.println();
			}
			
			y1.addChild(leqOrGreq);
			
			if (DEBUG_CONVERSION_REDUCE) {
				System.out.println("y1");
				System.out.println(y1);
				if (DEBUG_CONVERSION_REDUCE_VERBOSE) {
					System.out.println(y1.toTreeString(""));
				}
			
				System.out.println();
			}
			
			forall.setAsQuantifier();
			y1.setAsQuantifierVariable();
			forall.addChild(y1);
			
			if (DEBUG_CONVERSION_REDUCE) {
				System.out.println("forall");
				System.out.println(forall);
				if (DEBUG_CONVERSION_REDUCE_VERBOSE) {
					System.out.println(forall.toTreeString(""));
				}
			
				System.out.println();
			}
			
			
			and.addChild(newPred);
			and.addChild(forall);
			and.addChild(newOuterPred);
			
			if (DEBUG_CONVERSION_REDUCE) {
				System.out.println("and");
				System.out.println(and);
				if (DEBUG_CONVERSION_REDUCE_VERBOSE) {
					System.out.println(and.toTreeString(""));
				}
			
				System.out.println();
			}
			
			
			y0.addChild(and);
			
			if (DEBUG_CONVERSION_REDUCE) {
				System.out.println("y0");
				System.out.println(y0);
				if (DEBUG_CONVERSION_REDUCE_VERBOSE) {
					System.out.println(y0.toTreeString(""));
				}

				System.out.println();
			}
			
			exists.setAsQuantifier();
			y0.setAsQuantifierVariable();
			exists.addChild(y0);
			
			if (DEBUG_CONVERSION_REDUCE) {
				System.out.println("exists");
				System.out.println(exists);
				if (DEBUG_CONVERSION_REDUCE_VERBOSE) {
					System.out.println(exists.toTreeString(""));
				}
			
		
				System.out.println();
				System.out.println();
			}
			
			subtreeRoot = exists;
			return exists;

		} else {
			System.err.printf("Currently unable to convert %s to Tweety expresssion\n", subtreeRoot.toString());
			return new Node("COULD NOT CONVERT", Node.ROOT);
		}		
	//////////////////////////////
	// eventually we need to do repeated beta-reduction
	// aka function application
	// for example in haskell when we have expressions like
	// (\f -> \x -> f(x)) (\y -> y+y) 3
	// this is interpreted as
	//
	//
	// (\f -> \x -> f(x)) (\y -> y+y) 3
	//		|
	//		v
	// (\x -> (\y -> y+y)(x)) 3
	//		|
	//		v
	// (\x -> (\y -> y+y)(x)) 3
	//		|				|
	//		v 				v
	// (\y -> y+y)(3) or  (\x -> x+x)(3)? not 100% sure but it doesn't matter in this case
	//		|
	//		v
	// 3+3
	//		|
	//		v
	// 6
	
	// however, if we have a function that takes two lambda expressions as arguments
	
	// func(\f -> (\x -> f(x)),(\y -> y+y),3)
	// the current node data structure does not distinguish these cases
	// since `func` would have \f -> (\x -> f(x)), (\y -> y+y), and 3 as children
	// perhaps have a "isArgument" flag in the node class?
	// 
	//		} else {
	//			List<Node> children = subtreeRoot.getChildren();
	//			Node c1, c2;
	//			for (int i = 0; i<children.size()-1; i++) {
	//				c1 = children.get(i);
	//				c2 = children.get(i+1);
	//				if (c1.isLambda() && c2.isLambda()) {
	//					System.out.println(apply(c1.getChild(0).getChild(0).toString(), c1.getChild(0).getValue(), c2.toString()));
	//				}
	//			}
	//		}
	}
	
	/**
	 * Traverses (DFS) a LogicalExpression from the SPF library,
	 * and duplicates the relevant content and structure into a Node object
	 * @param current
	 * @param depth
	 * @param variables
	 * @param currentNode
	 */
	private static void recursivelyTraverseExpression(LogicalExpression current, int depth, HashSet<Variable> variables, Node currentNode) {
		// recursion safe-guard
		if (depth > MAX_DEPTH) {
			System.out.printf("your depth is over %d, returning\n", MAX_DEPTH);
			return;
		}
		
		String pad;
		if (DEBUG_SPF_TRAVERSAL) {
			pad = "";
			for (int i = 0; i<depth; i++) {
				pad += "\t";
			}

			System.out.println(pad + current);
			System.out.println(pad + current.getType());
			System.out.println(pad + "Current node value " + currentNode.value);
		}
		
		if (current instanceof Lambda) {
			
			Lambda lambdaExpression = (Lambda)current;
			variables.add(lambdaExpression.getArgument());
			int counter = getVariableCount(lambdaExpression.getArgument(), variables);
			
			if (DEBUG_SPF_TRAVERSAL) {
				System.out.println(pad + "LAMBDA");
				System.out.println();
				System.out.println(pad + lambdaExpression.getArgument());
			}

			currentNode.addChild(new Node("lambda", Node.LAMBDA));
			currentNode = currentNode.children.get(currentNode.children.size()-1);
			currentNode.addChild(new Node("X" + counter, Node.VARIABLE));
			currentNode = currentNode.children.get(currentNode.children.size()-1);
			currentNode.setAsQuantifierVariable();
			
			recursivelyTraverseExpression(
							lambdaExpression.getBody(),
							depth+1,
							variables,
							currentNode);		
			
		} else if (current instanceof Literal) {

			Literal literal = (Literal)current;
			LogicalExpression[] args = literal.argumentCopy();
			
			String predicateName = literal.getPredicate().toString();
			predicateName = predicateName.substring(0, predicateName.length() - literal.getPredicateType().toString().length()-1);
			
			if (DEBUG_SPF_TRAVERSAL) {
				System.out.println(pad +  "LITERAL");
				System.out.println(pad + "predicate\t->\t" + literal.getPredicate());
				System.out.println();
			}

			currentNode.addChild(new Node(predicateName, Node.PREDICATE));
			Node childNode = currentNode.children.get(currentNode.children.size()-1);
			childNode.setParent(currentNode);
			currentNode = childNode;

			for (LogicalExpression le : args) {
				recursivelyTraverseExpression(le, depth+1, variables, currentNode);
			}

		} else if (current instanceof Term) {

			Term term = (Term)current;

			if (term instanceof LogicalConstant) {
				LogicalConstant l = (LogicalConstant)term;
				currentNode.addChild(new Node(l.getBaseName(), Node.LOGICAL_CONSTANT));

				if (DEBUG_SPF_TRAVERSAL) {
					System.out.println(pad +  "TERM\t->\t"+l.getBaseName()+", LOGICAL_CONSTANT");
				}

			} else if (term instanceof Variable) {
				int count = getVariableCount(term, variables);
				currentNode.addChild(new Node("X"+count, Node.VARIABLE));

				if (DEBUG_SPF_TRAVERSAL) {
					System.out.println(pad +  "TERM\t->\tX"+count+", VARIABLE");
				}
			} else {
				currentNode.addChild(new Node("TERM", Node.TERM));

				if (DEBUG_SPF_TRAVERSAL) {
					System.out.println(pad +  "TERM\t->\tTERM");
				}
			}
		} else {
			System.out.println("not sure how to deal with "+current);
		}
	}

	
	/**
	 * Gets the count for a given variable in a set of variables
	 * used to ensure that no two variables are given the same number while traversing
	 * @param t
	 * @param variables
	 * @return
	 */
	private static int getVariableCount(LogicalExpression t, HashSet<Variable> variables) {
		int counter = 0;
		for (Variable v : variables) {
			if (t.equals(v)) {
				return counter;
			}
			counter++;
		}
		return -1;
	}

	/**
	 * gets the max count for a set of variables
	 * see: getVariableCount
	 * @param variables
	 * @return
	 */
	private static int getMaxVariableCount(HashSet<Variable> variables) {
		return variables.size();
	}
}