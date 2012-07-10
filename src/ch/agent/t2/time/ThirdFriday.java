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
 * Type: ThirdFriday
 * Version: 1.0.0
 */
package ch.agent.t2.time;

import ch.agent.core.KeyedException;
import ch.agent.t2.T2Msg;
import ch.agent.t2.time.engine.Time2;

/**
 * A ThirdFriday is a {@link Time2} with month base resolution and one day each month falling
 * on the 3d Friday. The domain label is <em>friday3</em>.
 *
 * @author Jean-Paul Vetterli
 * @version 1.0.0
 */
public class ThirdFriday extends Time2 {

	/**
	 * A constant holding the definition. 
	 */
	public static final TimeDomainDefinition DEF = init();

	/**
	 * A constant holding the domain.
	 */
	public static final TimeDomain DOMAIN = TimeDomainManager.getFactory().get(DEF, true);

	private static TimeDomainDefinition init() {
		DayByNameAndRank[] dbnar = new DayByNameAndRank[]{new DayByNameAndRank(DayOfWeek.Fri, 3)};
		SubPeriodPattern spp = new DayRankingSubPeriodPattern(Resolution.MONTH, dbnar);
		return new TimeDomainDefinition("friday3", Resolution.MONTH, 0L, null, spp);
	}

	/**
	 * Construct a <q>friday3</q> time from another time object. 
	 * @param time a non-null time in the domain <q>friday3</q>
	 * @throws KeyedException
	 */
	public ThirdFriday(TimeIndex time) throws KeyedException {
		super(DOMAIN, time.asLong());
		if (DOMAIN != time.getTimeDomain())
			throw T2Msg.exception(32152, time.getTimeDomain().getLabel(), DOMAIN.getLabel());
	}
	
	/**
	 * Construct a <q>friday3</q> time from a string.
	 *  
	 * @param date a non-null string
	 * @param adjust a non-null adjustment mode
	 * @throws KeyedException
	 */
	public ThirdFriday(String date, Adjustment adjust) throws KeyedException {
		super(DOMAIN, date, adjust);
	}
	
	/**
	 * Construct a <q>friday3</q> time from the given time components.
	 * 
	 * @param year a non-negative number
	 * @param month a number in [1-12]
	 * @throws KeyedException
	 */
	public ThirdFriday(long year, int month) throws KeyedException { 
		super(DOMAIN, year, month, 1, 0, 0, 0, 0, Adjustment.UP);
	}

}
