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

import ch.agent.t2.T2Exception;
import ch.agent.t2.T2Msg;
import ch.agent.t2.T2Msg.K;

/**
 * A Month is a {@link Time2} with month resolution.
 * The domain label is <b>monthly</b>.
 *
 * @author Jean-Paul Vetterli
 */
public class Month extends Time2 {

	/**
	 * A constant holding the domain label.
	 */
	public static final String LABEL = "monthly";
	
	/**
	 * A constant holding the domain.
	 */
	public static final TimeDomain DOMAIN = new TimeDomainDefinition(LABEL, Resolution.MONTH, 0L).asTimeDomain();

	/**
	 * Construct a <q>monthly</q> time from another time object. 
	 * @param time a non-null time in the domain <q>monthly</q>
	 * @throws T2Exception
	 */
	public Month(TimeIndex time) throws T2Exception {
		super(DOMAIN, time.asLong());
		if (DOMAIN != time.getTimeDomain())
			throw T2Msg.exception(K.T1073, time.getTimeDomain().getLabel(), DOMAIN.getLabel());
	}
	
	/**
	 * Construct a <q>monthly</q> time from a string.
	 *  
	 * @param date a non-null string
	 * @throws T2Exception
	 */
	public Month(String date) throws T2Exception {
		super(DOMAIN, date);
	}
	
	/**
	 * Construct a <q>monthly</q> time from the given time components.
	 * 
	 * @param year a non-negative number
	 * @param month a number in [1-12]
	 * @throws T2Exception
	 */
	public Month(long year, int month) throws T2Exception { 
		super(DOMAIN, year, month, 0, 0, 0, 0, 0, Adjustment.NONE);
	}

}
