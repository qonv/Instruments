/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.classifiers.trees;

import us.parr.animl.classifiers.ClassifierModel;
import us.parr.animl.data.DataTable;
import us.parr.lib.ParrtStats;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static us.parr.lib.ParrtStats.majorityVote;


/** A Random Forest classifier operating on categorical and numerical
 *  values. Predicts integer categories only. -1 is an invalid predicted
 *  category value.
 */
public class RandomForest implements ClassifierModel {
	/** How many trees to create in the forest */
	protected int numEstimators;

	protected int minLeafSize;

	/** How much of data to examine at each node to find split point */
	protected int nodeSampleSize = 20;

	/** From 0..1, how many observations to sample from table for each tree. 1.0 implies all.
	 *  0.5 implies bootstrap a sample of size .5 * data.size().
	 */
	protected double bootstrapSampleRate = 1.0;

	/** The forest of trees */
	protected List<DecisionTree> trees;

	/** Which observations (indexes) were out-of-bag for each tree trained on data? */
	protected List<Set<Integer>> treeOutOfBagSampleIndexes;

	/** Constructors for classifiers / regressors should capture all parameters
	 *  needed to train except for the actual data, which could vary.
	 */
	public RandomForest(int numEstimators, int minLeafSize, int nodeSampleSize, double bootstrapSampleRate) {
		this.numEstimators = numEstimators;
		this.minLeafSize = minLeafSize;
		this.nodeSampleSize = Math.max(nodeSampleSize, minLeafSize+1); // can't be smaller than min node or we get a single root node
		this.bootstrapSampleRate = bootstrapSampleRate;
	}

	public RandomForest(int numEstimators, int minLeafSize) {
		this(numEstimators, minLeafSize, 20, 1.0);
	}


	/** Train on this data. Wipe out any existing trees etc... */
	public void train(DataTable data) {
		this.trees = new ArrayList<>(numEstimators);
		this.treeOutOfBagSampleIndexes = new ArrayList<>(numEstimators);
		if ( data==null || data.size()==0 || numEstimators==0 ) return;
		int M = data.getNumberOfPredictorVar();
		// Number of variables to select at random at each decision node to find best split
		int m = (int)Math.round(Math.sqrt(M));
		List<int[]> bootstrap = new ArrayList<>(data.size()); // reuse for each tree
		for (int i = 1; i<=numEstimators; i++) {
			if ( DecisionTree.debug ) System.out.println("Estimator "+i+" ------------------");
			Set<Integer> outOfBagSamples = new HashSet<>(); // gets filled in
			int sampleSize = (int)(bootstrapSampleRate * data.size());
//			List<int[]> bootstrap = ParrtStats.bootstrapWithRepl(data.getRows(), sam