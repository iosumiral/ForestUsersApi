package com.nicepeopleatwork.forest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.nicepeopleatwork.forest.conf.Configuration;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.NominalPrediction;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;
import weka.filters.unsupervised.attribute.Remove;

public class FUABuilder {

	private long initTimeProcess;

	// save these outside to use other methods
	private RandomForest forest;
	private AdaBoostM1 adaboost;

	// three values to judge the system's accuracy
	private double falsePositives;
	private double falseNegatives;
	private double generalAccuracy;
	
	private Map<Integer,Instance> userIdToRevisit = new ConcurrentHashMap<Integer,Instance>();

	public static void main(String[] args) throws Exception {
		FUABuilder fb = new FUABuilder();
		fb.run();
		
	}

	public double getFalsePositives() {
		return falsePositives;
	}

	public void setFalsePositives(double falsePositives) {
		this.falsePositives = falsePositives;
	}

	public double getFalseNegatives() {
		return falseNegatives;
	}

	public void setFalseNegatives(double falseNegatives) {
		this.falseNegatives = falseNegatives;
	}

	public double getGeneralAccuracy() {
		return generalAccuracy;
	}

	public void setGeneralAccuracy(double generalAccuracy) {
		this.generalAccuracy = generalAccuracy;
	}

	public RandomForest getForest() {
		return forest;
	}

	public AdaBoostM1 getAdaboost() {
		return adaboost;
	}

	public void run() throws Exception {
		// first, create a list of labeled points from the users we have, since
		// we don't care anymore about order
		// Read all the instances in the files
		this.initTimeProcess = System.currentTimeMillis();
		Instances trainData, testData, checkData;
		Remove remove = new Remove();
		remove.setAttributeIndices("last");

		trainData = new DataSource(Configuration.TRAIN_DATABASE_PATH).getDataSet();
		trainData = sanitize(trainData);
		trainData.setClassIndex(0);
		remove.setInputFormat(trainData);
		trainData = Filter.useFilter(trainData, remove);
		
		testData = new DataSource(Configuration.TEST_DATABASE_PATH).getDataSet();
		testData = sanitize(testData);
		testData.setClassIndex(0);
		remove.setInputFormat(testData);
		testData = Filter.useFilter(testData, remove);
		
//		checkData = new DataSource(Configuration.CHECK_DATABASE_PATH).getDataSet();
//		for (Instance i : checkData){
//			userIdToRevisit.put((int) i.value(17), i);
//		}
//		checkData = sanitize(checkData);
//		checkData.setClassIndex(0);
//		remove.setInputFormat(checkData);
//		checkData = Filter.useFilter(checkData, remove);

		System.out.println();
		System.out.println("Creating database...");

		System.out.println("Databases created. Starting classification...");
		System.out.println();

		// Show result time
		System.out.println(
				" (Result) Time to process databases: " + (System.currentTimeMillis() - this.initTimeProcess) + "ms");
		this.initTimeProcess = System.currentTimeMillis();
		System.out.println();

		String[] options = new String[10];

		try {
			synchronized (this) {
				adaboost = new AdaBoostM1();
				forest = new RandomForest();

				// tree depth
				options[0] = "-depth";
				options[1] = "" + Configuration.DEPTH;

				// set number of trees
				options[2] = "-I";
				options[3] = "" + Configuration.NUM_TREES;

				// set seed for the trees
				options[4] = "-S";
				options[5] = "" + Configuration.SEED;

				// number of features
				options[6] = "-K";
				options[7] = "" + Configuration.NUM_FEATURES;

				// sets the number of threads as the maximum possible
				options[8] = "-num-slots";
				options[9] = "" + 0;

				// add all options
				forest.setOptions(options);

				// print the forest options
				for (String op : forest.getOptions()) {
					System.out.print(op + " ");
				}
				System.out.println();

				adaboost.setClassifier(forest);
				// Collect every group of predictions for current model in an
				// ArrayList
				ArrayList<Prediction> predictions = new ArrayList<Prediction>();
				// For each training-testing split pair, train and test the
				// classifier
				Evaluation validation = classify(adaboost, trainData, testData);
				predictions.addAll(validation.predictions());

				// Calculate overall accuracy of current classifier on all
				// splits
				double[] falseValues = calculateFalseValues(predictions);
				setFalsePositives(falseValues[2]);
				setFalseNegatives(falseValues[3]);
				setGeneralAccuracy(calculateAccuracy(predictions));

				// Print current accuracy
				System.out.println(forest.getTechnicalInformation());
				System.out.format(
						"Accuracy: \n" + "\t %.2f percent of the users predicted to stay will go, \n"
								+ "\t %.2f percent of the users predicted to go will stay, \n"
								+ "\t %.2f percent general accuracy of the algorithm. \n",
						getFalsePositives(), getFalseNegatives(), getGeneralAccuracy());
				System.out.println();
				weka.core.SerializationHelper.write(Configuration.FOREST_PATH, adaboost);
				System.out.println("Model saved in " + Configuration.FOREST_PATH);
				
			}

//			adaboost = (AdaBoostM1) weka.core.SerializationHelper.read(Configuration.FOREST_PATH);
			// Show result time
			System.out.println(
					" (Result) Time to train forest: " + (System.currentTimeMillis() - this.initTimeProcess) + "ms");
			this.initTimeProcess = System.currentTimeMillis();
//			System.out.println();
//			File file = new File(Configuration.PREDICTIONS_PATH);
//			
//				// if files dont exist, then create them
//				if (!file.exists()) {
//					file.createNewFile();
//				}
//				FileWriter fw = new FileWriter(file.getAbsoluteFile());
//				BufferedWriter bw = new BufferedWriter(fw);
//				bw.write("id,revisit_prediction,revisit_actual");
//				bw.newLine();
//				for (int i : userIdToRevisit.keySet()) {
//					Instance instance = userIdToRevisit.get(i);
//
//					remove.setInputFormat(instance.dataset());
//					instance.setDataset(null);
//					instance.deleteAttributeAt(17);
//					instance.setDataset(testData);
//					double v = instance.value(0);
//					double c = adaboost.classifyInstance(instance);
//						bw.write(i+","+c+","+v);
//						bw.newLine();
//				}
//				bw.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Evaluation classify(Classifier model, Instances trainingSet, Instances testingSet) throws Exception {
		Evaluation evaluation = new Evaluation(trainingSet);

		model.buildClassifier(trainingSet);
		evaluation.evaluateModel(model, testingSet);

		return evaluation;
	}

	// returns a size 4 array to store the four possibilities: true positive,
	// true negative, false positive, false negative
	// this is a delicate part, it is important to distinguish between false
	// positives and false negatives
	public static double[] calculateFalseValues(ArrayList<Prediction> predictions) {
		double[] posibilities = { 0, 0, 0, 0 };
		// these two are used to calculate the amount of predicted values, true
		// or false
		int totalObtainedPositives = 0;
		int totalObtainedNegatives = 0;
		for (int i = 0; i < predictions.size(); i++) {
			NominalPrediction np = (NominalPrediction) predictions.get(i);
			// let's cast expected value and obtained value
			int obtained = (int) np.predicted(); // this is the value that the
													// algorithm predicts, the
													// value we obtain
			int expected = (int) np.actual(); // this is the true known value,
												// the value that we expect to
												// get

			if (obtained == 1 && expected == 1) {
				// obtained positive, expected positive: true positive
				posibilities[0]++;
				totalObtainedPositives++;
			} else if (obtained == 0 && expected == 0) {
				// obtained negative, expected negative: true negative
				posibilities[1]++;
				totalObtainedNegatives++;
			} else if (obtained == 1 && expected == 0) {
				// obtained positive, expected negative: false positive
				posibilities[2]++;
				totalObtainedPositives++;
			} else if (obtained == 0 && expected == 1) {
				// obtained negative, expected positive: false negative
				posibilities[3]++;
				totalObtainedNegatives++;
			}
		}
		// now let's turn these quantities into percentages
		posibilities[0] = 100 * posibilities[0] / (double) totalObtainedPositives;
		posibilities[1] = 100 * posibilities[1] / (double) totalObtainedNegatives;
		posibilities[2] = 100 * posibilities[2] / (double) totalObtainedPositives;
		posibilities[3] = 100 * posibilities[3] / (double) totalObtainedNegatives;
		return posibilities;
	}

	public static double calculateAccuracy(ArrayList<Prediction> predictions) {
		double correct = 0;
		for (int i = 0; i < predictions.size(); i++) {
			NominalPrediction np = (NominalPrediction) predictions.get(i);
			if (np.predicted() == np.actual())
				correct++;
		}

		return 100 * correct / predictions.size();
	}

	public static Instances sanitize(Instances instances) throws Exception {
		NumericToNominal convert = new NumericToNominal();
		String[] options = new String[2];
		options[0] = "-R";
		options[1] = "first";
		convert.setOptions(options);
		convert.setInputFormat(instances);
		return Filter.useFilter(instances, convert);
	}
	
}
