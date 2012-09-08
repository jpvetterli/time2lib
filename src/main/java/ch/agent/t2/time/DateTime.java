/*
 *   Copyright 2011, 2012 Hauser Olsson GmbH
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
 * Type: DateTime
 * Version: 1.0.1
 */
package ch.agent.t2.time;

import ch.agent.t2.T2Exception;
import ch.agent.t2.T2Msg;
import ch.agent.t2.T2Msg.K;
import ch.agent.t2.time.engine.Time2;

/**
 * A DateTime is a {@link Time2} with second resolution and the origin at
 * 2000-01-01. For times before the origin, {@link TimeIndex#asOffset()} will return
 * negative values. The domain label is <b>datetime</b>.
 * <p>
 * This time corresponds to Unix time, except for the "epoch", 2000-01-01
 * instead of 1970-01-01.
 * 
 * @author Jean-Paul Vetterli
 * @version 1.0.1
 */
public class DateTime extends Time2 {

	private static final long ORIGIN = TimeDomain.DAYS_TO_20000101 * 24 * 60 * 60;
	/**
	 * A constant holding the definition. 
	 */

	public static final TimeDomainDefinition DEF = new TimeDomainDefinition("datetime", Resolution.SEC, ORIGIN);
	/**
	 * A constant holding the domain.
	 */
	public static final TimeDomain DOMAIN = TimeDomainManager.getFactory().get(DEF, true);

	/**
	 * Construct a <q>datetime</q> time from another time object. 
	 * @param time a non-null time in the domain <q>datetime</q>
	 * @throws T2Exception
	 */
	public DateTime(TimeIndex time) throws T2Exception {
		super(DOMAIN, time.asLong());
		if (DOMAIN != time.getTimeDomain())
			throw T2Msg.exception(K.T1073, time.getTimeDomain().getLabel(), DOMAIN.getLabel());
	}
	
	/**
	 * Construct a <q>datetime</q> time from a string.
	 *  
	 * @param date a non-null string
	 * @throws T2Exception
	 */
	public DateTime(String date) throws T2Exception {
		super(DOMAIN, date);
	}
	
	/**
	 * Construct a <q>datetime</q> time from the given time components.
	 * 
	 * @param year a non-negative number
	 * @param month a number in [1-12]
	 * @param day a number between 1 and the last day of the month
	 * @param hour a number in [0-23]
	 * @param min a number in [0-59]
	 * @param sec a number in [0-59]
	 * @throws T2Exception
	 */
	public DateTime(long year, int month, int day, int hour, int min, int sec) throws T2Exception { 
		super(DOMAIN, year, month, day, hour, min, sec, 0, Adjustment.NONE);
	}

}
