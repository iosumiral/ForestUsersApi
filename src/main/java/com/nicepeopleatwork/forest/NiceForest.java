package com.nicepeopleatwork.forest;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.nicepeopleatwork.forest.beans.ForestInstanceBean;
import com.nicepeopleatwork.forest.conf.ForestConfiguration;
import com.nicepeopleatwork.forest.exceptions.WrongClassifierException;

import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.NominalPrediction;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;

public class NiceForest
{
	private static final Logger logger = LogManager.getLogger(NiceForest.class);
	
	private RandomForest forest;

	private AdaBoostM1 adaboost;

	private double falseChurn;

	private double falseNotChurn;

	private double generalAccuracy;

	private double rsme;

	private Instances trainData;

	private Instances testData;
	
	/**
	 * Create a model without training or testing sets, only a String
	 * representing the attributes, separated by commas. The classifier must
	 * then be then imported by the load() method
	 * 
	 * @param atributes
	 */
	public NiceForest( String atributes )
	{
		String[] range = atributes.split ( "," );
		ForestConfiguration.setATTRIBUTES ( Arrays.copyOfRange ( range , 0 , range.length ) );
		forest = new RandomForest ( );
		adaboost = new AdaBoostM1 ( );
	}

	/**
	 * Create a RandomForest from two .csv files
	 * 
	 * @param trainingFile
	 * @param testingFile
	 * @throws Exception
	 */
	public NiceForest ( String trainingFile , String testingFile ) throws Exception
	{
		BufferedReader bw = new BufferedReader ( new FileReader ( trainingFile ) );
		String[] range = bw.readLine ( ).split ( "," );
		ForestConfiguration.setATTRIBUTES ( Arrays.copyOfRange ( range , 1 , range.length ) );
		bw.close ( );
		trainData = new DataSource ( trainingFile ).getDataSet ( );
		testData = new DataSource ( testingFile ).getDataSet ( );
		forest = new RandomForest ( );
		adaboost = new AdaBoostM1 ( );
		
		initialiseDatasets ( );
	}

	/**
	 * Create a RandomForest from two lists of ViewBeans
	 * 
	 * @param trainList
	 * @param testList
	 * @throws Exception
	 */
	public NiceForest ( List < ForestInstanceBean > trainList , List < ForestInstanceBean > testList ) throws Exception
	{
		trainData = new Instances ( "trainData" , ForestInstanceBean.attributes ( ) , 0 );
		testData = new Instances ( "testData" , ForestInstanceBean.attributes ( ) , 0 );
		trainList.stream ( ).filter ( v -> v != null ).forEach ( v -> trainData.add ( v.getInstance ( ) ) );
		testList.stream ( ).filter ( v -> v != null ).forEach ( v -> testData.add ( v.getInstance ( ) ) );
		forest = new RandomForest ( );
		adaboost = new AdaBoostM1 ( );
		
		initialiseDatasets ( );
	}

	private void initialiseDatasets ( ) throws Exception
	{
		trainData.setClassIndex ( 0 );
		trainData = sanitize ( trainData );
		testData.setClassIndex ( 0 );
		testData = sanitize ( testData );
	}

	/**
	 * Save the model in a folder. It is saved in two different files, as there
	 * are technically two classifiers
	 * 
	 * @param foldername
	 */
	public void save( String foldername) throws Exception
	{
		logger.info ( "Exporting model in file " + foldername + "..." );
		SerializationHelper.write(foldername + "/forest.model", forest);
		SerializationHelper.write(foldername + "/adaboost.model", adaboost);
		
	}
	
	/**
	 * Load model from folder. It is loaded from two different files, as there
	 * are technically two classifiers
	 * 
	 * @param filename
	 */
	public void load( String foldername) throws Exception
	{
		logger.info ( "Importing model from file " + foldername + "..." );
		Object forestObject = SerializationHelper.read ( foldername + "/forest.model" );
		if ( forestObject instanceof RandomForest )
		{
			this.forest = ( RandomForest ) forestObject;
		}
		else
		{
			throw new WrongClassifierException ( );
		}
		Object adaboostObject = SerializationHelper.read ( foldername + "/adaboost.model" );
		if ( forestObject instanceof RandomForest )
		{
			this.adaboost = ( AdaBoostM1 ) adaboostObject;
		}
		else
		{
			throw new WrongClassifierException ( );
		}
		adaboost.setClassifier ( forest );
	}
	
	/**
	 * Train and then test the model
	 * @throws Exception
	 */
	public void run() throws Exception
	{
		train();
		test();
	}
	
	/**
	 * Train the model. This does not ensure accuracy, the test() method must be called for that
	 * 
	 * @throws Exception
	 */
	public void train ( ) throws Exception
	{
		logger.info ( "Training model..." );

		adaboost.setClassifier ( forest );
		
		adaboost.buildClassifier ( trainData );
	}
	
	/**
	 * Test the model and set the general accuracy
	 * @throws Exception
	 */
	public void test() throws Exception
	{
		logger.info ( "Testing model..." );
		
		Evaluation evaluation = new Evaluation ( trainData );
		
		evaluation.evaluateModel ( adaboost , testData );
		
		ArrayList < Prediction > predictions = new ArrayList < Prediction > ( );

		predictions.addAll ( evaluation.predictions ( ) );

		calculateFalseValues ( predictions );

		calculateRSME ( predictions );
	}
	
	/**
	 * Recover the forest options
	 * @return
	 */
	public String options()
	{
		StringBuilder str = new StringBuilder ( );
		for ( String s : forest.getOptions ( ) )
		{
			str.append ( s + " " );
		}
		return str.toString ( );
	}
	
	/**
	 * Recover the model accuracy
	 * @return
	 */
	public String accuracy()
	{
		return String.format (
				"Accuracy: %n" + "\t %.4f probability of false churn, %n" + "\t %.4f probability of false not churn, %n"
						+ "\t %.4f general accuracy of the algorithm. %n" + "\t %.4f Root Square Mean Error. \n" ,
				getFalseChurn ( ) , getFalseNotChurn ( ) , getGeneralAccuracy ( ) , getRsme ( ) );
	}
	
	/**
	 * Set the default options for the model
	 * @throws Exception
	 */
	public void setDefaultOptions() throws Exception
	{
		forest.setOptions ( new String [ ] { "-num-slots" , "0" , "-M" , "1" , "-attribute-importance" , "-K" ,
				ForestConfiguration.getNUM_FEATURES ( ) + "" , "-I" , ForestConfiguration.getNUM_TREES ( ) + "" , "-depth" ,
				ForestConfiguration.getDEPTH ( ) + "" , "-P" , ForestConfiguration.getBAG_PERCENTAGE ( ) + "" } );
		
		adaboost.setNumIterations ( ForestConfiguration.getADABOOST_ITERATIONS ( ) );
	}

	private void calculateFalseValues ( ArrayList < Prediction > predictions )
	{
		double falseChurn = 0;
		double predictedChurn = 0;
		double falseNotChurn = 0;
		double predictedNotChurn = 0;
		for ( Prediction p : predictions )
		{
			if ( p.predicted ( ) == ForestConfiguration.getChurn ( ) )
			{
				predictedChurn ++ ;
				if ( p.actual ( ) == ForestConfiguration.getNotChurn ( ) )
				{
					falseChurn ++ ;
				}
			}
			else if ( p.predicted ( ) == ForestConfiguration.getNotChurn ( ) )
			{
				predictedNotChurn ++ ;
				if ( p.actual ( ) == ForestConfiguration.getChurn ( ) )
				{
					falseNotChurn ++ ;
				}
			}
		}

		this.falseChurn = ( falseChurn / predictedChurn );
		this.falseNotChurn = ( falseNotChurn / predictedNotChurn );
		this.generalAccuracy = ( 1 - ( falseChurn + falseNotChurn ) / ( predictedChurn + predictedNotChurn ) );
	}

	private void calculateRSME ( ArrayList < Prediction > predictions )
	{
		double preRSME = 0;
		for ( int i = 0 ; i < predictions.size ( ) ; i ++ )
		{
			NominalPrediction np = ( NominalPrediction ) predictions.get ( i );
			preRSME += Math.pow ( np.actual ( ) - np.distribution ( ) [ ForestConfiguration.getChurn ( ) ] , 2 );
		}

		this.rsme = ( Math.sqrt ( preRSME / predictions.size ( ) ) );
	}

	private static Instances sanitize ( Instances instances ) throws Exception
	{
		NumericToNominal convert = new NumericToNominal ( );
		convert.setAttributeIndices ( "first" );
		convert.setInputFormat ( instances );
		return Filter.useFilter ( instances , convert );
	}

	/**
	 * Given a forest instance, returns the chance of churn.
	 * 
	 * @param instance
	 * @return
	 * @throws Exception
	 */
	public double churnPercentage ( ForestInstanceBean instance ) throws Exception
	{
		Instances trainData = new Instances ( ForestConfiguration.getFOREST_PATH ( ) , ForestInstanceBean.attributes ( ) , 1 );
		trainData.setClassIndex ( 0 );
		Instance instance1 = instance.getInstance ( );
		instance1.setDataset ( trainData );
		return adaboost.distributionForInstance ( instance1 ) [ ForestConfiguration.getChurn ( ) ];
	}

	/**
	 * Given that a user has been predicted to churn, the probability that they
	 * will actually stay. This number is important, we want it to be as low as
	 * possible.
	 * 
	 * @return
	 */
	public double getFalseChurn ( )
	{
		return falseChurn;
	}

	/**
	 * Given that a user has been predicted to stay, the probability that they
	 * will actually churn.
	 * 
	 * @return
	 */
	public double getFalseNotChurn ( )
	{
		return falseNotChurn;
	}

	/**
	 * General accuracy of the algorithm: predictions that were successful in
	 * the test dataframe.
	 * 
	 * @return
	 */
	public double getGeneralAccuracy ( )
	{
		return generalAccuracy;
	}

	/**
	 * Returns the RandomForest, to check values of it.
	 * 
	 * @return
	 */
	public RandomForest getForest ( )
	{
		return forest;
	}

	/**
	 * Root Square Mean Error, a measure on how wrong the confidence in
	 * predictions is. Anything below 0.5 should be considered acceptable.
	 * 
	 * @return
	 */
	public double getRsme ( )
	{
		return rsme;
	}

	public void setNumTrees ( int trees )
	{
		this.forest.setNumIterations ( trees );
	}

	public void setMaxDepth ( int depth )
	{
		this.forest.setMaxDepth ( depth );
	}

	public void setNumFeatures ( int features )
	{
		this.forest.setNumFeatures ( features );
	}
	
	public void setNumIterations( int iterations )
	{
		this.adaboost.setNumIterations ( iterations );
	}
}
