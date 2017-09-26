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
 * ExternalTimeFormat defines the interface for scanning time strings and for
 * formatting time. The actual class or classes to use are defined using
 * {@link TimeDomainCatalogSingleton}.
 * 
 * @author Jean-Paul Vetterli
 */
public interface ExternalTimeFormat {

	/**
	 * Scan the date string into a time parts object. The scanner extracts
	 * fields from the input and assigns them to time parts without checking
	 * their validity. From the point of view of the scanner day 42 of month 99
	 * is okay. 
	 * 
	 * @param time
	 *            a non-null string containing a date and time specification
	 * @return a {@link TimeParts}
	 * @throws Exception
	 */
	TimeParts scan(String time) throws T2Exception;
	
	/**
	 * Generate a string representing the time in the time parts object.
	 * The format can be made to vary depending on the time resolution.
	 * 
	 * @param unit a non-null resolution
	 * @param timeParts a non-null time parts object
	 * @return a string with the external representation of the time
	 */
	String format(Resolution unit, TimeParts timeParts);
}
