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

import ch.agent.t2.T2Exception;
import ch.agent.t2.T2Msg;
import ch.agent.t2.T2Msg.K;
import ch.agent.t2.time.Adjustment;
import ch.agent.t2.time.Cycle;
import ch.agent.t2.time.Resolution;
import ch.agent.t2.time.Time2;
import ch.agent.t2.time.TimeDomain;
import ch.agent.t2.time.TimeDomainDefinition;
import ch.agent.t2.time.TimeIndex;

/**
 * A Workday is a {@link Time2} with day resolution and a cycle of 2 days OFF
 * days and 5 days ON. The domain label is <b>workweek</b>. The days OFF are
 * Saturday and Sunday.
 * 
 * @author Jean-Paul Vetterli
 */
public class Workday extends Time2 {

	/**
	 * A constant holding the domain label.
	 */
	public static final String LABEL = "workweek";
	
	/**
	 * A constant holding the domain.
	 */
	public static final TimeDomain DOMAIN = new TimeDomainDefinition(LABEL, Resolution.DAY, 0L, 
			new Cycle(false, false, true, true, true, true, true), null).asTimeDomain();
	
	/**
	 * Construct a <q>workweek</q> time from another time object. 
	 * @param time a non-null time in the domain <q>workweek</q>
	 * @throws T2Exception
	 */
	public Workday(TimeIndex time) throws T2Exception {
		super(DOMAIN, time.asLong());
		if (DOMAIN != time.getTimeDomain())
			throw T2Msg.exception(K.T1073, time.getTimeDomain().getLabel(), DOMAIN.getLabel());
	}
	
	/**
	 * Construct a <q>workweek</q> time from a string.
	 *  
	 * @param date a non-null string
	 * @throws T2Exception
	 */
	public Workday(String date) throws T2Exception {
		super(DOMAIN, date);
	}
	
	/**
	 * Construct a <q>workweek</q> time from a string.
	 *  
	 * @param date a non-null string
	 * @param adjust a non-null adjustment mode
	 * @throws T2Exception
	 */
	public Workday(String date, Adjustment adjust) throws T2Exception {
		super(DOMAIN, date, adjust);
	}
	
	/**
	 * Construct a <q>workweek</q> time from the given time components.
	 * 
	 * @param year a non-negative number
	 * @param month a number in [1-12]
	 * @param day a number between 1 and the last day of the month
	 * @param adjust a non-null adjustment mode
	 * @throws T2Exception
	 */
	public Workday(long year, int month, int day, Adjustment adjust) throws T2Exception { 
		super(DOMAIN, year, month, day, 0, 0, 0, 0, adjust);
	}

}
