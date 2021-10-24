/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.classifiers.trees;

import us.parr.animl.classifiers.ClassifierModel;
import us.parr.animl.data.DataTable;
import us.parr.lib.ParrtStats;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static us.parr.lib.ParrtStats.majorityVote;


/** A Random Forest classifier operating on categorical and numerical
 *  values. Predicts integer categories only. -1 is an invalid predicted
 *  category value.
 */
public class