/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.validation;

import us.parr.animl.classifiers.ClassifierModel;
import us.parr.animl.data.DataTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static us.parr.lib.ParrtStats.mean;

public class Validation {
	public static final int SEED = 333888333; // need randomness but use same seed to get reproducibility
	public static final Random random = new Random(SEED);

	public static int leaveOneOut(ClassifierModel classifier, DataTable data) {
		int miss = 0;
		for (int whichToLeaveOut = 0; whichToLeaveOut<data.size(); whichToLeaveOut++) {
			DataTable subset = data.subsetNot(whichToLeaveOut); // shallow copy data set
			int[] leaveOut = data.getRow(whichToLeaveOut);
			classifier.train(subset); // wipes old data, retrains
			int cat = classifier.classify(leaveOut);
			int trueCat = leaveOut[data.getPredictedCol()];
			if ( cat!=trueCat ) {
				miss++;
			}
		}
		return miss;
	}

	public static double kFoldCross(ClassifierModel classifier, int k, DataTable data) {
		data.shuffle(random);
		int n = data.size();
		int foldSize = n / k;
		int remainder = n % k;
//		System.out.printf("%d-fold cross-validation n=%d, size=%d, rem=%d\n", k, n, foldSize, remainder);
		assert remainder