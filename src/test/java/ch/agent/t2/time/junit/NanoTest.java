package ch.agent.t2.time.junit;

import ch.agent.core.KeyedException;
import ch.agent.t2.T2Msg.K;
import ch.agent.t2.time.Resolution;
import ch.agent.t2.time.TimeDomain;
import ch.agent.t2.time.TimeDomainDefinition;
import ch.agent.t2.time.TimeIndex;
import ch.agent.t2.time.TimeTools;
import junit.framework.TestCase;

public class NanoTest extends TestCase {

	private static final int BASEYEAR = 2000;
	private static final TimeDomain nano = new TimeDomainDefinition("time_nsec", Resolution.NSEC, 0L).asTimeDomain();
	
	@Override
	protected void setUp() throws Exception {
	}

	public void test01() {
		assertEquals(BASEYEAR, TimeDomain.BASE_YEAR_FOR_NANO);
	}
	
	public void test02() {
		try {
			nano.time("1900-01-01 00:00:00.000000000");
			fail("exception expected");
		} catch (KeyedException e) {
			assertEquals(K.T1013, e.getMsg().getKey());
		}
	}
	
	public void test03() {
		try {
			nano.time("2010-01-01 00:00:00.000000000");
		} catch (KeyedException e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}
	
	public void test04() {
		try {
			TimeIndex t = nano.time("2010-01-01 00:00:00.000000000");
			assertEquals("2010-01-01 00:00:00.000000000", t.toString());
		} catch (KeyedException e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}
	
	public void test05() {
		try {
			TimeIndex t = nano.time(BASEYEAR + "-01-01 00:00:00.000000000");
			assertEquals(nano.minTime(), t);
			assertEquals("Sat", t.getDayOfWeek().name());
		} catch (KeyedException e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}
	
	public void test06() {
		try {
			TimeIndex t = nano.time("2292-04-10 23:47:16.854775807");
			assertEquals(nano.maxTime(), t);
		} catch (KeyedException e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}
	
	public void test07() {
		try {
			TimeIndex t = nano.time("2292-04-10 23:47:16.854775807");
			t.add(1);
			fail("exception expected");
		} catch (KeyedException e) {
			assertEquals(K.T1075, e.getMsg().getKey());
		}
	}

	public void test08() {
		try {
			TimeIndex t = nano.time(BASEYEAR + "-01-01 00:00:00.000000000");
			t.add(-1);
			fail("exception expected");
		} catch (KeyedException e) {
			assertEquals(K.T1075, e.getMsg().getKey());
		}
	}

	public void test09() {
		try {
			TimeIndex t = nano.time("2017-10-02 00:00:00.000000000");
			assertEquals("Mon", t.getDayOfWeek().name());
			t = nano.time("2200-02-18 00:00:00.000000000");
			assertEquals("Tue", t.getDayOfWeek().name());
		} catch (KeyedException e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}
	
	public void test09b() {
		try {
			TimeIndex t = nano.time("2017-10-02 12:34:29.123456789");
			assertEquals("Mon", t.getDayOfWeek().name());
			t = nano.time("2200-02-18 12:34:29.123456789");
			assertEquals("Tue", t.getDayOfWeek().name());
		} catch (KeyedException e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}
	
	public void test09c() {
		try {
			assertTrue(TimeTools.isLeap(2000));
			assertFalse(TimeTools.isLeap(1910));
			assertTrue(TimeTools.isLeap(1916));
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}
	
	
	public void test10() {
		try {
//			for (int i = 0; i < 100; i+=1) {
//				TimeIndex t = Day.DOMAIN.time(String.format("19%02d-01-01", i));
//				if (t.getDayOfWeek().equals(DayOfWeek.Sat) && TimeTools.isLeap(t.getYear())) {
//					System.out.println(t.toString() + " " + t.getDayOfWeek() + " leap year");
//				}
//			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}
	
	
}

