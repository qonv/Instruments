/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.data;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringUtils;
import sun.misc.FloatingDecimal;
import us.parr.lib.ParrtCollections;
import us.parr.lib.ParrtStats;
import us.parr.lib.collections.CountingDenseIntSet;
import us.parr.lib.collections.CountingSet;
import us.parr.lib.collections.DenseIntSet;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static java.util.Collections.max;
import static us.parr.animl.data.DataTable.VariableFormat.CENTER;
import static us.parr.animl.data.DataTable.VariableFormat.RIGHT;
import static us.parr.animl.data.DataTable.VariableType.CATEGORICAL_INT;
import static us.parr.animl.data.DataTable.VariableType.CATEGORICAL_STRING;
import static us.parr.animl.data.DataTable.VariableType.INVALID;
import static us.parr.animl.data.DataTable.VariableType.NUMERICAL_FLOAT;
import static us.parr.animl.data.DataTable.VariableType.NUMERICAL_INT;
import static us.parr.animl.data.DataTable.VariableType.TARGET_CATEGORICAL_INT;
import static us.parr.animl.data.DataTable.VariableType.TARGET_CATEGORICAL_STRING;
import static us.parr.animl.data.DataTable.VariableType.UNUSED_FLOAT;
import static us.parr.animl.data.DataTable.VariableType.UNUSED_INT;
import static us.parr.animl.data.DataTable.VariableType.UNUSED_STRING;
import static us.parr.lib.ParrtCollections.indexOf;
import static us.parr.lib.ParrtCollections.join;
import static us.parr.lib.ParrtCollections.map;

public class DataTable implements Iterable<int[]> {
	// 9.466524720191955566e-01
	public static final Pattern floatPattern = Pattern.compile("^-?[0-9]+\\.[0-9]*|\\.[0-9]+[eE][+-][0-9]+$");
	public static final Pattern intPattern = Pattern.compile("^-?[0-9]+$");

	/** Input sometimes has NA or blanks for unknown values */
	public static final Set<String> UNKNOWN_VALUE_STRINGS = new HashSet<String>() {{
		add("");
		add("NA");
		add("N/A");
	}};

	public enum VariableType {
		CATEGORICAL_INT, CATEGORICAL_STRING, NUMERICAL_INT, NUMERICAL_FLOAT,
		TARGET_CATEGORICAL_INT, TARGET_CATEGORICAL_STRING,
		UNUSED_INT,
		UNUSED_FLOAT,
		UNUSED_STRING,
		INVALID
	}
	public enum VariableFormat {
		LEFT, CENTER, RIGHT
	}

	public static final String[] varTypeShortNames = new String[VariableType.values().length];
	public static final VariableFormat[] defaultVarFormats = new VariableFormat[VariableType.values().length];
	static {
		varTypeShortNames[CATEGORICAL_INT.ordinal()] = "cat";
		varTypeShortNames[CATEGORICAL_STRING.ordinal()] = "string";
		varTypeShortNames[NUMERICAL_INT.ordinal()] = "int";
		varTypeShortNames[NUMERICAL_FLOAT.ordinal()] = "float";
		varTypeShortNames[TARGET_CATEGORICAL_INT.ordinal()] = "target";
		varTypeShortNames[TARGET_CATEGORICAL_STRING.ordinal()] = "target-string";
		varTypeShortNames[UNUSED_INT.ordinal()] = "unused";
		varTypeShortNames[UNUSED_FLOAT.ordinal()] = "unused";
		varTypeShortNames[UNUSED_STRING.ordinal()] = "unused";

		defaultVarFormats[CATEGORICAL_INT.ordinal()] = RIGHT;
		defaultVarFormats[CATEGORICAL_STRING.ordinal()] = CENTER;
		defaultVarFormats[NUMERICAL_INT.ordinal()] = RIGHT;
		defaultVarFormats[NUMERICAL_FLOAT.ordinal()] = RIGHT;
		defaultVarFormats[TARGET_CATEGORICAL_INT.ordinal()] = RIGHT;
		defaultVarFormats[TARGET_CATEGORICAL_STRING.ordinal()] = CENTER;
		defaultVarFormats[UNUSED_INT.ordinal()] = RIGHT;
		defaultVarFormats[UNUSED_FLOAT.ordinal()] = RIGHT;
		defaultVarFormats[UNUSED_STRING.ordinal()] = CENTER;
	}

	// TODO: this should be int[j][i] stored in columnar form; first index is the column then it goes down rows in that column
	protected List<int[]> rows;
	protected String[] colNames;
	protected VariableType[] colTypes;
	protected StringTable[] colStringToIntMap;
	protected int[] colMaxes;

	protected Set<Integer> cachedPredictionCategories;
	protected int cachedMaxPredictionCategoryValue = -1;

	public DataTable() {
	}

	public DataTable(List<int[]> rows, VariableType[] colTypes, String[] colNames, int[] colMaxes) {
		this(rows, colTypes, colNames, colMaxes, null);
	}

	public DataTable(List<int[]> rows, VariableType[] colTypes, String[] colNames, int[] colMaxes, StringTable[] colStringToIntMap) {
		this.rows = rows;
		this.colMaxes = colMaxes;
		this.colNames = colNames;
		this.colTypes = colTypes;
		this.colStringToIntMap = colStringToIntMap;
		if ( this.colMaxes==null ) {
			computeColMaxes();
		}
	}

	public static DataTable empty(VariableType[] colTypes, String[] colNames) {
		return new DataTable(new ArrayList<>(), colTypes, colNames, null, null);
	}

	/** Make a new table from an old table with a subset of rows */
	public DataTable(DataTable old, List<int[]> rows) {
		this(rows, old.colTypes, old.colNames, old.colMaxes, old.colStringToIntMap);
	}

	/** Make a new table from an old table with shallow copy of rows */
	public DataTable(DataTable old) {
		this.rows = new ArrayList<>(old.rows.size());
		this.rows.addAll(old.rows);
		this.colNames = old.colNames;
		System.arraycopy(old.colMaxes, 0, this.colMaxes, 0, old.colMaxes.length);
		this.colTypes = old.colTypes;
		this.colStringToIntMap = old.colStringToIntMap;
	}

	public static DataTable fromInts(List<int[]> rows, VariableType[] colTypes, String[] colNames) {
		if ( rows==null ) return empty(colTypes, colNames);
		if ( rows.size()==0 && colTypes==null ) {
			return empty(colTypes, colNames);
		}

		int dim = rows.size()>0 ? rows.get(0).length : colTypes.length;
		if ( colTypes==null ) {
			colTypes = getDefaultColTypes(dim);
		}
		if ( colNames==null ) {
			colNames = getDefaultColNames(colTypes, dim);
		}
		return new DataTable(rows, colTypes, colNames, null);
	}

	public static DataTable fromStrings(List<String[]> rows) {
		if ( rows==null ) return empty(null, null);
		if ( rows.size()==0 ) {
			return empty(null, null);
		}
		String[] headerRow = rows.get(0);
		if ( headerRow==null ) {
			return empty(null, null);
		}
		int numCols = headerRow.length;
		if ( rows.size()==1 ) { // just header row?
			return empty(null, headerRow);
		}

		rows = rows.subList(1, rows.size()); // don't use first row.

		VariableType[] actualTypes = computeColTypes(rows, numCols);

		return fromStrings(rows, actualTypes, headerRow, false);
	}

	public static DataTable fromStrings(List<String[]> rows, VariableType[] colTypes, String[] colNames, boolean hasHeaderRow) {
		if ( rows==null || rows.size()==0 ) return empty(colTypes, colNames);
		if ( rows.size()==1 && hasHeaderRow ) {
			return empty(colTypes, colNames);
		}

		if ( hasHeaderRow && colNames==null ) {
			colNames = rows.get(0);
		}

		int dim = rows.get(0).length;
		if ( colTypes==null ) {
			colTypes = getDefaultColTypes(dim);
		}
		if ( colNames==null ) {
			colNames = getDefaultColNames(colTypes, dim);
		}
		StringTable[] colStringToIntMap = new StringTable[colTypes.length];
		// don't waste space on string tables unless we need to
		for (int j = 0; j < colTypes.length; j++) {
			if ( colTypes[j]==CATEGORICAL_STRING || colTypes[j]==TARGET_CATEGORICAL_STRING ) {
				colStringToIntMap[j] = new StringTable();
			}
		}
		// process strings into ints using appropriate conversion
		List<int[]> rows2 = new ArrayList<>();
		for (int i = hasHeaderRow ? 1 : 0; i < rows.size();