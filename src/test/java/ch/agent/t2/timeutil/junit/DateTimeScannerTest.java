package ch.agent.t2.timeutil.junit;

import junit.framework.TestCase;
import ch.agent.core.KeyedException;
import ch.agent.t2.T2Msg.K;
import ch.agent.t2.applied.DateTime;
import ch.agent.t2.time.TimeIndex;
import ch.agent.t2.timeutil.DateTimeScanner;
import ch.agent.t2.timeutil.TimeUtil;

public class DateTimeScannerTest extends TestCase {

	TimeUtil util;
	
	public DateTimeScannerTest(String name) {
		super(name);
	}

	public void test1() {
		try {
			DateTimeScanner scanner = new DateTimeScanner(null, null);
			TimeIndex t = scanner.scan(DateTime.DOMAIN, "2010-11-30 14:11:23");
			assertEquals("2010-11-30 14:11:23", t.toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception");
		}
	}
	
	public void test2() {
		try {
			int[] groups = {3,2,1,4,5,6};
			String pattern = "(\\d\\d)\\.(\\d\\d)\\.(\\d\\d) (\\d\\d):(\\d\\d):(\\d\\d)";
			DateTimeScanner scanner = new DateTimeScanner(pattern, groups);
			scanner.setTwoDigitYearThreshold(100);
			TimeIndex t = scanner.scan(DateTime.DOMAIN, "30.11.10 12:29:23");
			assertEquals("2010-11-30 12:29:23", t.toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception");
		}
	}
	
	public void test3() {
		try {
			int[] groups = {3,2,1,4,4,6};
			String pattern = "(\\d\\d)\\.(\\d\\d)\\.(\\d\\d) (\\d\\d):(\\d\\d):(\\d\\d)";
			new DateTimeScanner(pattern, groups);
			fail("exception was expected");
		} catch (KeyedException e) {
			assertEquals(K.T7017, e.getMsg().getKey());
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception");
		}
	}
	
	public void test4() {
		try {
			int[] groups = {6,1,2,3,4,5};
			String pattern = "\\S+ (\\S+) (\\d+) (\\d\\d):(\\d\\d):(\\d\\d) \\S+ (\\d\\d\\d\\d)";
			String[] months = 
				      {"Jan", "Feb", "Mar", "Apr", "May", "Jun", 
				       "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
			DateTimeScanner scanner = new DateTimeScanner(pattern, groups);
			scanner.setMonths(months);
			TimeIndex t = scanner.scan(DateTime.DOMAIN, "Wed Nov 30 12:29:23 CET 2010");
			assertEquals("2010-11-30 12:29:23", t.toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception");
		}
	}
	
	public void test5() {
		try {
			int[] groups = {3,2,1,4,4,6};
			String pattern = "(\\S+) (\\S+) (\\d+) (\\d\\d):(\\d\\d):(\\d\\d) (\\S+) (\\d\\d\\d\\d)";
			new DateTimeScanner(pattern, groups);
			fail("exception was expected");
		} catch (KeyedException e) {
			assertEquals(K.T7016, e.getMsg().getKey());
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception");
		}
	}

	public void test6() {
		try {
			int[] groups = {6,1,2,3,4,5};
			String pattern = "\\S+ (\\S+) (\\d+) (\\d\\d):(\\d\\d):(\\d\\d) \\S+ (\\d\\d\\d\\d)";
			String[] months = 
				      {"Jan", "Feb", "Mar", "Apr", "May", "Jun", 
				       "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
			DateTimeScanner scanner = new DateTimeScanner(pattern, groups);
			scanner.setMonths(months);
			scanner.scan(DateTime.DOMAIN, "Wed Foo 30 12:29:23 CET 2010");
			fail("exception expected");
		} catch (KeyedException e) {
			assertEquals(K.T7019, e.getMsg().getKey());
		}
	}

}
