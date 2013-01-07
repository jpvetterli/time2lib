package ch.agent.t2.time.junit;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTestsExceptTwo {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTestsExceptTwo.class.getName());
		//$JUnit-BEGIN$
		// do not include  TimeDomainManagerTest in the suite, it can only work standalone
		// do not include  TimeLargeTest in the suite, it's not a test
		suite.addTestSuite(TimeTest.class);
		suite.addTestSuite(TimeConversionTest.class);
		suite.addTestSuite(RangeTest.class);
		suite.addTestSuite(TimeDomainTest.class);
		//$JUnit-END$
		return suite;
	}

}
