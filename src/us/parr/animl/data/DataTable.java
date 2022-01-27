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
	public static final Pat