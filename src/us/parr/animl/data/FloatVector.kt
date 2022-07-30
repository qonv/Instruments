/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.data

import java.lang.Math.abs
import java.lang.Math.max

/** A simple vector to learn some Kotlin */
class FloatVector {
    var elements: kotlin.FloatArray

    constructor(n : Int) {
        elements = kotlin.FloatArray(n)
    }
    constructor(v : FloatVector) {
        elements = v.elements.copyOf()
    }
    constructor(x :