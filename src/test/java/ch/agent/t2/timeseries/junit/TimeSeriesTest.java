package ch.agent.t2.timeseries.junit;

import java.util.Iterator;

import junit.framework.TestCase;
import ch.agent.core.KeyedException;
import ch.agent.t2.T2Msg.K;
import ch.agent.t2.time.Adjustment;
import ch.agent.t2.time.Day;
import ch.agent.t2.time.Range;
import ch.agent.t2.time.TimeDomain;
import ch.agent.t2.time.TimeIndex;
import ch.agent.t2.time.Workday;
import ch.agent.t2.timeseries.Filler;
import ch.agent.t2.timeseries.Observation;
import ch.agent.t2.timeseries.TimeAddressable;
import ch.agent.t2.timeseries.TimeIndexable;
import ch.agent.t2.timeseries.TimeSeriesFactory;
import ch.agent.t2.timeseries.UpdateReviewer;

public class TimeSeriesTest extends TestCase {

	private static void dump(Object expr) {
		// System.out.println(expr);
	}

	private static final double EPSILON = 1e-10d;

	TimeDomain dom;
	TimeIndexable<Integer> ti;
	TimeAddressable<Double> ta;
	TimeIndex time;
	
	public TimeSeriesTest() {
		super();
	    dom = Workday.DOMAIN;
	}

	private boolean epsilon_same(Double oldValue, Double newValue) {
		if (oldValue == null || newValue == null)
			return false;
		return Math.abs(oldValue - newValue) < EPSILON;
	}
	
	private void print(String comment, TimeAddressable<Double> ts) throws Exception {
		dump(comment);
		for (Observation<Double> obs : ts) {
			dump(obs.getTime().getDayOfWeek() + " " + obs.toString());
		}
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ti = TimeSeriesFactory.make(dom, Integer.class);
		time = dom.time("2006-06-23");
		ti.put(time, 123);
		time = dom.time("2006-06-26");
		ti.put(time, 456);
	}

	public void test1() {
		try {
			long first = ti.getRange().getFirstIndex();
			assertEquals(456, ti.get(first + 1).intValue());
			dump("test1");
			for (Observation<Integer> o : ti)
				dump(o.toString());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}
	
	public void test1b() {
		try {
			TimeAddressable<Integer>tint = TimeSeriesFactory.make(Day.DOMAIN, Integer.class);
			tint.put(new Day("2000-01-15"), 41);
			tint.put(new Day("2000-01-20"), 42);
			assertEquals(6, tint.getRange().getSize());
			dump("test1b");
			for (Observation<Integer> o : tint)
				dump(o.toString());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void test2() {
		try {
			long first = ti.getRange().getFirstIndex();
			ti.put(first, 521);
			assertEquals(521, ti.get(first).intValue());
			assertEquals(456, ti.get(first + 1).intValue());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void test3() {
		try {
			time = dom.time("2006-06-27");
			ti.put(time, 42);
			assertEquals(3, ti.getSize());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void test4() {
		try {
			time = dom.time("2006-06-22");
			ti.put(time, 42);
			assertEquals(3, ti.getSize());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void test5() {
		try {
			time = dom.time("2006-06-28");
			ti.put(time, 42);
			assertEquals(4, ti.getSize());
			assertTrue(ti.isMissing(ti.get(ti.getRange().getFirstIndex() + 2)));
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void test6() {
		try {
			time = dom.time("2006-06-21");
			ti.put(time, 42);
			assertEquals(4, ti.getSize());
			assertTrue(ti.isMissing(ti.get(ti.getRange().getFirstIndex() + 1)));
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}
	
	public void test7() {
		try {
			ta = TimeSeriesFactory.make(dom, Double.class, true);
			time = dom.time("2006-06-21"); ta.put(time, 1d); 
			time = time.add(1); ta.put(time, 2d); 
			time = time.add(1); ta.put(time, 3d); 
			time = time.add(1); ta.put(time, 4d); // 2006-06-26
			time = time.add(1); ta.put(time, 5d);
			Iterator<Observation<Double>> i = ta.iterator();
			i.next();
			i.next();
			i.next();
			Observation<Double> obs = i.next();
			assertEquals("2006-06-26", obs.getTime().toString());
			assertEquals(4d, obs.getValue());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}
	
	public void test8() {
		try {
			ta = TimeSeriesFactory.make(dom, Double.class, false);
			time = dom.time("2006-06-21"); ta.put(time, 1d); 
			time = time.add(1); ta.put(time, 2d); 
			time = time.add(1); ta.put(time, 3d); 
			time = time.add(1); ta.put(time, 4d); // 2006-06-26
			time = time.add(1); ta.put(time, 5d);
			Iterator<Observation<Double>> i = ta.iterator();
			i.next();
			i.next();
			i.next();
			Observation<Double> obs = i.next();
			assertEquals("2006-06-26", obs.getTime().toString());
			assertEquals(4d, obs.getValue());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void test9() {
		try {
			TimeDomain calendar = Day.DOMAIN;
			TimeAddressable<Double> ts = TimeSeriesFactory.make(calendar, Double.class, true);
			long t1 = calendar.time("2008-02-26").asLong();
			Double[] d1 = {Double.NaN, Double.NaN, 3d, Double.NaN, 5d, Double.NaN};
			ts.put(t1, d1);
			print("*** test9", ts);
			assertEquals("2008-02-28", ts.getRange().getFirst().toString());
			assertEquals("2008-03-01", ts.getRange().getLast().toString());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}
	
	public void test10() {
		try {
			TimeDomain calendar = Day.DOMAIN;
			TimeAddressable<Double> ts = TimeSeriesFactory.make(calendar, Double.class);
			long t1 = calendar.time("2008-02-26").asLong();
			Double[] d1 = {Double.NaN, Double.NaN, 3d, Double.NaN, 5d, Double.NaN};
			ts.put(t1, d1);
			print("*** test10", ts);
			assertEquals("2008-02-28", ts.getRange().getFirst().toString());
			assertEquals("2008-03-01", ts.getRange().getLast().toString());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}
	
	public void test11() {
		try {
			TimeDomain calendar = Day.DOMAIN;
			TimeAddressable<Double> ts = TimeSeriesFactory.make(calendar, Double.class);
			long t1 = calendar.time("2008-02-25").asLong();
			Double[] d1 = {0d, Double.NaN, Double.NaN, 3d, Double.NaN, 5d, Double.NaN, Double.NaN, 8d};
			ts.put(t1, d1);
			Range range = new Range(calendar, "2008-02-26", "2008-03-03", Adjustment.NONE);
			ts.setRange(range);
			dump("test11");
			for (Observation<Double> obs : ts)
				dump(obs.getTime().toString() + " " + obs.getValue());
			assertEquals("2008-02-28", ts.getRange().getFirst().toString());
			assertEquals("2008-03-01", ts.getRange().getLast().toString());
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void test12() {
		try {
			TimeDomain calendar = Day.DOMAIN;
			TimeAddressable<Double> ts = TimeSeriesFactory.make(calendar, Double.class);
			long t1 = calendar.time("2008-02-25").asLong();
			Double[] d1 = {0d, Double.NaN, Double.NaN, 3d, Double.NaN, 5d, Double.NaN, Double.NaN, 8d};
			ts.put(t1, d1);
			Range range = new Range(calendar, "2008-02-26", "2008-03-04", Adjustment.NONE);
			ts.setRange(range);
			dump("test12");
			TimeAddressable<Double> ts2 = ts.copy();
			for (Observation<Double> obs : ts2)
				dump(obs.getTime().toString() + " " + obs.getValue());
			assertEquals("2008-02-28", ts2.getRange().getFirst().toString());
			assertEquals("2008-03-04", ts2.getRange().getLast().toString());

		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void test13S() {
		try {
			TimeDomain calendar = Day.DOMAIN;
			TimeAddressable<Double> ts = TimeSeriesFactory.make(calendar, Double.class, true);
			long t1 = calendar.time("2008-02-25").asLong();
			Double[] d1 = {0d, Double.NaN, Double.NaN, 3d, Double.NaN, 5d, Double.NaN, Double.NaN, 8d};
			ts.put(t1, d1);
//			print("*** test13S, ts type=" + ts.getClass().getSimpleName(), ts);
			Range range = new Range(calendar, "2008-02-26", "2008-03-04", Adjustment.NONE);
			ts.setRange(range);
//			print("*** test13S, ts after setRange to " + range.toString(), ts);
			TimeAddressable<Double> ts2 = ts.copy();
//			print("*** test13S, ts2", ts2);
			
			assertEquals("2008-02-28", ts2.getRange().getFirst().toString());
			assertEquals("2008-03-04", ts2.getRange().getLast().toString());

		} catch (Exception e) {
			fail("unexpected exception");
		}
	}
	
	public void test13R() {
		try {
			TimeDomain calendar = Day.DOMAIN;
			TimeAddressable<Double> ts = TimeSeriesFactory.make(calendar, Double.class);
			long t1 = calendar.time("2008-02-25").asLong();
			Double[] d1 = {0d, Double.NaN, Double.NaN, 3d, Double.NaN, 5d, Double.NaN, Double.NaN, 8d};
			ts.put(t1, d1);
//			print("*** test13R, ts type=" + ts.getClass().getSimpleName(), ts);
			Range range = new Range(calendar, "2008-02-26", "2008-03-04", Adjustment.NONE);
			ts.setRange(range);
//			print("*** test13R, ts after setRange to " + range.toString(), ts);
			TimeAddressable<Double> ts2 = ts.copy();
//			print("*** test13R, ts2", ts2);
			
			assertEquals("2008-02-28", ts2.getRange().getFirst().toString());
			assertEquals("2008-03-04", ts2.getRange().getLast().toString());

		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void test13aS() {
		try {
			TimeDomain calendar = Day.DOMAIN;
			TimeAddressable<Double> ts = TimeSeriesFactory.make(calendar, Double.class, true);
			long t1 = calendar.time("2008-02-25").asLong();
			Double[] d1 = {0d, Double.NaN, Double.NaN, 3d, Double.NaN, 5d, Double.NaN, Double.NaN, 8d};
			ts.put(t1, d1);
			print("*** test13aS, ts type=" + ts.getClass().getSimpleName(), ts);
			ts.remove(new Day("2008-03-04"));
			print("*** test13aS, ts after removing last", ts);
			
			assertEquals("2008-02-25", ts.getRange().getFirst().toString());
			assertEquals("2008-03-01", ts.getRange().getLast().toString());

		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}
	
	public void test13aR() {
		try {
			TimeDomain calendar = Day.DOMAIN;
			TimeAddressable<Double> ts = TimeSeriesFactory.make(calendar, Double.class);
			long t1 = calendar.time("2008-02-25").asLong();
			Double[] d1 = {0d, Double.NaN, Double.NaN, 3d, Double.NaN, 5d, Double.NaN, Double.NaN, 8d};
			ts.put(t1, d1);
			print("*** test13aR, ts type=" + ts.getClass().getSimpleName(), ts);
			ts.remove(new Day("2008-03-04"));
			print("*** test13aR, ts after removing last", ts);
			
			assertEquals("2008-02-25", ts.getRange().getFirst().toString());
			assertEquals("2008-03-01", ts.getRange().getLast().toString());

		} catch (Exception e) {
			assertEquals("UnsupportedOperationException", e.getClass().getSimpleName());
//			e.printStackTrace();
//			fail("unexpected exception");
		}
	}
	
	public void test13a1R() {
		try {
			TimeDomain calendar = Day.DOMAIN;
			TimeAddressable<Double> ts = TimeSeriesFactory.make(calendar, Double.class);
			long t1 = calendar.time("2008-02-25").asLong();
			Double[] d1 = {0d, Double.NaN, Double.NaN, 3d, Double.NaN, 5d, Double.NaN, Double.NaN, 8d};
			ts.put(t1, d1);
			print("*** test13a1R, ts type=" + ts.getClass().getSimpleName(), ts);
			ts.put(new Day("2008-03-04"), ts.getMissingValue());
			print("*** test13a1R, ts after setting last to missing value", ts);
			
			assertEquals("2008-02-25", ts.getRange().getFirst().toString());
			assertEquals("2008-03-01", ts.getRange().getLast().toString());

		} catch (Exception e) {
			assertEquals("UnsupportedOperationException", e.getClass().getSimpleName());
//			e.printStackTrace();
//			fail("unexpected exception");
		}
	}
	
	public void test13bS() {
		try {
			TimeDomain calendar = Day.DOMAIN;
			TimeAddressable<Double> ts = TimeSeriesFactory.make(calendar, Double.class, true);
			long t1 = calendar.time("2008-02-25").asLong();
			Double[] d1 = {0d, Double.NaN, Double.NaN, 3d, Double.NaN, 5d, Double.NaN, Double.NaN, 8d};
			ts.put(t1, d1);
			print("*** test13bS, ts type=" + ts.getClass().getSimpleName(), ts);
			ts.put(new Day("2008-03-05"), Double.NaN);
			print("*** test13bS, ts after adding a last NaN", ts);
			
			assertEquals("2008-02-25", ts.getRange().getFirst().toString());
			assertEquals("2008-03-04", ts.getRange().getLast().toString());

		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}
	public void test13bR() {
		try {
			TimeDomain calendar = Day.DOMAIN;
			TimeAddressable<Double> ts = TimeSeriesFactory.make(calendar, Double.class);
			long t1 = calendar.time("2008-02-25").asLong();
			Double[] d1 = {0d, Double.NaN, Double.NaN, 3d, Double.NaN, 5d, Double.NaN, Double.NaN, 8d};
			ts.put(t1, d1);
			print("*** test13bR, ts type=" + ts.getClass().getSimpleName(), ts);
			ts.put(new Day("2008-03-05"), Double.NaN);
			print("*** test13bR, ts after adding a last NaN", ts);
			
			assertEquals("2008-02-25", ts.getRange().getFirst().toString());
			assertEquals("2008-03-04", ts.getRange().getLast().toString());

		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}
	
	public void test13cR() {
		try {
			TimeDomain calendar = Day.DOMAIN;
			TimeAddressable<Double> ts = TimeSeriesFactory.make(calendar, Double.class);
			long t1 = calendar.time("2008-02-25").asLong();
			Double[] d1 = {Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN};
			ts.put(t1, d1);
			assertEquals(true, ts.getRange().isEmpty());
			assertEquals(null, ts.getRange().getLast());
			assertEquals(null, ts.getRange().getLast());
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

	public void test13cS() {
		try {
			TimeDomain calendar = Day.DOMAIN;
			TimeAddressable<Double> ts = TimeSeriesFactory.make(calendar, Double.class, true);
			long t1 = calendar.time("2008-02-25").asLong();
			Double[] d1 = {Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN};
			ts.put(t1, d1);
			assertEquals(true, ts.getRange().isEmpty());
			assertEquals(null, ts.getRange().getLast());
			assertEquals(null, ts.getRange().getLast());
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}
	
	public void test14() {
		try {
			ta = TimeSeriesFactory.make(dom, Double.class);
			time = dom.time("2006-06-21"); ta.put(time, 1d); 
			time = time.add(1); ta.put(time, 2d); 
			time = time.add(1); ta.put(time, 3d); 
			time = time.add(1); ta.put(time, 4d); // 2006-06-26
			time = time.add(1); ta.put(time, 5d);
			time = time.add(1); 
			time = time.add(1); ta.put(time, 7d);
			Iterator<Observation<Double>> i = ta.iterator();
			i.next();
			i.next();
			i.next();
			Observation<Double> obs = i.next();
			assertEquals("2006-06-26", obs.getTime().toString());
			assertEquals(4d, obs.getValue());
			assertEquals(Double.NaN, ta.get(new Workday("2006-06-28")));	
		} catch (Exception e) {
			fail("unexpected exception");
		}
	}
	
	public void test15() {
		try {
			ta = TimeSeriesFactory.make(dom, Double.class, true);
			time = dom.time("2006-06-21"); ta.put(time, 1d); 
			time = time.add(1); ta.put(time, 2d); 
			time = time.add(1); ta.put(time, 3d); 
			time = time.add(1); ta.put(time, 4d); // 2006-06-26
			time = time.add(1); ta.put(time, 5d);
			time = time.add(1); 
			time = time.add(1); ta.put(time, 7d);
			Iterator<Observation<Double>> i = ta.iterator();
			i.next();
			i.next();
			i.next();
			Observation<Double> obs = i.next();
			assertEquals("2006-06-26", obs.getTime().toString());
			assertEquals(4d, obs.getValue());
			dump("test15 (notice 2006-06-28 skipped)");
			assertEquals(Double.NaN, ta.get(new Workday("2006-06-28")));	

			for(Observation<Double> o : ta)
				dump(o.toString());
				
		} catch (Exception e) {
			fail("unexpected exception");
		}
	}

	public void test17() {
		try {
			ta = TimeSeriesFactory.make(Day.DOMAIN, Double.class);
			ta.put(new Day("2006-04-04"), 4d);
			ta.put(new Day("2006-04-08"), 8d);
			ta.put(new Day("2006-04-09"), 9d);
			ta.put(new Day("2006-04-15"), 15d);
			ta.put(new Day("2006-04-16"), 16d);
//			print("test17 raw", ta);
			TimeAddressable<Double> tb = ta.get(new Range(Day.DOMAIN, "2006-04-06", "2006-04-12", Adjustment.NONE));
//			print("test17 range", tb);
			assertEquals("2006-04-08", tb.getRange().getFirst().toString());
			assertEquals("2006-04-09", tb.getRange().getLast().toString());
		} catch (Exception e) {
			fail("unexpected exception");
		}
	}

	public void test17a() {
		// note: once upon a time, tb was a view of ta; it was messy
		try {
			ta = TimeSeriesFactory.make(Day.DOMAIN, Double.class);
			ta.put(new Day("2006-04-04"), 4d);
			ta.put(new Day("2006-04-08"), 8d);
			ta.put(new Day("2006-04-09"), 9d);
			ta.put(new Day("2006-04-15"), 15d);
			ta.put(new Day("2006-04-16"), 16d);
//			print("test17a raw", ta);
			TimeAddressable<Double> tb = ta.get(new Range(Day.DOMAIN, "2006-04-09", "2006-04-15", Adjustment.NONE));
//			print("test17a range", tb);
			assertEquals("2006-04-09", tb.getRange().getFirst().toString());
			assertEquals("2006-04-15", tb.getRange().getLast().toString());
			tb.put(new Day("2006-04-12"), 12d);
//			print("test17a raw after modifying the copy", ta);
			assertEquals(ta.getMissingValue(), ta.get(new Day("2006-04-12")));
			assertEquals(12.0, tb.get(new Day("2006-04-12")));
			assertEquals("2006-04-16", ta.getRange().getLast().toString());
		} catch (Exception e) {
			fail("unexpected exception");
		}
	}
	
	public void test17b() {
		// note: once upon a time, tb was a view of ta; it was messy
		try {
			ta = TimeSeriesFactory.make(Day.DOMAIN, Double.class);
			ta.put(new Day("2006-04-04"), 4d);
			ta.put(new Day("2006-04-08"), 8d);
			ta.put(new Day("2006-04-09"), 9d);
			ta.put(new Day("2006-04-15"), 15d);
			ta.put(new Day("2006-04-16"), 16d);
//			print("test17b raw", ta);
			TimeAddressable<Double> tb = ta.get(new Range(Day.DOMAIN, "2006-04-06", "2006-04-12", Adjustment.NONE));
//			print("test17b range", tb);
			assertEquals("2006-04-08", tb.getRange().getFirst().toString());
			assertEquals("2006-04-09", tb.getRange().getLast().toString());
			tb.put(new Day("2006-04-12"), 12d);
//			print("test17b raw after modifying the copy", ta);
//			print("test17b range after modifying the copy", tb);
			assertEquals(ta.getMissingValue(), ta.get(new Day("2006-04-12")));
			assertEquals(12.0, tb.get(new Day("2006-04-12")));
			assertEquals("2006-04-12", tb.getRange().getLast().toString());
		} catch (Exception e) {
			fail("unexpected exception");
		}
	}
	
	public void test17c() {
		// note: once upon a time, tb was a view of ta; it was messy
		try {
			ta = TimeSeriesFactory.make(Day.DOMAIN, Double.class, false);
			ta.put(new Day("2006-04-04"), 4d);
			ta.put(new Day("2006-04-08"), 8d);
			ta.put(new Day("2006-04-09"), 9d);
			ta.put(new Day("2006-04-15"), 15d);
			ta.put(new Day("2006-04-16"), 16d);
//			print("test17c raw", ta);
			TimeAddressable<Double> tb = ta.get(new Range(Day.DOMAIN, "2006-04-06", "2006-04-12", Adjustment.NONE));
//			print("test17c range", tb);
			assertEquals("2006-04-08", tb.getRange().getFirst().toString());
			assertEquals("2006-04-09", tb.getRange().getLast().toString());
			tb.put(new Day("2006-04-18"), 18d);
//			print("test17c raw after modifying the copy", ta);
//			print("test17c range after modifying the copy", tb);
			assertEquals("2006-04-16", ta.getRange().getLast().toString());
			assertEquals(18.0, tb.get(new Day("2006-04-18")));
			assertEquals("2006-04-18", tb.getRange().getLast().toString());
		} catch (Exception e) {
			fail("unexpected exception");
		}
	}
	
	public void test17d() {
		// note: once upon a time, tb was a view of ta; it was messy
		try {
			ta = TimeSeriesFactory.make(Day.DOMAIN, Double.class, true);
			ta.put(new Day("2006-04-04"), 4d);
			ta.put(new Day("2006-04-08"), 8d);
			ta.put(new Day("2006-04-09"), 9d);
			ta.put(new Day("2006-04-15"), 15d);
			ta.put(new Day("2006-04-16"), 16d);
			print("test17d raw", ta);
			TimeAddressable<Double> tb = ta.get(new Range(Day.DOMAIN, "2006-04-06", "2006-04-12", Adjustment.NONE));
			print("test17d range", tb);
			assertEquals("2006-04-08", tb.getRange().getFirst().toString());
			assertEquals("2006-04-09", tb.getRange().getLast().toString());
			tb.put(new Day("2006-04-18"), 18d);
			print("test17d raw after modifying the copy", ta);
			print("test17d range after modifying the copy", tb);
			assertEquals(ta.getMissingValue(), ta.get(new Day("2006-04-18")));
			assertEquals("2006-04-16", ta.getRange().getLast().toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}
	
	public void test17d1() {
		try {
			ta = TimeSeriesFactory.make(Day.DOMAIN, Double.class, true);
			ta.put(new Day("2006-04-04"), 4d);
			ta.put(new Day("2006-04-08"), 8d);
			ta.put(new Day("2006-04-09"), 9d);
			ta.put(new Day("2006-04-15"), 15d);
			ta.put(new Day("2006-04-16"), 16d);
			print("test17d1 raw", ta);
			TimeAddressable<Double> tb = ta.get(new Range(Day.DOMAIN, "2006-04-06", "2006-04-15", Adjustment.NONE));
			print("test17d1 range", tb);
			assertEquals("2006-04-08", tb.getRange().getFirst().toString());
			assertEquals("2006-04-15", tb.getRange().getLast().toString());
			ta.put(new Day("2006-04-14"), 14d);
			print("test17d1 raw after modifying the original", ta);
			print("test17d1 range after modifying the original", tb);
			assertEquals(14d, ta.get(new Day("2006-04-14")));
			assertEquals(tb.getMissingValue(), tb.get(new Day("2006-04-14")));
			assertEquals("2006-04-16", ta.getRange().getLast().toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}
	
	public void test17d2() {
		try {
			ta = TimeSeriesFactory.make(Day.DOMAIN, Double.class, true);
			ta.put(new Day("2006-04-04"), 4d);
			ta.put(new Day("2006-04-08"), 8d);
			ta.put(new Day("2006-04-09"), 9d);
			ta.put(new Day("2006-04-15"), 15d);
			ta.put(new Day("2006-04-16"), 16d);
			print("test17d2 raw", ta);
			TimeAddressable<Double> tb = ta.get(new Range(Day.DOMAIN, "2006-04-06", "2006-04-15", Adjustment.NONE));
			print("test17d2 range", tb);
			assertEquals("2006-04-08", tb.getRange().getFirst().toString());
			assertEquals("2006-04-15", tb.getRange().getLast().toString());
			ta.put(new Day("2006-04-09"), 90d);
			ta.put(new Day("2006-04-18"), 18d);
			print("test17d2 raw after modifying the original", ta);
			print("test17d2 range after modifying the original", tb);
			assertEquals(18d, ta.get(new Day("2006-04-18")));
			assertEquals(9d, tb.get(new Day("2006-04-09")));
			assertEquals(tb.getMissingValue(), tb.get(new Day("2006-04-18")));
			assertEquals("2006-04-18", ta.getRange().getLast().toString());
		} catch (Exception e) {
			fail("unexpected exception");
		}
	}

	public void test17e() {
		try {
			ta = TimeSeriesFactory.make(Day.DOMAIN, Double.class, false);
			ta.put(new Day("2006-04-04"), 4d);
			ta.put(new Day("2006-04-08"), 8d);
			ta.put(new Day("2006-04-09"), 9d);
			ta.put(new Day("2006-04-15"), 15d);
			ta.put(new Day("2006-04-16"), 16d);
			print("test17e raw", ta);
			TimeAddressable<Double> tb = ta.get(new Range(Day.DOMAIN, "2006-04-06", "2006-04-12", Adjustment.NONE));
			tb.setRange(null);
			assertTrue(tb.getRange().isEmpty());
			assertEquals("2006-04-04", ta.getRange().getFirst().toString());
			assertEquals("2006-04-16", ta.getRange().getLast().toString());
		} catch (Exception e) {
			fail("unexpected exception");
		}
	}
	
	public void test17f() {
		try {
			ta = TimeSeriesFactory.make(Day.DOMAIN, Double.class, true);
			ta.put(new Day("2006-04-04"), 4d);
			ta.put(new Day("2006-04-08"), 8d);
			ta.put(new Day("2006-04-09"), 9d);
			ta.put(new Day("2006-04-15"), 15d);
			ta.put(new Day("2006-04-16"), 16d);
			TimeAddressable<Double> tb = ta.get(new Range(Day.DOMAIN, "2006-04-06", "2006-04-12", Adjustment.NONE));
			tb.setRange(null);
			assertTrue(tb.getRange().isEmpty());
			assertEquals("2006-04-04", ta.getRange().getFirst().toString());
			assertEquals("2006-04-16", ta.getRange().getLast().toString());
		} catch (Exception e) {
			fail("unexpected exception");
		}
	}
	
	public void test17g() {
		try {
			ta = TimeSeriesFactory.make(Day.DOMAIN, Double.class, false);
			ta.put(new Day("2006-04-04"), 4d);
			ta.put(new Day("2006-04-08"), 8d);
			ta.put(new Day("2006-04-09"), 9d);
			ta.put(new Day("2006-04-15"), 15d);
			ta.put(new Day("2006-04-16"), 16d);
			print("test17g raw", ta);
			TimeAddressable<Double> tb = ta.get(ta.getRange());
			tb.setRange(new Range(tb.getRange().getFirst().add(1), tb.getRange().getLast().add(-1)));
			assertEquals("2006-04-04", ta.getRange().getFirst().toString());
			assertEquals("2006-04-16", ta.getRange().getLast().toString());
			assertEquals("2006-04-08", tb.getRange().getFirst().toString());
			assertEquals("2006-04-15", tb.getRange().getLast().toString());
		} catch (Exception e) {
			fail("unexpected exception");
		}
	}

	public void test17h() {
		try {
			ta = TimeSeriesFactory.make(Day.DOMAIN, Double.class, true);
			ta.put(new Day("2006-04-04"), 4d);
			ta.put(new Day("2006-04-08"), 8d);
			ta.put(new Day("2006-04-09"), 9d);
			ta.put(new Day("2006-04-15"), 15d);
			ta.put(new Day("2006-04-16"), 16d);
			print("test17h raw", ta);
			TimeAddressable<Double> tb = ta.get(ta.getRange());
			tb.setRange(new Range(tb.getRange().getFirst().add(1), tb.getRange().getLast().add(-1)));
			assertEquals("2006-04-04", ta.getRange().getFirst().toString());
			assertEquals("2006-04-16", ta.getRange().getLast().toString());
			assertEquals("2006-04-08", tb.getRange().getFirst().toString());
			assertEquals("2006-04-15", tb.getRange().getLast().toString());
		} catch (Exception e) {
			fail("unexpected exception");
		}
	}
	
	public void test18R() {
		try {
			TimeDomain calendar = Day.DOMAIN;
			TimeAddressable<Double> ts = TimeSeriesFactory.make(calendar, Double.class);
			long t1 = calendar.time("2008-02-25").asLong();
			Double[] d1 = {0d, 1d, 2d, null};
			ts.put(t1, d1);
			print("*** test18R", ts);
			
			assertEquals("2008-02-25", ts.getRange().getFirst().toString());
			assertEquals("2008-02-28", ts.getRange().getLast().toString());

		} catch (Exception e) {
			assertEquals(K.T5014, ((KeyedException) e).getMsg().getKey());
		}
	}
	
	public void test18S() {
		try {
			TimeDomain calendar = Day.DOMAIN;
			TimeAddressable<Double> ts = TimeSeriesFactory.make(calendar, Double.class, true);
			long t1 = calendar.time("2008-02-25").asLong();
			Double[] d1 = {0d, 1d, 2d, null};
			ts.put(t1, d1);
			print("*** test18R", ts);
			
			assertEquals("2008-02-25", ts.getRange().getFirst().toString());
			assertEquals("2008-02-28", ts.getRange().getLast().toString());

		} catch (Exception e) {
			assertEquals(K.T5014, ((KeyedException) e).getMsg().getKey());
		}
	}

	public void test19R() {
		try {
			TimeDomain calendar = Day.DOMAIN;
			TimeAddressable<Double> ts = TimeSeriesFactory.make(calendar, Double.class);
			long t1 = calendar.time("2008-02-25").asLong();
			Double[] d1 = {1d, Double.NaN, 2d};
			ts.put(t1, d1);
			print("*** test19R", ts);
			
			assertEquals(true, ts.get(new Day("2008-02-26")).equals(ts.getMissingValue()));
			assertEquals(true, ts.get(new Day("2008-02-26")) == ts.getMissingValue());

		} catch (Exception e) {
			assertEquals("null values temporarily illegal", e.getMessage());
		}
	}

	public void test19S() {
		try {
			TimeDomain calendar = Day.DOMAIN;
			TimeAddressable<Double> ts = TimeSeriesFactory.make(calendar, Double.class, true);
			long t1 = calendar.time("2008-02-25").asLong();
			Double[] d1 = {1d, Double.NaN, 2d};
			ts.put(t1, d1);
			print("*** test19S", ts);
			
			assertEquals(true, ts.get(new Day("2008-02-26")).equals(ts.getMissingValue()));
			assertEquals(true, ts.get(new Day("2008-02-26")) == ts.getMissingValue());

		} catch (Exception e) {
			assertEquals("null values temporarily illegal", e.getMessage());
		}
	}

	private class Reviewer1 implements UpdateReviewer<Double> {
		
		int count;
		
		@Override
		public boolean accept(TimeAddressable<Double> series, long index, Double newValue) throws KeyedException {
			Double oldValue = series.get(index);
			if (series.isMissing(oldValue) || epsilon_same(oldValue, newValue))
				return true;
			else {
				dump(String.format("reject %s %10.3g %10.3g", series.getTimeDomain().time(index), oldValue, newValue));
				count++;
				return false;
			}
		}
		
	}
 	
	public void test20R() {
		Reviewer1 rev = new Reviewer1();
		try {
			TimeAddressable<Double> ts1 = TimeSeriesFactory.make(Day.DOMAIN, Double.class);
			TimeAddressable<Double> ts2 = TimeSeriesFactory.make(Day.DOMAIN, Double.class);
			TimeIndex date = Day.DOMAIN.time("2008-02-25");
			ts1.put(date.asLong(), new Double[]{1d, 2d, 3d, 4d, Double.NaN, 6d});
			ts2.put(date.asLong(), new Double[]{1d, 2d, 5d, 5d, 5d, 6d});
			ts1.put(ts2, rev);
			fail("exception expected");
		} catch (KeyedException e) {
			assertEquals(K.T5012, e.getMsg().getKey());
		}
	}
	
	public void test20S() {
		Reviewer1 rev = new Reviewer1();
		try {
			TimeAddressable<Double> ts1 = TimeSeriesFactory.make(Day.DOMAIN, Double.class, true);
			TimeAddressable<Double> ts2 = TimeSeriesFactory.make(Day.DOMAIN, Double.class);
			TimeIndex date = Day.DOMAIN.time("2008-02-25");
			ts1.put(date.asLong(), new Double[]{1d, 2d, 3d, 4d, Double.NaN, 6d});
			ts2.put(date.asLong(), new Double[]{1d, 2d, 5d, 5d, 5d, 6d});
			ts1.put(ts2, rev);
			fail("exception expected");
		} catch (KeyedException e) {
			assertEquals(K.T5012, e.getMsg().getKey());
			assertEquals(2, rev.count);
		}
	}

	public void test21() {
		try {
			TimeAddressable<Double> ts = TimeSeriesFactory.make(Day.DOMAIN, Double.class, true);
			TimeIndex date = Day.DOMAIN.time("2008-02-25");
			ts.put(date.asLong(), new Double[]{1d, 2d, 3d, 4d, Double.NaN, 6d});
			assertTrue(ts.get(1, 0).getRange().isEmpty());
		} catch (KeyedException e) {
			e.printStackTrace();
			fail("exception not expected");
		}
	}

	private class Filler1 implements Filler<Double> {
		@Override
		public void fillHole(Double[] val, int first, int last) {
//			System.out.println("first:" + first + " last:" + last);
			int length = last - first - 1;
			double step = (val[last] - val[first] - 1) / length; 
			for (int i = first + 1; i < last; i++)
				val[i] = val[i-1] + step;
		}
	}
	
	public void test22() {
		try {
			ta = TimeSeriesFactory.make(Day.DOMAIN, Double.class);
			ta.put(new Day("2006-04-04"), 4d);
			ta.put(new Day("2006-04-08"), 8d);
			ta.put(new Day("2006-04-09"), 9d);
			ta.put(new Day("2006-04-15"), 15d);
			ta.put(new Day("2006-04-16"), 16d);
			ta.put(new Day("2006-04-25"), 25d);
//			print("test22 raw", ta);
			((TimeIndexable<Double>) ta).fill(new Filler1());
//			print("test22 filled", ta);
			assertEquals(13d, ta.get(new Day("2006-04-13")));
			assertEquals(22d, ta.get(new Day("2006-04-22")));
			assertEquals(23d, ta.get(new Day("2006-04-23")));

		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

	private class Filler23 implements Filler<Double> {
		@Override
		public void fillHole(Double[] val, int first, int last) {
			for (int i = first + 1; i < last; i++)
				val[i] = null;
		}
	}
	
	public void test23() {
		try {
//			TimeSeriesFactory.getInstance().define(Double.class, null);
			ta = TimeSeriesFactory.make(Day.DOMAIN, Double.class);
			ta.put(new Day("2006-04-04"), 4d);
			ta.put(new Day("2006-04-08"), 8d);
			ta.put(new Day("2006-04-09"), 9d);
			ta.put(new Day("2006-04-15"), 15d);
			ta.put(new Day("2006-04-16"), 16d);
			ta.put(new Day("2006-04-25"), 25d);
			((TimeIndexable<Double>) ta).fill(new Filler23());
			fail("exception expected"); // no nulls allowed (when using null as MV, no exception)
		} catch (Exception e) {
			assertEquals(K.T5014, ((KeyedException) e).getMsg().getKey());
		}
	}

	private class Filler24 implements Filler<Double> {
		@Override
		public void fillHole(Double[] val, int first, int last) throws Exception {
			throw new Exception("testing...");
		}
	}

	public void test24() {
		try {
			ta = TimeSeriesFactory.make(Day.DOMAIN, Double.class);
			ta.put(new Day("2006-04-04"), 4d);
			ta.put(new Day("2006-04-08"), 8d);
			ta.put(new Day("2006-04-09"), 9d);
			ta.put(new Day("2006-04-15"), 15d);
			ta.put(new Day("2006-04-16"), 16d);
			ta.put(new Day("2006-04-25"), 25d);
			((TimeIndexable<Double>) ta).fill(new Filler24());
			fail("exception expected");
		} catch (Exception e) {
			assertEquals(K.T5017, ((KeyedException) e).getMsg().getKey());
		}
	}

	public void test25() {
		try {
			ta = TimeSeriesFactory.make(Day.DOMAIN, Double.class);
			ta.put(new Day("2006-04-04"), 4d);
			ta.put(new Day("2006-04-08"), 8d);
			ta.put(new Day("2006-04-09"), 9d);
			ta.put(new Day("2006-04-15"), 15d);
			ta.put(new Day("2006-04-16"), 16d);
			ta.put(new Day("2006-04-25"), 25d);
			int count = ((TimeIndexable<Double>) ta).fill(Double.NaN, 0);
			print("test25", ta);
			assertEquals(16, count);
			assertSame(ta.getMissingValue(), ta.get(Day.DOMAIN.time("2006-04-17")));
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}
	
}
