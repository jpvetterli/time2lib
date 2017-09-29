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
 * A ThirdFriday is a {@link Time2} with month base resolution and one day each month falling
 * on the 3d Friday. The domain label is <b>friday3</b>.
 *
 * @author Jean-Paul Vetterli
 */
public class ThirdFriday extends Time2 {

	/**
	 * A constant holding the domain label.
	 */
	public static final String LABEL = "friday3";
	
	/**
	 * A constant holding the domain.
	 */
	public static final TimeDomain DOMAIN = new TimeDomainDefinition(
		LABEL, Resolution.MONTH, 0L, null, 
		new DayRankingSubPeriodPattern(Resolution.MONTH, 
		new DayByNameAndRank[]{new DayByNameAndRank(DayOfWeek.Fri, 3)})
	).asTimeDomain();

	/**
	 * Construct a <q>friday3</q> time from another time object. 
	 * @param time a non-null time in the domain <q>friday3</q>
	 * @throws T2Exception
	 */
	public ThirdFriday(TimeIndex time) throws T2Exception {
		super(DOMAIN, time.asLong());
		if (DOMAIN != time.getTimeDomain())
			throw T2Msg.exception(K.T1073, time.getTimeDomain().getLabel(), DOMAIN.getLabel());
	}
	
	/**
	 * Construct a <q>friday3</q> time from a string.
	 *  
	 * @param date a non-null string
	 * @param adjust a non-null adjustment mode
	 * @throws T2Exception
	 */
	public ThirdFriday(String date, Adjustment adjust) throws T2Exception {
		super(DOMAIN, date, adjust);
	}
	
	/**
	 * Construct a <q>friday3</q> time from the given time components.
	 * 
	 * @param year a non-negative number
	 * @param month a number in [1-12]
	 * @throws T2Exception
	 */
	public ThirdFriday(long year, int month) throws T2Exception { 
		super(DOMAIN, year, month, 1, 0, 0, 0, 0, Adjustment.UP);
	}

}
