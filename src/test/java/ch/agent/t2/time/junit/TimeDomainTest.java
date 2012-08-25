package ch.agent.t2.time.junit;

import junit.framework.TestCase;
import ch.agent.core.KeyedException;
import ch.agent.t2.T2Msg.K;
import ch.agent.t2.time.Adjustment;
import ch.agent.t2.time.BasePeriodPattern;
import ch.agent.t2.time.Cycle;
import ch.agent.t2.time.Day;
import ch.agent.t2.time.DayByNameAndRank;
import ch.agent.t2.time.DayOfWeek;
import ch.agent.t2.time.DayRankingSubPeriodPattern;
import ch.agent.t2.time.Month;
import ch.agent.t2.time.Range;
import ch.agent.t2.time.Resolution;
import ch.agent.t2.time.SimpleSubPeriodPattern;
import ch.agent.t2.time.SubPeriodPattern;
import ch.agent.t2.time.ThirdFriday;
import ch.agent.t2.time.TimeDomain;
import ch.agent.t2.time.TimeDomainDefinition;
import ch.agent.t2.time.TimeDomainFactory;
import ch.agent.t2.time.TimeDomainManager;
import ch.agent.t2.time.TimeIndex;
import ch.agent.t2.time.Workday;
import ch.agent.t2.time.engine.Time2;

public class TimeDomainTest extends TestCase {

	// for testTimeDomain09
	private static final TimeDomain Test = 
	TimeDomainManager.getFactory().get(new TimeDomainDefinition("domain10", Resolution.DAY, 42L), true);

	
	private static final TimeDomain GoodTestTimeDOMAIN = 
		TimeDomainManager.getFactory().get(
				new TimeDomainDefinition("workweek_we_su", Resolution.DAY, 0L,
						new Cycle(true, true, false, false, true, true, true), null), true);
	private static final TimeDomain BadTestTime1DOMAIN = 
		TimeDomainManager.getFactory().get(
				new TimeDomainDefinition("workweek", Resolution.DAY, 0L,
						new Cycle(true, true, false, false, true, true, true), null));
	private static final TimeDomain BadTestTime2DOMAIN = 
		TimeDomainManager.getFactory().get(
				new TimeDomainDefinition(/*"workweek2"*/null, Resolution.DAY, 0L,
						new Cycle(true, true, true, false, false, true, true), null), true);
	
	private class GoodTestTime extends Time2 {
		private GoodTestTime(String date) throws Exception {
			super(GoodTestTimeDOMAIN, date);
		}
	}

	private class BadTestTime1 extends Time2 {
		private BadTestTime1(String date) throws Exception {
			super(BadTestTime1DOMAIN, date);
		}
	}

	private class BadTestTime2 extends Time2 {

		private BadTestTime2(String date) throws Exception {
			super(BadTestTime2DOMAIN, date);
		}

	}
	
	public void testSameDomain() {
		try {
			TimeDomainFactory fac = TimeDomainManager.getFactory();
			TimeDomain d1 = new Day("0102-03-04").getTimeDomain();;
			TimeDomain d2 = Day.DOMAIN;
			TimeDomain d3 = fac.get("daily");
			TimeDomain d4 = fac.get(new TimeDomainDefinition(null, Resolution.DAY, 0L));
			assertSame(d1, d2);
			assertSame(d1, d3);
			assertSame(d1, d4);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	public void testCycle01() {
		try {
			new Cycle(null);
			fail("exception expected");
		} catch(IllegalArgumentException e) {
			
		}
	}
	
	public void testCycle02() {
		try {
			new Cycle();
			fail("exception expected");
		} catch(IllegalArgumentException e) {
			
		}
	}
	
	public void testCycle03() {
		BasePeriodPattern c = new Cycle(true);
		assertEquals(1, c.getSize());
	}
	
	public void testCycle04() {
		BasePeriodPattern c = new Cycle(true, true);
		assertEquals(2, c.getSize());
		assertFalse(c.effective());
	}
	
	public void testOrigin01() {
		try {
			TimeDomainDefinition year4defShiftedBy2 = new TimeDomainDefinition("year4s2", Resolution.YEAR, 2L, new Cycle(false, false, true, false));
			TimeDomain year4s2 = TimeDomainManager.getFactory().get(year4defShiftedBy2, true);
			year4s2.time("1996");
			fail("exception expected");
		} catch (Exception e) {
			assertEquals(K.T1071, ((KeyedException) e.getCause()).getMsg().getKey());
		}
	}
	
	public void testOrigin02() {
		try {
			TimeDomainDefinition year4defShiftedBy2 = new TimeDomainDefinition("year4s2", Resolution.YEAR, 2L, new Cycle(false, false, true, false));
			TimeDomain year4s2 = TimeDomainManager.getFactory().get(year4defShiftedBy2, true);
			TimeIndex t = year4s2.time("1998");
			assertEquals("1998", t.toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

	public void testTimeDomain01() {
		TimeDomainFactory factory = TimeDomainManager.getFactory();
		TimeDomain d = factory.get(new TimeDomainDefinition("yearly", Resolution.DAY, 0L));
		assertEquals("daily", d.getLabel());
		assertSame(Day.DOMAIN, d);
	}
	
	public void testTimeDomain01a() {
		TimeDomainFactory factory = TimeDomainManager.getFactory();
		TimeDomain d = factory.get(new TimeDomainDefinition("foo", Resolution.DAY, 0L));
		assertEquals("daily", d.getLabel());
		assertSame(Day.DOMAIN, d);
	}
	
	public void testTimeDomain01b() {
		TimeDomainFactory factory = TimeDomainManager.getFactory();
		TimeDomain d = factory.get(new TimeDomainDefinition("daily", Resolution.DAY, 0L));
		assertSame(Day.DOMAIN, d);
	}
	
	public void testTimeDomain01c() {
		try {
			TimeDomainFactory factory = TimeDomainManager.getFactory();
			TimeDomain d = factory.get("daily");
			assertSame(Day.DOMAIN, d);
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception");
		}
	}
	
	public void testTimeDomain01d() {
		try {
			TimeDomainFactory factory = TimeDomainManager.getFactory();
			TimeDomain d = factory.get(new TimeDomainDefinition("", Resolution.DAY, 0L));
			assertSame(Day.DOMAIN, d);
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception");
		}
	}
	public void testTimeDomain02() {
		TimeDomainFactory factory = TimeDomainManager.getFactory();
		TimeDomain d = factory.get(new TimeDomainDefinition("nightly", Resolution.DAY, 0L));
		assertEquals("daily", d.getLabel());
		assertSame(Day.DOMAIN, d);
	}
	
	public void testTimeDomain03() {
		TimeDomainFactory factory = TimeDomainManager.getFactory();
		TimeDomain d = factory.get(new TimeDomainDefinition("daily", Resolution.MONTH, 0L));
		assertFalse("daily".equals(d.getLabel()));
		assertEquals("monthly", d.getLabel());
	}
	
	public void testTimeDomain04() {
		try {
			TimeDomainFactory factory = TimeDomainManager.getFactory();
			TimeDomain d = factory.get(new TimeDomainDefinition("", Resolution.MONTH, 0L));
			assertSame(Month.DOMAIN, d);
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception");
		}
	}
	
	public void testTimeDomain05() {
		try {
			TimeDomainFactory factory = TimeDomainManager.getFactory();
			TimeDomain d = factory.get("daily");
			assertSame(Day.DOMAIN, d);
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception");
		}
	}
	
	public void testTimeDomain06() {
		try {
			TimeDomainFactory factory = TimeDomainManager.getFactory();
			TimeDomain d = factory.get(new TimeDomainDefinition("workweek_we_su", Resolution.DAY, 0L,
					new Cycle(true, true, false, false, true, true, true)), true);
			TimeIndex t = d.time("2011-05-25");
			t = t.add(-1);
			assertEquals(DayOfWeek.Sun, t.getDayOfWeek());
			t = t.add(+2);
			assertEquals(DayOfWeek.Thu, t.getDayOfWeek());
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception");
		}
	}
	
	public void testTimeDomain07() {
		try {
			TimeDomainFactory factory = TimeDomainManager.getFactory();
			TimeDomain d = factory.get(new TimeDomainDefinition("daily_tu_sa", Resolution.DAY, 0L,
					new Cycle(true, false, false, true, true,	true, true)), true);
			TimeIndex t = d.time("2011-05-25");
			t = t.add(-1);
			assertEquals(DayOfWeek.Tue, t.getDayOfWeek());
			t = t.add(-1);
			assertEquals(DayOfWeek.Sat, t.getDayOfWeek());
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception");
		}
	}
	
	public void testTimeDomain08() {
		try {
			TimeIndex t = new GoodTestTime("2011-05-25");
			t = t.add(-1);
			assertEquals(DayOfWeek.Sun, t.getDayOfWeek());
			t = t.add(+2);
			assertEquals(DayOfWeek.Thu, t.getDayOfWeek());
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception");
		}
	}
	
	public void testTimeDomain09() {
		try {
			TimeIndex t1 = new BadTestTime1("2011-05-25");
			TimeIndex t2 = new BadTestTime2("2011-05-26");
			assertEquals("workweek_we_su", t1.getTimeDomain().getLabel());
			assertEquals("domain10.1", t2.getTimeDomain().getLabel());
			assertEquals("domain10", Test.getLabel());
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception");
		}
	}

	public void testTimeDomain10() {
		try {
			TimeDomainFactory factory = TimeDomainManager.getFactory();
			TimeDomain d = factory.get("daily");
			assertEquals("L=daily O=0 U=DAY P=null S=null", d.toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception");
		}
	}
	public void testTimeDomain11() {
		try {
			TimeDomainFactory factory = TimeDomainManager.getFactory();
			TimeDomain d = factory.get("workweek");
			assertEquals("L=workweek O=0 U=DAY P=0011111 S=null", d.toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception");
		}
	}


	
	public void testTimeDomain12() {
		try {
			TimeDomainManager.getFactory().get(
					new TimeDomainDefinition("foo", Resolution.DAY, 0L,
					new Cycle(false)), true);
			fail("exception expected");
		} catch (IllegalArgumentException e) {
		}
	}
	
	public void testTimeDomain13() {
		try {
			TimeDomainManager.getFactory().get(
					new TimeDomainDefinition("foo", Resolution.DAY, 0L,
					new Cycle(false, false, false)), true);
			fail("exception expected");
		} catch (IllegalArgumentException e) {
		}
	}
	
	public void testTimeDomain14() {
		try {
			TimeDomainManager.getFactory().get(
					new TimeDomainDefinition("foo", Resolution.DAY, 0L,
							new Cycle(true, false, true, false, true)), true);
			TimeDomainManager.getFactory().get(
				new TimeDomainDefinition("foo2", Resolution.DAY, 0L,
						new Cycle(true, false, true, false, true)), false);
			fail("exception expected");
		} catch (Exception e) {
			assertEquals(K.T0009, ((KeyedException) e.getCause()).getMsg().getKey());
		}
	}

	public void testMinTime1() {
		TimeIndex time = Day.DOMAIN.minTime();
		assertEquals("0000-01-01", time.toString());
	}
	
	public void testMinTime2() {
		TimeIndex time = Workday.DOMAIN.minTime();
		assertEquals("0000-01-03", time.toString());
	}
	
	public void testMaxTime1() {
		TimeIndex time = Day.DOMAIN.maxTime();
		assertEquals("+25252734927766554-07-27", time.toString());
	}
	
	public void testMaxTime2() {
		TimeIndex time = Workday.DOMAIN.maxTime();
		assertEquals("+25252734927766554-07-26", time.toString());
	}
	
	public void testMaxTime3() {
		TimeIndex time = Day.DOMAIN.maxTime();
		assertEquals(Long.MAX_VALUE, time.asLong());
	}

	public void testMaxTime4() {
		TimeIndex time = Workday.DOMAIN.maxTime();
		assertEquals(6588122883467697004L, time.asLong());
	}

	public void testMaxTime5() {
		TimeDomain usec = TimeDomainManager.getFactory().get(
				new TimeDomainDefinition("usec", Resolution.USEC, 0L), true);
		System.out.println("* testMaxTime5");
		assertEquals("0000-01-01 00:00:00.000000", usec.minTime().toString());
		assertEquals("+292277-01-09 04:00:54.775807", usec.maxTime().toString());
	}
	
	public void testSubPeriod00(String input, Adjustment adjust, String expected) {
		try {
			TimeDomain d = TimeDomainManager.getFactory().get(
					new TimeDomainDefinition("abc", Resolution.MONTH, 0L, null, 
							new SimpleSubPeriodPattern(Resolution.MONTH, Resolution.DAY, new int[]{10,20})), 
							true);
			TimeDomain d2 = TimeDomainManager.getFactory().get(
					new TimeDomainDefinition("abc", Resolution.MONTH, 0L, null, 
							new SimpleSubPeriodPattern(Resolution.MONTH, Resolution.DAY, new int[]{10,20})));
			assertEquals("abc", d.getLabel());
			assertEquals("abc", d2.getLabel());
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}
	
	private void helperSubPeriod01(String input, Adjustment adjust, String expected) {
		try {
			TimeDomain d = TimeDomainManager.getFactory().get(
					new TimeDomainDefinition("abc", Resolution.MONTH, 0L, null, 
							new SimpleSubPeriodPattern(Resolution.MONTH, Resolution.DAY, new int[]{10,20})), 
							true);			
			assertEquals("abc", d.getLabel());
			TimeIndex t = d.time(input, adjust);
			TimeIndex day = new Day(expected);
//			System.out.println(t.getDayOfWeek().name() + " " + t);
			assertEquals(expected, t.toString());
			assertEquals(day.getDayOfWeek(), t.getDayOfWeek());
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}
	
	public void testSubPeriod01() {
		helperSubPeriod01("2008-06-10",  Adjustment.NONE, "2008-06-10");
		helperSubPeriod01("2008-06-20", Adjustment.NONE, "2008-06-20");
		helperSubPeriod01("2008-06-05",  Adjustment.UP, "2008-06-10");
		helperSubPeriod01("2008-06-25", Adjustment.DOWN, "2008-06-20");
		helperSubPeriod01("2008-06-25", Adjustment.UP, "2008-07-10");
		helperSubPeriod01("2008-06-05", Adjustment.DOWN, "2008-05-20");

	}
	
	public void testSubPeriod02() {
		try {
			
			TimeDomain d = TimeDomainManager.getFactory().get(
					new TimeDomainDefinition("abc2", Resolution.MONTH, 0L, null, 
							new SimpleSubPeriodPattern(Resolution.MONTH, Resolution.DAY, new int[]{2,4})), 
							true);			
			assertEquals("abc2", d.getLabel());
			TimeIndex t = d.time("2005-01-01", Adjustment.UP);
			Range r = new Range(t, t.add(23));
//			System.out.println("* testSubPeriod02");
			TimeIndex lastDate = null;
			for (TimeIndex date : r) {
				lastDate = date;
//				System.out.println(date.getDayOfWeek().name() + " " + date);
			}
			assertEquals("2005-12-04", lastDate.toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}
	
	private void helperSubPeriod03(String input, Adjustment adjust, String expected) {
		try {
			TimeDomain d = TimeDomainManager.getFactory().get(
					new TimeDomainDefinition("abcd", Resolution.DAY, 0L, null, 
							new SimpleSubPeriodPattern(Resolution.DAY, Resolution.SEC, new int[]{36000, 54000, 64800})), 
							true);			
			assertEquals("abcd", d.getLabel());
			TimeIndex t = d.time(input, adjust);
			TimeIndex day = new Day(expected);
//			System.out.println("t   " + t.getDayOfWeek().name() + " " + t);
//			System.out.println("day " + day.getDayOfWeek().name() + " " + day);
			assertEquals(expected, t.toString());
			assertEquals(day.getDayOfWeek(), t.getDayOfWeek());
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

	public void testSubPeriod03() {
//		System.out.println("* testSubPeriod03");
		helperSubPeriod03("2008-06-25 10:00:00", Adjustment.NONE, "2008-06-25 10:00:00");
		helperSubPeriod03("2008-06-25 11:00:00", Adjustment.UP, "2008-06-25 15:00:00");
		helperSubPeriod03("2008-06-25 11:00:00", Adjustment.DOWN, "2008-06-25 10:00:00");
		helperSubPeriod03("2008-06-25 19:00:00", Adjustment.UP, "2008-06-26 10:00:00");
		helperSubPeriod03("2008-06-25 09:00:00", Adjustment.DOWN, "2008-06-24 18:00:00");
	}

	public void testSubPeriod04() {
		try {
			TimeDomain d = TimeDomainManager.getFactory().get(
					new TimeDomainDefinition("abcde", Resolution.DAY, 0L, 
							new Cycle(false, false, true, true, true, true, true), 
							new SimpleSubPeriodPattern(Resolution.DAY, Resolution.SEC, new int[]{36000, 54000, 63000})), 
							true);			
			TimeIndex t = d.time("2005-05-02", Adjustment.UP);
			Range r = new Range(t, t.add(29));
//			System.out.println("* testSubPeriod04");
			TimeIndex lastDate = null;
			for (TimeIndex date : r) {
				lastDate = date;
//				System.out.println(date.getDayOfWeek().name() + " " + date);
			}
			assertEquals("2005-05-13 17:30:00", lastDate.toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}
	
	public void testSubPeriod04a() {
		try {
			TimeDomain d = TimeDomainManager.getFactory().get(
					new TimeDomainDefinition("abcde", Resolution.DAY, 0L, 
							new Cycle(false, false, true, true, true, true, true), 
							new SimpleSubPeriodPattern(Resolution.DAY, Resolution.SEC, new int[]{36000, 54000, 63000})), 
							true);			
			d.time("2005-05-01", Adjustment.UP);
			fail("exception expected");
		} catch (KeyedException e) {
			assertEquals(K.T1069, e.getMsg().getKey());
		}
	}
	
	public void testSubPeriod05() {
		try {
			TimeDomain d = TimeDomainManager.getFactory().get(
					new TimeDomainDefinition("abc4", Resolution.YEAR, 0L, 
							new Cycle(true, false, false, false), 
							new SimpleSubPeriodPattern(Resolution.YEAR, Resolution.MONTH, new int[]{3, 9})), 
							true);			
			TimeIndex t = d.time("2000-01-01", Adjustment.UP);
			Range r = new Range(t, t.add(3));
//			System.out.println("* testSubPeriod05");
			TimeIndex lastDate = null;
			for (TimeIndex date : r) {
				lastDate = date;
//				System.out.println(date);
			}
			assertEquals("2004-09", lastDate.toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

	public void testSubPeriod06() {
		try {
			TimeDomainManager.getFactory().get(
					new TimeDomainDefinition("abc", Resolution.DAY, 0L, 
							new Cycle(true, false, false, false), 
							new SimpleSubPeriodPattern(Resolution.DAY, Resolution.MIN, new int[]{3, 9})), 
							true);			
			fail("exception expected");
		} catch (KeyedException e) {
			assertEquals(K.T1112, e.getMsg().getKey());
		}
	}
	
	public void testSubPeriod07() {
		try {
			TimeDomain d = TimeDomainManager.getFactory().get(
					new TimeDomainDefinition("abc5", Resolution.DAY, 0L, 
							null, 
							new SimpleSubPeriodPattern(Resolution.DAY, Resolution.MONTH, new int[]{3, 9})), 
							true);			
			d.time("2005-05-02", Adjustment.UP);
			fail("exception expected");
		} catch (KeyedException e) {
			assertEquals(K.T1115, ((KeyedException) e.getCause()).getMsg().getKey());
		}
	}
	
	public void testSubPeriod08() {
		try {
			TimeDomain d = TimeDomainManager.getFactory().get(
					new TimeDomainDefinition("abc6", Resolution.DAY, 0L, 
							new Cycle(true, false, true), 
							new SimpleSubPeriodPattern(Resolution.DAY, Resolution.MONTH, new int[]{3, 9})), 
							true);			
			assertEquals("L=abc6 O=0 U=DAY P=101 S=MONTH[3, 9]", d.toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception");
		}
	}

	/**
	 * @param initDate
	 * @param periods
	 * @param adjust
	 * @param testDate
	 * @param exception no exception expected if negative
	 * @param print
	 */
	private void helperDaySubPeriod01(String initDate, int periods, Adjustment adjust, String testDate, String key, boolean print) {
		try {
			DayByNameAndRank[] dbnar = new DayByNameAndRank[]{new DayByNameAndRank(DayOfWeek.Fri, 3)};
			SubPeriodPattern spp = new DayRankingSubPeriodPattern(Resolution.MONTH, dbnar);
			TimeDomain d = TimeDomainManager.getFactory().get(
					new TimeDomainDefinition("friday3", Resolution.MONTH, 0L, null, spp), true);			
			assertSame(ThirdFriday.DOMAIN, d);
			TimeIndex t = d.time(initDate, adjust);
			Range r = new Range(t, t.add(periods));
			if (print)
				System.out.println("* helperDaySubPeriod01 " + initDate + " (" + periods + ") " + adjust.name());
			TimeIndex lastDate = null;
			for (TimeIndex date : r) {
				lastDate = date;
				if (print)
					System.out.println(date.getDayOfWeek() + " " + date);
			}
			if (key == null)
				assertEquals(testDate, lastDate.toString());
			else
				fail("exception expected");
		} catch (KeyedException e) {
			if (key == null) {
				e.printStackTrace();
				fail("unexpected exception");
			} else
				assertEquals(key, e.getMsg().getKey());
		}
	}

	public void testDaySubPeriod01a() {
		helperDaySubPeriod01("2000-01-01", 11, Adjustment.UP, "2000-12-15", null, false);
	}
	public void testDaySubPeriod01b() {
		helperDaySubPeriod01("2000-01-01", 11, Adjustment.DOWN, "2000-11-17", null, false);
	}
	public void testDaySubPeriod01c() {
		helperDaySubPeriod01("2000-01-01", 11, Adjustment.NONE, "2000-11-17", K.T1069, false);
	}
	public void testDaySubPeriod01d() {
		helperDaySubPeriod01("2000-01-22", 11, Adjustment.UP, "2001-01-19", null, false);
	}
	public void testDaySubPeriod01e() {
		helperDaySubPeriod01("2000-01-21", 11, Adjustment.NONE, "2000-12-15", null, false);
	}

	public void testDaySubPeriod02() {
		try {
			
			DayByNameAndRank[] dbnar = new DayByNameAndRank[]{new DayByNameAndRank(DayOfWeek.Fri, 5)};
			SubPeriodPattern spp = new DayRankingSubPeriodPattern(Resolution.MONTH, dbnar);
			TimeDomainManager.getFactory().get(
					new TimeDomainDefinition("abc9", Resolution.MONTH, 0L, null, spp), true);			
			fail("exception expected");
		} catch (Exception e) {
			assertEquals(K.T1051, ((KeyedException) e.getCause()).getMsg().getKey());
		}
	}

	private void helperDaySubPeriod02(String initDate, int periods, Adjustment adjust, String testDate, String key, boolean print) {
		try {
			
			/*
			 * 1st Tue, 3d Fri, last Mon of last month of each quarter
			 */

			DayByNameAndRank[] dbnar = new DayByNameAndRank[]{new DayByNameAndRank(DayOfWeek.Tue, 1),
					new DayByNameAndRank(DayOfWeek.Fri, 3),
					new DayByNameAndRank(DayOfWeek.Mon, -1)};
			SubPeriodPattern spp = new DayRankingSubPeriodPattern(Resolution.MONTH, dbnar);
			TimeDomain d = TimeDomainManager.getFactory().get(
					new TimeDomainDefinition("abc10", Resolution.MONTH, 0L, new Cycle(false, false, true), spp), true);			
			TimeIndex t = d.time(initDate, adjust);
			Range r = new Range(t, t.add(periods));
			if (print)
				System.out.println("* helperDaySubPeriod02 " + initDate + " (" + periods + ") " + adjust.name());
			TimeIndex lastDate = null;
			for (TimeIndex date : r) {
				lastDate = date;
				if (print)
					System.out.println(date.getDayOfWeek() + " " + date);
			}
			if (key == null)
				assertEquals(testDate, lastDate.toString());
			else
				fail("exception expected");
		} catch (KeyedException e) {
			if (key == null) {
				e.printStackTrace();
				fail("unexpected exception");
			} else
				assertEquals(key, e.getMsg().getKey());
		}
	}

	public void testDaySubPeriod02a() {
		helperDaySubPeriod02("2000-03-01", 11, Adjustment.UP, "2000-12-25", null, false);
	}
	public void testDaySubPeriod02b() {
		helperDaySubPeriod02("2000-03-01", 11, Adjustment.DOWN, "2000-12-15", null, false);
	}
	public void testDaySubPeriod02c() {
		helperDaySubPeriod02("2000-03-01", 11, Adjustment.NONE, "2000-12-15", K.T1069, false);
	}
	public void testDaySubPeriod02d() {
		helperDaySubPeriod02("2000-03-10", 11, Adjustment.UP, "2001-03-06", null, false);
	}
	public void testDaySubPeriod02e() {
		helperDaySubPeriod02("2000-03-20", 11, Adjustment.UP, "2001-03-16", null, false);
	}
	public void testDaySubPeriod02f() {
		helperDaySubPeriod02("2000-03-28", 11, Adjustment.UP, "2001-03-26", null, false);
	}
	public void testDaySubPeriod02g() {
		helperDaySubPeriod02("2000-03-10", 11, Adjustment.DOWN, "2000-12-25", null, false);
	}
	public void testDaySubPeriod02h() {
		helperDaySubPeriod02("2000-03-20", 11, Adjustment.DOWN, "2001-03-06", null, false);
	}
	public void testDaySubPeriod02i() {
		helperDaySubPeriod02("2000-03-28", 11, Adjustment.DOWN, "2001-03-16", null, false);
	}

	private void helperDaySubPeriod03(String initDate, int periods, Adjustment adjust, String testDate, String key, boolean print) {
		try {
			
			/*
			 * 7th Tue, last but one Mon of each year
			 */
			DayByNameAndRank[] dbnar = new DayByNameAndRank[]{
					new DayByNameAndRank(DayOfWeek.Tue, 7),
					new DayByNameAndRank(DayOfWeek.Mon, -2)};
			SubPeriodPattern spp = new DayRankingSubPeriodPattern(Resolution.YEAR, dbnar);
			TimeDomain d = TimeDomainManager.getFactory().get(
					new TimeDomainDefinition("abc11", Resolution.YEAR, 0L, null, spp), true);			
			TimeIndex t = d.time(initDate, adjust);
			Range r = new Range(t, t.add(periods));
			if (print)
				System.out.println("* helperDaySubPeriod03 " + initDate + " (" + periods + ") " + adjust.name());
			TimeIndex lastDate = null;
			for (TimeIndex date : r) {
				lastDate = date;
				if (print)
					System.out.println(date.getDayOfWeek() + " " + date);
			}
			if (key == null)
				assertEquals(testDate, lastDate.toString());
			else
				fail("exception expected");
		} catch (Exception e) {
			if (key == null) {
				e.printStackTrace();
				fail("unexpected exception");
			} else
				assertTrue(e.toString().startsWith("TTS." + key));
		}
	}

	public void testDaySubPeriod03a() {
		helperDaySubPeriod03("2000-01-01", 9, Adjustment.UP, "2004-12-20", null, false);
	}

}
