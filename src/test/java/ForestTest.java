import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import com.nicepeopleatwork.forest.NiceForest;
import com.nicepeopleatwork.forest.conf.ForestConfiguration;

public class ForestTest
{
	private static final Logger logger = LogManager.getLogger(ForestTest.class);
		
	@Test
	public void test()
	{
		try {
			NiceForest forest = new NiceForest( "resources/trainingFile.csv" , "resources/testingFile.csv" );
//			NiceForest forest = new NiceForest (
//					"avg_avg_bitrate,avg_buffer_ratio,avg_buffer_underruns,avg_playtime,avg_startup_time,avg_bitrate,bufferRatio,buffer_underruns,playtime,startup_time,betterBitrate,betterBufferRatio,betterBufferUnderruns,betterPlayTime,betterStartupTime,visitsSameDay" );
//					forest.run ( );
			forest.load ( "." );
			forest.test ( );
			logger.info ( forest.options ( ) );
			logger.info ( forest.accuracy ( ) );
		} catch ( Exception e )
		{
			e.printStackTrace ( );
		}
	}
}
