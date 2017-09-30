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

import java.util.Formatter;

/**
 * This time formatter is used to turn a time into a string. It supports the ISO
 * 8601:2004 international standard for the representation of calendar dates and
 * times, except week dates and ordinal dates. Depending on the constructor it
 * does or it does not insert a "T" between the date and the time.
 * 
 * @author Jean-Paul Vetterli
 */
public class DefaultTimeFormatter implements TimeFormatter {

	private final char T; 
	
	/**
	 * Constructor.
	 * 
	 * @param withT
	 *            if true insert a "T" between date and time
	 */
	public DefaultTimeFormatter(boolean withT) {
		super();
		T = withT ? 'T' : ' ';
	}
	
	/**
	 * Constructor for a mode without a "T" between date and time.
	 */
	public DefaultTimeFormatter() {
		this(false);
	}
	
	/**
	 * Generate a string representing the time in the time parts object. The actual
	 * format depends on the time resolution. For example, a time with day
	 * resolution will be formatted as "yyyy-mm-dd"; with second resolution it will
	 * be formatted as "yyyy-mm-dd hh:MM:ss" (without a "T" between date and time
	 * when using the default constructor).
	 * 
	 * @param unit
	 *            a non-null resolution
	 * @param tp
	 *            a non-null time parts object
	 * @return a string with the external representation of the time
	 */
	public String format(Resolution unit, TimeParts tp) {
		StringBuilder sb = new StringBuilder();
		Formatter fmt = new Formatter(sb);
		String plus = tp.getYear() > 9999 ? "+" : "";
		switch (unit) {
		case YEAR:
			fmt.format("%s%04d", plus, tp.getYear());
			break;
		case MONTH:
			fmt.format("%s%04d-%02d", plus, tp.getYear(), tp.getMonth());
			break;
		case DAY:
			fmt.format("%s%04d-%02d-%02d", plus, tp.getYear(), tp.getMonth(), tp.getDay());
			break;
		case HOUR:
			fmt.format("%s%04d-%02d-%02d%c%02d", plus, tp.getYear(), tp.getMonth(), tp.getDay(), T,
					tp.getHour());
			break;
		case MIN:
			fmt.format("%s%04d-%02d-%02d%c%02d:%02d", plus, tp.getYear(), tp.getMonth(),
					tp.getDay(), T, tp.getHour(), tp.getMin());
			break;
		case SEC:
			fmt.format("%s%04d-%02d-%02d%c%02d:%02d:%02d", plus, tp.getYear(), tp.getMonth(),
					tp.getDay(), T, tp.getHour(), tp.getMin(), tp.getSec());
			break;
		case MSEC:
			fmt.format("%s%04d-%02d-%02d%c%02d:%02d:%02d.%03d", plus, tp.getYear(),
					tp.getMonth(), tp.getDay(), T, tp.getHour(), tp.getMin(), tp.getSec(), tp.getFsec() / 1000);
			break;
		case USEC:
			fmt.format("%s%04d-%02d-%02d%c%02d:%02d:%02d.%06d", plus, tp.getYear(),
					tp.getMonth(), tp.getDay(), T, tp.getHour(), tp.getMin(), tp.getSec(), tp.getFsec());
			break;
		default:
			fmt.close();
			throw new RuntimeException("bug: " + unit.name());
		}
		fmt.close();
		return sb.toString();
	}
	
}
