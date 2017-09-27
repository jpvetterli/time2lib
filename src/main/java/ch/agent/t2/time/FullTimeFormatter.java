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
 * This time formatter is used to turn a time into a string. It always includes
 * all time components.
 * 
 * @author Jean-Paul Vetterli
 */
public class FullTimeFormatter extends DefaultTimeFormatter {

	/**
	 * Constructor.
	 * 
	 * @param withT
	 *            if true insert a "T" between date and time
	 */
	public FullTimeFormatter(boolean withT) {
		super(withT);
	}
	
	/**
	 * Constructor for a mode without a "T" between date and time.
	 */
	public FullTimeFormatter() {
		this(false);
	}
	
	/**
	 * Generate a string representing the time in the time parts object. The time
	 * resolution is ignored and all times are formatted as
	 * "yyyy-mm-dd hh:MM:ss.uuuuuu" or "yyyy-mm-ddThh:MM:ss.uuuuuu".
	 * 
	 * @param unit
	 *            ignored
	 * @param tp
	 *            a non-null time parts object
	 * @return a string with the external representation of the time
	 */
	public String format(Resolution unit, TimeParts tp) {
		return super.format(Resolution.USEC, tp);
	}

}
