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
 * Type: Year
 * Version: 1.0.1
 */
package ch.agent.t2.time;

import ch.agent.t2.T2Exception;
import ch.agent.t2.T2Msg;
import ch.agent.t2.T2Msg.K;
import ch.agent.t2.time.engine.Time2;

/**
 * A Year is a {@link Time2} with year resolution.
 * The domain label is <em>yearly</em>.
 *
 * @author Jean-Paul Vetterli
 * @version 1.0.1
 */
public class Year extends Time2 {

	/**
	 * A constant holding the definition. 
	 */
	public static final TimeDomainDefinition DEF = new TimeDomainDefinition("yearly", Resolution.YEAR, 0L);

	/**
	 * A constant holding the domain.
	 */
	public static final TimeDomain DOMAIN = TimeDomainManager.getFactory().get(DEF, true);
	
	/**
	 * Construct a <q>yearly</q> time from another time object. 
	 * @param time a non-null time in the domain <q>yearly</q>
	 * @throws T2Exception
	 */
	public Year(TimeIndex time) throws T2Exception {
		super(DOMAIN, time.asLong());
		if (DOMAIN != time.getTimeDomain())
			throw T2Msg.exception(K.T1073, time.getTimeDomain().getLabel(), DOMAIN.getLabel());
	}
	
	/**
	 * Construct a <q>yearly</q> time from a string.
	 *  
	 * @param date a non-null string
	 * @throws T2Exception
	 */
	public Year(String date) throws T2Exception {
		super(DOMAIN, date);
	}
	
	/**
	 * Construct a <q>yearly</q> time from the given time components.
	 * 
	 * @param year a non-negative number
	 * @throws T2Exception
	 */
	public Year(long year) throws T2Exception { 
		super(DOMAIN, year, 0, 0, 0, 0, 0, 0, Adjustment.NONE);
	}

}