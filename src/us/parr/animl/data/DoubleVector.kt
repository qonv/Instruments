/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.data

import java.math.BigDecimal
import java.math.RoundingMode

val NUM_DECIMALS_TOLERANCE_FOR_EQUALS = 9

class DoubleVector {
    var elements: kotlin.DoubleArray

    constructor(n : Int) {
        elements = kotlin.DoubleArray(n)
    }
    constructor(v : DoubleVector) {
        elements = v.elements.copyOf()
    }
    constructor(x : List<Number>) {
        elements = kotlin.DoubleArray(x.size)
        for (i in elements.indices) {
            elements[i] = x[i].toDouble()
        }
    }