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
 * Package: ch.agent.t2.time.engine
 * Type: TimeTools
 * Version: 1.0.0
 */
package ch.agent.t2.time.engine;

import java.util.Arrays;

import ch.agent.core.KeyedException;
import ch.agent.t2.T2Msg;
import ch.agent.t2.time.Day;
import ch.agent.t2.time.DayOfWeek;
import ch.agent.t2.time.TimeIndex;
import ch.agent.t2.time.TimeParts;
import ch.agent.t2.time.Resolution;

/**
 * TimeTools provides a selection of methods useful in time computations. 
 * These methods do not need any state.
 * 
 * @author Jean-Paul Vetterli
 * @version 1.0.0
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
	 * @param totalSeconds a non-negative number
	 * @param tp a non-null time parts object
	 */
	public static void computeHMS(long totalSeconds, TimeParts tp) {
		if (totalSeconds < 0)
			throw new IllegalArgumentException("totalSeconds negative");
		// avoid modulo arithmetic
		long minutes = totalSeconds / 60;
		tp.setSec((int) (totalSeconds - minutes * 60));
		tp.setHour((int) (minutes / 60));
		tp.setMin((int)(minutes - tp.getHour() * 60));
	}
	
	/**
	 * Extract years, month of year and day of month from total days. 
	 * This method must deal
	 * with large inputs, corresponding to ridiculously large years. TimeIndex
	 * inputs are not validated at object creation time. This is for performance
	 * reasons: many TimeIndex objects to create, few of them to format. So we
	 * must deal with the issue here. To avoid the consequence of losing
	 * precision when converting large longs to double, we avoid using doubles
	 * altogether.
	 * 
	 * @param days a non-negative number
	 * @param tp
	 *            time parts to fill in
	 */
	public static void computeYMD(long days, TimeParts tp) {
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
		
		tp.setYear(y400Intervals * 400 + remainingYears);
		int[]monthDay = TimeTools.computeMonthAndDay(tp.getYear(), dayOffset + 1);
		tp.setMonth(monthDay[0]);
		tp.setDay(monthDay[1]);
	}

	/**
	 * Return the numeric representation of the time specified.
	 * 
	 * @param unit a non-null resolution
	 * @param tp a non-null time parts object
	 * @return a numeric time index
	 * @throws KeyedException
	 */
	public static long makeRawIndex(Resolution unit, TimeParts tp) throws KeyedException {
		long time = 0;
		if (tp.getYear() < 0)
			throw T2Msg.exception(32010, tp.getYear());
		if (unit == Resolution.YEAR) {
			time = tp.getYear();
		} else {
			if (tp.getMonth() < 1 || tp.getMonth() > 12)
				throw T2Msg.exception(32020, tp.getMonth());
			if (unit == Resolution.MONTH) {
				time = tp.getYear() * 12 + tp.getMonth() - 1; // -1: zero-based months
			} else {
				int daysInThisMonth = TimeTools.daysInMonth(tp.getYear(), tp.getMonth());
				if (tp.getDay() < 1 || tp.getDay() > daysInThisMonth)
					throw T2Msg.exception(32030, tp.getDay(),	daysInThisMonth);
				time = tp.getYear() * 365 + TimeTools.leapYears(tp.getYear())
						+ TimeTools.daysToMonth(tp.getYear(), tp.getMonth()) + tp.getDay() - 1; // -1: zero
				if (unit != Resolution.DAY) {
					if (tp.getHour() < 0 || tp.getHour() > 23)
						throw T2Msg.exception(32040, tp.getHour());
					time = time * 24 + tp.getHour();
					if (unit != Resolution.HOUR) {
						if (tp.getMin() < 0 || tp.getMin() > 59)
							throw T2Msg.exception(32050, tp.getMin());
						time = time * 60 + tp.getMin();
						if (unit != Resolution.MIN) {
							if (tp.getSec() < 0 || tp.getSec() > 59)
								throw T2Msg.exception(32060, tp.getSec());
							time = time * 60 + tp.getSec();
							if (unit != Resolution.SEC) {
								if (tp.getUsec() < 0 || tp.getUsec() > 999999)
									throw T2Msg.exception(32070, tp.getUsec());
								if (unit == Resolution.MSEC)
									time = time * 1000L + tp.getUsec() / 1000;
								else if (unit == Resolution.USEC)
									time = time * 1000000L + tp.getUsec();
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
	
	/**
	 * Return the day index computed from the uncompressed numerical time.
	 * 
	 * @param unit a non- null resolution
	 * @param time a non-negative numerical time index
	 * @return a number of days
	 * @throws KeyedException
	 */
	public static long dayIndex(Resolution unit, long time) throws KeyedException {
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
			throw T2Msg.exception(32140, unit);
		}
		return dayIndex;
	}
	
	/**
	 * Return the day of week for the given unit and uncompressed time index.
	 * The computation assumes that January 1 of year zero falls on a Saturday. 
	 * 
	 * @param time an uncompressed numerical time
	 * @return a {@link DayOfWeek}
	 * @throws KeyedException
	 */
	public static DayOfWeek getDayOfWeek(Resolution unit, long time) throws KeyedException {
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
	 * @throws KeyedException
	 */
	public static int getDayByRank(long year, int month, DayOfWeek name, int rank) throws KeyedException {
		int max = 5;
		if (month == 0)
			max = 53;
		if (rank == 0 || rank < -max || rank > max)
			throw T2Msg.exception(32123, rank, -max, max);
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
	 * @throws KeyedException
	 */
	public static Day getDayOfMonthByRank(TimeIndex time, DayOfWeek dayName, int rank) throws KeyedException {
		if (time.getTimeDomain().compareResolutionTo(Resolution.MONTH) > 0)
			throw T2Msg.exception(32124, time.toString(), Resolution.MONTH.name());
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
	 * @throws KeyedException
	 */
	public static Day getDayOfYearByRank(TimeIndex time, DayOfWeek dayName, int rank) throws KeyedException {
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
