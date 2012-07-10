/*
 *   Copyright 2011 Hauser Olsson GmbH
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
 * Package: ch.agent.t2.time
 * Type: Week
 * Version: 1.0.0
 */
package ch.agent.t2.time;

import ch.agent.core.KeyedException;
import ch.agent.t2.T2Msg;
import ch.agent.t2.time.engine.Time2;

/**
 * A Week is a {@link Time2} with day resolution and a cycle of 6 days OFF and 1 day
 * ON. The domain label is <em>weekly</em>. The ON day is Thursday. This means
 * that when creating a Week using a date other than a Thursday, the
 * {@link Adjustment} is important. An UP adjustment will
 * advance to the next Thursday and a DOWN adjustment will backtrack to the
 * previous one.
 * <p>
 * Using Thursday to identify weeks has advantages. This is a quote from a <a
 * href="http://en.wikipedia.org/wiki/ISO_8601#Week_dates">Wikipedia article</a>
 * on ISO 8601, the international date standard: <blockquote>
 * <em>The week number can be described by counting the Thursdays: 
 * week 12 contains the 12th Thursday of the year.</em> </blockquote> The domain
 * label is <em>weekly</em>.
 * 
 * @author Jean-Paul Vetterli
 * @version 1.0.0
 */
public class Week extends Time2 {

	/**
	 * A constant holding the definition. 
	 */
	public static final TimeDomainDefinition DEF = new TimeDomainDefinition("weekly", Resolution.DAY, 0L, 
			new Cycle(false, false, false, false, false, true, false), null);

	/**
	 * A constant holding the domain.
	 */
	public static final TimeDomain DOMAIN = TimeDomainManager.getFactory().get(DEF, true);
	
	/**
	 * Construct a <q>weekly</q> time from another time object. 
	 * @param time a non-null time in the domain <q>weekly</q>
	 * @throws KeyedException
	 */
	public Week(TimeIndex time) throws KeyedException {
		super(DOMAIN, time.asLong());
		if (DOMAIN != time.getTimeDomain())
			throw T2Msg.exception(32152, time.getTimeDomain().getLabel(), DOMAIN.getLabel());
	}
	
	/**
	 * Construct a <q>weekly</q> time from a string.
	 *  
	 * @param date a non-null string
	 * @param adjust a non-null adjustment mode
	 * @throws KeyedException
	 */
	public Week(String date, Adjustment adjust) throws KeyedException {
		super(DOMAIN, date, adjust);
	}
	
	/**
	 * Construct a <q>weekly</q> time from the given time components.
	 * 
	 * @param year a non-negative number
	 * @param month a number in [1-12]
	 * @param day a number between 1 and the last day of the month
	 * @param adjust a non-null adjustment mode
	 * @throws KeyedException
	 */
	public Week(long year, int month, int day, Adjustment adjust) throws KeyedException { 
		super(DOMAIN, year, month, day, 0, 0, 0, 0, adjust);
	}

}
