import org.junit.Test;

import com.nicepeopleatwork.forest.NiceForest;
import com.nicepeopleatwork.forest.conf.Configuration;

public class ForestTest
{
	@Test
	public void test()
	{
		try {
		NiceForest forest = new NiceForest( Configuration.TRAIN_DATABASE_PATH , Configuration.TEST_DATABASE_PATH );
		forest.run ( );
		} catch ( Exception e )
		{
			e.printStackTrace ( );
		}
	}
}
