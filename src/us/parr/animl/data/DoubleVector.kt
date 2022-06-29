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
    constructor(vararg x : Double) {
        elements = x.copyOf()
    }

    /** Two vectors are equal if their elements are isclose() */
    override fun equals(other: Any?): Boolean = equals(other, 1e-9)

    fun equals(other: Any?, tolerance: Double): Boolean {
        return other is DoubleVector &&
                this.dims()==other.dims() &&
                isclose(this, other, tolerance)
    }

//    fun isclose(other: DoubleVector, ndec : Int = NUM_DECIMALS_TOLERANCE_FOR_EQUALS) : Boolean {
//        for (i in elements.indices) {
//            if ( !isclose(this[i], other[i], ndec) ) return false
//        }
//        return true
//    }


    /** Hash of this vector is derived from element values rounded to ndec decimal places */
    override fun hashCode(): Int = hashCode(NUM_DECIMALS_TOLERANCE_FOR_EQUALS)

    fun hashCode(ndec : Int): Int {
        var hash : Int = 1
        for (element in elements) {
            var rounded = BigDecimal(element)
            rounded = rounded.setScale(ndec, RoundingMode.HALF_UP)
            val bits = java.lang.Double.doubleToLongBits(rounded.toDouble())
            hash = 31 * hash + (bits xor (bits ushr 32)).toInt()
        }

        return hash
    }

    fun rounded(ndec : Int = NUM_DECIMALS_TOLERANCE_FOR_EQUALS) : DoubleVector {
        val dup = DoubleVector(this)
        dup.round(ndec)
        return dup
    }

    /** Round to ndec decimals rounding to nearest "neighbor" */
    fun round(ndec : Int = NUM_DECIMALS_TOLERANCE_FOR_EQUALS) {
        for (i in elements.indices) {
            var d = BigDecimal(elements[i])
            d = d.setScale(ndec, RoundingMode.HALF_UP)
            elements[i] = d.toDouble()
        }
    }

    operator fun get(i : Int) : Double = elements.get(i)

    operator fun set(i : Int, v : Double) { elements[i] = v }

    fun copy() : DoubleVector = DoubleVector(elements.toList())

    fun dims() = elements.size

    infix fun dot(b:DoubleVector) : Double {
        var sum : Double = 0.0
        for(i in elements.indices) {
            sum += elements[i] * b.elements[i]
        }
        return sum
    }

    fun sum() : Double {
        var sum : Double = 0.0
        for(i in elements.indices) {
            sum += elements[i]
        }
        return sum
    }

    operator infix fun plus(b:DoubleVector) : DoubleVector {
        val r = DoubleVector(b)
        for(i in elements.indices) {
            r.elements[i] = elements[i] + b.elements[i]
        }
        return r
    }

    operator infix fun minus(b:DoubleVector) : DoubleVector {
        val r = DoubleVector(b)
        for(i in elements.indices) {
            r.elements[i] = elements[i] - b.elements[i]
        }
        return r
    }

    operator infix fun times(b:Double) : DoubleVector {
        return DoubleVector(elements.map { it * b })
    }

    operator infix fun div(b:Double) : DoubleVector {
        return DoubleVector(elements.map { it / b })
    }

    operator fun unaryMinus() : DoubleVector {
        return DoubleVector(elements.map { -it })
    }

    fun abs() : DoubleVector {
        return DoubleVector(elements.map { Math.abs(it) })
    }

    infix fun map(trans