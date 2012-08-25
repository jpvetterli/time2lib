package ch.agent.t2.time.junit;

import junit.framework.TestCase;
import ch.agent.core.KeyedException;
import ch.agent.t2.time.TimeDomain;
import ch.agent.t2.time.TimeDomainFactory;
import ch.agent.t2.time.TimeDomainManager;

public class TimeDomainManagerTest extends TestCase {

	@Override
	protected void setUp() throws Exception {
		System.setProperty("TimeDomainFactory", CustomTimeDomainFactory.class.getName());
	}

	public void test01() {
		TimeDomainFactory tdf = TimeDomainManager.getFactory();
		// if test fails, set the factory with system property -DTimeDomainFactory=...
		assertEquals("ch.agent.t2.time.junit.CustomTimeDomainFactory", tdf.getClass().getCanonicalName());
	}
	
	public void test02() {
		try {
			System.out.println(TimeDomainManager.getFactory().getBuiltIns().toString());
			TimeDomain foo = TimeDomainManager.getFactory().get("foo");
			assertEquals("foo", foo.getLabel());
		} catch (KeyedException e) {
			fail(e.getMessage());
		}
	}

}

