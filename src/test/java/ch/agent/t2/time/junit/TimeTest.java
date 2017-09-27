package ch.agent.t2.time.junit;

import java.util.Date;

import ch.agent.core.KeyedException;
import ch.agent.t2.T2Msg.K;
import ch.agent.t2.time.Adjustment;
import ch.agent.t2.time.Cycle;
import ch.agent.t2.time.DateTime;
import ch.agent.t2.time.Day;
import ch.agent.t2.time.DayOfWeek;
import ch.agent.t2.time.DefaultTimeDomainCatalog;
import ch.agent.t2.time.Month;
import ch.agent.t2.time.Resolution;
import ch.agent.t2.time.SystemTime;
import ch.agent.t2.time.TimeDomain;
import ch.agent.t2.time.TimeDomainCatalog;
import ch.agent.t2.time.TimeDomainDefinition;
import ch.agent.t2.time.TimeIndex;
import ch.agent.t2.time.Week;
import ch.agent.t2.time.Workday;
import ch.agent.t2.time.Year;
import ch.agent.t2.time.engine.Time2;
import junit.framework.TestCase;

public class TimeTest extends TestCase {

	private static final boolean PRINT = false;
	
	private static void dump(Object o) {
		if (PRINT)
			System.out.println(o.toString());
	}

	private class TestTime extends Time2 {

		public TestTime(TimeDomain domain, long time) {
			super(domain, time);
		}
		
	}
	
	private final static TimeDomainCatalog catalog = new DefaultTimeDomainCatalog();
	
	private static TimeDomain getTimeDomain(TimeDomainDefinition def) {
		TimeDomain domain = catalog.get(def);
		return domain == null ? def.asTimeDomain() : domain;
	}
	
	private TimeDomain hour() {
		return getTimeDomain(new TimeDomainDefinition("time_hour", Resolution.HOUR, 0L));
	}
	
	private TimeDomain min() {
		return getTimeDomain(new TimeDomainDefinition("time_min", Resolution.MIN, 0L));
	}
	
	private TimeDomain sec() {
		return getTimeDomain(new TimeDomainDefinition("time_sec", Resolution.SEC, 0L));
	}
	
	private TimeDomain msec() {
		return getTimeDomain(new TimeDomainDefinition("time_msec", Resolution.MSEC, 0L));
	}
	
	private TimeDomain usec() {
		return getTimeDomain(new TimeDomainDefinition("time_usec", Resolution.USEC, 0L));
	}
	
	public void testDay() {
		try {
			TimeDomain d1 = Day.DOMAIN;
			TimeDomain d2 = new Day("1999-10-11").getTimeDomain();
			TimeDomain d3 = catalog.get("daily");
			TimeDomain d4 = catalog.get(new TimeDomainDefinition(null, Resolution.DAY, 0));
			assertSame(d1, d2);
			assertSame(d1, d3);
			assertSame(d1, d4);
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}
	
	public void testDay1() {
		try {
			TimeIndex t = new Day("1999-10-11");
			for (TimeDomain d : catalog.get()) {
				dump(d.getLabel());
			}
			TimeDomain d = catalog.get("daily");
			assertSame(d, t.getTimeDomain());
			for (TimeDomain domain : catalog.get()) {
				dump(domain);
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}
	
	public void testDay2() {
		try {
			TimeIndex t = new Day("2001-05-26").add(1);
			assertEquals("2001-05-27", t.toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}
	
	public void testDay3() {
		try {
			TimeDomain d = catalog.get("weekly");
			TimeIndex t = d.time(0L);
			new Day(t);
			fail("exception expected");
		} catch (KeyedException e) {
			assertEquals(K.T1073, e.getMsg().getKey());
			assertEquals("Constructor expects time domain \"daily\", not \"weekly\".", e.getMessage());
		}
	}

	public void testDay4() {
		try {
			TimeIndex t1 = new Day("2001-05-26");
			TimeIndex t2 = new Day("2001-05-26");
			TimeIndex t3 = new Day("2001-05-26");
			assertEquals(t1, t2);
			assertEquals(t1, t3);
			assertSame(Day.DOMAIN, Day.DOMAIN);
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}
	
	public void testNullTimeDomain() {
		try {
			new TestTime(null, 0L);
			fail("exception expected");
		} catch (IllegalArgumentException expected) {
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}
	
	public void testDate() {
		TimeIndex time = Day.DOMAIN.time(0L);
		assertEquals("0000-01-01", time.toString());
	}
	
	/**
	 * The software assumes that:
	 * <ul>
	 * <li>a normal year as 365 days, a leap year has 366 days
	 * <li>a leap year can be divided evenly by 4 or by 400, but not by
	 * 100
	 * <li>there is a year 0 (and it is a leap year)
	 * <li>the gregorian calendar starts on year 0
	 * </ul>
	 * As a consequence, the number of days in an interval of 400 years
	 * is a constant which can be easily computed "by hand". The
	 * internal representation of a date by the system is a long with
	 * the value 0 corresponding to January 1 of year 0. As a
	 * consequence the internal representation of January 1 2000 is 5
	 * times the 400 years constant.
	 * 
	 */
	public void testDate20000101() {
		try {
			int daysIn400Years = 365 * 303 + 366 * 97;
			int daysIn2000Years = 5 * daysIn400Years;
			TimeIndex time = new Day( "2000-01-01");
			assertEquals(daysIn2000Years, time.asLong());
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception");
		}
	}
	
	public void testSetString1() {
		try {
			usec().time( "1964-44-12");
		} catch (Exception e) {
			assertEquals("Month 44 outside valid range [1, 12].", 
					e.getCause().getMessage());
		}
	}

	public void testSetString2() {
		try {
			usec().time("2006-02-29 14:00:12");
		} catch (Exception e) {
			assertEquals("Day 29 outside valid range [1, 28].", 
					e.getCause().getMessage());
		}
	}

	public void testSetString3() {
		try {
			TimeIndex time = usec().time("2004-02-29 14:00:12.010101");
			assertEquals("2004-02-29 14:00:12.010101", time.toString());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testSetString3a() {
		try {
			TimeIndex time = usec().time("8004-02-29 14:00:12.010101");
			assertEquals("8004-02-29 14:00:12.010101", time.toString());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}
	
	public void testSetString3b() {
		try {
			usec().time("8003-02-29 14:00:12.010101");
			fail ("exception was expected");
		} catch (KeyedException e) {
			assertEquals(K.T1068, e.getMsg().getKey());
		}
	}
	
	public void testSetString3c() {
		try {
			new Day("0000-02-29");
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testSetString3e() {
		try {
			TimeIndex time = msec().time("8004-02-29 14:00:12.345678");
			String s = time.toString();
			assertEquals("8004-02-29 14:00:12.345", s);
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}
	
	public void testSetString3f() {
		try {
			TimeIndex time = usec().time("8004-02-29 14:00:12.345");
			String s = time.toString();
			assertEquals("8004-02-29 14:00:12.345000", s);
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}
	
	public void testSetString3g() {
		try {
			TimeIndex time = usec().time("8004-02-29 14:00:12.5");
			String s = time.toString();
			assertEquals("8004-02-29 14:00:12.500000", s);
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testSetString3h() {
		try {
			TimeIndex time = usec().time("8004-02-29T14:00:12,50");
			String s = time.toString();
			assertEquals("8004-02-29 14:00:12.500000", s);
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}
	
	public void testSetString3i() {
		try {
			TimeIndex time = usec().time("80040229T140012,5");
			String s = time.toString();
			assertEquals("8004-02-29 14:00:12.500000", s);
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}
	
	public void testSetString3j() {
		try {
			usec().time("80040229 140012.5");
			fail("exception expected");
		} catch (Exception e) {
			assertEquals(K.T1082, ((KeyedException)e).getMsg().getKey());
		}
	}
	
	public void testSetString3k() {
		try {
			TimeIndex time = msec().time("20040229T140012,5Z");
			String s = time.toString();
			assertEquals("2004-02-29 14:00:12.500", s);
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}
	
	public void testSetStringTZO1() {
		try {
			msec().time("2004-02-29T14:00:12+12:70:60");
			fail("exception expected");
		} catch (KeyedException e) {
			assertEquals(K.T1018, (e.getMsg().getKey()));
		}
	}
	
	public void testSetStringTZO2() {
		try {
			msec().time("2004-02-29T14:00:12-11:70:60");
			fail("exception expected");
		} catch (KeyedException e) {
			assertEquals(K.T1021, (e.getMsg().getKey()));
		}
	}
	public void testSetStringTZO3() {
		try {
			msec().time("2004-02-29T14:00:11-11:50:60");
			fail("exception expected");
		} catch (KeyedException e) {
			assertEquals(K.T1023, (e.getMsg().getKey()));
		}
	}

	public void testSetStringTZO4() {
		try {
			TimeIndex time = msec().time("20040229T140012,5Z");
			String s = time.toString();
			assertEquals("2004-02-29 14:00:12.500", s);
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}
	
	public void testSetStringTZO5() {
		try {
			TimeIndex time = msec().time("20040229T140012,5-0200");
			String s = time.toString();
			assertEquals("2004-02-29 16:00:12.500", s);
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}
	
	public void testSetStringTZO6() {
		try {
			TimeIndex time = hour().time("2004-02-29 14:00:12.500+02:30");
			String s = time.toString();
			assertEquals("2004-02-29 11", s);
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testSetStringTZO7() {
		try {
			TimeIndex time = hour().time("2004-02-29 14:00-02:30");
			String s = time.toString();
			assertEquals("2004-02-29 16", s);
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}
	
	public void testSetStringTZO8() {
		try {
			// netherland time, see RFC 3339
			TimeIndex time = usec().time("1937-01-01 00-00:19:32.13");
			String s = time.toString();
			assertEquals("1937-01-01 00:19:32.130000", s);
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testSetStringTZO9() {
		try {
			TimeIndex time = sec().time("2004-02-29 14-02:15:20");
			String s = time.toString();
			assertEquals("2004-02-29 16:15:20", s);
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}
	
	public void testSetStringTZ10() {
		try {
			TimeIndex time = sec().time("2004-02-29T+02:15:20");
			String s = time.toString();
			assertEquals("2004-02-28 21:44:40", s);
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}
	
	public void testSetStringTZ11() {
		try {
			sec().time("2004T-02:15:20");
			fail("exception expected");
		} catch (KeyedException e) {
			assertEquals(K.T1082, e.getMsg().getKey());
		}
	}
	
	public void testSetStringTZ12() {
		try {
			sec().time("2004T-021520");
			fail("exception expected");
		} catch (KeyedException e) {
			assertEquals(K.T1082, e.getMsg().getKey());
		}
	}
	
	public void testSetStringTZ13() {
		try {
			sec().time("2004T021520");
			fail("exception expected");
		} catch (KeyedException e) {
			assertEquals(K.T1082, e.getMsg().getKey());
		}
	}
	
	public void testSetString4() {
		try {
			TimeIndex time = new Day("1956-09-14");
			assertEquals("1956-09-14", time.toString());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testSetString4a() {
		try {
			TimeIndex time = new Day("1956-09");
			assertEquals("1956-09-01", time.toString());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testSetString4b() {
		try {
			TimeIndex time = new Day("1956");
			assertEquals("1956-01-01", time.toString());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}
	
	public void testSetString5() {
		try {
			TimeIndex time = min().time("2004-02-29 12:15");
			assertEquals("2004-02-29 12:15", time.toString());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testSetString5a() {
		try {
			TimeIndex time = min().time("2003-02-28 12:15");
			assertEquals("2003-02-28 12:15", time.toString());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testSetString5b() {
		try {
			TimeIndex time = min().time("2004-03-01 12:15");
			assertEquals("2004-03-01 12:15", time.toString());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testSetString5c() {
		try {
			TimeIndex time = min().time("2003-03-01 12:15");
			assertEquals("2003-03-01 12:15", time.toString());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testSetString6() {
		try {
			TimeIndex time = sec().time("2004-02-29 12:15:02");
			assertEquals("2004-02-29 12:15:02", time.toString());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testSetString6a() {
		try {
			// ISO 8601 specialty
			TimeIndex time = sec().time("2004-02-29 24:00:00");
			assertEquals("2004-03-01 00:00:00", time.toString());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}
	
	public void testSetString6b() {
		try {
			// leap second
			TimeIndex time = sec().time("2008-12-31 23:59:60");
			assertEquals("2008-12-31 23:59:59", time.toString());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}
	
	public void testSetString6c() {
		try {
			// leap second
			sec().time("2008-10-31 23:59:60");
			fail("exception expected");
		} catch (KeyedException e) {
			assertEquals(K.T1025, ((KeyedException)e.getCause()).getMsg().getKey());
		}
	}
	
	public void testSetString7() {
		try {
			TimeIndex time = usec().time("0000-01-01 00:00:00.000000");
			assertEquals("0000-01-01 00:00:00.000000", time.toString());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testSetString7a() {
		try {
			TimeIndex time = new Day("0000-01-01");
			assertEquals("0000-01-01", time.toString());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testSetString7b() {
		try {
			TimeIndex time = new Month("0000-01");
			assertEquals("0000-01", time.toString());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testSetString7c() {
		try {
			TimeIndex time = new Year("0000");
			assertEquals("0000", time.toString());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testSetString8() {
		try {
			TimeIndex time = new Month("2000-12");
			assertEquals("2000-12", time.toString());
			assertEquals("monthly", time.getTimeDomain().getLabel());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testSetString8a() {
		try {
			TimeIndex time = new Month("0000-12");
			assertEquals("0000-12", time.toString());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testSetString8b() {
		try {
			TimeIndex time = new Day("2000-12-01");
			assertEquals("2000-12-01", time.toString());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testSetString8c() {
		try {
			TimeIndex time = new Day("2000-10-01");
			assertEquals("2000-10-01", time.toString());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testSetString8d() {
		try {
			TimeIndex time = new Day("2000-10-02");
			assertEquals("2000-10-02", time.toString());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testSetString8e() {
		try {
			TimeIndex time = new Day("2000-02-01");
			assertEquals("2000-02-01", time.toString());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testSetString8f() {
		try {
			TimeIndex time = new Day("2000-01-31");
			assertEquals("2000-01-31", time.toString());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testSetString9() {
		try {
			TimeIndex time = new Day("2005-01-01");
			assertEquals("2005-01-01", time.toString());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testSetString9a() {
		try {
			TimeIndex time = new Day("2005-12-31");
			assertEquals("2005-12-31", time.toString());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testSetString10() {
		try {
			TimeIndex time = Day.DOMAIN.time(Long.MAX_VALUE);
			String date = time.toString(); // 25252734927766554-07-27
			time = new Day(date);
			assertEquals(date, time.toString());
		} catch (Exception e) {
			e.printStackTrace();
			assertEquals(null, e);
		}
	}
	
	public void testSetString10a() {
		try {
			String date = "+25252734927766554-07-28";
			new Day(date);
			fail("exception expected");
		} catch (Exception e) {
			assertEquals(K.T1070, ((KeyedException)e.getCause()).getMsg().getKey());
		}

	}
	
	public void testTime10b() {
		try {
			TimeDomain domain = Workday.DOMAIN;
			TimeIndex time = domain.time(6588122883467697004L); // == getCycle().computeMaxCompressedTime());
			String date = time.toString(); // "25252734927766554-07-26"
			time = domain.time(date);
			assertEquals(date, time.toString());
		} catch (Exception e) {
			e.printStackTrace();
			assertEquals(null, e);
		}
	}

	public void testTime10b1() {
		try {
			TimeDomain domain = Workday.DOMAIN;
			TimeIndex time = domain.time(6588122883467697004L); // == getCycle().computeMaxCompressedTime());
			String date = time.toString("%d%02d%02d"); // "252527349277665540726"
			time = domain.time(date);
			fail("exception expected");
		} catch (KeyedException e) {
			assertEquals(K.T1082, e.getMsg().getKey());
		}
	}
	
	public void testTime10c() {
		try {
			TimeDomain domain = Workday.DOMAIN;
			String date = "+25252734927766554-07-27"; // max is "25252734927766554-07-26"
			domain.time(date);
			fail("exception expected");
		} catch (KeyedException e) {
			assertEquals(K.T1068, e.getMsg().getKey());
		}
	}
	
	public void testTime10d() {
		try {
			TimeDomain domain = Workday.DOMAIN;
			String date = "+25252734927766554-07-30"; // max is "25252734927766554-07-26"
			domain.time(date);
			fail("exception expected");
		} catch (KeyedException e) {
			assertEquals(K.T1070, ((KeyedException)e.getCause()).getMsg().getKey());
		}
	}
	
	public void testTime10e() {
		try {
			TimeDomain domain = Workday.DOMAIN;
			TimeIndex time = domain.time(6588122883467697004L); // == getCycle().computeMaxCompressedTime());
			String date = time.toString(); // "25252734927766554-07-26"
			time = domain.time(date);
			assertEquals(date + " 00:00:00.000000", ((Time2)time).toString(true));
		} catch (Exception e) {
			e.printStackTrace();
			assertEquals(null, e);
		}
	}
	
	public void testSetString11() {
		try {
			String date = "+100000000092005-12-31";
			TimeIndex time = new Day(date);
			assertEquals(date, time.toString());
		} catch (Exception e) {
			e.printStackTrace();
			assertEquals(null, e);
		}
	}
	
	public void testSetString12() {
		try {
			String date = "2010-03-04 12:34:44";
			TimeIndex time = new DateTime(date);
			assertEquals(date, time.toString());
		} catch (Exception e) {
			e.printStackTrace();
			assertEquals(null, e);
		}
	}
	
	public void testSetString13() {
		try {
			String date = "2010-03-31 122744";
			new DateTime(date);
			fail("exception expected");
		} catch (KeyedException e) {
			assertEquals(K.T1083,e.getMsg().getKey());
		}
	}
	
	public void testSetString14() {
		try {
			String date = "20100331T12:27:44";
			new DateTime(date);
			fail("exception expected");
		} catch (KeyedException e) {
			assertEquals(K.T1084,e.getMsg().getKey());
		}
	}
	
	public void testSetString15() {
		try {
			String date = "2010-MAR-31";
			new DateTime(date);
			fail("exception expected");
		} catch (KeyedException e) {
			assertEquals(K.T1081,e.getMsg().getKey());
		}
	}
	
	public void testSetString16() {
		try {
			String date = "2010.03.31";
			new DateTime(date);
			fail("exception expected");
		} catch (KeyedException e) {
			assertEquals(K.T1082,e.getMsg().getKey());
		}
	}
	
	public void testSetString17() {
		try {
			String date = "2008-12-31T23:59:60";
			TimeIndex t = new DateTime(date);
			assertEquals("2008-12-31 23:59:59", t.toString());
			t = t.add(1);
			assertEquals("2009-01-01 00:00:00", t.toString());
		} catch (KeyedException e) {
			e.printStackTrace();
			fail("exception");
		}
	}
	
	public void testResolution1() {
		try {
			TimeIndex time = new Year("2004");
			assertEquals(2004, time.asLong());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testResolution2() {
		try {
			TimeIndex time = new Month("2004-01");
			assertEquals(2004 * 12, time.asLong());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testDayOfWeek1() {
		try {
			TimeIndex time = new Day("2006-06-21");
			assertEquals(DayOfWeek.Wed, time.getDayOfWeek());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testDayOfWeek2() {
		try {
			TimeIndex time = hour().time("2006-06-21");
			assertEquals(DayOfWeek.Wed, time.getDayOfWeek());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testDayOfWeek3() {
		try {
			TimeIndex time = min().time("2006-06-21");
			assertEquals(DayOfWeek.Wed, time.getDayOfWeek());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testDayOfWeek4() {
		try {
			TimeIndex time = sec().time("2006-06-21");
			assertEquals(DayOfWeek.Wed, time.getDayOfWeek());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testDayOfWeek5() {
		try {
			TimeIndex time = usec().time("2006-06-21");
			assertEquals(DayOfWeek.Wed, time.getDayOfWeek());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testDayOfWeek6() {
		try {
			TimeIndex time = new Day("1996-09-14");
			assertEquals(DayOfWeek.Sat, time.getDayOfWeek());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testDayOfWeek6a() {
		try {
			TimeIndex time = new Day("1956-09-14");
			assertEquals(DayOfWeek.Fri, time.getDayOfWeek());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testDayOfWeek6b() {
		try {
			TimeIndex time = new Day("1956-09-13");
			assertEquals(DayOfWeek.Thu, time.getDayOfWeek());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testDayOfWeek6c() {
		try {
			TimeIndex time = new Day("1956-09-15");
			assertEquals(DayOfWeek.Sat, time.getDayOfWeek());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testDayOfWeek7() {
		try {
			TimeIndex time = new Day("2006-12-31");
			assertEquals(DayOfWeek.Sun, time.getDayOfWeek());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testDayOfWeek8() {
		try {
			TimeIndex time = new Day("1970-01-01");
			assertEquals(DayOfWeek.Thu, time.getDayOfWeek());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}
	
	public void testDayOfWeek9() {
		try {
			TimeIndex time = new Day("0000-01-01");
			assertEquals(DayOfWeek.Sat, time.getDayOfWeek());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testTime1() {
		TimeIndex time = usec().time(86400000000L);
		assertEquals(86400000000L, time.asLong());
		dump(time.toString());
	}

	public void testTime2() {
		TimeIndex time = usec().time(0L);
		assertEquals("0000-01-01 00:00:00.000000", time.toString());
	}

	public void testTime2a() {
		TimeIndex time = usec().time(Long.MAX_VALUE);
		dump(time.toString());
		assertEquals("+292277-01-09 04:00:54.775807", time.toString());
	}
	
	public void testTime2c() {
		try {
			TimeIndex time = usec().time("+292277-01-09 04:00:54.775807");
			assertEquals(Long.MAX_VALUE, time.asLong());
		} catch (Exception e) {
			fail(e.toString());
		}
	}
	
	public void testTime3() {
		TimeIndex time = Month.DOMAIN.time(0L);
		assertEquals("0000-01", time.toString());
	}

	public void testTime3a() {
		TimeIndex time = Month.DOMAIN.time(11L);
		assertEquals("0000-12", time.toString());
	}

	public void testTime4() {
		TimeIndex time = Day.DOMAIN.time(0L);
		assertEquals("0000-01-01", time.toString());
	}

	public void testTime5() {
		TimeDomain domain = usec();
		try {
			TimeIndex time = domain.time(0, 1, 1, 0, 0, 0, 0, Adjustment.NONE);
			assertEquals(0, time.asLong());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testTime6() {
		try {
			Day.DOMAIN.time(20000000000L);
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testTime6a() {
		TimeDomain domain = Day.DOMAIN;
		TimeIndex time = domain.time(Long.MAX_VALUE);
		time.toString();
	}

	public void testTime6b() {
		try {
			Day.DOMAIN.time(Long.MAX_VALUE + 1);
			fail ("exception was expected");
		} catch (IllegalArgumentException e) {
			assertEquals(K.T1070, ((KeyedException)e.getCause()).getMsg().getKey());
		}
	}
	
	public void testTime6c() {
		try {
			TimeDomain domain = Workday.DOMAIN;
			domain.time(6588122883467697005L); // == getCycle().computeMaxCompressedTime() + 1);
			fail ("exception was expected");
		} catch (IllegalArgumentException e) {
			assertEquals(K.T1070, ((KeyedException)e.getCause()).getMsg().getKey());
		}
	}
	
	public void testTime7() {
		// TimeIndex time = new Time(TimeDomain.DAILY);
		try {
			TimeIndex time = Day.DOMAIN.time(Long.MAX_VALUE - 1);
			time = time.add(2);
			fail ("exception was expected");
		} catch (KeyedException e) {
			assertEquals(K.T1075, e.getMsg().getKey());
		}
	}
	
	public void testTimeJanuaryFirst1() {
		try {
			TimeIndex time = new Day("1996-01-01");
			assertEquals("1996-01-01", time.toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail ("exception");
		}
	}
	
	public void testTimeJanuaryFirst2() {
		try {
			TimeIndex time = new Day("1964-01-01");
			assertEquals("1964-01-01", time.toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail ("exception");
		}
	}
	
	public void testTimeJanuaryFirst3() {
		// TimeIndex time = new Time(TimeDomain.DAILY);
		try {
			int y1 = 0;
			int y2 = 9999;
			int errors = 0;
			for (int y = y1; y < y2; y++) {
				String date = String.format("%04d-01-01", y);
				TimeIndex t = new Day(date);
				if (!date.equals(t.toString())) {
					errors++;
//					System.out.println(t);
				}
			}
			assertEquals(0, errors);
		} catch (Exception e) {
			e.printStackTrace();
			fail ("exception");
		}
	}
	
	public void testIncr1() {
		try {
			TimeIndex time = new Workday("2008-03-20"); // Thursday
			time = time.add(1);
			time = time.add(1);
			time = time.add(1);
			assertEquals("2008-03-25", time.toString()); // Tuesday
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}
	
	public void testIncr2() {
		try {
			TimeIndex time = Workday.DOMAIN.time(523918); // Thursday
			time = time.add(1);
			time = time.add(1);
			time = time.add(1);
			assertEquals("2008-03-25", time.toString()); // Tuesday
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testCycle1() {
		try {
			/*
			 * Note : base day 1 is a Sat
			 */
			TimeDomain d = catalog.get(
					new TimeDomainDefinition("foo", Resolution.DAY, 0L, 
							new Cycle(false, false, true, true, true, true, true), null));
			d.time("0000-01-03");
			d.time("0000-01-04");
			d.time("0000-01-05");
			d.time("0000-01-06");
			d.time("0000-01-07");
			d.time("0000-01-10");
			assertEquals("workweek", d.getLabel());
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

	public void testCycle2() {
		try {
			TimeDomain d = catalog.get(
					new TimeDomainDefinition("foo", Resolution.DAY, 0L, 
							new Cycle(false, false, true, true, true, true, true), null));
			d.time("2006-06-19");
			d.time("2006-06-20");
			d.time("2006-06-21");
			d.time("2006-06-22");
			d.time("2006-06-23");
			assertEquals("workweek", d.getLabel());
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

	public void testCycle3() {
		try {
			TimeDomain d = getTimeDomain(new TimeDomainDefinition("foo", Resolution.MONTH, 0L, new Cycle(false, false, true), null));
			d.time("2006-03");
			d.time("2006-06");
			d.time("2006-09");
			d.time("2006-12");
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

	public void testCycle4() {
		
		try {
			TimeIndex time = new Workday("2006-06-14");
			assertEquals("2006-06-14", time.toString());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testCycle4a() {
		try {
			TimeIndex time = new Workday("2006-06-15");
			assertEquals("2006-06-15", time.toString());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testCycle4b() {
		try {
			TimeIndex time = new Workday("2006-06-16");
			assertEquals("2006-06-16", time.toString());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testCycle4c() {
		try {
			TimeIndex time = new Workday("2006-06-19");
			assertEquals("2006-06-19", time.toString());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testCycle4d() {
		try {
			TimeIndex time = new Workday("2006-06-20");
			assertEquals("2006-06-20", time.toString());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testCycle4e() {
		try {
			TimeIndex time = new Workday("2006-06-21");
			assertEquals("2006-06-21", time.toString());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testCycle4f() {
		try {
			TimeIndex time = new Workday("2006-06-22");
			assertEquals("2006-06-22", time.toString());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testCycle4g() {
		try {
			TimeIndex time = new Workday("2006-06-23");
			assertEquals("2006-06-23", time.toString());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testCycle4h() {
		try {
			TimeIndex time = new Workday("2006-06-26");
			assertEquals("2006-06-26", time.toString());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testCycle4i() {
		try {
			TimeIndex time = new Workday("2006-06-27");
			assertEquals("2006-06-27", time.toString());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testCycle5() {
		try {
			TimeIndex time = new Workday("0000-01-03");
			assertEquals("0000-01-03", time.toString());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testCycle6() {
		try {
			TimeIndex time = new Workday("1985-05-24");
			assertEquals("1985-05-24", time.toString());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testCycle7() {
		try {
			TimeIndex time = new Workday("0000-01-03");
			assertEquals("0000-01-03", time.toString());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testCycle7a() {
		try {
			TimeIndex time = new Workday("0000-01-04");
			assertEquals("0000-01-04", time.toString());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testCycle7b() {
		try {
			TimeIndex time = new Workday("0000-01-05");
			assertEquals("0000-01-05", time.toString());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testCycle7c() {
		try {
			TimeIndex time = new Workday("0000-01-06");
			assertEquals("0000-01-06", time.toString());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testCycle7d() {
		try {
			TimeIndex time = new Workday("0000-01-07");
			assertEquals("0000-01-07", time.toString());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}
	
	public void testDayMin() {
		TimeIndex time = Day.DOMAIN.time(0);
		assertEquals("0000-01-01", time.toString());
	}
	
	public void testDayMax() {
		TimeIndex time = Day.DOMAIN.time(Integer.MAX_VALUE);
		assertEquals("+5879610-07-11", time.toString());
	}
	public void testHourMax() {
		TimeIndex time = hour().time(Integer.MAX_VALUE);
		assertEquals("+244983-10-09 07", time.toString());
	}
	public void testMinuteMax() {
		TimeIndex time = min().time(Integer.MAX_VALUE);
		assertEquals("4083-01-23 02:07", time.toString());
	}
	
	public void testSecondMax() {
		TimeIndex time = sec().time(Integer.MAX_VALUE);
		assertEquals("0068-01-19 03:14:07", time.toString());
	}
	
	public void testSecondMax2() {
		try {
			TimeDomain d = getTimeDomain(new TimeDomainDefinition("foo", Resolution.SEC, 10000000000L));
			TimeIndex time = d.time(Integer.MAX_VALUE);
			assertEquals("0068-01-19 03:14:07", time.toString());
			//fail("exception was expected");
		} catch (Exception e) {
			// not a limitation any more, origin now supported
			assertEquals(K.T0007, ((KeyedException)e.getCause()).getMsg().getKey());
		}
	}
	
	public void testSecondMax3() {
		try {
			TimeDomain d = getTimeDomain(new TimeDomainDefinition("fooo", Resolution.SEC, 10000000000L));
			TimeIndex time = d.time(Integer.MAX_VALUE);
			assertEquals("0068-01-19 03:14:07", time.toString());
			//fail("exception was expected");
		} catch (Exception e) {
			// not a limitation any more, origin now supported
			assertEquals("Temporary limitation: origin must be 0L", e.getMessage());
		}
	}
	
	public void testSystemTime1() {
		try {
			TimeIndex time = new SystemTime("2005-04-03");
			assertEquals("2005-04-03 00:00:00.000", time.toString());
		} catch (Exception e) {
			fail(e.toString());
		}
	}
	
	public void testSystemTime2() {
		try {
			TimeIndex time = new SystemTime("2005-04-03 02:03:04.567");
			assertEquals("2005-04-03 02:03:04.567", time.toString());
		} catch (Exception e) {
			fail(e.toString());
		}
	}
	
	public void testSystemTime3() {
		try {
			Date d = new CalendarUtil().dateUTC("2005-03-25 12:12:13");
			TimeIndex time = new SystemTime("2005-03-25 12:12:13");
			assertEquals(d.getTime(), time.asLong() - time.getTimeDomain().getOrigin());
		} catch (Exception e) {
			fail(e.toString());
		}
	}
	
	public void testSystemTime4() {
		try {
			TimeIndex time = new SystemTime();
			String javaDate = new CalendarUtil().format(new Date());
			assertEquals(time.toString("%04d-%02d-%02d %02d:%02d:%02d"), javaDate);
		} catch (Exception e) {
			fail(e.toString());
		}
	}
	
	public void testSystemTime5() {
		try {
			long now1 = System.currentTimeMillis();
			TimeIndex t = new SystemTime(now1);
			long now2 = t.asLong() - SystemTime.DOMAIN.getOrigin(); 
			assertEquals(now2, now1);
		} catch (Exception e) {
			fail(e.toString());
		}
	}
	
	public void testSystemTime6() {
		try {
			long now1 = System.currentTimeMillis();
			SystemTime t = new SystemTime(now1);
			long now2 = t.asFastJavaTime(); 
			assertEquals(now2, now1);
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	public void testDateTime1() {
		try {
			TimeIndex time = new DateTime("2000-01-01 00:00:00");
			assertEquals(0, time.asOffset());
		} catch (Exception e) {
			fail(e.toString());
		}
	}
	
	public void testDateTime2() {
		try {
			TimeIndex time = new DateTime("1999-12-31 23:59:59");
			assertEquals(-1, time.asOffset());
		} catch (Exception e) {
			fail(e.toString());
		}
	}
	
	public void testDateTime3() {
		try {
			TimeIndex time = new DateTime("1956-09-14 12:00:00");
			assertEquals(-1366286400, time.asOffset());
		} catch (Exception e) {
			fail(e.toString());
		}
	}
	
	public void testDateTime4() {
		try {
			TimeIndex time = DateTime.DOMAIN.time((TimeDomain.DAYS_TO_20000101 * 24 * 60 * 60)-1366286400);
			assertEquals("1956-09-14 12:00:00", time.toString());
		} catch (Exception e) {
			fail(e.toString());
		}
	}
	
	public void testAddSub1() {
		try {
			TimeIndex t1 = new Day("2004-03-25");
			TimeIndex t2 = new Day("2005-07-12");
			assertEquals(t2, t1.add(t2.sub(t1)));
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}
	
	public void testAddSub2() {
		try {
			TimeIndex t1 = new Day("2004-03-25");
			TimeIndex t2 = new Workday("2005-07-12");
			t2.sub(t1);
			fail("exception expected");
		} catch (KeyedException e) {
			assertEquals(K.T1077, e.getMsg().getKey());
		}
	}
	
	public void testAddSub3() {
		try {
			TimeIndex t1 = new Day("2004-03-25");
			TimeIndex t2 = new Day("2004-03-26");
			assertEquals(1, t2.sub(t1));
			assertEquals(-1, t1.sub(t2));
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}
	
	public void testAddSub4() {
		try {
			TimeIndex t1 = usec().time(0);
			TimeIndex t2 = usec().time(Long.MAX_VALUE);
			assertEquals(Long.MAX_VALUE, t2.sub(t1));
			dump(Long.MIN_VALUE + " " + (-Long.MAX_VALUE)  + " " + t1.sub(t2));
			assertEquals(-Long.MAX_VALUE, t1.sub(t2));
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}
	
	public void testAddSub5() {
		try {
			TimeIndex t1 = sec().time("2000-01-01");
			TimeIndex t2 = t1.add(Integer.MAX_VALUE);
			assertEquals(Integer.MAX_VALUE, t2.sub(t1));
			dump(Integer.MIN_VALUE + " " + (-Integer.MAX_VALUE)  + " " + t1.sub(t2));
			assertEquals(-Integer.MAX_VALUE, t1.sub(t2));
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}
	
	public void testDayOfMonth001() {
		try {
			TimeIndex t1 = new Month(2009, 1);
			Day d = new Day(t1.getDayByRank(Resolution.MONTH, DayOfWeek.Fri, 3));
			dump(d.toString(true) + " " + d.getDayOfWeek());
			assertEquals(DayOfWeek.Fri, d.getDayOfWeek());
			TimeIndex t2 = d.add(-21);
			assertEquals(t2.getMonth(), 12);
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}
	
	public void testDayOfMonth002() {
		try {
			TimeIndex t1 = new Year(2009);
			t1.getDayByRank(Resolution.MONTH, DayOfWeek.Fri, 3);
			fail("exception expected");
		} catch (KeyedException e) {
			assertEquals(K.T1059, e.getMsg().getKey());
		}
	}
	
	public void testDayOfMonth003() {
		try {
			TimeIndex t1 = new Day(2009, 3, 1);
			assertEquals("2009-03-20", t1.getDayByRank(Resolution.MONTH, DayOfWeek.Fri, 3).toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

	public void testDayOfMonth004() {
		try {
			TimeIndex t1 = new Day(2009, 3, 1);
			assertEquals("2009-03-20", t1.getDayByRank(Resolution.MONTH, DayOfWeek.Fri, 3).toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

	private void testHelperDayOfMonth01(long year, int month, DayOfWeek dow, int rank, int exception) {
		try {
			TimeIndex t1 = new Month(year, month);
			TimeIndex d = t1.getDayByRank(Resolution.MONTH, dow, rank);
			if (d != null) {
				dump(d.toString() + " " + d.getDayOfWeek());
				assertEquals(dow, d.getDayOfWeek());
				TimeIndex t2 = d.add(-rank * 7);
				assertEquals(t2.getMonth(), month > 1 ? month - 1 : 12);
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}
	
	public void testDayOfMonth01() {
		testHelperDayOfMonth01(2009, 1, DayOfWeek.Fri, 3, -1);
		testHelperDayOfMonth01(2009, 6, DayOfWeek.Tue, 5, -1);
		testHelperDayOfMonth01(2007, 2, DayOfWeek.Sun, 1, -1);
		testHelperDayOfMonth01(2007, 2, DayOfWeek.Wed, 4, -1);
		testHelperDayOfMonth01(2011, 6, DayOfWeek.Fri, 5, -1);
	}
	
	private void testHelperDayOfMonth02(String date, int rank, String day) {
		try {
			Day d = new Day(date);
			TimeIndex t1 = new Month(d.getYear(), d.getMonth());
			TimeIndex result = t1.getDayByRank(Resolution.MONTH, d.getDayOfWeek(), rank);
			dump(d.toString() + " " + d.getDayOfWeek());
			assertEquals(date, result.toString());
			assertEquals(day, result.getDayOfWeek().name());
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception: " + date);
		}
	}

	public void testDayOfMonth02() {
		testHelperDayOfMonth02("2007-02-15", 3, "Thu");
		testHelperDayOfMonth02("2006-11-17", 3, "Fri");
		testHelperDayOfMonth02("2003-10-29", 5, "Wed");
		testHelperDayOfMonth02("2004-01-16", 3, "Fri");
		testHelperDayOfMonth02("1989-06-16", 3, "Fri");
		testHelperDayOfMonth02("1989-06-26", -1, "Mon");
		testHelperDayOfMonth02("1993-08-03", -5, "Tue");
		testHelperDayOfMonth02("1985-09-25", -1, "Wed");
	}
	
	private void testHelperDayOfYear01(String date, int rank, String day) {
		try {
			Day d = new Day(date);
			TimeIndex result = d.getDayByRank(Resolution.YEAR, d.getDayOfWeek(), rank);
			dump(d.toString() + " " + d.getDayOfWeek());
			assertEquals(date, result.toString());
			assertEquals(day, result.getDayOfWeek().name());
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception: " + date);
		}
	}

	public void testDayOfYear01() {
		testHelperDayOfYear01("2007-01-26", 4, "Fri");
		testHelperDayOfYear01("2006-12-29", -1, "Fri");
		testHelperDayOfYear01("2006-10-13", -12, "Fri");
		testHelperDayOfYear01("2000-06-21", 25, "Wed");
	}
	
	public void testWorkingDayOverflow01() {
		try {
			TimeIndex wtmax = Workday.DOMAIN.maxTime();
			TimeIndex wtmin = Workday.DOMAIN.minTime();
//			System.out.println("testWorkingDayOverflow01 ");
//			System.out.println(wtmax + " " + wtmax.getDayOfWeek().name() + " " + wtmax.asLong());
//			System.out.println(wtmin + " " + wtmin.getDayOfWeek().name() + " " + wtmin.asLong());
			Day tmax = new Day(Day.DOMAIN.maxTime());
			Day tmin = new Day(Day.DOMAIN.minTime());
//			System.out.println(tmax + " " + tmax.getDayOfWeek().name() + " " + tmax.asLong());
//			System.out.println(tmin + " " + tmin.getDayOfWeek().name() + " " + tmin.asLong());
			if (!tmin.isWeekEnd())
				assertEquals(wtmin.toString(), tmin.toString());
			if (!tmax.isWeekEnd())
				assertEquals(wtmax.toString(), tmax.toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}
	
	public void testWorkingDayOverflow02() {
		try {
			Workday.DOMAIN.time("+25252734927766554-07-26"); // max for Workday
			Day.DOMAIN.time("+25252734927766554-07-27"); // max for Day
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception");
		}
	}
	
	public void testWorkingDayOverflow03() {
		try {
			Workday.DOMAIN.time("+25252734927766554-07-27", Adjustment.DOWN); // max for Workday
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception");
		}
	}
	
	public void testWeek1() {
		try {
			TimeIndex t = new Week("2005-06-01", Adjustment.UP);
			assertEquals("2005-06-02", t.toString());
			dump(t.getDayOfWeek() + " " + t);
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception");
		}
	}
	
	public void testWeek2() {
		try {
			TimeIndex t = new Week("2005-06-01", Adjustment.DOWN);
			assertEquals("2005-05-26", t.toString());
			dump(t.getDayOfWeek() + " " + t);
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception");
		}
	}
	

}


