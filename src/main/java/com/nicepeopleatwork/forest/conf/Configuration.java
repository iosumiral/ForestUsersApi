package com.nicepeopleatwork.forest.conf;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map.Entry;

import java.util.Properties;

public class Configuration
{

	public final static String CONFIG_FILE_NAME = "resources/forest.properties";

	// Configuration properties
	public static String DRUID_BROKER = "http://druid-brokers-nl.youbora.com/druid/v2/?pretty";

	public static int DEPTH = 0;

	public static int NUM_TREES = 200;

	public static int NUM_FEATURES = 12;

	public static int SEED = 4;
	
	public static String FOREST_PATH = "resources/randomForest.model";
	
	public static String TRAIN_DATABASE_PATH = "resources/trainingFile.csv";
	
	public static String TEST_DATABASE_PATH = "resources/testingFile.csv";
	
	public static String CHECK_DATABASE_PATH = "resources/checkingFile.csv";
	
	public static String PREDICTIONS_PATH = "resources/predictions.csv";

	public static int DEFAULT_PORT = 8090;
	
	public static int CONTAINER_THREAD_POOL = 32;
	
	private Properties properties = null;
	
	private static class InstanceHolder
	{
		public static Configuration instance = new Configuration();
	}
	
	public static Configuration getInstance()
	{
		return InstanceHolder.instance;
	}
	
	private Configuration()
	{
		this.properties = new Properties();
	        
		try
		{
		  	File file = new File(CONFIG_FILE_NAME);
		  	
		  	if( !file.exists() )
		  	{
		  		System.out.println("Configuration File Not Found");
		  		return;
		  	}
		  	
		    System.out.println("Configuration File: "+file.getAbsolutePath() );
        	FileInputStream in = new FileInputStream(CONFIG_FILE_NAME);
        	properties.load(in);
        	in.close();
        	
        	loadProperties();
	    }
		catch( Exception ex )
		{
			ex.printStackTrace();
	    }
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void loadProperties()
	{
		Iterator it = properties.entrySet().iterator();

		while ( it.hasNext() )
		{	
			Entry<String,String> values = (Entry<String,String>)it.next();
			
			String key   = values.getKey();
			String value = values.getValue();

			Field field;
			
			try 
			{
				field = getClass().getDeclaredField(key);
				field.setAccessible(true);
	
				System.out.println("Configuration :: " + key + " : " + value );
				
				if( field.getType().equals (long.class) )
				{			
					field.setLong( null,  new Long(value).longValue() );
				}
				else if( field.getType().equals ( int.class ) )
				{
					field.setInt(  null,  new Integer(value).intValue() );
				}
				else if( field.getType().equals ( double.class ) )
				{
					field.setDouble(  null,  new Double(value).doubleValue() );
				}
				else if( field.getType().equals ( boolean.class ) )
				{
					field.setBoolean(null,  new Boolean(value).booleanValue() );
				}
				else
				{
					field.set(null, value);
				}
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			} 
		}	
	}

}
