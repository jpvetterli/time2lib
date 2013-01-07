package ch.agent.t2.timeseries.junit;

import java.util.Iterator;

import junit.framework.TestCase;
import ch.agent.t2.time.Day;
import ch.agent.t2.time.TimeIndex;
import ch.agent.t2.timeseries.Observation;
import ch.agent.t2.timeseries.TimeAddressable;
import ch.agent.t2.timeseries.TimeSeriesFactory;


public class TimeSeriesFactoryTest extends TestCase {

	private static void dump(Object expr) {
		// System.out.println(expr);
	}

	public TimeSeriesFactoryTest() {
		super();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void test1() {
		/*
		 * This test cannot be mixed with other because of the missing value can only be defined once.
		 */
		try {
			TimeSeriesFactory.getInstance().define(Double.class, new Double(42.4242));
			TimeAddressable<Double> ta = TimeSeriesFactory.make(Day.DOMAIN, Double.class);
			TimeIndex time = Day.DOMAIN.time("2006-06-21"); ta.put(time, 1d); 
			time = time.add(1); 
			time = time.add(1); ta.put(time, 3d); 
			Iterator<Observation<Double>> i = ta.iterator();
			i.next();
			Observation<Double> obs = i.next();
			assertEquals("2006-06-22", obs.getTime().toString());
			assertEquals(42.4242, obs.getValue());
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}
	
	public void test2() {
		try {
			TimeSeriesFactory.getInstance().define(Integer.class, new Integer(100));
			TimeAddressable<Integer>tint = TimeSeriesFactory.make(Day.DOMAIN, Integer.class);
			tint.put(new Day("2000-01-15"), 41);
			tint.put(new Day("2000-01-20"), 42);
			assertEquals(6, tint.getRange().getSize());
			assertEquals(new Integer(100), tint.get(new Day("2000-01-16")));
			dump("test2");
			for (Observation<Integer> o : tint)
				dump(o.toString());
		} catch (Exception e) {
//			e.printStackTrace();
			fail("unexpected exception");
		}
	}
	

}
