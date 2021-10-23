/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.classifiers.trees;

import us.parr.animl.classifiers.Classifier;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public abstract class DecisionTreeNode implements Classifier {
	// for debugging