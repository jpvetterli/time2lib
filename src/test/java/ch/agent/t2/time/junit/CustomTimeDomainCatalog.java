package ch.agent.t2.time.junit;

import ch.agent.t2.applied.Month;
import ch.agent.t2.applied.Year;
import ch.agent.t2.time.ImmutableTimeDomainCatalog;
import ch.agent.t2.time.Resolution;
import ch.agent.t2.time.TimeDomainDefinition;

public class CustomTimeDomainCatalog extends ImmutableTimeDomainCatalog {

	public CustomTimeDomainCatalog() {
		super(Year.DOMAIN, Month.DOMAIN, new TimeDomainDefinition("foo", Resolution.MIN, 42L).asTimeDomain());
	}
	
}
