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
 * Package: ch.agent.t2.timeutil
 * Type: TimeUtil
 * Version: 1.0.0
 */
package ch.agent.t2.timeutil;

import ch.agent.core.KeyedException;
import ch.agent.t2.time.Adjustment;
import ch.agent.t2.time.SystemTime;
import ch.agent.t2.time.TimeDomain;
import ch.agent.t2.time.TimeIndex;

/**
 * TimeUtil provides a selection of stateless methods useful in various applications.
 *
 * @author Jean-Paul Vetterli
 * @version 1.0.0
 */
public class TimeUtil {

	/**
	 * Return the current date and time. If required by the time domain, the
	 * date is adjusted downward.
	 * 
	 * @param domain a non-null time domain
	 * @return the current time
	 */
	public static TimeIndex now(TimeDomain domain) {
		try {
			TimeIndex t = new SystemTime();
			return t.convert(domain, Adjustment.DOWN);
		} catch (KeyedException e) {
			// should not occur because of the adjustment
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Interpret a positive 2-digit year between 90 and 99 in the 1990s and all
	 * others in the 2000s. All other years are returned as is, even negative
	 * years.
	 * 
	 * @param year
	 *            a year, possibly with only two digits
	 * @return a four digit year
	 */
	public static int normalize2DigitYear(int year) {
		if (year > -1 && year < 100) {
			if (year >= 90)
				year += 1900;
			else
				year += 2000;
		}
		return year;
	}
	
}
