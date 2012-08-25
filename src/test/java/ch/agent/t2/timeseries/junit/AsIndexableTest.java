package ch.agent.t2.timeseries.junit;

import junit.framework.TestCase;
import ch.agent.core.KeyedException;
import ch.agent.t2.time.Day;
import ch.agent.t2.time.TimeDomain;
import ch.agent.t2.timeseries.Observation;
import ch.agent.t2.timeseries.TimeAddressable;
import ch.agent.t2.timeseries.TimeIndexable;
import ch.agent.t2.timeseries.TimeSeriesFactory;


public class AsIndexableTest extends TestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void test1() {
		try {
			TimeDomain calendar = Day.DOMAIN;
			TimeAddressable<Double> ts = TimeSeriesFactory.make(calendar, Double.class, true);
			long t1 = calendar.time("2008-02-26").asLong();
			long t2 = calendar.time("2008-03-02").asLong();
			Double[] d1 = {1d, 2d, 3d};
			Double[] d2 = {4d, 5d, 6d};
			ts.put(t1, d1);
			ts.put(t2, d2);
			for (Observation<Double> obs : ts)
				System.out.println(obs.getTime().toString() + " " + obs.getValue());
			
			TimeIndexable<Double> tsi = ts.asIndexable();
			Double[] values = tsi.getArray();
			for (Double d : values)
				System.out.println(d);
			
		} catch (KeyedException e) {
			assertEquals(null, e);
		}
	}
}
