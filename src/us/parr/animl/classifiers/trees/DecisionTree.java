
/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.classifiers.trees;

import us.parr.animl.classifiers.ClassifierModel;
import us.parr.animl.data.DataPair;
import us.parr.animl.data.DataTable;
import us.parr.lib.ParrtStats;
import us.parr.lib.collections.CountingDenseIntSet;

import javax.json.Json;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static us.parr.lib.ParrtMath.minus;
import static us.parr.lib.ParrtStats.sum;

/** A classic CART decision tree but this implementation is suitable just for
 *  classification, not regression. I extended it to handle a subset of predictor
 *  variables at each node to support random forest construction.
 */
public class DecisionTree implements ClassifierModel {
	public static final int SEED = 777111333; // need randomness but use same seed to get reproducibility
	public static final Random random = new Random(SEED);