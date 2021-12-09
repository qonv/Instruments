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
 *  that's probably still good enough to cluster. That said, if the
 *  cluster maxima are very close together, then a higher tolerance should be
 *  used.
 *
 *  Note: you should normalize the range of your features since this function
 *  uses euclidean distance to compute data point density.
 *
 *  Note: From "A review of mean-shift algorithms for clustering" by
 *  Miguel Á. Carreira-Perpińan
 *  https://pdfs.semanticscholar.org/399e/00c8a1cc5c3d98d3ce76747d3e0fe57c88f5.pdf
 *  "KDEs break down in high dimensions ... Indeed, most successful
 *  applications of mean-shift have been in low-dimensional problems,
 *  in particular image segmentation (using a few features
 *  per pixel, such as color in LAB space)"
 */
fun meanShift(data : List<DoubleVector>,
              bandwidth : Double,
              tolerance : Double = 1e-2,
              mergeTolerance : Double = 1e-2,
              max_blurred_iterations : Int = 20)
        : Triple<List<DoubleVector>, IntArray, Int>
{
    val start = System.nanoTime()
    // first use blurred mean-shift with max_blurred_iterations iterations to get faster
    // initial movement of particles. See comments on that method
    var particles = data.toMutableList() // start particles at all data points
    var count = 0
    if (max_blurred_iterations > 0) {
        do {
            count++
            val new_particles: MutableList<DoubleVector> = particles.map { shift(it, particles, bandwidth) }.toMutableList()
            println("num distinct particles "+ distinct(particles, 3).size)
            val done = count == max_blurred_iterations ||
                       isclose(particles, new_particles, tolerance = tolerance)
            particles = new_particles
        } while (!done)  // until we converge
//    println("Iterations "+count)
//    println("blurred left on here: "+particles.distinct())
    }

    var stillShifting = BooleanArray(data.size, {true})
    count = 0
    do {
        var maxMinDistance = 0.0
        count++
        // update each particle, moving towards nearest density maximum
//        var new_particles = particles.toMutableList()
        for (i in data.indices) {
            if ( !stillShifting[i] ) continue
            val p = shift(particles[i], data, bandwidth)
            val d = euclidean_distance(p, particles[i])
            if ( d > maxMinDistance ) {
                maxMinDistance = d
            }
            if ( d < tolerance ) {
                stillShifting[i] = false
            }
            particles[i] = p
        }
//        val new_particles: List<DoubleVector> = particles.map { shift(it, data, bandwidth) }
//        println("particles "+particles.joinToString { it.toString(3) })
        println("distinct particles "+ distinct(particles, 3))
        // Keep refining when particles move by at least tolerance; they slow down as they approach maxima
//        println("num distinct particles "+ distinct(particles, 3).size)
//        val done = isclose(particles, new_particles, tolerance = tolerance)
//        particles = new_particles
        // stop when the max distance a particle travels is < tolerance
    } while (maxMinDistance > tolerance)  // until we converge
    val stop = System.nanoTime()
    println("Iterations " + count+", time "+(stop-start)/1_000_000+"ms")

    // At this point, particles[i] has converged on maxima for cluster k
    // and the goal is now to assign data[i] to cluster k

    // merge cluster maxima that are within mergeTolerance; unlike the tolerance for
    // continued iteration above, we want to merge maxima that are pretty close.
    return mapVectorsToClusters(particles, data, ndecimals = round(-log10(mergeTolerance)).toInt())
}

fun parallelMeanShift(data : List<DoubleVector>,
                      bandwidth : Double,
                      tolerance : Double = 1e-2,
                      mergeTolerance : Double = 1e-2)
        : Triple<List<DoubleVector>, IntArray, Int>
{
    val n = data.size
    val ncpu = Runtime.getRuntime().availableProcessors()
    val chunkSize = n / ncpu
    val pool = Executors.newFixedThreadPool(ncpu - 1)
    val start = System.nanoTime()

    // first use blurred mean-shift with max_blurred_iterations iterations to get faster
    // initial movement of particles. See comments on that method
    var particles = data.toMutableList() // start particles at all data points
    var count = 0
    val max_blurred_iterations = 20
    if (max_blurred_iterations > 0) {
        // we operate on particles and create new_particles list ala pure functional
        var new_particles: MutableList<DoubleVector> = data.toMutableList()
        do {
            count++
            val jobs = ArrayList<Callable<Unit>>()
            for (i in 0..n-1 step chunkSize) {
                val job = Callable<Unit> {
                    try {
                        val end = min(n-1, i+chunkSize)
//                        println("blurred j = $i..$end")
                        for (j in i..end) {
                            