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
 * Type: Month
 * Version: 1.0.0
 */
package ch.agent.t2.time;

import ch.agent.core.KeyedException;
import ch.agent.t2.T2Msg;
import ch.agent.t2.time.engine.Time2;

/**
 * A Month is a {@link Time2} with month resolution.
 * The domain label is <em>monthly</em>.
 *
 * @author Jean-Paul Vetterli
 * @version 1.0.0
 */
public class Month extends Time2 {

	/**
	 * A constant holding the definition. 
	 */
	public static final TimeDomainDefinition DEF = new TimeDomainDefinition("monthly", Resolution.MONTH, 0L);

	/**
	 * A constant holding the domain.
	 */
	public static final TimeDomain DOMAIN = TimeDomainManager.getFactory().get(DEF, true);

	/**
	 * Construct a <q>monthly</q> time from another time object. 
	 * @param time a non-null time in the domain <q>monthly</q>
	 * @throws KeyedException
	 */
	public Month(TimeIndex time) throws KeyedException {
		super(DOMAIN, time.asLong());
		if (DOMAIN != time.getTimeDomain())
			throw T2Msg.exception(32152, time.getTimeDomain().getLabel(), DOMAIN.getLabel());
	}
	
	/**
	 * Construct a <q>monthly</q> time from a string.
	 *  
	 * @param date a non-null string
	 * @throws KeyedException
	 */
	public Month(String date) throws KeyedException {
		super(DOMAIN, date);
	}
	
	/**
	 * Construct a <q>monthly</q> time from the given time components.
	 * 
	 * @param year a non-negative number
	 * @param month a number in [1-12]
	 * @throws KeyedException
	 */
	public Month(long year, int month) throws KeyedException { 
		super(DOMAIN, year, month, 0, 0, 0, 0, 0, Adjustment.NONE);
	}

}
