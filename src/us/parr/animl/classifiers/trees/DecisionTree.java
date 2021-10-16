
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
	public static final int INVALID_CATEGORY = -1;

	protected static class BestInfo {
		public double gain = 0.0;
		public int var = -1;
		public double val = 0.0;
		public int cat = INVALID_CATEGORY;

		public BestInfo() { }

		public BestInfo(double gain, int var, int val) {
			this.gain = gain;
			this.var = var;
			this.val = val;
		}
	}

	public static boolean debug = false;

	protected DecisionTreeNode root;

	/** 0 implies use all possible vars when searching for a split var */
	protected int varsPerSplit;

	protected int minLeafSize;

	/** How much of data to examine at each node to find split point */
	protected int nodeSampleSize = 20;

	public DecisionTree() { this(0, 1, 20); }

	public DecisionTree(int varsPerSplit, int minLeafSize) {
		this(varsPerSplit, minLeafSize, 20);
	}

	public DecisionTree(int varsPerSplit, int minLeafSize, int nodeSampleSize) {
		this.varsPerSplit = varsPerSplit;
		this.minLeafSize = minLeafSize;
		this.nodeSampleSize = Math.max(nodeSampleSize, minLeafSize+1); // can't be smaller than min node or we get a single root node
	}

	public int classify(int[] X) { return root.classify(X); };

	@Override
	public Map<Integer, Double> classProbabilities(int[] X) {
		return root.classProbabilities(X);
	}

	/** Build a decision tree starting with arg data and recursively
	 *  build up children. data_i is the ith observation and the (usually) last column of
	 *  data is the predicted (dependent) variable.  Keeping the data together
	 *  makes it easier to implement since splitting a data set splits both
	 *  features and predicted variables.
	 *
	 *  If varsPerSplit>0, select split var from random subset of size m from all variable set.
	 */
	public void train(DataTable data) {
		root = build(data, varsPerSplit, minLeafSize, nodeSampleSize);
	}

	protected static DecisionTreeNode build(DataTable data, int varsPerSplit, int minLeafSize, int nodeSampleSize) {
		if ( data==null || data.size()==0 ) { return null; }

		// sample from data to get subset for finding best split at this node;
		// track original though as that is what we split and use to build
		// the left/right children.
		DataTable original = data;
		if ( nodeSampleSize>0 ) {
//			List<int[]> out = new ArrayList<>(nodeSampleSize);
//			ParrtStats.bootstrapWithRepl(data.getRows(), out, null);
//			data = new DataTable(data, );
			data = data.randomSubset(Math.min(nodeSampleSize, data.size()));
		}

		int N = data.size();
		int yi = data.getPredictedCol(); // last index is usually the target variable

		// if all predict same category or only one row of data,
		// create leaf predicting that
		// TODO: valueCountsInColumn() is another O(n) walk of all data rows! try to remove
		CountingDenseIntSet completeCategoryCounts = (CountingDenseIntSet)data.valueCountsInColumn(yi);
		if ( completeCategoryCounts.size()==1 || data.size()<=minLeafSize ) {
			DecisionTreeNode t = new DecisionLeafNode(data, completeCategoryCounts, yi);
			return t;
		}

		// When subsetting, make sure that we compare gains in bestCategoricalSplit()
		// and bestNumericSplit() to the same overall entropy.
		double complete_entropy = completeCategoryCounts.entropy();

		if ( debug ) System.out.printf("entropy of all %d values = %.2f\n", N, complete_entropy);
		BestInfo best = new BestInfo();
		// Non-random forest decision trees do just: for (int i=0; i<M; i++) {
		// but RF must use a subset m << M of predictor variables so this is
		// a generalization
		List<Integer> indexes = data.getSubsetOfVarIndexes(varsPerSplit, random); // consider all or a subset of M variables
		for (Integer j : indexes) { // for each variable i
			// The goal is to find the lowest expected entropy for all possible
			// values of predictor variable j.  Then we compare best for j against
			// best for any variable
			DataTable.VariableType colType = data.getColTypes()[j];
			BestInfo bestj;
			if ( DataTable.isCategoricalVar(colType) ) {
				// TODO: only do if <= 5 levels else treat as numeric int
				bestCategoricalSplit(data, j, yi, completeCategoryCounts, complete_entropy, best);
			}
			else {
				bestNumericSplit(data, j, yi, completeCategoryCounts, complete_entropy, best);
			}
		}
		if ( best.gain>0.0 ) {
			if ( debug ) {
				System.out.printf("FINAL best is var %s val %s gain=%.2f\n",
				                  original.getColNames()[best.var], best.val, best.gain);
			}
			DataPair split;
			DecisionSplitNode t;
			DataTable.VariableType colType = original.getColTypes()[best.var];
			if ( DataTable.isCategoricalVar(colType) ) {
				// split is expensive, do it only after we get best var/val
				split = categoricalSplit(original, best.var, best.cat);
				t = new DecisionCategoricalSplitNode(original, best.var, colType, best.cat);
			}
			else {
				if ( colType==DataTable.VariableType.NUMERICAL_FLOAT ) {
					split = numericalFloatSplit(original, best.var, best.val);
				}
				else {
					split = numericalIntSplit(original, best.var, best.val);
				}
				t = new DecisionNumericalSplitNode(original, best.var, colType, best.val);
			}
			t.numRecords = N;
			t.entropy = (float)complete_entropy;
			if ( split.region2.size()==0 ) {
				System.out.println("what?");
			}
			t.left = build(split.region1,  varsPerSplit, minLeafSize, nodeSampleSize);
			t.right = build(split.region2, varsPerSplit, minLeafSize, nodeSampleSize);
			return t;
		}
		// we would gain nothing by splitting, make a leaf predicting majority vote
		int majorityVote = completeCategoryCounts.argmax();
		if ( debug ) {
			System.out.printf("FINAL no improvement; make leaf predicting %s\n",
			                  DataTable.getValue(original,majorityVote,yi));
		}
		DecisionTreeNode t = new DecisionLeafNode(original, completeCategoryCounts, yi);
		return t;
	}

	protected static BestInfo bestNumericSplit(DataTable data, int j, int yi,
	                                           CountingDenseIntSet completePredictionCounts,
	                                           double complete_entropy,
	                                           BestInfo best)
	{
		int n = data.size();
		// Rather than splitting the data table for each unique value of this variable
		// (which would be O(n^2)), we sort on this variable and then
		// walk the data records, keeping track of the predicted category counts.
		// We keep a snapshot of the category counts every time the predictor variable
		// changes in the sorted list.
		data.sortBy(j);

		int[] allCounts = completePredictionCounts.toDenseArray();

		// look for discontinuities (transitions) in predictor var values,
		// computing less than, greater than entropy for each from target cat counts;
		// track best split
		DataTable.VariableType colType = data.getColTypes()[j];
		int targetCatMaxValue = (Integer) data.getColMax(yi);
		int[] currentCounts = new int[targetCatMaxValue+1];
		int[] greaterThanCounts = new int[targetCatMaxValue+1]; // allocate this just once
		for (int i = 0; i<n; i++) { // walk all records, updating currentCounts
			// note; if all values in col j are the same, then we don't enter this IF and return zeroed best
			if ( i>0 && data.compare(i-1, i, j)<0 ) { // if row i-1 < row i, discontinuity in predictor var
				double splitValue; // midpoint between new value and previous
				if ( colType==DataTable.VariableType.NUMERICAL_INT ) {
					splitValue = (data.getAsInt(i, j)+data.getAsInt(i-1, j))/2.0; // assumes col j sorted!
				}
				else {
					splitValue = (data.getAsFloat(i, j)+data.getAsFloat(i-1, j))/2.0;
				}
				int[] lessThanCounts = currentCounts;
				int n1 = i; // how many observations less than current discontinuity value
				int n2 = n - i;
				minus(allCounts, lessThanCounts, greaterThanCounts);
				double expectedEntropyValue = expectedEntropy(lessThanCounts, n1, greaterThanCounts, n2);
				double gain = complete_entropy - expectedEntropyValue;
				if ( gain>best.gain ) {
					best.gain = gain;
					best.var = j;
					best.val = splitValue;
				}
				String var = data.getColNames()[j];
				if ( debug ) {
					double r1_entropy = ParrtStats.entropy(lessThanCounts);
					double r2_entropy = ParrtStats.entropy(greaterThanCounts);
					System.out.printf("Entropies var=%13s val=%.2f r1=%d/%d*%.2f r2=%d/%d*%.2f, ExpEntropy=%.2f gain=%.2f\n",
					                  var, splitValue, n1, n1+n2, r1_entropy, n2, n1+n2, r2_entropy, expectedEntropyValue, gain);
				}
			}
			int targetCat = data.getAsInt(i, yi);
			currentCounts[targetCat]++;
		}

		return best;
	}

//	static int[][] catCounts = new int[20][20]; // seems to help but not by much

	protected static BestInfo bestCategoricalSplit(DataTable data, int j, int yi,
	                                               CountingDenseIntSet completePredictionCounts,
	                                               double complete_entropy,
	                                               BestInfo best)
	{
		int n = data.size();
		Integer targetCatMaxValue = (Integer) data.getColMax(yi);
		Integer colCatMaxValue = (Integer) data.getColMax(j);
//		for (int i = 0; i<20; i++) { // walk all records, counting dep categories in two groups: indep cat equal and not-equal to splitCat
//			Arrays.fill(catCounts[i], 0);
//		}
		int[][] catCounts = new int[colCatMaxValue+1][targetCatMaxValue+1];
		for (int i = 0; i<n; i++) { // walk all records, counting dep categories in two groups: indep cat equal and not-equal to splitCat
			int currentColCat = data.getAsInt(i, j);
			int currentTargetCat = data.getAsInt(i, yi);
			catCounts[currentColCat][currentTargetCat]++;
		}
		int[] notEqCounts = new int[targetCatMaxValue+1];
		int[] allCounts = completePredictionCounts.toDenseArray();
		for (int colCat = 0; colCat<catCounts.length; colCat++) {
			int[] currentCatCounts = catCounts[colCat];
			int n1 = sum(currentCatCounts);
			// category values are not necessarily contiguous; ignore col category values w/o observations
			if ( n1==0 ) continue;
			minus(allCounts, currentCatCounts, notEqCounts);
			int n2 = sum(notEqCounts);
			double expectedEntropyValue = expectedEntropy(currentCatCounts, n1, notEqCounts, n2);
			double gain = complete_entropy-expectedEntropyValue;
			// It's possible that all values in col j are the same, which would
			// leave n2 as 0 (and n1 should be data.size()). That would imply
			// that all catCounts[*] but this one are empty
			// We don't want to split on this data for col j so don't set a best
			if ( gain>best.gain && n2>0 ) {
//				System.out.println("set best "+gain+" n1 "+n1+", n2 "+n2);
				best.gain = gain;
				best.var = j;
				best.cat = colCat;
			}
			if ( debug ) {
				double r1_entropy = ParrtStats.entropy(currentCatCounts);
				double r2_entropy = ParrtStats.entropy(notEqCounts);
				String var = data.getColNames()[j];
				Object p = DataTable.getValue(data, colCat, j);
				System.out.printf("Entropies var=%13s cat=%-13s r1=%2d/%3d*%.2f r2=%2d/%3d*%.2f, ExpEntropy=%.2f gain=%.2f\n",
				                  var, p, n1, n1+n2, r1_entropy, n2, n1+n2, r2_entropy,