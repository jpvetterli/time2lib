package ch.agent.t2.timeutil.junit;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTests.class.getName());
		//$JUnit-BEGIN$
		suite.addTestSuite(DateTimeScannerTest.class);
		suite.addTestSuite(DayExpressionTest.class);
		suite.addTestSuite(TimeUtilTest.class);
		suite.addTestSuite(JavaDateUtilTest.class);
		//$JUnit-END$
		return suite;
	}

}
