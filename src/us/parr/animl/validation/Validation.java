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
		in