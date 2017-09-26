package ch.agent.t2.time.junit;

import ch.agent.core.KeyedException;
import ch.agent.t2.T2Msg.K;
import ch.agent.t2.time.Adjustment;
import ch.agent.t2.time.Cycle;
import ch.agent.t2.time.DateTime;
import ch.agent.t2.time.Day;
import ch.agent.t2.time.Month;
import ch.agent.t2.time.Resolution;
import ch.agent.t2.time.ThirdFriday;
import ch.agent.t2.time.TimeDomain;
import ch.agent.t2.time.TimeDomainDefinition;
import ch.agent.t2.time.TimeIndex;
import ch.agent.t2.time.Week;
import ch.agent.t2.time.Workday;
import ch.agent.t2.time.Year;
import junit.framework.TestCase;

public class TimeConversionTest extends TestCase {

	public void testComp01() {
		try {
			TimeIndex day1 = new Day("2005-06-01");
			TimeIndex day2 = new Day("2005-06-02");
			assertEquals(-1, day1.compareTo(day2));
			assertEquals(1, day2.compareTo(day1));
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception");
		}
	}
	
	public void testComp02() {
		try {
			TimeIndex day1 = new Day("2005-06-06");
			TimeIndex day2 = new Workday("2005-06-04", Adjustment.UP);
			assertEquals(0, day1.compareTo(day2));
			assertEquals(0, day2.compareTo(day1));
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception unexpected");
		}
	}
	
	public void testComp03() {
		try {
			TimeIndex day1 = new Day("2005-06-01");
			TimeIndex day2 = new Workday("2005-06-02", Adjustment.UP);
			assertEquals(-1, day1.compareTo(day2));
			assertEquals(1, day2.compareTo(day1));
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception unexpected");
		}
	}
	
	public void testComp04() {
		try {
			TimeIndex day = new Day("2005-06-02");
			TimeIndex week = new Week("2005-06-01", Adjustment.UP);
			assertEquals(0, day.compareTo(week));
			assertEquals(0, week.compareTo(day));
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception unexpected");
		}
	}
	
	public void testComp05() {
		try {
			TimeIndex day = new Day("2005-06-02");
			TimeIndex week = new Week("2005-06-03", Adjustment.DOWN);
			assertEquals(0, day.compareTo(week));
			assertEquals(0, week.compareTo(day));
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception unexpected");
		}
	}
	
	public void testComp06() {
		try {
			TimeIndex day = new Day("2005-06-01");
			TimeIndex week = new Week("2005-06-23", Adjustment.DOWN);
			assertEquals(-1, day.compareTo(week));
			assertEquals(1, week.compareTo(day));
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception unexpected");
		}
	}
	
	public void testComp07() {
		try {
			TimeIndex day = new Day("2005-06-01");
			TimeIndex year = new Year("2006");
			assertEquals(-1, day.compareTo(year));
			assertEquals(1, year.compareTo(day));
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception unexpected");
		}
	}

	public void testComp08() {
		try {
			TimeIndex day = new Day("2005-01-01");
			TimeIndex year = new Year("2005");
			assertEquals(0, day.compareTo(year));
			assertEquals(0, year.compareTo(day));
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception unexpected");
		}
	}
	
	public void testCompOlympics() {
		try {
			// define time domain "once every fourth year"
			TimeDomain year4A = new TimeDomainDefinition("year4A", Resolution.YEAR, 0L, new Cycle(true, false, false, false)).asTimeDomain();
			// define time domain "once every fourth year", shifted
			TimeDomain year4B = new TimeDomainDefinition("year4B", Resolution.YEAR, 0L, new Cycle(false, false, true, false)).asTimeDomain();
			TimeIndex y1 = year4A.time("2000");
			TimeIndex y2 = year4B.time("2002");
			assertTrue(y2.compareTo(y1) > 0);
			assertTrue(y1.compareTo(y2) < 0);
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception unexpected");
		}
	}

	
	
	public void testConvert01() {
		try {
			TimeIndex time = new DateTime("2009-06-01 12:34:12");
			assertEquals("2009-06-01 12:34:12", time.toString());
			assertEquals("2009-06-01", time.convert(Workday.DOMAIN).toString());
		} catch (Exception e) {
			fail(e.toString());
		}
	}
	
	public void testConvert02() {
		try {
			TimeIndex time = new DateTime("2009-06-07 12:34:12");
			time.convert(Workday.DOMAIN, Adjustment.NONE);
			fail("conversion should have failed, because 2009-06-07 is a Sunday");
		} catch (KeyedException e) {
			assertEquals(K.T1068, e.getMsg().getKey());
		}
	}

	public void testConvert03() {
		try {
			TimeIndex t = new Day("2005-06-01");
			TimeIndex year = t.convert(Year.DOMAIN);
			assertEquals("2005", year.toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail("excecption not expected");
		}
	}
	
	public void testConvert04() {
		try {
			TimeIndex t = new Year("2005");
			TimeIndex day = t.convert(Day.DOMAIN);
			assertEquals("2005-01-01", day.toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail("excecption not expected");
		}
	}
	
	public void testConvert05() {
		try {
			TimeIndex t = new Month("2005-02");
			TimeIndex day = t.convert(Day.DOMAIN);
			assertEquals("2005-02-01", day.toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail("excecption not expected");
		}
	}

	public void testConvert06() {
		try {
			TimeIndex t = new Week("2005-02-01", Adjustment.UP);
			TimeIndex day = t.convert(Day.DOMAIN);
			assertEquals("2005-02-03", day.toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail("excecption not expected");
		}
	}
	
	public void testConvert07() {
		try {
			TimeIndex t = new Week("2005-02-01", Adjustment.DOWN);
			TimeIndex day = t.convert(Day.DOMAIN);
			assertEquals("2005-01-27", day.toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail("excecption not expected");
		}
	}

	public void testConvert08() {
		try {
			TimeIndex t = new Workday("2005-02-01");
			TimeIndex day = t.convert(Day.DOMAIN);
			assertEquals("2005-02-01", day.toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail("excecption not expected");
		}
	}
	
	public void testConvert09() {
		try {
			TimeIndex t = new Month("2005-02");
			TimeIndex day = t.convert(ThirdFriday.DOMAIN, Adjustment.UP);
			assertEquals("2005-02-18", day.toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail("excecption not expected");
		}
	}

}


