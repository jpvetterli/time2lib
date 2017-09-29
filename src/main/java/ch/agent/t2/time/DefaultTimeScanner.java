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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.agent.t2.T2Exception;
import ch.agent.t2.T2Msg;
import ch.agent.t2.T2Msg.K;
import ch.agent.t2.time.TimeParts.HMSU;
import ch.agent.t2.time.TimeParts.TimeZoneOffset;

/**
 * The default time scanner supports the ISO 8601:2004 international
 * standard for the representation of calendar dates and times. Week
 * dates and ordinal dates are not supported.
 * <p>
 * The page <a href="http://en.wikipedia.org/wiki/ISO_8601">ISO 8601</a> at
 * Wikipedia describes the standard and provides a link to the official document.
 * <p>
 * DefaultExternalFormat scans combined dates and times  
 * agreeing with the following syntax (here in pseudo-code) :
 * <blockquote>
 * <pre>
 * <em>date-time</em> := <em>date-time-basic</em>|<em>date-time-extended</em>
 * <em>date-time-basic</em> := <em>date-basic</em>[<u>T</u><em>time-basic</em>[<u>Z</u>|(<u>+</u>|<u>-</u><em>time-basic</em>)]]
 * <em>date-basic</em> := yyyy[mm[dd]]
 * <em>time-basic</em> := hh[mm[ss[<u>,</u>|<u>.</u>u{1,6}]]]
 * <em>date-time-extended</em> := <em>date-extended</em>[<u>T</u>|<u> </u><em>time-extended</em>[<u>Z</u>|(<u>+</u>|<u>-</u><em>time-extended</em>)]]
 * <em>date-extended</em> := (<u>+</u>y+)?yyyy[<u>-</u>mm[<u>-</u>dd]]
 * <em>time-extended</em> := hh[<u>:</u>mm[<u>:</u>ss[<u>,</u>|<u>.</u>u{1-6}]]]</pre>
 * 
 * 
 * <div style="font-size:smaller">
 * Meta syntax: meta elements in italic; literals underlined; 
 * a single lowercase letter represents a single digit;
 * * means zero or more; + means one or more; ? means zero or one; 
 * {n,m} means n to m times; optional elements in square brackets; 
 * alternatives separated by vertical bar; parentheses for grouping. 
 * <p>
 * The fields represented by lowercase letters are year (y), month (m), day (d), 
 * hour (h), minute (m), second (s), fraction of a second (u). 
 * </div>
 * </blockquote>
 * </p>
 * The scanner does not interpret numbers itself and from
 * its point of view, hour 42 and minute 88 are fine.
 * The {@link DefaultTimeScanner#scan(String) scan(String)} method returns a TimeParts object, but there is no
 * guarantee that the date and time components have already been validated when the method returns. 
 * The responsibility for validating numbers falls to {@link TimeTools#makeRawIndex}. 
 * <p>
 * For the validation to succeed, the components must have the following values:
 * <ul>
 * <li>year non-negative;
 * <li>month in [1, 12];
 * <li>day in [1, n], where n is the last day of the month;
 * <li>hour in [0, 24] with 24 valid only when minute and second are 0;
 * <li>minute in [0, 59];
 * <li>second in [0, 60], with 60 (leap second) only tolerated on last of June and
 * December;
 * <li>microsecond in [0, 999999].
 * </ul>
 * <p>
 * There are some differences between the calendar date and time representation supported here 
 * and ISO 8601:2004:
 * <ul>
 * <li>years cannot be negative;
 * <li>years can only have more than 4 digits in the extended format;
 * <li>time can only be combined with a full date, with year, month, and day specified;
 * <li>in the extended format, date and time can be separated by a space;
 * <li>a decimal fraction can only be added to seconds;
 * <li>the time zone designator Z is redundant because the "local" time of the
 * the Time2 Library is always UTC;
 * <li>a time zone offset can have microsecond precision.
 * </ul>
 * <p>
 * 
 * @author Jean-Paul Vetterli
 */
public class DefaultTimeScanner implements TimeScanner {
	
	private static final Pattern PATTERN_1 = Pattern.compile("((?:\\+\\d+)?\\d\\d\\d\\d)(?:-(\\d\\d)(?:-(\\d\\d)(?:[T ]([0-9:.,]*)(?:Z|([+-][0-9:.,]*))?)?)?)?");
	// note: colon intentionally allowed in time pattern, improves error diagnostics
	private static final Pattern PATTERN_2 = Pattern.compile("(\\d\\d\\d\\d)(?:(\\d\\d)(?:(\\d\\d)(?:T([0-9:.,]*)(?:Z|([+-][0-9:.,]*))?)?)?)?");
	private static final Pattern TIME_PATTERN_1 = Pattern.compile("(\\d\\d)(?::(\\d\\d)(?::(\\d\\d)(?:[.,](\\d\\d?\\d?\\d?\\d?\\d?))?)?)?");
	private static final Pattern TIME_PATTERN_2 = Pattern.compile("(\\d\\d)(?:(\\d\\d)(?:(\\d\\d)(?:[.,](\\d\\d?\\d?\\d?\\d?\\d?))?)?)?");
	
	public DefaultTimeScanner() {
	}

	@Override
	public TimeParts scan(String datetime) throws T2Exception {
		TimeParts tp = null;
		if (datetime == null)
			throw new IllegalArgumentException("date null");
		int hyphen = datetime.indexOf('-');
		boolean hyphenated = hyphen > 0;
		if (hyphenated) {
			// make sure the T is in the date part (T mandatory when no separators)
			int bigT = datetime.indexOf('T');
			if (bigT >= 0 && bigT < hyphen)
				hyphenated = false;
		}
		
		Matcher matcher = hyphenated ? PATTERN_1.matcher(datetime) : PATTERN_2.matcher(datetime); 
		if (!matcher.matches())
			throw T2Msg.exception(hyphenated ? K.T1081 : K.T1082, datetime);
		else {
			if (matcher.groupCount() != 5)
				throw new RuntimeException("bug: unexpected count " + matcher.groupCount());
			String group = null;
			try {
				long year = 0;
				int month = 1; // allow to scan 2000 and 2000-01 as if 2000-01-01
				int day = 1; // same remark
				HMSU hmsu = new HMSU(0, 0, 0, 0);
				TimeZoneOffset tzo = null;
				for (int i = 0; i < 5; i++) {
					group = matcher.group(i + 1);
					if (group == null || group.length() == 0)
						continue;
					switch(i) {
					case 0:
						try {
							year = Long.valueOf(group).longValue();
						} catch (NumberFormatException e) {
							if (group.startsWith("+"))
								year = Long.valueOf(group.substring(1)).longValue();
							// don't ask me
						}
						break;
					case 1:
						month = Integer.valueOf(group).intValue();
						break;
					case 2:
						day = Integer.valueOf(group).intValue();
						break;
					case 3:
						hmsu = scanTime(hyphenated ? TIME_PATTERN_1.matcher(group) : TIME_PATTERN_2.matcher(group));
						if (hmsu == null)
							throw T2Msg.exception(hyphenated ? K.T1083 : K.T1084, group);
						break;
					case 4:
						HMSU hmsu2 = scanTime(hyphenated ? TIME_PATTERN_1.matcher(group.substring(1)) : TIME_PATTERN_2.matcher(group.substring(1)));
						if (hmsu2 == null)
							throw T2Msg.exception(hyphenated ? K.T1085 : K.T1086, group);
						tzo = new TimeZoneOffset(group.startsWith("-"), hmsu2.h(), hmsu2.m(), hmsu2.s(), hmsu2.u());
						break;
					default:
						throw new RuntimeException("bug: " + i);
					}
				}
				tp = new TimeParts(year, month, day, hmsu.h(), hmsu.m(), hmsu.s(), hmsu.u(), tzo);
			} catch (NumberFormatException e) {
				throw new RuntimeException("bug: group not numeric " + group);
			}
		}
		return tp;
	}

	/**
	 * Return null if match fails.
	 * @param matcher initialized with the input
	 * @return an HMSU object
	 * @throws T2Exception
	 */
	private HMSU scanTime(Matcher matcher) throws T2Exception {
		
		if (!matcher.matches())
			return null;
		else {
			if (matcher.groupCount() != 4)
				throw new RuntimeException("bug: unexpected count " + matcher.groupCount());
			String group = null;
			int h = 0, m = 0, s = 0, u = 0;
			try {
				for (int i = 0; i < 4; i++) {
					group = matcher.group(i + 1);
					if (group == null)
						break;
					switch(i) {
					case 0:
						h = Integer.valueOf(group).intValue();
						break;
					case 1:
						m = Integer.valueOf(group).intValue();
						break;
					case 2:
						s = Integer.valueOf(group).intValue();
						break;
					case 3:
						u = Integer.valueOf(group).intValue();
						switch (group.length()) {
						case 1:
							u = u * 100000;
							break;
						case 2:
							u = u * 10000;
							break;
						case 3:
							u = u * 1000;
							break;
						case 4:
							u = u * 100;
							break;
						case 5:
							u = u * 10;
							break;
						case 6:
							break;
						default:
							throw new RuntimeException("bug: " + group.length());
						}
						break;
					default:
						throw new RuntimeException("bug: " + i);
					}
				}
			} catch (NumberFormatException e) {
				throw new RuntimeException("bug: group not numeric " + group);
			}
			return new HMSU(h, m, s, u);
		}
	}

}
