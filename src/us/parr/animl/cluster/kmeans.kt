/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */
package us.parr.animl.cluster

import us.parr.animl.data.*

/** Given a list of vectors, initial centroids, and number of desired clusters k,
 *  return a list of k centroids and a list of clusters (lists of vectors).
 */
fun kmeans(data : List<DoubleVector>, initialCentroids: List<DoubleVector>, k : Int)
        : Pair<List<DoubleVector>, List<List<DoubleV