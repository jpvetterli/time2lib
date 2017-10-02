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

/**
 * Resolution defines the available units of time, from years to nanoseconds.
 * Weeks are not supported directly. However a time domain for weeks is easy to implement.
 * <p>
 * Values are declared in order of increasing resolution, starting with YEAR,
 * and finishing with NSEC (nanoseconds). It is however recommended to compare
 * resolutions with {@link TimeDomain#compareResolutionTo(Resolution)}, rather
 * than relying on the natural ordering of this enum.
 * 
 * @author Jean-Paul Vetterli
 */
public enum Resolution {
	/**
	 * The smallest unit is a year.
	 */
	YEAR, 
	/**
	 * The smallest unit is a month.
	 */
	MONTH, 
	/**
	 * The smallest unit is a day.
	 */
	DAY, 
	/**
	 * The smallest unit is an hour.
	 */
	HOUR, 
	/**
	 * The smallest unit is a minute.
	 */
	MIN, 
	/**
	 * The smallest unit is a second.
	 */
	SEC, 
	/**
	 * The smallest unit is a millisecond.
	 */
	MSEC, 
	/**
	 * The smallest unit is a microsecond.
	 */
	USEC,
	/**
	 * The smallest unit is a nanosecond.
	 */
	NSEC

}