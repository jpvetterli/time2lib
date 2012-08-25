package ch.agent.t2.time.junit;

import ch.agent.t2.time.Month;
import ch.agent.t2.time.TimeDomainDefinition;
import ch.agent.t2.time.Resolution;
import ch.agent.t2.time.Year;
import ch.agent.t2.time.engine.AbstractTimeDomainFactory;

public class CustomTimeDomainFactory extends AbstractTimeDomainFactory {

	private static class Singleton {
		private static CustomTimeDomainFactory factory;
		static {
			factory = new CustomTimeDomainFactory();
			factory.declareBuiltIn(Year.DEF.getLabel());
			factory.declareBuiltIn(Month.DEF.getLabel());
			factory.get(new TimeDomainDefinition("foo", Resolution.MIN, 42L), true);
			factory.declareBuiltIn("foo");
			factory.lockBuiltIns();
		};
	}
	
	public static CustomTimeDomainFactory getInstance() {
		return Singleton.factory;
	}
	
	private CustomTimeDomainFactory() {
	}
	
}
