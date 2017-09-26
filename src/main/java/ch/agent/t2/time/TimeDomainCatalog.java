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

import java.util.Collection;

import ch.agent.t2.T2Exception;
import ch.agent.t2.timeseries.Observation;

/**
 * TimeDomainCatalog is an interface for retrieving {@link TimeDomain} objects.
 * A time domain catalog makes it easier to ensure that there is only one domain
 * with a given set of properties. A catalog provides built-in domains guaranteed
 * to be available whenever the catalog is present. The labels of such domains
 * can be used as external identifiers.
 * <p>
 * There are different ways to access a time domain. If the catalog is
 * implemented correctly, all methods should give the same object. Because time
 * domains are immutable it is not wrong in principle to use different objects
 * for the same time domain, but it has performance consequences. As an example,
 * an {@link Observation} encapsulates a time domain and processing observations
 * often involves comparing their time domains with #equals. As applications can
 * have thousands or even millions of observations, using the same object for
 * the same time domain makes applications faster. The following example code
 * shows 4 different ways to get the same domain object, in this case the
 * <em>daily</em> domain implemented by {@link Day}:
 * <p>
 * <blockquote>
 * 
 * <pre>
 * TimeDomainCatalog catalog = TimeDomainCatalogSingleton.instance();
 * TimeDomain d1 = new Day(&quot;0102-03-04&quot;).getTimeDomain();
 * TimeDomain d2 = Day.DOMAIN;
 * TimeDomain d3 = catalog.get(&quot;daily&quot;);
 * TimeDomain d4 = catalog.get(new TimeDomainDefinition(null, Resolution.DAY, 0L));
 * assertSame(d1, d2);
 * assertSame(d1, d3);
 * assertSame(d1, d4);
 * </pre>
 * 
 * </blockquote>
 * 
 * @author Jean-Paul Vetterli
 * @see TimeDomainDefinition
 * @see DefaultTimeDomainCatalog
 */
public interface TimeDomainCatalog {
	
	/**
	 * Return the time domain corresponding to the definition, ignoring the label.
	 * When no suitable time domain is found in this catalog, the method returns
	 * null.
	 * 
	 * @param definition
	 *            a non-null time domain definition
	 * @return a time domain matching the definition or null
	 */
	TimeDomain get(TimeDomainDefinition definition);
	
	/**
	 * Return the time domain with the given label. Throw an exception when not
	 * found.
	 * 
	 * @param label non-null string
	 * @return a time domain
	 * @throws T2Exception
	 */
	TimeDomain get(String label) throws T2Exception;
	
	/**
	 * Return all time domains defined in the catalog.
	 * 
	 * @return the collection of time domains 
	 */
	Collection<TimeDomain> get();
	
}
