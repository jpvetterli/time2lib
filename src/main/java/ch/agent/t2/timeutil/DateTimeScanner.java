/*
 *   Copyright 2011, 2012 Hauser Olsson GmbH
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
 * Package: ch.agent.t2.timeutil
 * Type: DateTimeScanner
 * Version: 1.0.1
 */
package ch.agent.t2.timeutil;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import ch.agent.t2.T2Exception;
import ch.agent.t2.T2Msg;
import ch.agent.t2.T2Msg.K;
import ch.agent.t2.time.Adjustment;
import ch.agent.t2.time.TimeDomain;
import ch.agent.t2.time.TimeIndex;

/**
 * DateTimeScanner supports the interpretation of strings as {@link TimeIndex} objects.
 * <p>
 * Here is an example of use:
 * <blockquote><pre><code> String pattern = 
 *     "\\S+ (\\S+) (\\d+) (\\d\\d):(\\d\\d):(\\d\\d) \\S+ (\\d\\d\\d\\d)";
 * int[] groups = {6, 1, 2, 3, 4, 5};
 * String[] months = 
 *     {"Jan", "Feb", "Mar", "Apr", "May", "Jun", 
 *      "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
 * DateTimeScanner scanner = new DateTimeScanner(pattern, groups);
 * scanner.setMonths(months);
 * TimeIndex time = scanner.scan(DateTime.DOMAIN, "Wed Nov 30 12:29:23 UTC 2010");
 * assertEquals("2010-11-30 12:29:23", time.toString());
 * </code></pre></blockquote>
 *
 * @author Jean-Paul Vetterli
 * @version 1.0.1
 */
public class DateTimeScanner {

	private TimeDomain domain;
	private Matcher matcher;
	private int[] patternGroups;
	private Map<String, Integer> months;
	private int twoDigitYearThreshold;
	
	/**
	 * Construct a date time scanner with the given pattern and groups. The
	 * pattern is parsed by {@link Pattern}. If the pattern is null, scanning is
	 * done directly by {@link TimeDomain#time(String)}. Group indexes can be
	 * specified in an array, with 1 for the first group. When the groups array
	 * is null, group indexes are assumed to be in increasing sequence. Group
	 * indexes are useless when the pattern is null.
	 * 
	 * @param pattern
	 *            a pattern or null
	 * @param groups
	 *            an array of indexes or null
	 * @throws T2Exception
	 */
	public DateTimeScanner(String pattern, int[] groups) throws T2Exception {
		if (pattern != null) {
			try {
				matcher = Pattern.compile(pattern).matcher("");
			} catch (PatternSyntaxException e) {
				throw T2Msg.exception(e, K.T7015, pattern);
			}
			int n = matcher.groupCount();
			if (n < 1 || n > 7)
				throw T2Msg.exception(K.T7016, pattern);							

			if (groups != null) {
				verifyGroups(n, groups);
			} else {
				groups = new int[n];
				for (int i = 0; i < n; i++) {
					groups[i] = i + 1;
				}
			}
			this.patternGroups = groups;
		}
		this.twoDigitYearThreshold = -1;
	}
	
	private void verifyGroups(int requiredLength, int[] groups) throws T2Exception {
		boolean error = false;
		if (groups.length != requiredLength)
			error = true;
		else {
			int[] check = new int[requiredLength];
			for (int i : groups) {
				if (i < 1 || i > requiredLength) {
					error = true;
					break;
				}
				if (check[i - 1] != 0) {
					error = true;
					break;
				}
				check[i - 1] = i;
			}
		}
		if (error)
			throw T2Msg.exception(K.T7017, requiredLength);
	}

	
	/**
	 * Set the default time domain. It will be used when dates are scanned
	 * without specifying a domain by {@link #scan(String)}.
	 * 
	 * @param domain
	 *            a time domain
	 */
	public void setDomain(TimeDomain domain) {
		this.domain = domain;
	}

	/**
	 * Define textual months. The method can be used repeatedly to
	 * define multiple keywords for months.
	 * 
	 * @param keywords an array of 12 month keywords
	 */
	public void setMonths(String[] keywords) {
		if (keywords == null || keywords.length != 12)
			throw new IllegalArgumentException("keywords.length != 12");
		if (months == null)
			months = new HashMap<String, Integer>(12);
		for (int i = 0; i < keywords.length; i++) {
			months.put(keywords[i], i + 1);
		}
	}

	/**
	 * Set a threshold to trigger special handling of two digit years. When a
	 * non-negative threshold is defined, a year in the range [0, 99] is handled
	 * using the following rule:
	 * <ul>
	 * <li>if the year is less than the threshold, 2000 is added,
	 * <li>else 1900 is added.
	 * </ul>
	 * Specify a threshold of 0 to always add 1900 and a threshold of 100 (or
	 * more) to always add 2000.
	 * <p>
	 * By default the threshold is negative, and there is no special handling of 
	 * two digit years.
	 *  
	 * @param threshold
	 */
	public void setTwoDigitYearThreshold(int threshold) {
		this.twoDigitYearThreshold = threshold;
	}

	/**
	 * Scan the date using the default domain.
	 * The default domain must be set with {@link #setDomain(TimeDomain)}.
	 * 
	 * @param date a non-null date/time
	 * @return a TimeIndex in the default domain
	 * @throws T2Exception
	 */
	public TimeIndex scan(String date) throws T2Exception {
		return scan(domain, date);
	}
	
	/**
	 * Scan the date in the given time domain.
	 * 
	 * @param domain a non-null time domain
	 * @param date a non-null date/time
	 * @return a TimeIndex in the given domain
	 * @throws T2Exception
	 */
	public TimeIndex scan(TimeDomain domain, String date) throws T2Exception {
		if (domain == null)
			throw new IllegalArgumentException("domain == null");
		if (date == null)
			throw new IllegalArgumentException("text null");
		if (matcher == null) {
			return domain.time(date);
		} else  {
			matcher.reset(date);
			if (matcher.matches()) {
				int[] t = new int[] { 0, 1, 1, 0, 0, 0, 0};
				for (int i = 0; i < patternGroups.length; i++) {
					if (months != null && i == 1) {
						String month = matcher.group(patternGroups[i]);
						Integer m = months.get(month);
						if (m == null)
							throw T2Msg.exception(K.T7019, date, month);
						t[i] = m;
					} else
						t[i] = Integer.valueOf(matcher.group(patternGroups[i]));
				}
				if (twoDigitYearThreshold >= 0) {
					if (t[0] < 100 && t[0] >= 0) {
						if (t[0] < twoDigitYearThreshold)
							t[0] += 2000;
						else
							t[0] += 1900;
					}
				}
				return domain.time(t[0], t[1],	t[2], t[3], t[4], t[5], t[6], Adjustment.NONE);
			} else
				throw T2Msg.exception(K.T7018, date, matcher.pattern().toString());
		} 
	}
	
}
