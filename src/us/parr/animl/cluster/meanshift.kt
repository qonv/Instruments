/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.cluster

import us.parr.animl.data.DoubleVector
import us.parr.animl.data.distinct
import us.parr.animl.data.euclidean_distance
import us.parr.animl.data.isclose
import java.lang.Math.*
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/** Mean shift algorithm. Given a list of vectors, return a list of density
 *  estimate maxima, a mapping of data point index to cluster number 0..k-1,
 *  and k (number of clusters).
 *
 *  The blurred meaning-shift mechanism rapidly converges but it's
 *  harder to tell when to stop iterating (using particle deltas)
 *  because the points will eventually merge together.  My approach is
 *  to use the blurred shift to get good approximations as a head start
 *  for the particles. Then, using the regular mean-shift, iterate more
 *  stably to the cluster maxima. We don't actually need very precise
 *  Maxima estimates because all we really care about is assigning
 *  vectors to clusters. If the maxima are off even by as much as 0.01,
 *  that's probably still good enough to cluster. That said, if th