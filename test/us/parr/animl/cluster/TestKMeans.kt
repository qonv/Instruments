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
        val grades = doubleArrayOf(92.65, 93.87, 74.06, 86.94, 92.26, 94.46, 92.94, 80.65, 92.86,
                85.94, 91.79, 95.23, 85.37, 87.85, 87.71, 93.03)
        val data = grades.map { g -> DoubleVector(g) }
        val means  = doubleArrayOf(90.0, 87.5, 70.0)
        val centroids = means