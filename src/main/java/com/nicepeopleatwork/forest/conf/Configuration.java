package com.nicepeopleatwork.forest.conf;

import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.nicepeopleatwork.krillin.configuration.ConfigurationLoader;

public class Configuration
{
	public static final Logger logger = LogManager.getLogger(Configuration.class);

	public final static String CONFIG_FILE_NAME = "resources/forest.properties";

	// Configuration properties
	public static String DRUID_BROKER = "http://druid-brokers-nl.youbora.com/druid/v2/?pretty";

	public static int DEPTH = 0;

	public static int NUM_TREES = 100;

	public static int NUM_FEATURES = 12;

	public static int NUM_INSTANCES_PER_LEAF = 1;
	
	public static final int CHURN = 0;
	
	public static final int NOT_CHURN = 1;

	public static int SEED = 4;
	
	public static int ADABOOST_ITERATIONS = 25;
	
	public static String FOREST_PATH = "resources/randomForest.model";
	
	public static String TRAIN_DATABASE_PATH = "resources/trainingFile.csv";
	
	public static String TEST_DATABASE_PATH = "resources/testingFile.csv";
	
	public static String CHECK_DATABASE_PATH = "resources/checkingFile.csv";
	
	public static String PREDICTIONS_PATH = "resources/predictions.csv";
	
	public static String RESULTS_PATH = "resources/results.csv";

	public static int DEFAULT_PORT = 8090;
	
	public static int CONTAINER_THREAD_POOL = 32;
	
	private Properties properties = null;
	
//
//	private static class InstanceHolder {
//		public static Configuration instance = new Configuration();
//	}
//
//	public static Configuration getInstance() {
//		return InstanceHolder.instance;
//	}
//
//	private Configuration() 
//	{
//		try
//		{
//			ConfigurationLoader.loadConfiguration(Configuration.class, CONFIG_FILE_NAME);
//		}
//		catch ( Exception e )
//		{
////			logger.error(MessageException.KRILLIN_EXCEPTION, e);
//		}
//	}

}
