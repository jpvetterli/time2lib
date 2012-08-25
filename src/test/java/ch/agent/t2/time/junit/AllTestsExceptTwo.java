package ch.agent.t2.time.junit;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTestsExceptTwo {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTestsExceptTwo.class.getName());
		//$JUnit-BEGIN$
		// do not include  TimeDomainManagerTest in the suite
		// do not include  TimeDomainTest in the suite
		suite.addTestSuite(RangeTest.class);
		suite.addTestSuite(TimeLargeTest.class);
		suite.addTestSuite(TimeConversionTest.class);
		suite.addTestSuite(TimeTest.class);
		//$JUnit-END$
		return suite;
	}

}
