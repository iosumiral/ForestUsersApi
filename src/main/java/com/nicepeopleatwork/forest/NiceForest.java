package com.nicepeopleatwork.forest;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.nicepeopleatwork.forest.beans.ViewBean;
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

public class NiceForest
{
	private RandomForest forest;

	private double falseChurn;

	private double falseNotChurn;

	private double generalAccuracy;

	private double rsme;

	private Instances trainData;
	
	private Instances testData;

	/**
	 * Initialise a RandomForest form two .csv files
	 * @param trainingFile
	 * @param testingFile
	 * @throws Exception
	 */
	public NiceForest ( String trainingFile , String testingFile ) throws Exception
	{
		trainData = new DataSource ( trainingFile ).getDataSet ( );
		testData = new DataSource ( testingFile ).getDataSet ( );
	}
	
	/**
	 * Initialise a RandomForest from two lists of ViewBeans
	 * @param trainList
	 * @param testList
	 * @throws Exception
	 */
	public NiceForest ( List < ViewBean > trainList , List < ViewBean > testList ) throws Exception
	{
		trainData = new Instances ( "trainData" , ViewBean.attributes ( ) , 0 );
		testData = new Instances ( "trainData" , ViewBean.attributes ( ) , 0 );
		trainList.stream ( ).forEach ( v -> trainData.add ( v.getInstance ( ) ) );
		testList.stream ( ).forEach ( v -> testData.add ( v.getInstance ( ) ) );
	}
	
	private void initialiseDatasets() throws Exception
	{
		trainData.setClassIndex ( 0 );
		trainData = sanitize ( trainData );
		testData.setClassIndex ( 0 );
		testData = sanitize ( testData );
	}

	/**
	 * Launch the forest. After this method is called, the accuracy can be
	 * checked by other methods such as getFalseChurn(), getGeneralAccuracy()
	 * and GetRsme()
	 * 
	 * @throws Exception
	 */
	public void run ( ) throws Exception
	{
		// first, create a list of labeled points from the users we have, since
		// we don't care anymore about order
		// Read all the instances in the files
		
		initialiseDatasets ( );

		String [ ] forestOptions = new String [ 13 ];
		ArrayList < Prediction > predictions = new ArrayList < Prediction > ( );

		try
		{
			synchronized ( this )
			{
//				adaboost = new AdaBoostM1 ( );
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
				
				// attribute importance
				forestOptions [ 12 ] = "-attribute-importance";

				// add all options
				forest.setOptions ( forestOptions );

				Evaluation evaluation = new Evaluation ( trainData );

				forest.buildClassifier ( trainData );

				// System.out.println(
				// " (Result) Time to create model: " +
				// (System.currentTimeMillis() - this.initTimeProcess) + "ms");

				evaluation.evaluateModel ( forest , testData );
				
				predictions.addAll ( evaluation.predictions ( ) );

				calculateFalseValues ( predictions );

				calculateRSME ( predictions );

				// Print current accuracy
				System.out.format ( "Accuracy: \n" + "\t %.2f probability of false churn, \n"
						+ "\t %.2f probability of false not churn, \n" + "\t %.2f general accuracy of the algorithm. \n"
						+ "\t %.2f Root Square Mean Error. \n" , getFalseChurn ( ) , getFalseNotChurn ( ) ,
						getGeneralAccuracy ( ) , getRsme ( ) );
			}

			BufferedWriter bw = new BufferedWriter ( new FileWriter ( Configuration.PREDICTIONS_PATH ) );
			bw.write ( "churn_percent,churn_actual" );
			bw.newLine ( );
			for ( Prediction p : predictions )
			{
				NominalPrediction np = ( NominalPrediction ) p;
				bw.write ( np.distribution ( ) [ Configuration.CHURN ] + "," + np.actual ( ) );
				bw.newLine ( );
			}
			bw.close ( );

		} catch ( Exception e )
		{
			e.printStackTrace ( );
		}
	}

	private void calculateFalseValues ( ArrayList < Prediction > predictions )
	{
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

	private void calculateRSME ( ArrayList < Prediction > predictions )
	{
		double preRSME = 0;
		for ( int i = 0 ; i < predictions.size ( ) ; i ++ )
		{
			NominalPrediction np = ( NominalPrediction ) predictions.get ( i );
			preRSME += Math.pow ( np.actual ( ) - np.distribution ( ) [ Configuration.CHURN ] , 2 );
		}

		setRsme ( Math.sqrt ( preRSME / predictions.size ( ) ) );
	}

	private static Instances sanitize ( Instances instances ) throws Exception
	{
		NumericToNominal convert = new NumericToNominal ( );
		String [ ] options = new String [ 2 ];
		options [ 0 ] = "-R";
		options [ 1 ] = "first";
		convert.setOptions ( options );
		convert.setInputFormat ( instances );
		return Filter.useFilter ( instances , convert );
	}

	/**
	 * Given a ViewBean, returns the chance of churn.
	 * @param view
	 * @return
	 * @throws Exception
	 */
	public double churnPercentage ( ViewBean view ) throws Exception
	{
		Instances trainData = new Instances ( Configuration.FOREST_PATH , ViewBean.attributes ( ) , 1 );
		trainData.setClassIndex ( 0 );
		Instance instance = view.getInstance ( );
		instance.setDataset ( trainData );
		return forest.distributionForInstance ( instance ) [ Configuration.CHURN ];
	}

	/**
	 * Given that a user has been predicted to churn, the probability that they will actually stay. This number is important, we want it to be as low as possible.
	 * @return
	 */
	public double getFalseChurn ( )
	{
		return falseChurn;
	}

	private void setFalseChurn ( double falsePositives )
	{
		this.falseChurn = falsePositives;
	}

	/**
	 * Given that a user has been predicted to stay, the probability that they will actually churn.
	 * @return
	 */
	public double getFalseNotChurn ( )
	{
		return falseNotChurn;
	}

	private void setFalseNotChurn ( double falseNegatives )
	{
		this.falseNotChurn = falseNegatives;
	}

	/**
	 * General accuracy of the algorithm: predictions that were successful in the test dataframe.
	 * @return
	 */
	public double getGeneralAccuracy ( )
	{
		return generalAccuracy;
	}

	private void setGeneralAccuracy ( double generalAccuracy )
	{
		this.generalAccuracy = generalAccuracy;
	}

	/**
	 * Returns the RandomForest, to check values of it.
	 * @return
	 */
	public RandomForest getForest ( )
	{
		return forest;
	}

	/**
	 * Root Square Mean Error, a measure on how wrong the confidence in predictions is. Anything below 0.5 should be considered acceptable.
	 * @return
	 */
	public double getRsme ( )
	{
		return rsme;
	}

	private void setRsme ( double rsme )
	{
		this.rsme = rsme;
	}

}
