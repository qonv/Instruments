/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.cluster

import org.junit.Test
import us.parr.animl.BaseTest
import us.parr.animl.data.DoubleVector
import us.parr.lib.ParrtStats.normal
import kotlin.test.assertEquals

class TestKMeans: BaseTest() {
    @Test fun testGrades1Dk3() : Unit {
        val grades = doubleArrayOf(92.65, 93.8