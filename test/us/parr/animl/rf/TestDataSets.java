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
	// has picture of tree: https://people.eecs.berkeley.edu/~russell/classes/cs194/f11/lectures/CS194%20Fall%202011%20Lecture%2008.pdf
	public static final String[][] restaurant = {
		{"Alt", "Bar", "Fri&Sat", "Hungry", "Patrons", "Price", "Raining", "MadeRez", "Type", "WaitEstimate", "WillWait"},
		{"Yes", "No",  "No",      "Yes",     "Some", "$$$", "No", "Yes", "French", "0–10", "Yes"},
		{"Yes", "No",  "No",      "Yes",     "Full", "$", "No", "No", "Thai", "30–60", "No"},
		{"No",  "Yes", "No",      "No",      "Some", "$", "No", "No", "Burger", "0–10", "Yes"},
		{"Yes", "No",  "Yes",     "Yes",     "Full", "$", "Yes", "No", "Thai", "10–30", "Yes"},
		{"Yes", "No",  "Yes",     "No",      "Full", "$$$", "No", "Yes", "French", ">60", "No"},
		{"No",  "Yes", "No",      "Yes",     "Some", "$$", "Yes", "Yes", "Italian", "0–10", "Yes"},
		{"No",  "Yes", "No",      "No",      "None", "$", "Yes", "No", "Burger", "0–10", "No"},
		{"No",  "No",  "No",      "Yes",     "Some", "$$", "Yes", "Yes", "Thai", "0–10", "Yes"},
		{"No",  "Yes", "Yes",     "No",      "Full", "$", "Yes", "No", "Burger", ">60", "No"},
		{"Yes", "Yes", "Yes",     "Yes",     "Full", "$$$", "No", "Yes", "Italian