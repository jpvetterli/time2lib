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
 * times, except week dates and ordinal dates. Depending on the constructor, it
 * inserts a "T" between the date and the time and appends a "Z" to signify UTC.
 * 
 * @author Jean-Paul Vetterli
 */
public class DefaultTimeFormatter implements TimeFormatter {

	private final char T; 
	private final String Z; 
	
	/**
	 * Constructor.
	 * 
	 * @param withTandZ
	 *            if true insert a "T" between date and time and append a "Z" for UTC
	 */
	public DefaultTimeFormatter(boolean withTandZ) {
		super();
		T = withTandZ ? 'T' : ' ';
		Z = withTandZ ? "Z" : "";
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
	 * be formatted as "yyyy-mm-dd hh:MM:ss" (without a "T" between date and time and
	 * without the "Z" UTC time zone descriptor when using the default constructor).
	 * 
	 * @param tp
	 *            a non-null time parts object
	 * @return a string with the external representation of the time
	 */
	@Override
	public String format(TimeParts tp) {
		StringBuilder sb = new StringBuilder();
		Formatter fmt = new Formatter(sb);
		String plus = tp.getYear() > 9999 ? "+" : "";
		switch (tp.getResolution()) {
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
			fmt.format("%s%04d-%02d-%02d%c%02d%s", plus, tp.getYear(), tp.getMonth(), tp.getDay(), T,
					tp.getHour(), Z);
			break;
		case MIN:
			fmt.format("%s%04d-%02d-%02d%c%02d:%02d%s", plus, tp.getYear(), tp.getMonth(),
					tp.getDay(), T, tp.getHour(), tp.getMin(), Z);
			break;
		case SEC:
			fmt.format("%s%04d-%02d-%02d%c%02d:%02d:%02d%s", plus, tp.getYear(), tp.getMonth(),
					tp.getDay(), T, tp.getHour(), tp.getMin(), tp.getSec(), Z);
			break;
		case MSEC:
			fmt.format("%s%04d-%02d-%02d%c%02d:%02d:%02d.%03d%s", plus, tp.getYear(),
					tp.getMonth(), tp.getDay(), T, tp.getHour(), tp.getMin(), tp.getSec(), tp.getFsec(), Z);
			break;
		case USEC:
			fmt.format("%s%04d-%02d-%02d%c%02d:%02d:%02d.%06d%s", plus, tp.getYear(),
					tp.getMonth(), tp.getDay(), T, tp.getHour(), tp.getMin(), tp.getSec(), tp.getFsec(), Z);
			break;
		case NSEC:
			fmt.format("%s%04d-%02d-%02d%c%02d:%02d:%02d.%09d%s", plus, 
					TimeDomain.BASE_YEAR_FOR_NANO + tp.getYear(),
					tp.getMonth(), tp.getDay(), T, tp.getHour(), tp.getMin(), tp.getSec(), tp.getFsec(), Z);
			break;
		default:
			fmt.close();
			throw new RuntimeException("bug: " + tp.getResolution().name());
		}
		fmt.close();
		return sb.toString();
	}
	
}
