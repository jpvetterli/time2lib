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
 * See also {@link Year}, {@link Month}, {@link Day}, {@link Week},
 * {@link Workday}, {@link DateTime}, {@link SystemTime}, {@link ThirdFriday}.
 *
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
