/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.rf;

import org.junit.Test;
import us.parr.animl.BaseTest;
import us.parr.animl.classifiers.trees.DecisionTree;
import us.parr.animl.data.DataTable;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestDecisionTreeBasics extends BaseTest {
	@Test public void testEmptyData() {
		List<int[]> data = new ArrayList<>();
		DecisionTree tree = new DecisionTree();
		tree.train(DataTable.fromInts(data, null, null));
		String expecting = "{}";
		String result = toTestString(tree);
		assertEquals(expecting, result);
		checkPredictions(data, tree);
	}

	@Test public void testOneRow() {
		List<int[]> data = new ArrayList<>();
		data.add(new int[] {1,99}); // 1 row with 1 var of value 1 predicting category 99
		DecisionTree tree = new DecisionTree();
		tree.train(DataTable.fromInts(data, null, null));
		String expecting = "{'predict':99,'n':1}";
		String result = toTestString(tree);
		assertEquals(expecting, result);
		checkPredictions(data, tree);
	}

	@Test public void testMultiRowsSameColValue() {
		List<int[]> data = new ArrayList<>();
		data.add(new int[] {1,99});
		data.add(new int[] {1,100});
		data.add(new int[] {1,101});
		data.add(new int[] {1,102});
		DecisionTree tree = new DecisionTree();
		DecisionTree.debug = true;
		tree.train(DataTable.fromInts(data, null, null));
		String expecting = "{'predict':99,'n':4,'E':'2.00'}";
		String result = toTestString(tree);
		assertEquals(expecting, result);
	}

	@Test public void testTwoRowsSameCat() {
		List<int[]> data = new ArrayList<>();
		data.add(new int[] {1,99}); // 1 row with 1 var of value 1 predicting category 99
		data.add(new int[] {2,99}); // 2nd row with 1 var of value 2 predicting category 99
		DecisionTree tree = new DecisionTree();
		tree.train(DataTable.fromInts(data, null, null));
		String expecting = "{'predict':99,'n':2}";
		String result = toTestString(tree);
		assertEquals(expecting, result);
		checkPredictions(data, tree);
	}

	@Test public void testCannotPredict() {
		List<int[]> data = new ArrayList<>();
		data.add(new int[] {1,99});
		data.add(new int[] {1,100});
		data.add(new int[] {2,99});
		data.add(new int[] {2,100});
		DecisionTree tree = new DecisionTree();
		tree.train(DataTable.fromInts(data, null, null));
		String expecting = "{'predict':99,'n':4,'E':'1.00'}";
		String result = toTestString(tree);
		assertEquals(expecting, result);
		// cannot test prediction as it's noise
	}

	@Test public void testTwoRowsSameCatMultipleIndepVars() {
		List<int[]> data = new ArrayList<>();
		data.add(new int[] {1,2,3,99});
		data.add(new int[] {2,4,6,99});
		DecisionTree tree = new DecisionTree();
		tree.train(DataTable.fromInts(data, null, null));
		String expecting = "{'predict':99,'n':2}";
		String result = toTestString(tree);
		assertEquals(expecting, result);
		checkPredictions(data, tree);
	}

	@Test public void testTwoRowsDiffCat() {
		List<int[]> data = new ArrayList<>();
		data.add(new int[] {1,99});  // 1 row with 1 var of value 1 predicting category 99
		data.add(new int[] {2,100}); // 2nd row with 1 var of value 2 predicting category 50
		DecisionTree tree = new DecisionTree();
		tree.train(DataTable.fromInts(data, null, null));
		String expecting = "{'var':'x0','val':1.5,'n':2,'E':'1.00','left':{'predict':99,'n':1},'right':{'predict':100,'n':1}}";
		String result = toTestString(tree);
		assertEquals(expecting, result);
		checkPredictions(data, tree);
	}

	@Test public void testTwoRowsDiffCatMultipleIndepVars() {
		List<int[]> data = new ArrayList<>();
		data.add(new int[] {1,2,3,99});
		data.add(new int[] {2,4,6,100});
		DecisionTree tree = new DecisionTree();
		tree.train(DataTable.fromInts(data, null, null));
		String expecting = "{'var':'x0','val':1.5,'n':2,'E':'1.00','left':{'predict':99,'n':1},'right':{'predict':100,'n':1}}";
		String result = toTestString(tree);
		assertEquals(expecting, result);
		checkPredictions(data, tree);
	}

	@Test public void testTwoVarsOneGoodOneBadSplitVar() {
		List<int[]> data = new ArrayList<>(); // 1st var is perfect splitter, 2nd is bad
		data.add(new int[] {1,4,99});
		data.add(new int[] {1,5,99});
		data.add(new int[] {2,4,100});
		data.add(new int[] {2,5,100});
		DecisionTree tree = new DecisionTree();
		tree.train(DataTable.fromInts(data, null, null));
		String expecting = "{'var':'x0','val':