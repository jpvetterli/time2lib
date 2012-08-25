package ch.agent.t2.timeutil.junit;

import junit.framework.TestCase;
import ch.agent.core.KeyedException;
import ch.agent.t2.T2Msg.K;
import ch.agent.t2.time.Adjustment;
import ch.agent.t2.time.Day;
import ch.agent.t2.time.Month;
import ch.agent.t2.time.Range;
import ch.agent.t2.time.TimeIndex;
import ch.agent.t2.time.Workday;
import ch.agent.t2.time.Year;
import ch.agent.t2.timeutil.DayExpression;


public class DayExpressionTest extends TestCase {
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void test1() {
		try {
			DayExpression expr = new DayExpression(Adjustment.UP);
			expr.setExpression(Day.DOMAIN, "today+1+4-2");
			TimeIndex day = expr.getDate(Day.DOMAIN);
			System.out.println(day.toString());
		} catch (Exception e) {
			fail(e.toString());
		}
	}
	
	public void test2() {
		try {
			DayExpression expr = new DayExpression(Adjustment.UP);
			expr.setExpression(Day.DOMAIN, "end-4");
			expr.getDate(Day.DOMAIN);
		} catch (KeyedException e) {
			assertEquals(K.T7026, e.getMsg().getKey());
		}
	}
	
	public void test3() {
		try {
			TimeIndex date = new Workday("2009-11-20");
			Range range = new Range(date.add(-1), date);
			DayExpression expr = new DayExpression(Adjustment.UP);
			expr.setExpression(Workday.DOMAIN, "end-5");
			TimeIndex day = expr.getDate(range);
			System.out.println(day.toString());
			assertEquals("2009-11-13", day.toString());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	public void test4() {
		try {
			TimeIndex date = new Workday("2009-11-20");
			Range range = new Range(date, date.add(1));
			DayExpression expr = new DayExpression(Adjustment.UP);
			expr.setExpression(Workday.DOMAIN, "start+1");
			TimeIndex day = expr.getDate(range);
			System.out.println(day.toString());
			assertEquals("2009-11-23", day.toString());
		} catch (Exception e) {
			fail(e.toString());
		}
	}
	
	public void test5() {
		try {
			DayExpression expr = new DayExpression(Adjustment.UP);
			expr.setExpression(Day.DOMAIN, "20.11.2009");
		} catch (KeyedException e) {
			assertEquals(K.T1082, e.getMsg().getKey());
		}
	}

	public void test6() {
		try {
			TimeIndex date = new Month("2009-11");
			Range range = new Range(date, date.add(42));
			DayExpression expr = new DayExpression(Adjustment.UP);
			expr.setExpression(Month.DOMAIN, "start+1");
			TimeIndex day = expr.getDate(range);
			System.out.println(day.toString());
			assertEquals("2009-12", day.toString());
		} catch (Exception e) {
			fail(e.toString());
		}
	}
	
	public void test7() {
		try {
			TimeIndex date = new Month("3000-01");
			Range range = new Range(date, date.add(42));
			DayExpression expr = new DayExpression(Adjustment.UP);
			expr.setExpression(Month.DOMAIN, "start-1");
			TimeIndex day = expr.getDate(range);
			System.out.println(day.toString());
			assertEquals("2999-12", day.toString());
		} catch (Exception e) {
			fail(e.toString());
		}
	}
	
	public void test8() {
		try {
			TimeIndex date = new Year("2010-01-01");
			Range range = new Range(date.add(-1), date);
			DayExpression expr = new DayExpression(Adjustment.UP);
			expr.setExpression(Year.DOMAIN, "start");
			TimeIndex day = expr.getDate(range);
			System.out.println(day.toString());
			assertEquals("2009", day.toString());
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	public void test11() {
		try {
		TimeIndex time = DayExpression.parseDay("2001-05-26+2");
		assertEquals("2001-05-28", time.toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception");
		}
	}
	
	public void test12() {
		try {
		TimeIndex time = DayExpression.parseDay("2008-05-26-1", Workday.DOMAIN, Adjustment.DOWN);
		assertEquals("2008-05-23", time.toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception");
		}
	}
	
	public void test13() {
		try {
		TimeIndex time = DayExpression.parseDay("2008-05-26+10", Month.DOMAIN, Adjustment.NONE);
		assertEquals("2008-06", time.toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception");
		}
	}
	
	public void test14() {
		try {
		TimeIndex time = DayExpression.parseDay("2008-05-26+1", Month.DOMAIN, Adjustment.NONE);
		assertEquals("2008-05", time.toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception");
		}
	}
	
	public void test16() {
		try {
			DayExpression expr = new DayExpression(Adjustment.UP);
			expr.getDate(Day.DOMAIN);
			fail("exception expected");
		} catch (KeyedException e) {
			e.printStackTrace();
			fail("exception not expected");
		} catch (IllegalStateException e) {
		}
	}
	
	public void test17() {
		try {
			DayExpression expr = new DayExpression((Adjustment) null);
			expr.getDate(Day.DOMAIN);
			fail("exception expected");
		} catch (KeyedException e) {
			e.printStackTrace();
			fail("exception not expected");
		} catch (IllegalArgumentException e) {
		}
	}

	public void test18() {
		try {
			DayExpression expr = new DayExpression(Adjustment.UP);
			expr.setExpression(null, "today");
			fail("exception expected");
		} catch (KeyedException e) {
			e.printStackTrace();
			fail("exception not expected");
		} catch (NullPointerException e) {
		}
	}
	
	public void test19() {
		try {
			DayExpression expr = new DayExpression(Adjustment.UP);
			expr.setExpression(Day.DOMAIN, "2005-05-15+1");
			expr.incr(-1);
			assertEquals("2005-05-15", expr.getDate(Day.DOMAIN).toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception not expected");
		}
	}
	
	public void test20() {
		try {
			DayExpression expr = new DayExpression(Adjustment.UP);
			expr.setExpression(Day.DOMAIN, "2005-05-15+1--1");
			fail("exception expected");
		} catch (KeyedException e) {
			assertEquals(K.T7025, e.getMsg().getKey());
		}
	}

	
}