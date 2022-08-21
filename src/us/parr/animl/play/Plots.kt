
/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.play

import org.knowm.xchart.*
import org.knowm.xchart.style.Styler
import us.parr.animl.cluster.kmeans
import us.parr.animl.cluster.meanShift
import us.parr.animl.data.DataTable
import us.parr.animl.data.DataTable.getAsFloat
import us.parr.animl.data.DoubleVector
import us.parr.animl.data.transpose
import us.parr.lib.ParrtStats.normal
import java.util.Collections.min

fun main(args: Array<String>) {
    //plotGrades()
//    plot2Gaussian()
    plot3GaussianMeanShift()
//    plotSpiralMeanShift()
}

private fun plotGrades() {
    // Create Chart
    val chart = CategoryChartBuilder().width(800).height(600).title("Score Histogram").xAxisTitle("Mean").yAxisTitle("Count").build()

    // Customize Chart
    chart.styler.legendPosition = Styler.LegendPosition.InsideNW
    chart.styler.availableSpaceFill = .96
    chart.styler.isOverlapped = true
    chart.styler.setXAxisDecimalPattern("#00.0")

    val grades = doubleArrayOf(
            92.65, 93.87, 74.06, 86.94, 92.26, 94.46, 92.94, 80.65, 92.86,
            85.94, 91.79, 95.23, 85.37, 87.85, 87.71, 93.03
    )
    val histogram1 = Histogram(grades.toList(), 30, min(grades.toList()), 100.0)
    chart.addSeries("histogram 1", histogram1.getxAxisData(), histogram1.getyAxisData())
    SwingWrapper<CategoryChart>(chart).displayChart()
}

fun plot2Gaussian() {
    val chart = XYChartBuilder().width(800).height(600).build()

    // Customize Chart
    chart.styler.defaultSeriesRenderStyle = XYSeries.XYSeriesRenderStyle.Scatter
    chart.styler.isChartTitleVisible = false
    chart.styler.legendPosition = Styler.LegendPosition.InsideSW
    chart.styler.xAxisMax = 10.0
    chart.styler.xAxisMin = -3.0
    chart.styler.yAxisMax = 9.0
    chart.styler.yAxisMin = -3.0
    chart.styler.markerSize = 10

    val n = 1000
    val cluster1: List<DoubleVector> = (1..n).map { DoubleVector(normal(0.0, 1.0), normal(0.0, 1.0)) }
    val cluster2: List<DoubleVector> = (1..n).map { DoubleVector(normal(6.0, 1.5), normal(4.0, 1.5)) }
    val data = cluster1 + cluster2
    val xData = mutableListOf<Double>()
    val yData = mutableListOf<Double>()
    for (i in data.indices) {
        xData.add(data[i][0])
        yData.add(data[i][1])
    }
    chart.addSeries("Gaussian Blob", xData, yData)

    val centroids = mutableListOf<DoubleVector>(DoubleVector(-10.0,0.9), DoubleVector(4.0,9.0))
    val (new_centroids, clusters) = kmeans(data, centroids, k=2)

    val xCentroid = mutableListOf<Double>()
    val yCentroid = mutableListOf<Double>()
    for (i in new_centroids.indices) {
        xCentroid.add(new_centroids[i][0])
        yCentroid.add(new_centroids[i][1])
    }
    chart.addSeries("Centroids", xCentroid, yCentroid)

    SwingWrapper(chart).displayChart()
}

fun plot3GaussianMeanShift() {
    val chart = XYChartBuilder().width(800).height(600).build()

    // Customize Chart
    chart.styler.defaultSeriesRenderStyle = XYSeries.XYSeriesRenderStyle.Scatter
    chart.styler.isChartTitleVisible = false
    chart.styler.legendPosition = Styler.LegendPosition.InsideSW
    chart.styler.xAxisMax = 15.0
    chart.styler.yAxisMax = 15.0
    chart.styler.markerSize = 10

    val n = 1000
    val cluster1: List<DoubleVector> = (1..n).map { DoubleVector(normal(0.0, 1.0), normal(0.0, 1.0)) }
    val cluster2: List<DoubleVector> = (1..n).map { DoubleVector(normal(6.0, 2.5), normal(4.0, 1.0)) }
    val cluster3: List<DoubleVector> = (1..n).map { DoubleVector(normal(2.0, 1.5), normal(9.0, 1.0)) }
    val data = cluster1 + cluster2 + cluster3

    val bandwidth = 1.2
    val (maxima, pointToClusters, k) = meanShift(data, bandwidth, 1e-2, max_blurred_iterations = 15)
//    val (maxima, pointToClusters, k) = blurredMeanShift(data, bandwidth)
    // TODO: try groupBy to create list of clusters
    val clusters = Array<MutableList<DoubleVector>>(k, init={mutableListOf()})
    for (i in pointToClusters.indices) {
        clusters[pointToClusters[i]].add(data[i])
    }

    var i = 0
    for (cluster in clusters) {
        val columns: List<DoubleVector> = transpose(cluster)
        chart.addSeries("cluster "+i, columns[0].elements, columns[1].elements)
        i++
    }

    val xCentroid = mutableListOf<Double>()
    val yCentroid = mutableListOf<Double>()
    for (i in maxima.indices) {
        xCentroid.add(maxima[i][0])
        yCentroid.add(maxima[i][1])
    }
    chart.addSeries("Centroids", xCentroid, yCentroid)

    SwingWrapper(chart).displayChart()
}

fun plotSpiralMeanShift() {
    val chart = XYChartBuilder().width(800).height(600).build()

    // Customize Chart
    chart.styler.defaultSeriesRenderStyle = XYSeries.XYSeriesRenderStyle.Scatter
    chart.styler.isChartTitleVisible = false
    chart.styler.legendPosition = Styler.LegendPosition.InsideSW
//    chart.styler.xAxisMin = 29.4
//    chart.styler.yAxisMax = 15.0
    chart.styler.markerSize = 10

    val fileName = "clustering/artificial/3-spiral.csv"
//    val fileName = "clustering/artificial/play.csv"
    val url = DataTable().javaClass.getClassLoader().getResource(fileName)
    val csv = DataTable.loadCSV(url!!.getFile(), null, null, null, true)
//    val subset = bootstrapWithRepl(csv.rows, 45)
    val subset = csv.rows
    var data = mutableListOf<DoubleVector>()
    for (i in 0..subset.size-1) {
        val x = getAsFloat(subset[i][0]).toDouble()
        val y = getAsFloat(subset[i][1]).toDouble()
        data.add(DoubleVector(x,y))
    }
//    println(data)

//    val cluster1: MutableList<DoubleVector> = mutableListOf<DoubleVector>(
//            DoubleVector(0.298743,0.799992),
//            DoubleVector(0.299058,0.799996)
////            DoubleVector(0.299372,0.799998)
////            DoubleVector(0.299686,0.8)
//    )
//    val cluster2 = mutableListOf<DoubleVector>(
//            DoubleVector(0.305,0.793),
//            DoubleVector(0.3045,0.793)
////            DoubleVector(0.304,0.793)
////            DoubleVector(0.3035,0.793)
//            )
//    data = (cluster1 + cluster2).toMutableList()
//
//    data = data.map { it * 10.0 }.toMutableList()

    val bandwidth = 1.43
    val (maxima, pointToClusters, k) =
            meanShift(data, bandwidth, tolerance = 1e-9, mergeTolerance = 1e-4, max_blurred_iterations = 0)
//    val (maxima, pointToClusters, k) = blurredMeanShift(data, bandwidth, tolerance = 1e-9, mergeTolerance = 1e-4, iterations = 2000)
    // TODO: try groupBy to create list of clusters
    val clusters = Array<MutableList<DoubleVector>>(k, init = { mutableListOf() })
    for (i in pointToClusters.indices) {
        clusters[pointToClusters[i]].add(data[i])
    }

    var i = 0
    for (cluster in clusters) {
        val columns: List<DoubleVector> = transpose(cluster)
        chart.addSeries("cluster " + i, columns[0].elements, columns[1].elements)
        i++
    }

    val xCentroid = mutableListOf<Double>()
    val yCentroid = mutableListOf<Double>()
    for (i in maxima.indices) {
        xCentroid.add(maxima[i][0])
        yCentroid.add(maxima[i][1])
    }
    chart.addSeries("Centroids", xCentroid, yCentroid)

    SwingWrapper(chart).displayChart()
}