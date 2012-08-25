package ch.agent.t2.timeseries.junit;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTestsExceptOne {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTestsExceptOne.class.getName());
		//$JUnit-BEGIN$
		// do not include TimeSeriesFactoryTest in the suite
		suite.addTestSuite(TimeSeriesFactoryBootstrapTest.class);
		suite.addTestSuite(AsIndexableTest.class);
		suite.addTestSuite(TimeSeriesTest.class);
		//$JUnit-END$
		return suite;
	}

}
