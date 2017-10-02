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
 * A time formatter is used to turn a time into a string. It supports the ISO
 * 8601:2004 international standard for the representation of calendar dates and
 * times, except week dates and ordinal dates.
 * 
 * @author Jean-Paul Vetterli
 */
public interface TimeFormatter {

	
	/**
	 * Generate a string representing the time in the time parts object.
	 * 
	 * @param tp
	 *            a non-null time parts object
	 * @return a string with the external representation of the time
	 */
	public String format(TimeParts tp);
	
}
