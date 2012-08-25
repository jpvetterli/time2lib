package ch.agent.t2.timeseries.junit;

import junit.framework.TestCase;
import ch.agent.t2.time.Day;
import ch.agent.t2.timeseries.TimeAddressable;
import ch.agent.t2.timeseries.TimeSeriesFactory;

/**
 * TimeSeriesFactoryBaseTest tests first ever access to Double's missing value. This
 * is in its own class because the thing to test happens only once.
 *
 * @author Jean-Paul Vetterli
 */
public class TimeSeriesFactoryBootstrapTest extends TestCase {

	public TimeSeriesFactoryBootstrapTest() {
		super();
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void test0() {
		try {
			TimeAddressable<Double> ts = TimeSeriesFactory.make(Day.DOMAIN, Double.class);
			ts.put(new Day("2000-01-15"), 41d);
			ts.put(new Day("2000-01-20"), 42d);
			assertEquals(6, ts.getRange().getSize());
			assertEquals(new Double(Double.NaN), ts.get(new Day("2000-01-16")));
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}
}
