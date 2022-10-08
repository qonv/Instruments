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
import us.parr.lib.ParrtIO;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static us.parr.animl.data.DataTable.VariableType.UNUSED_INT;

public class TestDataSets extends BaseTest {
	// Figure 18.3 Examples for the restaurant domain. from Russell and Norvig
	// has picture of tree: https://people.ee