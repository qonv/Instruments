
/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.rf;

import org.junit.Ignore;
import org.junit.Test;
import us.parr.animl.BaseTest;
import us.parr.animl.classifiers.trees.DecisionTree;
import us.parr.animl.classifiers.trees.RandomForest;
import us.parr.animl.data.DataTable;
import us.parr.animl.validation.Validation;

import java.net.URL;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static us.parr.animl.data.DataTable.VariableType.CATEGORICAL_INT;
import static us.parr.animl.data.DataTable.VariableType.CATEGORICAL_STRING;
import static us.parr.animl.data.DataTable.VariableType.NUMERICAL_FLOAT;
import static us.parr.animl.data.DataTable.VariableType.TARGET_CATEGORICAL_INT;
import static us.parr.animl.data.DataTable.VariableType.TARGET_CATEGORICAL_STRING;
import static us.parr.animl.data.DataTable.VariableType.UNUSED_INT;
import static us.parr.lib.ParrtCollections.join;

public class TestRFDataSets extends BaseTest {
	// BEGIN GENERATED CODE
	// Generated by genPythonResults() with sizes = {10, 20, 50, 100};
	public static final int[] scikit_sizes = {20,50,75,100};
	double[] poker_hand_training_kfold = {0.42219, 0.41883, 0.41459, 0.41479, 0.41455};
	double[] Heart_wo_NA_kfold    = {0.17859, 0.18520, 0.17509, 0.16831};
	double[] connect_4_kfold      = {0.23283, 0.23254, 0.23216, 0.23206};
	double[] iris_kfold           = {0.06667, 0.06000, 0.06000, 0.06000};
	double[] shuttle_kfold        = {0.00189, 0.00184, 0.00184, 0.00184};
	double[] wine_kfold           = {0.05587, 0.03365, 0.03381, 0.03381};
	double[] house_votes_84_kfold = {0.08046, 0.05977, 0.06437, 0.06667};
	double[] higgs_kfold          = {0.0, 0.0, 0.25551, 0.0}; // fake 'cept for 50
	// END GENERATED CODE

	public static final int MIN_LEAF_SIZE = 20;

	@Ignore @Test public void testRestaurantOnTrainingSet() {
		DataTable data = DataTable.fromStrings(Arrays.asList(TestDataSets.restaurant));
		int N = 50; // try from 1 to 50 estimators
		int[] missed = trainingDataMisclassifications(data, N, MIN_LEAF_SIZE);
		// randomness is reproducible via same seed in various classes
		int[] expected = new int[] {
			4, 2, 4, 1, 1, 1, 0, 1, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1,
			0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0
		};
		assertArrayEquals(expected, missed);
	}

	@Ignore @Test public void testRestaurantLeaveOneOutError() {
		DataTable data = DataTable.fromStrings(Arrays.asList(TestDataSets.restaurant));
		int N = 50;
		int[] missed = RF_leaveOneOutErrors(data, 1, N, MIN_LEAF_SIZE);
//		System.out.println(Arrays.toString(missed));
		int[] expected = new int[] {
			4, 2, 4, 1, 1, 1, 0, 1, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0,
			1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0
		};
		assertArrayEquals(expected, missed);
	}

	@Ignore @Test public void testRestaurantOOBError() {
		DataTable data = DataTable.fromStrings(Arrays.asList(TestDataSets.restaurant));
//		RandomForest rf = RandomForest.train(data, 200, MIN_NODE_SIZE);
//		double result = rf.getErrorEstimate(data);
//		System.out.println(result);
		int N = 100; // try from 1 to 100 estimators
		for (int k = 1; k<=N; k++) {
			RandomForest rf = new RandomForest(k, MIN_LEAF_SIZE);
			rf.train(data);
			double result = rf.getErrorEstimate(data);
			System.out.println(result);
		}
	}

	@Ignore @Test public void testWebsiteSignups() {
		DataTable data = DataTable.fromStrings(Arrays.asList(TestDataSets.signups));
		DecisionTree tree = new DecisionTree(0, MIN_LEAF_SIZE);
		tree.train(data);
		// I verified this string by looking at DOT output
		// I get same tree has shown here: http://www.patricklamle.com/Tutorials/Decision%20tree%20python/tuto_decision%20tree.html
		String expecting = "{'var':'referrer','cat':'google','n':16,'E':'1.51','left':{'var':'pageviews','val':19.5,'n':5,'E':'1.37','left':{'var':'readfaq','cat':'yes','n':2,'E':'1.00','left':{'predict':'Basic','n':1},'right':{'predict':'None','n':1}},'right':{'predict':'Premium','n':3}},'right':{'var':'referrer','cat':'slashdot','n':11,'E':'0.99','left':{'predict':'None','n':3},'right':{'var':'readfaq','cat':'yes','n':8,'E':'0.95','left':{'predict':'Basic','n':4},'right':{'var':'pageviews','val':20.0,'n':4,'E':'0.81','left':{'predict':'None','n':3},'right':{'predict':'Basic','n':1}}}}}";
		String result = toTestString(tree);
		System.out.println(tree.toDOT());
		assertEquals(expecting, result);
		checkPredictions(data.getRows(), tree);
	}

	@Ignore @Test public void testHeart() {
		DataTable data = heartData();
		int m = 4; // sqrt(13) columns
		DecisionTree tree = new DecisionTree(0, MIN_LEAF_SIZE);
		DecisionTree.debug=true;
		tree.train(data);

		System.out.println(tree.toDOT());

		int missed = Validation.leaveOneOut(tree, data);
		assertEquals(0, missed);
		System.out.println(missed);
		// I verified this string by looking at DOT output
//		String expecting = "{'var':'Thal','val':'reversable','n':303,'E':'1.00','left':{'var':'Ca','val':1,'n':186,'E':'0.84','left':{'var':'MaxHR','val':162,'n':127,'E':'0.55','left':{'var':'Oldpeak','val':2.799999952316284,'n':64,'E':'0.79','left':{'var':'Chol','val':237,'n':59,'E':'0.69','left':{'var':'ExAng','val':1,'n':30,'E':'0.35','left':{'predict':'No','n':23},'right':{'var':'Oldpeak','val':1.2000000476837158,'n':7,'E':'0.86','left':{'var':'Age','val':52,'n':3,'E':'0.92','left':{'predict':'No','n':1},'right':{'predict':'Yes','n':2}},'right':{'predict':'No','n':4}}},'right':{'var':'Oldpeak','val':1.7999999523162842,'n':29,'E':'0.89','left':{'var':'Age','val':59,'n':27,'E':'0.83','left':{'var':'RestBP','val':110,'n':14,'E':'0.37','left':{'var':'Sex','val':1,'n':3,'E':'0.92','left':{'predict':'No','n':2},'right':{'predict':'Yes','n':1}},'right':{'predict':'No','n':11}},'right':{'var':'Age','val':61,'n':13,'E':'1.00','left':{'predict':'Yes','n':3},'right':{'var':'RestBP','val':155,'n':10,'E':'0.88','left':{'var':'RestECG','val':2,'n':6,'E':'1.00','left':{'var':'Age','val':62,'n':4,'E':'0.81','left':{'predict':'No','n':1},'right':{'predict':'Yes','n':3}},'right':{'predict':'No','n':2}},'right':{'predict':'No','n':4}}}},'right':{'predict':'Yes','n':2}}},'right':{'var':'RestBP','val':120,'n':5,'E':'0.72','left':{'predict':'No','n':1},'right':{'predict':'Yes','n':4}}},'right':{'var':'Age','val':61,'n':63,'E':'0.12','left':{'predict':'No','n':60},'right':{'var':'Age','val':62,'n':3,'E':'0.92','left':{'predict':'Yes','n':1},'right':{'predict':'No','n':2}}}},'right':{'var':'ChestPain','val':'nonanginal','n':59,'E':'0.98','left':{'var':'ChestPain','val':'asymptomatic','n':34,'E':'0.73','left':{'var':'RestBP','val':140,'n':7,'E':'0.99','left':{'var':'Age','val':59,'n':4,'E':'0.81','left':{'predict':'No','n':1},'right':{'predict':'Yes','n':3}},'right':{'predict':'No','n':3}},'right':{'var':'Sex','val':1,'n':27,'E':'0.50','left':{'var':'RestBP','val':138,'n':7,'E':'0.99','left':{'var':'MaxHR','val':169,'n':4,'E':'0.81','left':{'predict':'No','n':3},'right':{'predict':'Yes','n':1}},'right':{'predict':'Yes','n':3}},'right':{'predict':'Yes','n':20}}},'right':{'var':'Thal','val':'normal','n':25,'E':'0.86','left':{'predict':'Yes','n':3},'right':{'var':'RestECG','val':2,'n':22,'E':'0.68','left':{'predict':'No','n':10},'right':{'var':'Chol','val':246,'n':12,'E':'0.92','left':{'predict':'Yes','n':3},'right':{'var':'Chol','val':319,'n':9,'E':'0.50','left':{'predict':'No','n':7},'right':{'var':'Age','val':65,'n':2,'E':'1.00','left':{'predict':'Yes','n':1},'right':{'predict':'No','n':1}}}}}}}},'right':{'var':'Oldpeak','val':0.800000011920929,'n':117,'E':'0.79','left':{'var':'RestBP','val':124,'n':38,'E':'1.00','left':{'var':'Age','val':43,'n':11,'E':'0.68','left':{'predict':'Yes','n':1},'right':{'var':'RestBP','val':101,'n':10,'E':'0.47','left':{'var':'Age','val':58,'n':2,'E':'1.00','left':{'predict':'No','n':1},'right':{'predict':'Yes','n':1}},'right':{'predict':'No','n':8}}},'right':{'var':'Age','val':52,'n':27,'E':'0.95','left':{'predict':'Yes','n':7},'right':{'var':'Chol','val':229,'n':20,'E':'1.00','left':{'var':'Age','val':59,'n':9,'E':'0.76','left':{'predict':'No','n':6},'right':{'var':'Age','val':64,'n':3,'E':'0.92','left':{'predict':'Yes','n':2},'right':{'predict':'No','n':1}}},'right':{'var':'Age','val':59,'n':11,'E':'0.85','left':{'predict':'Yes','n':6},'right':{'var':'MaxHR','val':159,'n':5,'E':'0.97','left':{'predict':'No','n':2},'right':{'var':'Chol','val':254,'n':3,'E':'0.92','left':{'predict':'No','n':1},'right':{'predict':'Yes','n':2}}}}}}},'right':{'var':'MaxHR','val':145,'n':79,'E':'0.51','left':{'predict':'Yes','n':50},'right':{'var':'ChestPain','val':'asymptomatic','n':29,'E':'0.89','left':{'var':'Age','val':40,'n':5,'E':'0.72','left':{'predict':'Yes','n':1},'right':{'predict':'No','n':4}},'right':{'var':'ChestPain','val':'nonanginal','n':24,'E':'0.74','left':{'predict':'Yes','n':13},'right':{'var':'Chol','val':231,'n':11,'E':'0.99','left':{'predict':'Yes','n':4},'right':{'var':'Age','val':68,'n':7,'E':'0.86','left':{'predict':'No','n':4},'right':{'var':'RestBP','val':140,'n':3,'E':'0.92','left':{'predict':'No','n':1},'right':{'predict':'Yes','n':2}}}}}}}}}";
//		String result = toTestString(tree);
////		System.out.println(tree.toDOT());
//		assertEquals(expecting, result);
//		checkPredictions(data.getRows(), tree);
	}

	@Ignore @Test public void testHeartOnTrainingSet() {
		DataTable data = heartData();
		int N = 50;
		int[] missed = trainingDataMisclassifications(data, N, MIN_LEAF_SIZE);
		// randomness is reproducible via same seed in various classes
		System.out.println(Arrays.toString(missed));
		int[] expected = new int[] {
			47, 44, 42, 41, 35, 37, 34, 27, 28, 41, 27, 28, 27, 30, 28, 26, 28,
			27, 28, 23, 26, 25, 30, 23, 27, 28, 28, 27, 31, 26, 24, 33, 28, 28,
			28, 31, 24, 26, 23, 24, 28, 22, 26, 26, 26, 28, 22, 25, 21, 26
		};
		assertArrayEquals(expected, missed);
	}

	@Ignore @Test public void testHeartLeaveOneOutErrors() {
		DataTable data = heartData();
		int N = 50;
		int[] missed = RF_leaveOneOutErrors(data, 1, N, MIN_LEAF_SIZE);
		int[] expected = new int[] {
			31, 31, 10, 15, 3, 8, 7, 4, 3, 5, 2, 2, 0, 2, 3, 1, 1, 2, 0, 1,
			0, 0, 0, 0, 1, 0, 1, 1, 2, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 1,
			0, 0, 0, 0, 0, 0, 0, 0, 0
		};
		assertArrayEquals(expected, missed);
	}

	@Test public void testHeartkFoldCrossErrors() {
		DataTable data = heartData();
		int kfolds = 5;
		String fileName = "Heart-wo-NA.csv";
		int minLeafSize = MIN_LEAF_SIZE;
		int[] sizes = scikit_sizes;
		RF_kFoldCrossErrors(Heart_wo_NA_kfold, fileName, data, sizes, kfolds, minLeafSize, 0.015);
	}

	@Ignore @Test public void testHeartOOBError() {
		DataTable data = heartData();
		int N = 50;
		double[] missed = new double[N];
		for (int k = 1; k<=N; k++) {
			RandomForest rf = new RandomForest(k, 1);
			rf.train(data);
			missed[k-1] = rf.getErrorEstimate(data);
			System.out.println(missed[k-1]);
		}
	}

	@Ignore @Test public void testIrisLeaveOneOut() {
		URL url = this.getClass().getClassLoader().getResource("iris.csv");
		DataTable data = DataTable.loadCSV(url.getFile().toString(), null, null, null, true);
		int N = 50;
		int[] missed = RF_leaveOneOutErrors(data, 1, N, MIN_LEAF_SIZE);
//		System.out.println(Arrays.toString(missed));
		// bounces but settles on < 0.06 error rate
		int[] expected = new int[] {
			9, 13, 10, 9, 8, 8, 10, 8, 7, 10, 7, 8, 9, 9, 9, 6, 9, 6, 7, 8,
			8, 8, 9, 10, 8, 7, 9, 8, 8, 8, 7, 6, 8, 8, 7, 10, 8, 8, 7, 8, 9,
			7, 7, 10, 7, 8, 7, 9, 10, 8
		};
		assertArrayEquals(expected, missed);
	}

	@Test public void testIriskFoldCrossErrors() {
		String fileName = "iris.csv";
		URL url = this.getClass().getClassLoader().getResource(fileName);
		DataTable data = DataTable.loadCSV(url.getFile(), null, null, null, true);
//		DecisionTree.debug = true;
		int kfolds = 5;
		int minLeafSize = MIN_LEAF_SIZE;
		int[] sizes = {10, 50, 100, 200};
		RF_kFoldCrossErrors(iris_kfold, fileName, data, sizes, kfolds, minLeafSize, 0.03);
	}

	@Ignore @Test public void testIrisOOBError() {
		DataTable data = DataTable.fromStrings(Arrays.asList(TestDataSets.restaurant));
		int N = 50;
		for (int k = 1; k<=N; k++) {
			RandomForest rf = new RandomForest(k, TestRFBasics.MIN_LEAF_SIZE);
			rf.train(data);
			double result = rf.getErrorEstimate(data);
			System.out.println(result);
		}
	}

	@Test public void testWinekFoldErrors() {
		String fileName = "wine.csv";
		URL url = this.getClass().getClassLoader().getResource(fileName);
		DataTable data = DataTable.loadCSV(url.getFile(), null, null, null, true);
		int kfolds = 5;
		int minLeafSize = 20;
		RF_kFoldCrossErrors(wine_kfold, fileName, data, scikit_sizes, kfolds, minLeafSize, 0.05);
	}

	@Ignore @Test public void testConnect4kFoldCrossErrors() {
		String fileName = "connect-4.csv";
		URL url = this.getClass().getClassLoader().getResource(fileName);
		DataTable data = DataTable.loadCSV(url.getFile(), null, null, null, true);
		int kfolds = 5;
		int minLeafSize = 20;
		RF_kFoldCrossErrors(connect_4_kfold, fileName, data, scikit_sizes, kfolds, minLeafSize, 0.035);
	}

	@Test public void testHouseVoteskFoldCrossErrors() {
		String fileName = "house-votes-84.csv";
		URL url = this.getClass().getClassLoader().getResource(fileName);
		DataTable data = DataTable.loadCSV(url.getFile(), null, null, null, true);
//		DecisionTree.debug = true;
		int kfolds = 5;
		int minLeafSize = MIN_LEAF_SIZE;
		RF_kFoldCrossErrors(house_votes_84_kfold, fileName, data, scikit_sizes, kfolds, minLeafSize, 0.03);
	}

	@Test public void testShuttlekFoldCrossErrors() {
		String fileName = "shuttle.csv";
		URL url = this.getClass().getClassLoader().getResource(fileName);
		DataTable data = DataTable.loadCSV(url.getFile(), null, null, null, true);
//		DecisionTree.debug = true;
		int kfolds = 5;
		int minLeafSize = MIN_LEAF_SIZE;
		RF_kFoldCrossErrors(shuttle_kfold, fileName, data, scikit_sizes, kfolds, minLeafSize, 0.03);
	}

	@Ignore @Test public void testHiggskFoldCrossErrors() {
		int kfolds = 5;

		String fileName = "/Users/parrt/data/higgs10000.csv"; // too big to add to resources; intellij copies to target
		int minLeafSize = 20;
		DataTable.VariableType[] colTypes = new DataTable.VariableType[28+1];
		for (int i = 0; i<colTypes.length-1; i++) {
			colTypes[i] = NUMERICAL_FLOAT;
		}
		colTypes[colTypes.length-1] = TARGET_CATEGORICAL_INT;
		DataTable data = DataTable.loadCSV(fileName, colTypes, true);

//		int[] sizes = {1, 5, 10, 20};
		int[] sizes = scikit_sizes;
		RF_kFoldCrossErrors(higgs_kfold, fileName, data, sizes, kfolds, minLeafSize, 0.015);
	}

	@Test public void testPokerkFoldCrossErrors() {
		String fileName = "poker-hand-training.csv";
		URL url = this.getClass().getClassLoader().getResource(fileName);
		DataTable data = DataTable.loadCSV(url.getFile(), null, null, null, true);
//		DecisionTree.debug = true;
		int kfolds = 5;
		int minLeafSize = MIN_LEAF_SIZE;
		RF_kFoldCrossErrors(poker_hand_training_kfold, fileName, data, scikit_sizes, kfolds, minLeafSize, 0.03);
	}

	@Ignore @Test public void testRunWine() {
		String fileName = "wine.csv";
		URL url = this.getClass().getClassLoader().getResource(fileName);
		DataTable data = DataTable.loadCSV(url.getFile(), null, null, null, true);
//		DecisionTree.debug = true;
		RandomForest rf = new RandomForest(5000, 5);
		rf.train(data);
	}

	@Ignore @Test public void testRunHeart() {
		String fileName = "Heart-wo-NA.csv";
		URL url = this.getClass().getClassLoader().getResource(fileName);
		DataTable data = DataTable.loadCSV(url.getFile(), null, null, null, true);
//		DecisionTree.debug = true;
		RandomForest rf = new RandomForest(500, 1);
		rf.train(data);
	}

	@Ignore @Test public void testRunIris() {
		String fileName = "iris.csv";
		URL url = this.getClass().getClassLoader().getResource(fileName);
		DataTable data = DataTable.loadCSV(url.getFile(), null, null, null, true);
		DecisionTree.debug = true;
		RandomForest rf = new RandomForest(500, 1);
		rf.train(data);
	}

	@Ignore @Test public void testRunConnect4() {
		String fileName = "connect-4.csv";
		int n_estimators = 50;
		int minLeafSize = 20;
		double bootstrapSampleRate = 0.5;
		URL url = this.getClass().getClassLoader().getResource(fileName);
		DataTable data = DataTable.loadCSV(url.getFile(), null, null, null, true);
		int nodeSampleSize = (int)(0.15 * data.size());
		nodeSampleSize = 111;
//		DecisionTree.debug = true;
		long start = System.nanoTime();
		RandomForest rf = new RandomForest(n_estimators, minLeafSize, nodeSampleSize, bootstrapSampleRate);
		rf.train(data);
		long stop = System.nanoTime();
		System.out.printf("Fitting %d estimators %d min leaf size %dms\n", n_estimators, minLeafSize,
		                  (stop-start)/(1000*1000));
	}

	@Ignore @Test public void testRunShuttle() {
		String fileName = "shuttle.csv";
		int n_estimators = 100;
		int minLeafSize = 20;
		URL url = this.getClass().getClassLoader().getResource(fileName);
		DataTable data = DataTable.loadCSV(url.getFile(), null, null, null, true);
//		DecisionTree.debug = true;
		long start = System.nanoTime();
		RandomForest rf = new RandomForest(n_estimators, minLeafSize);
		rf.train(data);
		long stop = System.nanoTime();
		System.out.printf("Fitting %d estimators %d min leaf size %dms\n", n_estimators, minLeafSize,
		                  (stop-start)/(1000*1000));
	}

	@Ignore @Test public void testRunHiggs() {
		String fileName = "/Users/parrt/data/higgs400000.csv"; // too big to add to resources; intellij copies to target
//		String fileName = "/Users/parrt/github/AniML/data/small_higgs.csv";
		int n_estimators = 50;
		int minLeafSize = 20;
		long lstart = System.nanoTime();
		DataTable.VariableType[] colTypes = new DataTable.VariableType[28+1];
		for (int i = 0; i<colTypes.length-1; i++) {
			colTypes[i] = NUMERICAL_FLOAT;
		}
		colTypes[colTypes.length-1] = TARGET_CATEGORICAL_INT;
		DataTable data = DataTable.loadCSV(fileName, colTypes, true);
		long lstop = System.nanoTime();
		System.out.printf("Load time %dms\n", (lstop-lstart)/(1000*1000));
		try {Thread.sleep(5000);} catch (Exception e) { }
//		DecisionTree.debug = true;
		long start = System.nanoTime();
		RandomForest rf = new RandomForest(n_estimators, minLeafSize);
		rf.train(data);
		long stop = System.nanoTime();
		System.out.printf("Fitting %d estimators %d min leaf size %dms\n", n_estimators, minLeafSize,
		                  (stop-start)/(1000*1000));
	}

	public static void genPythonResults() {
		String[] fileNames =
			{
				"poker-hand-training.csv",
				"Heart-wo-NA.csv",
				"connect-4.csv",
				"iris.csv",
				"shuttle.csv",
				"wine.csv",
				"house-votes-84.csv"
			};
		int[] sizes = {20, 50, 75, 100, 200};
		System.out.println("public static final int[] scikit_sizes = {"+join(sizes, ",")+"};");
		int kfolds = 5;
		int minLeafSize = 20;
		System.out.println();
		for (String fileName : fileNames) {
			python_RF_kFoldCrossErrors(fileName, sizes, kfolds, minLeafSize);
		}
	}

	// ---------------------------------

	protected DataTable heartData() {
		URL url = this.getClass().getClassLoader().getResource("Heart-wo-NA.csv");
		DataTable data = DataTable.loadCSV(url.getFile().toString(), "excel", null, null, true);
		data.setColType(0, UNUSED_INT); // first column is ID
		data.setColType("Thal", CATEGORICAL_STRING);
		data.setColType("ChestPain", CATEGORICAL_STRING);
		data.setColType("Fbs", CATEGORICAL_INT);
		data.setColType("RestECG", CATEGORICAL_INT);
		data.setColType("ExAng", CATEGORICAL_INT);
		data.setColType("Slope", CATEGORICAL_INT);
		data.setColType("AHD", TARGET_CATEGORICAL_STRING);
		return data;
	}

	public static void main(String[] args) {
		genPythonResults();
	}
}