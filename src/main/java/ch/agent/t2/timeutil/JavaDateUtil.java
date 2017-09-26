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
package ch.agent.t2.timeutil;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import ch.agent.t2.T2Exception;
import ch.agent.t2.T2Msg;
import ch.agent.t2.T2Msg.K;
import ch.agent.t2.time.Adjustment;
import ch.agent.t2.time.Day;
import ch.agent.t2.time.SystemTime;
import ch.agent.t2.time.TimeDomain;
import ch.agent.t2.time.TimeIndex;

/**
 * JavaDateUtil converts between {@link TimeIndex} and Java {@link java.util.Date Date}.
 *
 * @author Jean-Paul Vetterli
 */
public class JavaDateUtil {
	
	private final static long gregorianCutover = new GregorianCalendar(new SimpleTimeZone(0, "No DST time zone")).getGregorianChange().getTime();
	
	private final static TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");
	
	/**
	 * Return the Java date corresponding to the given time index.
	 * 
	 * @param t a non-null time index
	 * @return a Java date
	 * @throws T2Exception
	 */
	public static Date toJavaDate(TimeIndex t) throws T2Exception {
		long index;
		switch (t.getTimeDomain().getResolution()) {
		case YEAR:
			index = yearOrMonthToMillisecSinceEpoch(t.getYear(), 1);
			break;
		case MONTH:
			index = yearOrMonthToMillisecSinceEpoch(t.getYear(), t.getMonth());
			break;
		default:
			index = dayOrLessToMillisecSinceEpoch(t);
		}
		if (index < gregorianCutover)
			throw T2Msg.exception(K.T7023, t.toString());
		return new Date(index);
	}

	/**
	 * Convert a Java Date to a TimeIndex in a given domain. No adjustment is
	 * allowed, and the conversion is done in the UTC time zone.
	 * 
	 * @param date a non-null Java date
	 * @param domain a non-null time domain
	 * @return a time index
	 * @throws T2Exception
	 */
	public static TimeIndex fromJavaDate(Date date, TimeDomain domain) throws T2Exception {
		return fromJavaDate(date, domain, Adjustment.NONE, true);
	}
	
	/**
	 * Convert a Java Date to a TimeIndex in a given domain, with tuning parameters.
	 * It is possible to
	 * specify which kind of adjustments are allowed, and whether to perform the
	 * conversion in the UTC time zone, or the default time zone (the current
	 * time zone and DST mode).
	 * 
	 * @param date a non-null Java date
	 * @param domain a non-null time domain
	 * @param adjustment a non-null adjustment
	 * @param UTC if true, perform the conversion in the UTC time zone, else in the local time zone
	 * @return a time index
	 * @throws T2Exception
	 */
	public static TimeIndex fromJavaDate(Date date, TimeDomain domain, Adjustment adjustment, boolean UTC) throws T2Exception {
		Calendar cal = null;
		if (UTC)
			cal = new GregorianCalendar(utcTimeZone);
		else
			cal = new GregorianCalendar();
		cal.setTimeInMillis(date.getTime());
		return domain.time(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH), 
				cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), 
				cal.get(Calendar.SECOND), 1000 * cal.get(Calendar.MILLISECOND), 
				adjustment);
	}
	
	// convert "difficult" calendars to Day and increase resolution to milliseconds
	private static long yearOrMonthToMillisecSinceEpoch (long year, int month) throws T2Exception {
		TimeIndex m = Day.DOMAIN.time(year, month, 1, 0, 0, 0, 0, Adjustment.NONE);
		long index = m.asLong() * 24L * 3600000L;
		return index - (TimeDomain.DAYS_TO_19700101 * 24L * 3600000L);
	}
	
	private static long dayOrLessToMillisecSinceEpoch (TimeIndex t) throws T2Exception {
		if (!(t instanceof SystemTime))
			t = new SystemTime(t.toString());
		return ((SystemTime) t).asFastJavaTime();
	}
	
}
