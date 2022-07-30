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
    constructor(x : List<Number>) {
        elements = kotlin.FloatArray(x.size)
        for (i in elements.indices) {
            elements[i] = x[i].toFloat()
        }
    }

    operator fun get(i : Int) : Float = elements.get(i)

    operator fun set(i : Int, v : Float) { elements[i] = v }

    fun size() = elements.size

    infix fun dot(b:FloatVector) : Double {
        var sum : Double = 0.0
        for(i in elements.indices) {
            sum += elements[i] * b.elements[i]
        }
        return sum
    }

    fun sum(