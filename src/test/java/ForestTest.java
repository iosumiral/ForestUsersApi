import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import com.nicepeopleatwork.forest.NiceForest;
import com.nicepeopleatwork.forest.beans.ForestInstanceBean;

public class ForestTest
{
	private static final Logger logger = LogManager.getLogger(ForestTest.class);
		
	@Test
	public void test()
	{
		try {
			NiceForest forest1 = new NiceForest( "resources/trainingFile.csv" , "resources/testingFile.csv" );
			forest1.run ( );
			logger.info ( forest1.options ( ) );
			logger.info ( forest1.accuracy ( ) );
			forest1.save ( "resources" );	
			NiceForest forest = new NiceForest (
					"avg_avg_bitrate,avg_buffer_ratio,avg_buffer_underruns,avg_playtime,avg_startup_time,avg_bitrate,bufferRatio,buffer_underruns,playtime,startup_time,betterBitrate,betterBufferRatio,betterBufferUnderruns,betterPlayTime,betterStartupTime,visitsSameDay" );
				
			forest.load ( "resources" );
			Random rand = new Random();
			for ( int i = 0 ; i < 10000000 ; i ++ )
			{
				double p = forest.churnPercentage ( new ForestInstanceBean ( new double [ ] { 0.0 ,
						rand.nextDouble ( ) , rand.nextDouble ( ) , rand.nextDouble ( ) , rand.nextDouble ( ) ,
						rand.nextDouble ( ) , rand.nextDouble ( ) , rand.nextDouble ( ) , rand.nextDouble ( ) ,
						rand.nextDouble ( ) , rand.nextDouble ( ) , rand.nextDouble ( ) , rand.nextDouble ( ) ,
						rand.nextDouble ( ) , rand.nextDouble ( ) , rand.nextDouble ( ) , rand.nextDouble ( ) } ) );
			}
			logger.info ( "Test ended succesfully." );
		} catch ( Exception e )
		{
			e.printStackTrace ( );
		}
	}
}
