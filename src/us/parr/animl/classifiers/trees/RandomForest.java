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
	public RandomFor