
/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.cluster

import org.junit.Test
import us.parr.animl.BaseTest
import us.parr.animl.data.DoubleVector
import us.parr.lib.ParrtStats

class TestMeanShift : BaseTest() {
    @Test fun testSimple2DSmallLinearClusters(): Unit {
        val n = 10
        val cluster1 = listOf<DoubleVector>(
                DoubleVector(-0.298743,0.799992),
                DoubleVector(-0.299058,0.799996),
                DoubleVector(-0.299372,0.799998),
                DoubleVector(-0.299686,0.8)