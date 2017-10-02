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
package ch.agent.t2.applied;

import ch.agent.t2.time.Day;
import ch.agent.t2.time.ImmutableTimeDomainCatalog;
import ch.agent.t2.timeseries.Observation;

/**
 * A default time domain catalog. It provides various built-in time domain
 * objects accessible in various ways.
 * <p>
 * The domains provided by this catalog have the following labels:
 * <ul>
 * <li>yearly
 * <li>monthly
 * <li>daily
 * <li>workweek
 * <li>weekly
 * <li>datetime
 * <li>systemtime
 * <li>friday3
 * </ul>
 * Refer to {@link Year}, {@link Month}, {@link Day}, {@link Week},
 * {@link Workday}, {@link DateTime}, {@link SystemTime}, {@link ThirdFriday}.
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
 * TimeDomainCatalog catalog = new DefaultTimeDomainCatalog();
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
 */
public class DefaultTimeDomainCatalog extends ImmutableTimeDomainCatalog {

	/**
	 * Construct a DefaultTimeDomainCatalog.
	 */
	public DefaultTimeDomainCatalog() {
		super(Year.DOMAIN, Month.DOMAIN, Day.DOMAIN, Week.DOMAIN, Workday.DOMAIN, DateTime.DOMAIN, SystemTime.DOMAIN, ThirdFriday.DOMAIN);
	}

}
