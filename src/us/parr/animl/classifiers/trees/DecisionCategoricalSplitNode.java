/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.classifiers.trees;

import us.parr.animl.data.DataTable;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.util.Map;

import static us.parr.lib.ParrtMath.isClose;


public class DecisionCategoricalSplitNode extends DecisionSplitNode {
	/** Split according to what variable category? An unknown matches category yes or no */
	protected int splitCategory;

	protected Object splitCategoryDisplayValue;

	protected String variableName;

	public DecisionCategoricalSplitNode(DataTable data, int splitVariable, DataTable.VariableType colType, int splitCategory) {
		super(splitVariable, colType);
		this.splitCategory = splitCategory;
		this.splitCategoryDisplayValue = DataTable.getValue(data, splitCategory, splitVariable);
		this.variableName = data.getColNames()[splitVariable];
	}

	public int classify(int[] X) {
		if ( X[splitVariable]==splitCategory ) { // if equal, choose left child
			return left.classify(X);
		}
		else {
			return right.classify(X);
		}
	}

	@Override
	public Map<Integer, Double> classProbabilities(int[] X) {
		if ( X[splitVariable]==splitCategory ) { // if equal, choose left child
			return left.classProbabilities(X);
		}
		else {
			return right.classProbabilities(X);
		}
	}

	@Override
	public JsonObjectBuilder getJSONData() {
		JsonObjectBuilder builder =  Json.createObjectBuilder();
		builder.add("var", variableName);
		bui