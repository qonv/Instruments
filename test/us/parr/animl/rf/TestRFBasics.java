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

import static junit.fram