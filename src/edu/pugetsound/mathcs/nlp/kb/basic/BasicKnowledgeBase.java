package edu.pugetsound.mathcs.nlp.kb.basic;

import java.util.HashMap;

import edu.pugetsound.mathcs.nlp.kb.KnowledgeBase;
import edu.pugetsound.mathcs.nlp.kb.MyPredicate;

public class BasicKnowledgeBase implements KnowledgeBase {
	private HashMap<String, MyPredicate> kb;
	
	public BasicKnowledgeBase() {
		kb = new HashMap<String, MyPredicate>();
	}

	@Override
	public MyPredicate makeConstant(String constant, String sort) {
		return makePredicate(0,constant);
	}

	@Override
	public MyPredicate makeBinaryPredicate(String name, String sort1, String sort2) {
		return makePredicate(2,name);
	}

	@Override
	public MyPredicate makeUnaryPredicate(String name, String sort) {
		return makePredicate(1,name);
	}

	private MyPredicate makePredicate(int arity, String name) {
		MyPredicate pred = new BasicPredicate(arity);
		pred.setName(name);
		kb.put(name, pred);
		return pred;
	}
	
	@Override
	public MyPredicate getConstant(String name) {
		return kb.get(name);
	}

	@Override
	public MyPredicate getPredicate(String name) {
		return kb.get(name);
	}
	
	@Override
	public boolean assertFormula(String formula) {
		return false;
	}
	
	@Override
	public boolean query(String formula) {
		for(MyPredicate pred : kb.values()) {
			if(pred.toString().equals(formula)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean loadKnowledgeBase(String filepath) {
		return false;
	}
	
	@Override
	public boolean saveKnowledgeBase(String filepath) {
		return false;
	}
}
