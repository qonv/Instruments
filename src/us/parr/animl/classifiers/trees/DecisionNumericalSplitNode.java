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


public class DecisionNumericalSplitNode extends DecisionSplitNode {
	/** Split at what variable value? */
	protected double splitValue;

	protected String variableName;

	public DecisionNumericalSplitNode(DataTable data, int splitVariable, DataTable.VariableType colType, double splitValue) {
		super(splitVariable, colType);
		this.splitValue = splitValue;
		this.variableName = data.getColNames()[splitVariable];
	}

	public int classify(int[] X) {
		double v;
		if ( colType==DataTable.VariableType.NUMERICAL_INT ) {
			v = X[splitVariable];
		}
		else {
			v = Float.intBitsToFloat(X[splitVariable]);
		}
		if ( v < splitValue ) {
			return left.classify(X);
		}
		else {
			return right.classify(X);
		}
	}

	@Override
	public Map<Integer, Double> classProbabilities(int[] X) {
		// TODO: hideous duplication with classify()
		double v;
		if ( colType==DataTable.VariableType.NUMERICAL_INT ) {
			v = X[splitVariable];
		}
		else {
			v = Float.intBitsToFloat(X[splitVariable]);
		}
		if ( v < splitValue ) {
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
		builder.ad