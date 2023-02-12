/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.rf;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import us.parr.animl.BaseTest;
import us.parr.animl.classifiers.trees.DecisionTree;
import us.parr.animl.classifiers.trees.RandomForest;
import us.parr.animl.data.DataTable;
import us.parr.animl.validation.Validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertArrayEquals;

public class TestRFBasics extends BaseTest {
	public static final int MIN_LEAF_SIZE = 1;

	@Test public void testEmpty() {
		RandomForest rf = new RandomForest(1, MIN_LEAF_SIZE);
		rf.train(DataTable.empty(null,null));
		String expecting = "{}";
		String result = toTestString(rf.getTree(0));
		Assert.assertEquals(expecting, result);
	}

	@Test public void testOneRow() {
		List<int[]> rows = new ArrayList<