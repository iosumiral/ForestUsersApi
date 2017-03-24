package com.nicepeopleatwork.forest;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import com.nicepeopleatwork.forest.beans.UserBean;
import com.nicepeopleatwork.forest.conf.Configuration;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.NominalPrediction;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;
import weka.filters.unsupervised.attribute.Remove;

public class FUABuilder
{

	private long initTimeProcess;

	// save these outside to use other methods
	private RandomForest forest;

	private AdaBoostM1 adaboost;

	// three values to judge the system's accuracy
	private double falseChurn;

	private double falseNotChurn;

	private double generalAccuracy;

	private double rsme;

	private static BufferedWriter writer;

	private String trainingFile;

	private String testingFile;

	private Map < Integer , Instance > userIdToRevisit = new ConcurrentHashMap < Integer , Instance > ( );

	public FUABuilder ( String trainingFile , String testingFile )
	{
		this.trainingFile = trainingFile;
		this.testingFile = testingFile;
	}

	public static void main ( String [ ] args ) throws Exception
	{
		writer = new BufferedWriter ( new FileWriter ( Configuration.PREDICTIONS_PATH ) );
		writer.write ( "iterations,numTrees,numFeatures,falseChurn,falseNotChurn,accuracy,rsme,test_time" );
		writer.newLine ( );
		for ( int depth = 0 ; depth < 15 ; depth ++ )
			for ( int iterations = 10 ; iterations < 80 ; iterations ++ )
				for ( int numTrees = 10 ; numTrees < 200 ; numTrees = numTrees + 10 )
					for ( int numFeatures = 0 ; numFeatures < 16 ; numFeatures ++ )
					{
						System.out.format ( "Attempting with depth %d, %d adaboost iterations, %d trees in the forest and %d max features on each tree%n" , depth , iterations , numTrees , numFeatures );
						Configuration.NUM_FEATURES = numFeatures;
						Configuration.ADABOOST_ITERATIONS = iterations;
						Configuration.NUM_TREES = numTrees;
						Configuration.DEPTH = depth;
						FUABuilder fb = new FUABuilder ( Configuration.TRAIN_DATABASE_PATH ,
								Configuration.TEST_DATABASE_PATH );
						fb.run ( );
					}
		writer.close ( );
	}

//	public static void main ( String [ ] args ) throws Exception
//	{
//		FUABuilder fb = new FUABuilder ( Configuration.TRAIN_DATABASE_PATH , Configuration.TEST_DATABASE_PATH );
//		fb.run ( );
//		Random rand = new Random ( );
//		List < UserBean > list = new ArrayList <> ( );
//		for ( int i = 0 ; i < 5000 ; i ++ )
//		{
//			UserBean user = new UserBean ( 0 , rand.nextDouble ( ) , rand.nextDouble ( ) , rand.nextDouble ( ) ,
//					rand.nextDouble ( ) , rand.nextDouble ( ) , rand.nextDouble ( ) , rand.nextDouble ( ) ,
//					rand.nextDouble ( ) , rand.nextDouble ( ) , rand.nextDouble ( ) , rand.nextDouble ( ) ,
//					rand.nextDouble ( ) , rand.nextDouble ( ) , rand.nextDouble ( ) , rand.nextDouble ( ) ,
//					rand.nextDouble ( ) );
//			list.add ( user );
//		}
//		long t = System.currentTimeMillis ( );
//		for ( UserBean user : list )
//		{
//			double d = fb.churnPercentage ( user );
//		}
//		t = System.currentTimeMillis ( ) - t;
//		System.out.println ( "t: " + t );
//	}

	public double getFalseChurn ( )
	{
		return falseChurn;
	}

	public void setFalseChurn ( double falsePositives )
	{
		this.falseChurn = falsePositives;
	}

	public double getFalseNotChurn ( )
	{
		return falseNotChurn;
	}

	public void setFalseNotChurn ( double falseNegatives )
	{
		this.falseNotChurn = falseNegatives;
	}

	public double getGeneralAccuracy ( )
	{
		return generalAccuracy;
	}

	public void setGeneralAccuracy ( double generalAccuracy )
	{
		this.generalAccuracy = generalAccuracy;
	}

	public RandomForest getForest ( )
	{
		return forest;
	}

	public AdaBoostM1 getAdaboost ( )
	{
		return adaboost;
	}

	public double getRsme ( )
	{
		return rsme;
	}

	public void setRsme ( double rsme )
	{
		this.rsme = rsme;
	}

	public void run ( ) throws Exception
	{
		// first, create a list of labeled points from the users we have, since
		// we don't care anymore about order
		// Read all the instances in the files
		this.initTimeProcess = System.currentTimeMillis ( );
		Instances trainData , testData , checkData;
		Remove remove = new Remove ( );
		remove.setAttributeIndices ( "last" );

		trainData = new DataSource ( trainingFile ).getDataSet ( );
		trainData = sanitize ( trainData );
		trainData.setClassIndex ( 0 );
		remove.setInputFormat ( trainData );
		trainData = Filter.useFilter ( trainData , remove );

		testData = new DataSource ( testingFile ).getDataSet ( );
		testData = sanitize ( testData );
		testData.setClassIndex ( 0 );
		remove.setInputFormat ( testData );
		testData = Filter.useFilter ( testData , remove );

		// System.out.println();
		// System.out.println("Creating database...");
		//
		// System.out.println("Databases created. Starting classification...");
		// System.out.println();

		// Show result time
		// System.out.println(
		// " (Result) Time to process databases: " + (System.currentTimeMillis()
		// - this.initTimeProcess) + "ms");
		// this.initTimeProcess = System.currentTimeMillis();
		// System.out.println();

		String [ ] forestOptions = new String [ 12 ];
		String [ ] adaBoostOptions = new String [ 2 ];
		ArrayList < Prediction > predictions = new ArrayList < Prediction > ( );

		try
		{
			synchronized ( this )
			{
				adaboost = new AdaBoostM1 ( );
				forest = new RandomForest ( );

				// tree depth
				forestOptions [ 0 ] = "-depth";
				forestOptions [ 1 ] = "" + Configuration.DEPTH;

				// set number of trees
				forestOptions [ 2 ] = "-I";
				forestOptions [ 3 ] = "" + Configuration.NUM_TREES;

				// set seed for the trees
				forestOptions [ 4 ] = "-S";
				forestOptions [ 5 ] = "" + Configuration.SEED;

				// number of features
				forestOptions [ 6 ] = "-K";
				forestOptions [ 7 ] = "" + Configuration.NUM_FEATURES;

				// sets the number of threads as the maximum possible
				forestOptions [ 8 ] = "-num-slots";
				forestOptions [ 9 ] = "" + 0;

				// Set minimum number of instances per leaf
				forestOptions [ 10 ] = "-M";
				forestOptions [ 11 ] = "" + Configuration.NUM_INSTANCES_PER_LEAF;

				// add all options
				forest.setOptions ( forestOptions );

				adaBoostOptions [ 0 ] = "-I";
				adaBoostOptions [ 1 ] = "" + Configuration.ADABOOST_ITERATIONS;

				// print the forest options
				// for (String op : forest.getOptions()) {
				// System.out.print(op + " ");
				// }
				// System.out.println();

				adaboost.setClassifier ( forest );

				Evaluation evaluation = new Evaluation ( trainData );

				adaboost.buildClassifier ( trainData );

				// System.out.println(
				// " (Result) Time to create model: " +
				// (System.currentTimeMillis() - this.initTimeProcess) + "ms");
				this.initTimeProcess = System.currentTimeMillis ( );

				evaluation.evaluateModel ( adaboost , testData );

				this.initTimeProcess = System.currentTimeMillis ( ) - this.initTimeProcess;

				predictions.addAll ( evaluation.predictions ( ) );

				calculateFalseValues ( predictions );

				calculateAccuracy ( predictions );

				// Print current accuracy
				// System.out.println(forest.getTechnicalInformation());
				// System.out.format ( "Accuracy: \n" + "\t %.2f probability of
				// false churn, \n"
				// + "\t %.2f probability of false not churn, \n" + "\t %.2f
				// general accuracy of the algorithm. \n"
				// + "\t %.2f Root Square Mean Error. \n" , getFalseChurn ( ) ,
				// getFalseNotChurn ( ) ,
				// getGeneralAccuracy ( ) , getRsme ( ) );
				// System.out.println();
				// weka.core.SerializationHelper.write(Configuration.FOREST_PATH,
				// adaboost);
				// System.out.println("Model saved in " +
				// Configuration.FOREST_PATH);
				writer.write ( Configuration.ADABOOST_ITERATIONS + "," + Configuration.NUM_TREES + ","
						+ Configuration.NUM_FEATURES + "," + getFalseChurn ( ) + "," + getFalseNotChurn ( ) + ","
						+ getGeneralAccuracy ( ) + "," + getRsme ( ) + "," + initTimeProcess );
				writer.newLine ( );
				writer.flush ( );
			}

			// adaboost = (AdaBoostM1)
			// weka.core.SerializationHelper.read(Configuration.FOREST_PATH);

			// Show result time
			// System.out.println ( " (Result) Time to train forest: "
			// + ( System.currentTimeMillis ( ) - this.initTimeProcess ) + "ms"
			// );
			// this.initTimeProcess = System.currentTimeMillis ( );

//			BufferedWriter bw = new BufferedWriter ( new FileWriter ( Configuration.PREDICTIONS_PATH ) );
//			bw.write ( "churn_percent,churn_actual" );
//			bw.newLine ( );
//			for ( Prediction p : predictions )
//			{
//				NominalPrediction np = ( NominalPrediction ) p;
//				bw.write ( np.distribution ( ) [ 0 ] + "," + np.actual ( ) );
//				bw.newLine ( );
//			}
//			bw.close ( );

		} catch ( Exception e )
		{
			e.printStackTrace ( );
		}
	}

	public static Evaluation classify ( Classifier model , Instances trainingSet , Instances testingSet )
			throws Exception
	{
		Evaluation evaluation = new Evaluation ( trainingSet );

		model.buildClassifier ( trainingSet );
		evaluation.evaluateModel ( model , testingSet );

		return evaluation;
	}

	// returns a size 4 array to store the four possibilities: true positive,
	// true negative, false positive, false negative
	// this is a delicate part, it is important to distinguish between false
	// positives and false negatives
	public void calculateFalseValues ( ArrayList < Prediction > predictions )
	{
		// these two are used to calculate the amount of predicted values, true
		// or false

		double falseChurn = 0;
		double predictedChurn = 0;
		double falseNotChurn = 0;
		double predictedNotChurn = 0;
		for ( Prediction p : predictions )
		{
			if ( p.predicted ( ) == Configuration.CHURN )
			{
				predictedChurn ++ ;
				if ( p.actual ( ) == Configuration.NOT_CHURN )
				{
					falseChurn ++ ;
				}
			}
			else if ( p.predicted ( ) == Configuration.NOT_CHURN )
			{
				predictedNotChurn ++ ;
				if ( p.actual ( ) == Configuration.CHURN )
				{
					falseNotChurn ++ ;
				}
			}
		}

		setFalseChurn ( falseChurn / predictedChurn );
		setFalseNotChurn ( falseNotChurn / predictedNotChurn );
		setGeneralAccuracy ( 1 - ( falseChurn + falseNotChurn ) / ( predictedChurn + predictedNotChurn ) );
	}

	public void calculateAccuracy ( ArrayList < Prediction > predictions )
	{
		double preRSME = 0;
		for ( int i = 0 ; i < predictions.size ( ) ; i ++ )
		{
			NominalPrediction np = ( NominalPrediction ) predictions.get ( i );
			preRSME += Math.pow ( np.actual ( ) - np.distribution ( ) [ 0 ] , 2 );
		}

		setRsme ( Math.sqrt ( preRSME ) );
	}

	public static Instances sanitize ( Instances instances ) throws Exception
	{
		NumericToNominal convert = new NumericToNominal ( );
		String [ ] options = new String [ 2 ];
		options [ 0 ] = "-R";
		options [ 1 ] = "first";
		convert.setOptions ( options );
		convert.setInputFormat ( instances );
		return Filter.useFilter ( instances , convert );
	}

	public double churnPercentage ( UserBean user ) throws Exception
	{
		Instances trainData = new Instances ( Configuration.FOREST_PATH , attributes ( ) , 1 );
		trainData.setClassIndex ( 0 );
		Instance instance = user.getInstance ( );
		instance.setDataset ( trainData );
		return adaboost.distributionForInstance ( instance ) [ 0 ];
	}

	public static final ArrayList < Attribute > attributes ( )
	{
		// add the attributes names to the database
		// TODO maybe add system to add as many attributes as the instance has
		ArrayList < Attribute > attributes = new ArrayList < Attribute > ( );
		// class value
		attributes.add ( new Attribute ( "revisited" , Arrays.asList ( "0" , "1" ) ) );
		// five averages
		attributes.add ( new Attribute ( "avg_avg_bitrate" ) );
		attributes.add ( new Attribute ( "avg_buffer_ratio" ) );
		attributes.add ( new Attribute ( "avg_buffer_underruns" ) );
		attributes.add ( new Attribute ( "avg_playtime" ) );
		attributes.add ( new Attribute ( "avg_startup_time" ) );
		// five attributes specific for the row
		attributes.add ( new Attribute ( "avg_bitrate" ) );
		attributes.add ( new Attribute ( "buffer_ratio" ) );
		attributes.add ( new Attribute ( "buffer_underruns" ) );
		attributes.add ( new Attribute ( "playtime" ) );
		attributes.add ( new Attribute ( "startup_time" ) );
		// five attributes depending on the row and the previous one
		attributes.add ( new Attribute ( "better_avg_bitrate" ) );
		attributes.add ( new Attribute ( "better_buffer_ratio" ) );
		attributes.add ( new Attribute ( "better_buffer_underruns" ) );
		attributes.add ( new Attribute ( "better_playtime" ) );
		attributes.add ( new Attribute ( "better_startup_time" ) );
		// one more to store the amount of views in the same timeframe
		attributes.add ( new Attribute ( "views_same_day" ) );
		return attributes;
	}

}
