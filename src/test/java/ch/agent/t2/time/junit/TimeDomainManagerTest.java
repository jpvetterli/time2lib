package ch.agent.t2.time.junit;

import junit.framework.TestCase;
import ch.agent.core.KeyedException;
import ch.agent.t2.time.TimeDomainFactory;
import ch.agent.t2.time.TimeDomainManager;

public class TimeDomainManagerTest extends TestCase {

	private static TimeDomainFactory getFactory() {
		return TimeDomainManager.getFactory();
	}
	
	@Override
	protected void setUp() throws Exception {
		// or on the java command line; -DTimeDomainFactory=...
		System.setProperty("TimeDomainFactory", CustomTimeDomainFactory.class.getName());
	}

	public void test01() {
		TimeDomainFactory tdf = getFactory();
		assertEquals("ch.agent.t2.time.junit.CustomTimeDomainFactory", tdf.getClass().getCanonicalName());
	}
	
	public void test02() {
		try {
			assertEquals(3, getFactory().getBuiltIns().size());
			assertEquals("foo", getFactory().get("foo").getLabel());
			assertEquals("monthly", getFactory().get("monthly").getLabel());
			assertEquals("yearly", getFactory().get("yearly").getLabel());
		} catch (KeyedException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}

