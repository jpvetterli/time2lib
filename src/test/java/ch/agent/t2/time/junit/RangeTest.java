package ch.agent.t2.time.junit;

import junit.framework.TestCase;
import ch.agent.core.KeyedException;
import ch.agent.t2.T2Msg.K;
import ch.agent.t2.time.Adjustment;
import ch.agent.t2.time.DateTime;
import ch.agent.t2.time.Day;
import ch.agent.t2.time.Range;
import ch.agent.t2.time.SystemTime;
import ch.agent.t2.time.ThirdFriday;
import ch.agent.t2.time.TimeDomain;
import ch.agent.t2.time.TimeIndex;
import ch.agent.t2.time.Workday;
import ch.agent.t2.time.Year;


public class RangeTest extends TestCase {

	private static void dump(Object expr) {
		// System.out.println(expr);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testA1() {
		try {
			int size = 25;
			TimeIndex wtmax = Day.DOMAIN.maxTime();
			TimeIndex wtstart = wtmax.add(-(size - 1));
			Range r = new Range(wtstart, wtmax);
			int i = 0;
			for (TimeIndex t : r) {
				if (++i > size) {
					fail("loop continues after range exhausted: " + i);
				}
				dump(t);
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}
	
	public void test1() {
		try {
			Range r = new Range(Day.DOMAIN, "2005-03-06", "2005-03-07", Adjustment.NONE);
			assertEquals(2, r.getSize());
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception");
		}
	}
	
	public void test2() {
		try {
			Range r = new Range(Day.DOMAIN, "2005-03-06", "2005-03-06", Adjustment.NONE);
			assertEquals(1, r.getSize());
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception");
		}
	}
	
	public void test3() {
		try {
			Range r = new Range(Day.DOMAIN, "2005-03-06", "2005-03-05", Adjustment.NONE);
			assertEquals(0, r.getSize());
			assertTrue(r.isEmpty());
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception");
		}
	}
	
	public void test4() {
		try {
			Range r = new Range(SystemTime.DOMAIN, "1900-01-01", "2099-12-31", Adjustment.NONE);
			r.getSizeAsInt();
			fail("exception expected");
		} catch (KeyedException e) {
			assertEquals(K.T5008, e.getMsg().getKey());
		}
	}
	
	public void test5() {
		try {
			Range r = new Range(Year.DOMAIN, "1900-01-01", "2099-12-31", Adjustment.DOWN);
			int size = r.getSizeAsInt();
			assertEquals(200, size);
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception");
		}
	}

	public void test6() {
		try {
			Range r1 = new Range(Day.DOMAIN, "2005-03-01", "2005-03-07", Adjustment.NONE);
			Range inter = r1.intersection(new Range(Day.DOMAIN, "2005-03-03", "2005-03-15", Adjustment.NONE));
			assertEquals("[2005-03-03, 2005-03-07]", inter.toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception");
		}
	}

	public void test6a() {
		try {
			Range r1 = new Range(Day.DOMAIN,  "2005-03-03", "2005-03-15", Adjustment.NONE);
			Range inter = r1.intersection(new Range(Day.DOMAIN, "2005-03-01", "2005-03-07", Adjustment.NONE));
			assertEquals("[2005-03-03, 2005-03-07]", inter.toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception");
		}
	}
	
	public void test7() {
		try {
			Range r1 = new Range(Day.DOMAIN, "2005-03-01", "2005-03-07", Adjustment.NONE);
			Range uni = r1.union(new Range(Day.DOMAIN, "2005-03-12", "2005-03-15", Adjustment.NONE));
			assertEquals("[2005-03-01, 2005-03-15]", uni.toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception");
		}
	}

	public void test8() {
		try {
			Range r1 = new Range(Day.DOMAIN, "2005-03-01", "2005-03-07", Adjustment.NONE);
			Range inter = r1.intersection(new Range(Day.DOMAIN, "2005-03-12", "2005-03-15", Adjustment.NONE));
			assertEquals("[]", inter.toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception");
		}
	}
	
	public void test9() {
		try {
			Range r1 = new Range(Day.DOMAIN, "2005-03-01", "2005-03-07", Adjustment.NONE);
			Range r2 = new Range(DateTime.DOMAIN, "2005-03-03 10:15:00", "2005-03-05 17:30:00", Adjustment.NONE);
			r1.intersection(r2);
			fail("exception expected");
		} catch (KeyedException e) {
			assertEquals(K.T1074, e.getMsg().getKey());
		}
	}

	public void test10() {
		try {
			Range r1 = new Range(Day.DOMAIN);
			Range r2 = new Range(Day.DOMAIN);
			assertTrue(r1.isInRange(r2));
			assertTrue(r2.isInRange(r1));
		} catch (KeyedException e) {
			e.printStackTrace();
			fail("exception");
		}
	}

	
	private long testIteration1Helper(TimeDomain domain, int sample1, int sample2) {
		try {
			long millis = System.currentTimeMillis();
			TimeIndex d = domain.time("1900-01-01", Adjustment.UP);
			Range r = new Range(d, d.add(sample2));
			for (int i = 0; i < sample1; i++) {
				for (TimeIndex t : r) {
					t.toString();
				}
			}
			millis -= System.currentTimeMillis();
			dump(String.format("%d iteration(s) with %d dates processed in %dms (domain %s)", 
					sample1, sample2, -millis, domain.getLabel()));
			return -millis;
		} catch (KeyedException e) {
			fail("unexpected exception");
			return -1;
		}
	}
	
	public void testIteration1() {
		int s1 = 100;
		int s2 = 200;
		long total = 0;
		total += testIteration1Helper(Day.DOMAIN, s1, s2);
		total += testIteration1Helper(SystemTime.DOMAIN, s1, s2);
		total += testIteration1Helper(Workday.DOMAIN, s1, s2);
		total += testIteration1Helper(ThirdFriday.DOMAIN, s1, s2);
		dump(String.format("total: %dms", total)); 
	}

	
}
