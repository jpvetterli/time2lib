package ch.agent.t2.time.junit;

import ch.agent.core.KeyedException;
import ch.agent.t2.time.TimeDomainCatalog;
import junit.framework.TestCase;

public class TimeDomainManagerTest extends TestCase {

	private TimeDomainCatalog catalog;
	
	@Override
	protected void setUp() throws Exception {
		catalog = new CustomTimeDomainCatalog();
	}

	public void test01() {
		assertEquals("ch.agent.t2.time.junit.CustomTimeDomainCatalog", catalog.getClass().getCanonicalName());
	}
	
	public void test02() {
		try {
			assertEquals(3, catalog.get().size());
			assertEquals("foo", catalog.get("foo").getLabel());
			assertEquals("monthly", catalog.get("monthly").getLabel());
			assertEquals("yearly", catalog.get("yearly").getLabel());
		} catch (KeyedException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}

