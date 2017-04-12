package com.nicepeopleatwork.forest.conf;

import com.nicepeopleatwork.krillin.configuration.ConfigurationLoader;

public class ForestConfiguration
{
	private final static String CONFIG_FILE_NAME = "resources/forest.properties";

	// Configuration properties
	private static String DRUID_BROKER = "http://druid-brokers-nl.youbora.com/druid/v2/?pretty";

	private static String [ ] ATTRIBUTES = { "avg_avg_bitrate" , "avg_buffer_ratio" , "avg_buffer_underruns" , "avg_playtime" ,
			"avg_startup_time" , "betterBitrate" , "betterBufferRatio" , "betterBufferUnderruns" ,
			"betterPlayTime" , "betterStartupTime" , "totalVisits" , " numberRevisits", "reciency" };

	private static int DEPTH = 0;

	private static int NUM_TREES = 200;

	private static int NUM_FEATURES = 0;

	private static int BAG_PERCENTAGE = 100;
	
	private static int ADABOOST_ITERATIONS = 20;
	
	private static final int CHURN = 1;
	
	private static final int NOT_CHURN = 0;
	
	private static String FOREST_PATH = "resources/randomForest.model";
	
	private static String TRAIN_DATABASE_PATH = "resources/trainingFile.csv";
	
	private static String TEST_DATABASE_PATH = "resources/testingFile.csv";
	
	private static String PREDICTIONS_PATH = "resources/predictions.csv";
	
	private static String RESULTS_PATH = "resources/results.csv";
	
	private static class InstanceHolder {
		public static ForestConfiguration instance = new ForestConfiguration();
	}

	public static ForestConfiguration getInstance() {
		return InstanceHolder.instance;
	}

	private ForestConfiguration() 
	{
		try
		{
			ConfigurationLoader.loadConfiguration(ForestConfiguration.class, CONFIG_FILE_NAME);
		}
		catch ( Exception e )
		{
//			logger.error(MessageException.KRILLIN_EXCEPTION, e);
		}
	}

	public static int getDEPTH ( )
	{
		return DEPTH;
	}

	public static void setDEPTH ( int dEPTH )
	{
		DEPTH = dEPTH;
	}

	public static int getNUM_TREES ( )
	{
		return NUM_TREES;
	}

	public static void setNUM_TREES ( int nUM_TREES )
	{
		NUM_TREES = nUM_TREES;
	}

	public static int getNUM_FEATURES ( )
	{
		return NUM_FEATURES;
	}

	public static void setNUM_FEATURES ( int nUM_FEATURES )
	{
		NUM_FEATURES = nUM_FEATURES;
	}

	public static int getADABOOST_ITERATIONS ( )
	{
		return ADABOOST_ITERATIONS;
	}

	public static void setADABOOST_ITERATIONS ( int aDABOOST_ITERATIONS )
	{
		ADABOOST_ITERATIONS = aDABOOST_ITERATIONS;
	}

	public static String getPREDICTIONS_PATH ( )
	{
		return PREDICTIONS_PATH;
	}

	public static void setPREDICTIONS_PATH ( String pREDICTIONS_PATH )
	{
		PREDICTIONS_PATH = pREDICTIONS_PATH;
	}

	public static String getRESULTS_PATH ( )
	{
		return RESULTS_PATH;
	}

	public static void setRESULTS_PATH ( String rESULTS_PATH )
	{
		RESULTS_PATH = rESULTS_PATH;
	}

	public static String getDRUID_BROKER ( )
	{
		return DRUID_BROKER;
	}

	public static void setATTRIBUTES ( String [ ] aTTRIBUTES )
	{
		ATTRIBUTES = aTTRIBUTES;
	}

	public static String [ ] getATTRIBUTES ( )
	{
		return ATTRIBUTES;
	}

	public static int getBAG_PERCENTAGE ( )
	{
		return BAG_PERCENTAGE;
	}

	public static int getChurn ( )
	{
		return CHURN;
	}

	public static int getNotChurn ( )
	{
		return NOT_CHURN;
	}

	public static String getFOREST_PATH ( )
	{
		return FOREST_PATH;
	}

	public static String getTRAIN_DATABASE_PATH ( )
	{
		return TRAIN_DATABASE_PATH;
	}

	public static String getTEST_DATABASE_PATH ( )
	{
		return TEST_DATABASE_PATH;
	}

	
}
