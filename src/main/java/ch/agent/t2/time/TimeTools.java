/*
 *   Copyright 2011-2013 Hauser Olsson GmbH
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

import java.util.Arrays;

import ch.agent.t2.T2Exception;
import ch.agent.t2.T2Msg;
import ch.agent.t2.T2Msg.K;
import ch.agent.t2.time.TimeParts.HMSU;
import ch.agent.t2.time.TimeParts.TimeZoneOffset;
import ch.agent.t2.time.TimeParts.YMD;

/**
 * TimeTools provides a selection of methods useful in time computations. 
 * These methods do not need any state.
 * 
 * @author Jean-Paul Vetterli
 */
public class TimeTools {
	
	/**
	 * Number of days in 400 years, the smallest repeating sequence of days.
	 */
	private static final long DAYS_IN_400_YEARS = 365 * 303 + 366 * 97; 
	
	/**
	 * Number of days in the year before this month, January = 0.
	 */
	private static final int[] daysToMonthCommonYear = { 0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334 };
	/**
	 * Number of days in a leap year before this month, January = 0.
	 */
	private static final int[] daysToMonthLeapYear = { 0, 31, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335 };

	/**
	 * Return the number of days for the indicated year and month.
	 * 
	 * @param year
	 *            a non-negative number
	 * @param month
	 *            a number in the range [1, 12]
	 * @return the number of days
	 */
	public static int daysInMonth(long year, int month) {
		int[] daysToMonth = isLeap(year) ? daysToMonthLeapYear : daysToMonthCommonYear;
		if (month == 12)
			return 31;
		else
			return daysToMonth[month] - daysToMonth[month - 1];
	}

	/**
	 * Return the number of days in the year before the indicated month. The
	 * caller is assumed to pass meaningful inputs.
	 * 
	 * @param year
	 *            a non-negative number
	 * @param month
	 *            a number between 1 and 12
	 * @return the number of days in this year before this month
	 */
	public static int daysToMonth(long year, int month) {
		int[] daysToMonth = isLeap(year) ? daysToMonthLeapYear : daysToMonthCommonYear;
		return daysToMonth[month - 1];
	}
	
	/**
	 * Return true if the given year is a leap year.
	 * 
	 * @param year
	 *            a non-negative number
	 * 
	 * @return true if the year is a leap year
	 */
	public static boolean isLeap(long year) {
		if (year < 0)
			throw new IllegalArgumentException();
		return (year % 4 == 0) && (year % 100 != 0) || (year % 400 == 0);
	}
	
	/**
	 * Return the number of leap years between year 0 and the year preceding the given
	 * year. Assumes that year zero is a leap year.
	 * 
	 * @param year
	 *            a non-negative number
	 * @return the number of leap year before the given year
	 */
	public static long leapYears(long year) {
		if (year < 0)
			throw new IllegalArgumentException();
		if (year == 0)
			return 0;
		year--;
		return 1 + year / 4 - year / 100 + year / 400;
	}

	/**
	 * Return an array with the month and the day for the given year and day in year.
	 * 
	 * @param year a non-negative number
	 * @param day the day number of the year, with the first day = 1
	 * @return a two element array with the month and the day
	 */
	public static int[] computeMonthAndDay(long year, int day) {
		int[] md = new int[2];
		int[] daysToMonth = isLeap(year) ? daysToMonthLeapYear : daysToMonthCommonYear;
		int i = Arrays.binarySearch(daysToMonth, day - 1);
		if (i >= 0) {
			md[0] = i + 1;
			md[1] = 1; // first day of month
		} else {
			i = -i - 2;
			md[0] = i + 1;
			md[1] = day - daysToMonth[i];
		}
		return md;
	}

	/**
	 * Extract hours, minutes and seconds from totalSeconds.
	 * 
	 * @param totalSeconds
	 *            a non-negative number
	 * @return an immutable object with houts, minutes, seconds (and 0 sub-second
	 *         units)
	 */
	public static HMSU computeHMS(long totalSeconds) {
		return computeHMS(totalSeconds, 0);
	}
	
	/**
	 * Extract hours, minutes and seconds from totalSeconds.
	 * 
	 * @param totalSeconds
	 *            a non-negative number
	 * @param subSeconds
	 *            a non-negative number of sub-seconds
	 * @return an immutable object with houts, minutes, seconds, and sub-second
	 *         units
	 */
	public static HMSU computeHMS(long totalSeconds, int subSeconds) {
		if (totalSeconds < 0)
			throw new IllegalArgumentException("totalSeconds negative");
		if (subSeconds < 0)
			throw new IllegalArgumentException("subSeconds negative");
		// avoid modulo arithmetic
		long minutes = totalSeconds / 60;
		int hours = (int) (minutes / 60);
		return new HMSU(hours, (int) (minutes - hours * 60), (int) (totalSeconds - minutes * 60), subSeconds);
	}
	
	/**
	 * Extract years, month of year and day of month from total days. This method
	 * must deal with large inputs, corresponding to ridiculously large years.
	 * TimeIndex inputs are not validated at object creation time. This is for
	 * performance reasons: many TimeIndex objects to create, few of them to format.
	 * So we must deal with the issue here. To avoid the consequence of losing
	 * precision when converting large longs to double, we avoid using doubles
	 * altogether.
	 * 
	 * @param days
	 *            a non-negative number
	 * @return an immutable object with years, months, and days
	 */
	public static YMD computeYMD(long days) {
		if (days < 0)
			throw new IllegalArgumentException("" + days);

		/*
		 * By definition of leap years, the number of leap years during a 400
		 * year interval is constant (97), and the number of days is also a
		 * constant. The algorithm to determine the year, month and day, is
		 * based on this fact.
		 * 
		 * The first step computes the number of full 400 years intervals in the
		 * total number of days and the number of days remaining. These can be
		 * computed exactly. The second step computes the number of years and
		 * days in the remainder. This second step is implemented using pseudo
		 * years of 365 days and an error correction scheme, taking in account
		 * that at most 97 days have been ignored, less than a full year. The
		 * error correction scheme is complicated by the fact that the relevant
		 * year can itself be a leap year.
		 */
		
		long y400Intervals = days / DAYS_IN_400_YEARS;
		int y400Remainder = (int) (days - y400Intervals * DAYS_IN_400_YEARS);
		
		int remainingYears = y400Remainder / 365;
		int dayOffset = y400Remainder - remainingYears * 365 - (int) TimeTools.leapYears(remainingYears);
		
		if (dayOffset < 0) {
			remainingYears--;
			dayOffset += (TimeTools.isLeap(remainingYears) ? 366 : 365);
		}
		
		long y = y400Intervals * 400 + remainingYears;
		int[]monthDay = TimeTools.computeMonthAndDay(y, dayOffset + 1);
		return new YMD(y, monthDay[0], monthDay[1]);
	}

	/**
	 * Return a long number representing a time parts object. The method
	 * enforces rules on acceptable values of components. Two special features
	 * of the ISO 8601 standard are supported:
	 * <ol>
	 * <li>Midnight can be represented either as hour 0 of the day or as hour 24
	 * of the preceding day.
	 * <li>A 61st second, known as a leap second, is tolerated on the last day
	 * of June or the last day of the year. There is some confusion about when
	 * leap seconds can be inserted; the last of June or December is mentioned
	 * by the IERS, see <a
	 * href="http://hpiers.obspm.fr/iers/bul/bulc/bulletinc.dat">IERS Bulletin C
	 * 42, July 2011</a>, which should be authoritative since it is the official
	 * "publisher" of leap seconds.
	 * </ol>
	 * <p>
	 * <b>Note about leap seconds</b>
	 * <p>
	 * When a 61st second occurs in the input for a day when leap seconds are
	 * tolerated, the software simply changes it into the 60th second. This is
	 * the only case in the Time2 Library where leap seconds play a role. When
	 * constructing a time with {@link TimeIndex#add(long)} for example, leap seconds play no
	 * role. Adding 1 second to the {@link DateTime} domain TimeIndex
	 * represented by 2008-12-31T23:59:59 yields 2009-01-01T00:00:00 instead of
	 * the official leap second 2008-12-31T23:59:60.
	 * 
	 * @param unit
	 *            a non-null resolution
	 * @param tp time represented in a time parts object
	 * @return a numeric time index
	 * @throws T2Exception
	 */
	public static long makeRawIndex(Resolution unit, TimeParts tp) throws T2Exception {
		long time = 0;
		long year = tp.getYear();
		int month = tp.getMonth();
		int day = tp.getDay();
		int hour = tp.getHour();
		int min = tp.getMin();
		int sec = tp.getSec();
		int usec = tp.getUsec();
		if (year < 0)
			throw T2Msg.exception(K.T1014, year);
		if (unit == Resolution.YEAR) {
			time = year;
		} else {
			if (month < 1 || month > 12)
				throw T2Msg.exception(K.T1015, month);
			if (unit == Resolution.MONTH) {
				time = year * 12 + month - 1; // -1: month 1-based
			} else {
				int daysInThisMonth = TimeTools.daysInMonth(year, month);
				if (day < 1 || day > daysInThisMonth)
					throw T2Msg.exception(K.T1016, day,	daysInThisMonth);
				time = year * 365 + TimeTools.leapYears(year)
						+ TimeTools.daysToMonth(year, month) + day - 1; // -1: day 1-based
				if (unit != Resolution.DAY) {
					// get 24 hour notation out of the way (ISO 8601 tolerates 24:00:00) 
					if (hour == 24) {
						if (min == 0 && sec == 0 && usec == 0) {
							hour = 0;
							time += 1;
						}
					}
					// get leap seconds out of the way
					if (sec == 60) {
						if (hour == 23 && min == 59 && usec == 0 && 
								(month == 12 && day == 31) || 
								(month == 6 && day == 30)) {
							sec = 59;
						} else {
							throw T2Msg.exception(K.T1025);
						}
					}
					
					CompositeOverflowAndHMSU checkResult = checkTimeComponentsAndApplyTimeZoneOffset(hour, min, sec, usec, tp.getTZOffset());
					
					time += checkResult.overflow;
					hour = checkResult.hmsu.h();
					min = checkResult.hmsu.m();
					sec = checkResult.hmsu.s();
					usec = checkResult.hmsu.u();
					
					time = time * 24 + hour;
					if (unit != Resolution.HOUR) {
						time = time * 60 + min;
						if (unit != Resolution.MIN) {
							time = time * 60 + sec;
							if (unit != Resolution.SEC) {
								if (unit == Resolution.MSEC)
									time = time * 1000L + usec / 1000;
								else if (unit == Resolution.USEC)
									time = time * 1000000L + usec;
								else
									throw new RuntimeException("bug: " + unit.name());
							}
						}
					}
				}
			}
		}
		return time;
	}
	
	private static class CompositeOverflowAndHMSU {
		int overflow; // -1 underflow, +1 overflow, 0 no overflow
		HMSU hmsu; // modified values
		public CompositeOverflowAndHMSU(int overflow, HMSU hmsu) {
			super();
			this.overflow = overflow;
			this.hmsu = hmsu;
		}
	}
	
	/**
	 * Apply the time zone offset to time components and resolve all overflows.
	 * Return a composite with an overflow indicator and modified time components.
	 * The overflow indicator is 1 if there was an hour overflow, -1 if there was an
	 * underflow, and 0 in all other cases. As a side effect, the method verifies
	 * the validity of all time components. Leap seconds and 24 hour notation for
	 * midnight are not supported (they must be dealt with before calling the
	 * method).
	 * 
	 * @param hour
	 *            hours
	 * @param min
	 *            minutes
	 * @param sec
	 *            seconds
	 * @param usec
	 *            sub-seconds
	 * @param tzOffset
	 *            time zone offset or null
	 * @return a composite with an overflow indicator and and HMSU object
	 * @throws T2Exception
	 */
	private static CompositeOverflowAndHMSU checkTimeComponentsAndApplyTimeZoneOffset(int hour, int min, int sec, int usec, TimeZoneOffset tzOffset) throws T2Exception {
		
		if (hour < 0 || hour > 23)
			throw T2Msg.exception(K.T1017, hour);
		if (min < 0 || min > 59)
			throw T2Msg.exception(K.T1019, min);
		if (sec < 0 || sec > 59)
			throw T2Msg.exception(K.T1022, sec);
		if (usec < 0 || usec > 999999)
			throw T2Msg.exception(K.T1026, usec);
		
		int overflow = 0;
		
		if (tzOffset != null) {
			if (tzOffset.isNegative()) {
				usec -= tzOffset.getUsec();
				if (usec > 999999) {
					usec -= 1000000;
					sec += 1;
				}
				sec -= tzOffset.getSec();
				if (sec > 59) {
					sec -= 60;
					min += 1;
				}
				min -= tzOffset.getMin();
				if (min > 59) {
					min -= 60;
					hour += 1;
				}
				hour -= tzOffset.getHour();
				if (hour > 23) {
					hour -= 24;
					overflow = 1;
				}
			} else {
				usec -= tzOffset.getUsec();
				if (usec < 0) {
					usec += 1000000;
					sec -= 1;
				}
				sec -= tzOffset.getSec();
				if (sec < 0) {
					sec += 60;
					min -= 1;
				}
				min -= tzOffset.getMin();
				if (min < 0) {
					min += 60;
					hour -= 1;
				}
				hour -= tzOffset.getHour();
				if (hour < 0) {
					hour += 24;
					overflow = -1;
				}
			}
			tzOffset = null;
		}
		
		return new CompositeOverflowAndHMSU(overflow, new HMSU(hour, min, sec, usec));
	}

	
	/**
	 * Return the day index computed from the uncompressed numerical time.
	 * 
	 * @param unit a non- null resolution
	 * @param time a non-negative numerical time index
	 * @return a number of days
	 * @throws T2Exception
	 */
	public static long dayIndex(Resolution unit, long time) throws T2Exception {
		long dayIndex;
		switch (unit) {
		case DAY:
			dayIndex = time;
			break;
		case HOUR:
			dayIndex = time / 24;
			break;
		case MIN:
			dayIndex = time / (24 * 60);
			break;
		case SEC:
			dayIndex = time / (24 * 60 * 60);
			break;
		case MSEC:
			dayIndex = time / (24L * 60L * 60L * 1000L);
			break;
		case USEC:
			dayIndex = time / (24L * 60L * 60L * 1000000L);
			break;
		default:
			throw T2Msg.exception(K.T1060, unit);
		}
		return dayIndex;
	}
	
	/**
	 * Return the day of week for the given unit and uncompressed time index.
	 * The computation assumes that January 1 of year zero falls on a Saturday. 
	 * 
	 * @param time an uncompressed numerical time
	 * @return a {@link DayOfWeek}
	 * @throws T2Exception
	 */
	public static DayOfWeek getDayOfWeek(Resolution unit, long time) throws T2Exception {
		int day = (int)(TimeTools.dayIndex(unit, time) % 7); // 0 -> Saturday
		if (day == 0)
			day = 6;
		else
			day--;
		return DayOfWeek.values()[day];
	}
	
	/**
	 * Return the day number of the month or of the year given the day's name and rank. 
	 * When the month parameter has a value of zero, the day of the year will be computed.
	 * A zero result indicates that a maximal rank of 5 or -5 (for a month) or 53 or -53 (for a year)
	 * was requested but there is no such day in the given month or year.
	 * 
	 * @param year a non-negative number
	 * @param month a number in [1, 12] for the day of the month, 0 for the day of the year
	 * @param name the day of week
	 * @param rank a non-zero number in the range [-5, 5] or [-53, 53]
	 * @return the day of the month or the year, starting with 1 
	 * @throws T2Exception
	 */
	public static int getDayByRank(long year, int month, DayOfWeek name, int rank) throws T2Exception {
		int max = 5;
		if (month == 0)
			max = 53;
		if (rank == 0 || rank < -max || rank > max)
			throw T2Msg.exception(K.T1051, rank, -max, max);
		int daysInPeriod;
		if (month == 0) {
			month = 1;
			daysInPeriod = isLeap(year) ? 366 : 365;
		} else
			daysInPeriod = daysInMonth(year, month);
		TimeIndex t = new Day(year, month, 1);
		DayOfWeek firstOfPeriod = t.getDayOfWeek();
		int workRank = rank;
		if (workRank < 0)
			workRank = max; // try the max 
		int week1Offset = name.ordinal() - firstOfPeriod.ordinal();
		if (week1Offset < 0)
			week1Offset += 7;
		int dayOfPeriod = 1 + week1Offset + (workRank - 1) * 7;
		if (rank < 0) {
			if (dayOfPeriod > daysInPeriod)
				dayOfPeriod -= 7; // tried the max, was too much
			dayOfPeriod += (rank + 1) * 7;
			if (dayOfPeriod < 0)
				dayOfPeriod = 0;
		} else {
			if (dayOfPeriod > daysInPeriod)
				dayOfPeriod = 0;
		}
		return dayOfPeriod;
	}

	/**
	 * Return the TimeIndex for the day defined by name and rank within the
	 * month of the indicated date. If the rank is negative, days are counted
	 * from the end, with -1 meaning the last day. The result is a TimeIndex
	 * with DAY resolution. For example 
	 * <blockquote>
	 * <code>getDayOfMonthByRank(DayOfWeek.Mon, -1)</code> 
	 * </blockquote>
	 * returns the last Monday of the month. If the day does not
	 * exist, like for a 5th Monday of a month with only four Mondays, the
	 * method returns null.
	 * 
	 * @param time
	 *            the reference time
	 * @param dayName
	 *            the day of week
	 * @param rank
	 *            a non-zero number in the range [-5, 5]
	 * @return the day 
	 * @throws T2Exception
	 */
	public static Day getDayOfMonthByRank(TimeIndex time, DayOfWeek dayName, int rank) throws T2Exception {
		if (time.getTimeDomain().compareResolutionTo(Resolution.MONTH) > 0)
			throw T2Msg.exception(K.T1059, time.toString(), Resolution.MONTH.name());
		long y = time.getYear();
		int m = time.getMonth();
		int day = getDayByRank(y, m, dayName, rank);
		return day == 0 ? null : new Day(y, m, day);
	}

	/**
	 * Return the TimeIndex for the day defined by name and rank within the year
	 * of the indicated date. If the rank is negative, days are counted from the
	 * end, with -1 meaning the last day. The result is a TimeIndex with DAY
	 * resolution. For example <xmp> getDayOfYearByRank(DayOfWeek.Mon, -1)</xmp>
	 * returns the last Monday of the year. If the day does not exist, like for
	 * a 53d Monday of a year with only 52 Mondays, the method returns null.
	 * 
	 * @param time
	 *            the reference time
	 * @param dayName
	 *            the day of week
	 * @param rank
	 *            a non-zero number in the range [-53, 53]
	 * @return the day
	 * @throws T2Exception
	 */
	public static Day getDayOfYearByRank(TimeIndex time, DayOfWeek dayName, int rank) throws T2Exception {
		long y = time.getYear();
		int yearDay = getDayByRank(y, 0, dayName, rank);
		if (yearDay == 0)
			return null;
		else {
			int[] md = computeMonthAndDay(y, yearDay);
			return new Day(y, md[0], md[1]);
		}
	}

}
