/*
 *   Copyright 2011-2017 Hauser Olsson GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package ch.agent.t2.time;

import ch.agent.t2.T2Msg;
import ch.agent.t2.T2Msg.K;

/**
 * Single point of access for a configurable {@link TimeDomainCatalog}. When no
 * catalog has been configured, {@link DefaultTimeDomainCatalog} is used as a
 * default. This is overridden by passing the name of an alternative catalog
 * class as a system property with the key <em>TimeDomainCatalog</em>.
 *
 * @author Jean-Paul Vetterli
 */
public class TimeDomainCatalogSingleton {

	private static final String PROP = "TimeDomainCatalog";
	
	private static final TimeDomainCatalog catalog = catalog();
	
	private static TimeDomainCatalog catalog() {
		
		String className = System.getProperty(PROP);
		if (className == null)
			return new DefaultTimeDomainCatalog();
		else {
			try {
				return (TimeDomainCatalog) Class.forName(className).newInstance();
			} catch (Exception e) {
				Exception cause = T2Msg.exception(e, K.T0004, PROP, className);
				throw T2Msg.runtimeException(K.T0001, cause);
			}
		}
	}
	
	/**
	 * Return the TimeDomainCatalog instance.
	 * 
	 * @return the TimeDomainCatalog instance
	 */
	public static TimeDomainCatalog instance() {
		return catalog;
	}
	
	/**
	 * Disable instance creation.
	 */
	private TimeDomainCatalogSingleton() {
	}
	
}
