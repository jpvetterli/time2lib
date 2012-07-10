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
 * Type: TimePacker
 * Version: 1.0.0
 */
package ch.agent.t2.time;

import ch.agent.core.KeyedException;

/**
 * TimePacker defines the backend interface for the machinery implementing {@link TimeDomain} and {@link TimeIndex}. 
 * 
 * @author Jean-Paul Vetterli
 * @version 1.0.0
 */
public interface TimePacker {

	/**
	 * Return the base period pattern. This pattern is used to compress or
	 * decompress the raw numeric time index to or from a denser one. The result
	 * is null when no such pattern is defined.
	 * 
	 * @return the base period pattern or null
	 */
	BasePeriodPattern getBasePeriodPattern();

	/**
	 * Return the sub period pattern. This pattern is used to add sub periods to
	 * a base period. The result is null when no such pattern is defined.
	 * 
	 * @return the sub period pattern or null
	 */
	SubPeriodPattern getSubPeriodPattern();
	
	/**
	 * Return true if the numeric time index is valid for the domain. Depending on the
	 * <em>testOnly</em> argument, the method throws an exception when the time is not valid.
	 * 
	 * @param time
	 *            numeric time to validate
	 * @param testOnly
	 *            if true do not throw an exception when time is not valid
	 * @return true if valid
	 */
	boolean valid(long time, boolean testOnly) throws KeyedException;

	/**
	 * Unpack the numeric representation of time into its component elements.
	 * 
	 * @param time a numeric time index
	 * @return a TimeParts
	 */
	TimeParts unpack(long time);

	/**
	 * Pack the component elements of the time into a numeric time index. 
	 * Depending on the domain, not
	 * all times exist. In such cases, the time is adjusted upwards or downwards if permitted.
	 * When a base period and a sub period pattern are both active,
	 * the adjustment applies only to the sub period.
	 *  
	 * @param timeParts a time parts object
	 * @param adjust a non-null adjustment mode
	 * @return a numerical time index
	 * @throws KeyedException
	 */
	long pack(TimeParts timeParts, Adjustment adjust) throws KeyedException;

	/**
	 * Return the day of week for the given time.
	 * 
	 * @param time a time index
	 * @return the day of the week
	 * @throws KeyedException
	 */
	DayOfWeek getDayOfWeek(TimeIndex time) throws KeyedException;
	
}
