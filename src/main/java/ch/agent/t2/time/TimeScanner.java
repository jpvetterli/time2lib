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

/**
 * A time scanner extracts time parts from a string representation.
 * 
 * @author Jean-Paul Vetterli
 */
public interface TimeScanner {

	/**
	 * Scan the date string into a time parts object. The scanner extracts fields
	 * from the input and assigns them to time parts without checking their
	 * validity. From the point of view of the scanner day 42 of month 99 is okay.
	 * The time resolution is used for interpreting fractional seconds if present,
	 * because in a decimal fraction, leading zeros are significant but trailing
	 * zeros can be omitted (for example 1 corresponds to 100 milliseconds but
	 * 100000 microseconds).
	 * 
	 * @param unit
	 *            the time resolution
	 * @param datetime
	 *            a non-null string containing a date and time specification
	 * @return a {@link TimeParts}
	 * @throws Exception
	 */
	public TimeParts scan(Resolution unit, String datetime) throws T2Exception;

}
