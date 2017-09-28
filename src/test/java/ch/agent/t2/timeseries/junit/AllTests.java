package ch.agent.t2.timeseries.junit;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTests.class.getName());
		//$JUnit-BEGIN$
		suite.addTestSuite(AsIndexableTest.class);
		suite.addTestSuite(TimeSeriesTest.class);
		//$JUnit-END$
		return suite;
	}

}
