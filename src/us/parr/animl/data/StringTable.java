/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/** A unique set of strings mapped to a monotonically increasing index.
 *  These indexes often useful to bytecode interpreters that have instructions
 *  referring to strings by unique integer. Indexing is from 0.
 *
 *  We can also get them back out in original order.
 *
 *  Yes, I know that this is similar to {@link String#intern()} but in this
 *  case, I need the index out not just to make these strings unique.
 *
 *  Copied from https://github.com/antlr/symtab
 */
public class StringTable {
	protected LinkedHashMap<String,Integer> table = new LinkedHashMap<String,Integer>();
	protected int index = -1; // index we have just written
	protected List<String> strings = new ArrayList<>();

	public int add(String s) {
		Integer I = table.get(s);
		if ( I!=null ) return I;
		index+