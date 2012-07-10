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
 * Type: DefaultExternalFormat
 * Version: 1.0.0
 */
package ch.agent.t2.time;

import java.util.Formatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.agent.core.KeyedException;
import ch.agent.t2.T2Msg;

/**
 * DefaultExternalFormat parses dates and time in an ISO-style format.
 * The following pseudo pattern is used: 
 * <blockquote>
 * <code>yyyyy*[-mm[-dd[ hh[:MM[:ss[.uuu[uuu]]]]]]]</code>
 * </blockquote>
 * The square brackets enclose optional parts, and y, m,
 * d, h, M, s, and u stand for decimal digits. The fields correspond to years, months,
 * days, hours, minutes, seconds and a hybrid field for milliseconds or
 * microseconds. With 3 digits, field 7 is interpreted as milliseconds, with 6
 * digits as microseconds.
 * The year can be any number,
 * the month must be in range [1, 12], the day between 1 and the last day of the 
 * current month in the current year, the hour in [0, 23], the minute and second in [0, 59], 
 * the millisecond in [0, 999] or the microsecond in  [0,999999]. 
 * The separators "-", ":" and "." are required.   
 * 
 * @author Jean-Paul Vetterli
 * @version 1.0.0
 */
public class DefaultExternalFormat implements ExternalTimeFormat {

	private static final String PATTERN = "(\\d*\\d\\d\\d\\d)(?:-(\\d\\d)(?:-(\\d\\d)(?: (\\d\\d)(?::(\\d\\d)(?::(\\d\\d)(?:\\.(\\d\\d\\d(?:\\d\\d\\d)?))?)?)?)?)?)?";
	
	/**
	 * Singleton implements thread-safe and lazy initialization of the
	 * DefaultExternalFormat instance.
	 * 
	 */
	private static class Singleton {
		private static DefaultExternalFormat singleton;
		static {
			singleton = new DefaultExternalFormat();
		};
	}
	
	/**
	 * Return the DefaultExternalFormat instance.
	 * @return the DefaultExternalFormat instance
	 */
	public static DefaultExternalFormat getInstance() {
		return Singleton.singleton;
	}

	private Matcher matcher;
	
	/**
	 * Construct a DefaultExternalFormat.
	 */
	public DefaultExternalFormat() {
		super();
		try {
			matcher = Pattern.compile(PATTERN).matcher("");
		} catch(Exception e) {
			throw new RuntimeException("bug", e);
		}
	}
	
	@Override
	public TimeParts scan(String time) throws KeyedException {
		if (time == null)
			throw new IllegalArgumentException("date null");
		matcher.reset(time);
		
		if (!matcher.matches())
			throw T2Msg.exception(32100, time);
		else {
			if (matcher.groupCount() != 7)
				throw T2Msg.exception(32110);
			TimeParts tp = new TimeParts();
			String group = null;
			try {
				for (int i = 0; i < 7; i++) {
					group = matcher.group(i + 1);
					if (group == null)
						break;
					switch(i) {
					case 0:
						tp.setYear(Long.valueOf(group).longValue());
						break;
					case 1:
						tp.setMonth(Integer.valueOf(group).intValue());
						break;
					case 2:
						tp.setDay(Integer.valueOf(group).intValue());
						break;
					case 3:
						tp.setHour(Integer.valueOf(group).intValue());
						break;
					case 4:
						tp.setMin(Integer.valueOf(group).intValue());
						break;
					case 5:
						tp.setSec(Integer.valueOf(group).intValue());
						break;
					case 6:
						tp.setUsec(Integer.valueOf(group).intValue());
						if (group.length() == 3)
							tp.setUsec(tp.getUsec() * 1000);
						break;
					default:
						throw new RuntimeException("bug: " + i);
					}
				}
			} catch (NumberFormatException e) {
				throw T2Msg.exception(e, 32120, group, time);
			}
			return tp;
		}
	}

	@Override
	public String format(Resolution unit, TimeParts tp) {
		StringBuilder sb = new StringBuilder();
		Formatter fmt = new Formatter(sb);
		switch (unit) {
		case YEAR:
			fmt.format("%04d", tp.getYear());
			break;
		case MONTH:
			fmt.format("%04d-%02d", tp.getYear(), tp.getMonth());
			break;
		case DAY:
			fmt.format("%04d-%02d-%02d", tp.getYear(), tp.getMonth(), tp.getDay());
			break;
		case HOUR:
			fmt.format("%04d-%02d-%02d %02d", tp.getYear(), tp.getMonth(), tp.getDay(),
					tp.getHour());
			break;
		case MIN:
			fmt.format("%04d-%02d-%02d %02d:%02d", tp.getYear(), tp.getMonth(),
					tp.getDay(), tp.getHour(), tp.getMin());
			break;
		case SEC:
			fmt.format("%04d-%02d-%02d %02d:%02d:%02d", tp.getYear(), tp.getMonth(),
					tp.getDay(), tp.getHour(), tp.getMin(), tp.getSec());
			break;
		case MSEC:
			fmt.format("%04d-%02d-%02d %02d:%02d:%02d.%03d", tp.getYear(),
					tp.getMonth(), tp.getDay(), tp.getHour(), tp.getMin(), tp.getSec(), tp.getUsec() / 1000);
			break;
		case USEC:
			fmt.format("%04d-%02d-%02d %02d:%02d:%02d.%06d", tp.getYear(),
					tp.getMonth(), tp.getDay(), tp.getHour(), tp.getMin(), tp.getSec(), tp.getUsec());
			break;
		default:
			throw new RuntimeException("bug: " + unit.name());
		}
		return sb.toString();
	}
	
	
}
