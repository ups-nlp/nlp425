package edu.pugetsound.mathcs.nlp.architecture_nlp.features.spf;

/*******************************************************************************
 * Copyright (C) 2011 - 2015 Yoav Artzi, All rights reserved.
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *******************************************************************************/
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.cornell.cs.nlp.spf.base.hashvector.HashVectorFactory;
import edu.cornell.cs.nlp.spf.base.hashvector.HashVectorFactory.Type;
import edu.cornell.cs.nlp.spf.base.hashvector.KeyArgs;
import edu.cornell.cs.nlp.spf.ccg.categories.syntax.Syntax;
import edu.cornell.cs.nlp.spf.ccg.lexicon.ILexicon;
import edu.cornell.cs.nlp.spf.ccg.lexicon.LexicalEntry;
import edu.cornell.cs.nlp.spf.ccg.lexicon.LexicalEntry.Origin;
import edu.cornell.cs.nlp.spf.ccg.lexicon.Lexicon;
import edu.cornell.cs.nlp.spf.ccg.lexicon.factored.lambda.FactoredLexicalEntry;
import edu.cornell.cs.nlp.spf.ccg.lexicon.factored.lambda.FactoredLexicon;
import edu.cornell.cs.nlp.spf.ccg.lexicon.factored.lambda.FactoringServices;
import edu.cornell.cs.nlp.spf.data.collection.CompositeDataCollection;
import edu.cornell.cs.nlp.spf.data.collection.IDataCollection;
import edu.cornell.cs.nlp.spf.data.sentence.Sentence;
import edu.cornell.cs.nlp.spf.data.sentence.SentenceCollection;
import edu.cornell.cs.nlp.spf.data.sentence.SentenceLengthFilter;
import edu.cornell.cs.nlp.spf.data.singlesentence.SingleSentence;
import edu.cornell.cs.nlp.spf.data.singlesentence.SingleSentenceCollection;
import edu.cornell.cs.nlp.spf.data.utils.LabeledValidator;
import edu.cornell.cs.nlp.spf.genlex.ccg.ILexiconGenerator;
import edu.cornell.cs.nlp.spf.genlex.ccg.template.TemplateSupervisedGenlex;
import edu.cornell.cs.nlp.spf.geoquery.GeoExpSimple;
import edu.cornell.cs.nlp.spf.learn.ILearner;
import edu.cornell.cs.nlp.spf.learn.validation.stocgrad.ValidationStocGrad;
import edu.cornell.cs.nlp.spf.learn.validation.stocgrad.ValidationStocGrad.Builder;
import edu.cornell.cs.nlp.spf.mr.lambda.FlexibleTypeComparator;
import edu.cornell.cs.nlp.spf.mr.lambda.LogicLanguageServices;
import edu.cornell.cs.nlp.spf.mr.lambda.LogicalConstant;
import edu.cornell.cs.nlp.spf.mr.lambda.LogicalExpression;
import edu.cornell.cs.nlp.spf.mr.lambda.ccg.LogicalExpressionCategoryServices;
import edu.cornell.cs.nlp.spf.mr.lambda.ccg.SimpleFullParseFilter;
import edu.cornell.cs.nlp.spf.mr.language.type.TypeRepository;
import edu.cornell.cs.nlp.spf.parser.ccg.cky.CKYBinaryParsingRule;
import edu.cornell.cs.nlp.spf.parser.ccg.cky.CKYUnaryParsingRule;
import edu.cornell.cs.nlp.spf.parser.ccg.cky.logger.ChartLogger;
import edu.cornell.cs.nlp.spf.parser.ccg.cky.single.CKYParser;
import edu.cornell.cs.nlp.spf.parser.ccg.cky.sloppy.BackwardSkippingRule;
import edu.cornell.cs.nlp.spf.parser.ccg.cky.sloppy.ForwardSkippingRule;
import edu.cornell.cs.nlp.spf.parser.ccg.cky.sloppy.SimpleWordSkippingLexicalGenerator;
import edu.cornell.cs.nlp.spf.parser.ccg.factoredlex.features.FactoredLexicalFeatureSet;
import edu.cornell.cs.nlp.spf.parser.ccg.features.basic.DynamicWordSkippingFeatures;
import edu.cornell.cs.nlp.spf.parser.ccg.features.basic.LexicalFeaturesInit;
import edu.cornell.cs.nlp.spf.parser.ccg.features.basic.scorer.ExpLengthLexicalEntryScorer;
import edu.cornell.cs.nlp.spf.parser.ccg.features.lambda.LogicalExpressionCoordinationFeatureSet;
import edu.cornell.cs.nlp.spf.parser.ccg.lambda.pruning.SupervisedFilterFactory;
import edu.cornell.cs.nlp.spf.parser.ccg.model.LexiconModelInit;
import edu.cornell.cs.nlp.spf.parser.ccg.model.Model;
import edu.cornell.cs.nlp.spf.parser.ccg.model.ModelLogger;
import edu.cornell.cs.nlp.spf.parser.ccg.normalform.NormalFormValidator;
import edu.cornell.cs.nlp.spf.parser.ccg.normalform.hb.HBComposedConstraint;
import edu.cornell.cs.nlp.spf.parser.ccg.rules.lambda.PluralExistentialTypeShifting;
import edu.cornell.cs.nlp.spf.parser.ccg.rules.lambda.ThatlessRelative;
import edu.cornell.cs.nlp.spf.parser.ccg.rules.lambda.typeraising.ForwardTypeRaisedComposition;
import edu.cornell.cs.nlp.spf.parser.ccg.rules.lambda.typeshifting.PrepositionTypeShifting;
import edu.cornell.cs.nlp.spf.parser.ccg.rules.primitivebinary.application.BackwardApplication;
import edu.cornell.cs.nlp.spf.parser.ccg.rules.primitivebinary.application.ForwardApplication;
import edu.cornell.cs.nlp.spf.parser.ccg.rules.primitivebinary.composition.BackwardComposition;
import edu.cornell.cs.nlp.spf.parser.ccg.rules.primitivebinary.composition.ForwardComposition;
import edu.cornell.cs.nlp.spf.parser.graph.IGraphParser;
import edu.cornell.cs.nlp.spf.test.Tester;
import edu.cornell.cs.nlp.spf.test.stats.ExactMatchTestingStatistics;
import edu.cornell.cs.nlp.utils.collections.SetUtils;
import edu.cornell.cs.nlp.utils.function.PredicateUtils;
import edu.cornell.cs.nlp.utils.log.ILogger;
import edu.cornell.cs.nlp.utils.log.Log;
import edu.cornell.cs.nlp.utils.log.LogLevel;
import edu.cornell.cs.nlp.utils.log.Logger;
import edu.cornell.cs.nlp.utils.log.LoggerFactory;

import edu.pugetsound.mathcs.nlp.architecture_nlp.features.spf.Interactor;


/**
 * Class to generate a SPF parser and load an SPF model from file. This class passes these values to 
 * an Interactor object and as a result returns an Interactor. This is modified from Yoav Artzi's GeoExpSimple, 
 * edited to load a model from file and allow said model to be held in an object.
 * 
 * @author Jared Polonitza
 */
public class GenerateInteractor {
	public static final ILogger LOG = LoggerFactory.create(GeoExpSimple.class);
	private Interactor<Sentence,LogicalExpression,Sentence> interactor;
	private boolean fromFile;
	private String fileName;

	//Constructor to create Interactor without pre-bundled data
	public GenerateInteractor() {
		fromFile = false;
		this.fileName = "";
	}
	
	//Generate Interactor with pre-bundled data
	public GenerateInteractor(String fileName) {
		fromFile = true;
		this.fileName = fileName;
	}

	/*
	 * Create parser and read model from file. These parameters are then fed to an Interactor builder, returning an Interactor
	 * @return Interactor
	 */
	public Interactor generate() {
		// //////////////////////////////////////////
		// Set some locations to use later
		// //////////////////////////////////////////
		String p = System.getProperty("user.dir");
		File f = new File(p + "/resources");
		String path = f.getAbsolutePath();
		final File resourceDir = new File(path + "/SpfResources/resources/");
		final File dataDir = new File(path + "/SpfResources/experiments/data");
		final File modelDir = new File(path + "/SpfResources/model");


		// //////////////////////////////////////////
		// Use tree hash vector
		// //////////////////////////////////////////

		HashVectorFactory.DEFAULT = Type.FAST_TREE;

		// //////////////////////////////////////////
		// Init lambda calculus system.
		// //////////////////////////////////////////

		final File typesFile = new File(resourceDir, "geo.types");
		final File predOntology = new File(resourceDir, "geo.preds.ont");
		final File simpleOntology = new File(resourceDir, "geo.consts.ont");

		try {
			// Init the logical expression type system
			LogicLanguageServices.setInstance(new LogicLanguageServices.Builder(
					new TypeRepository(typesFile), new FlexibleTypeComparator())
							.addConstantsToOntology(simpleOntology)
							.addConstantsToOntology(predOntology)
							.setUseOntology(true).setNumeralTypeName("i")
							.closeOntology(true).build());
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}

		// //////////////////////////////////////////////////
		// Category services for logical expressions
		// //////////////////////////////////////////////////

		final LogicalExpressionCategoryServices categoryServices = new LogicalExpressionCategoryServices(
				true);

		// //////////////////////////////////////////////////
		// Lexical factoring services
		// //////////////////////////////////////////////////

		FactoringServices.set(new FactoringServices.Builder()
				.addConstant(LogicalConstant.read("exists:<<e,t>,t>"))
				.addConstant(LogicalConstant.read("the:<<e,t>,e>")).build());

		// //////////////////////////////////////////////////
		// Read initial lexicon
		// //////////////////////////////////////////////////

		// Create a static set of lexical entries, which are factored using
		// non-maximal factoring (each lexical entry is factored to multiple
		// entries). This static set is used to init the model with various
		// templates and lexemes.

		final File seedLexiconFile = new File(resourceDir, "seed.lex");
		final File npLexiconFile = new File(resourceDir, "np-list.lex");

		final Lexicon<LogicalExpression> readLexicon = new Lexicon<LogicalExpression>();
		readLexicon.addEntriesFromFile(seedLexiconFile, categoryServices,
				Origin.FIXED_DOMAIN);

		final Lexicon<LogicalExpression> semiFactored = new Lexicon<LogicalExpression>();
		for (final LexicalEntry<LogicalExpression> entry : readLexicon
				.toCollection()) {
			for (final FactoredLexicalEntry factoredEntry : FactoringServices
					.factor(entry, true, true, 2)) {
				semiFactored.add(FactoringServices.factor(factoredEntry));
			}
		}

		// Read NP list
		final ILexicon<LogicalExpression> npLexicon = new FactoredLexicon();
		npLexicon.addEntriesFromFile(npLexiconFile, categoryServices,
				Origin.FIXED_DOMAIN);

		// //////////////////////////////////////////////////
		// CKY parser
		// //////////////////////////////////////////////////

		// Use the Hockenmeir-Bisk normal form parsing constaints. To parse with
		// NF constraints, just set this variable to null.
		final NormalFormValidator nf = new NormalFormValidator.Builder()
				.addConstraint(
						new HBComposedConstraint(Collections.emptySet(), false))
				.build();

		// Build the parser.
		final IGraphParser<Sentence, LogicalExpression> parser = new CKYParser.Builder<Sentence, LogicalExpression>(
				categoryServices)
						.setCompleteParseFilter(new SimpleFullParseFilter(
								SetUtils.createSingleton((Syntax) Syntax.S)))
						.setPruneLexicalCells(true)
						.addSloppyLexicalGenerator(
								new SimpleWordSkippingLexicalGenerator<Sentence, LogicalExpression>(
										categoryServices))
						.setMaxNumberOfCellsInSpan(50)
						.addParseRule(
								new CKYBinaryParsingRule<LogicalExpression>(
										new ForwardComposition<LogicalExpression>(
												categoryServices, 1, false),
										nf))
						.addParseRule(
								new CKYBinaryParsingRule<LogicalExpression>(
										new BackwardComposition<LogicalExpression>(
												categoryServices, 1, false),
										nf))
						.addParseRule(
								new CKYBinaryParsingRule<LogicalExpression>(
										new ForwardApplication<LogicalExpression>(
												categoryServices),
										nf))
						.addParseRule(
								new CKYBinaryParsingRule<LogicalExpression>(
										new BackwardApplication<LogicalExpression>(
												categoryServices),
										nf))
						.addParseRule(
								new CKYUnaryParsingRule<LogicalExpression>(
										new PrepositionTypeShifting(
												categoryServices),
										nf))
						.addParseRule(
								new ForwardSkippingRule<LogicalExpression>(
										categoryServices))
						.addParseRule(
								new BackwardSkippingRule<LogicalExpression>(
										categoryServices, false))
						.addParseRule(
								new CKYBinaryParsingRule<LogicalExpression>(
										new ForwardTypeRaisedComposition(
												categoryServices),
										nf))
						.addParseRule(
								new CKYBinaryParsingRule<LogicalExpression>(
										new ThatlessRelative(categoryServices),
										nf))
						.addParseRule(
								new CKYBinaryParsingRule<LogicalExpression>(
										new PluralExistentialTypeShifting(
												categoryServices),
										nf))
						.build();

		try {
			// //////////////////////////////////////////////////
			// Model
			// //////////////////////////////////////////////////
			
			final Model<Sentence, LogicalExpression> model = Model.readModel(new File(modelDir,"model"));
						
			// //////////////////////////////////////////////////
			// Wrap
			// //////////////////////////////////////////////////
			
			//Build interactor with prepackaged data
			if (fromFile) {
				//Create bundle of data objects to feed model
				final SentenceCollection talk = SentenceCollection.read(new File(dataDir, fileName));
				final Interactor.Builder<Sentence,LogicalExpression,Sentence> interBuild = new Interactor.Builder<Sentence, LogicalExpression,Sentence>(parser,model,talk);
				interactor = interBuild.build();
			}
			else { //Build interactor without data
				final Interactor.Builder<Sentence,LogicalExpression,Sentence> interBuild = new Interactor.Builder<Sentence, LogicalExpression,Sentence>(parser,model);
				interactor = interBuild.build();
			}
			
		}
		catch (ClassNotFoundException message){
			System.out.println(message);
		}
		catch (IOException message) {
			System.out.println(message);
		}
		return interactor;
	}
}