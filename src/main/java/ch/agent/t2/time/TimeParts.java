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

import ch.agent.t2.T2Exception;
import ch.agent.t2.T2Msg;
import ch.agent.t2.T2Msg.K;
import ch.agent.t2.time.engine.TimeTools;

/**
 * A TimeParts object groups the components of date and time, from the year down
 * to the microsecond. There is no guarantee that components are in a consistent
 * state at all times. It is for example possible to have hour 55 or month -378. The
 * class is intended for use by methods of the library for moving information
 * around. It is not meant to be used directly by applications.
 * <p>
 * Together with {@link DefaultTimeScanner}, TimeParts implements a subset of
 * ISO 8601, the international standard for representing dates and times. Refer
 * to {@link DefaultTimeScanner} for details.
 * 
 * @author Jean-Paul Vetterli
 */
public class TimeParts {
	
	/**
	 * Immutable object encapsulating years, months and days.
	 */
	public static class YMD {

		private final long y;
		private final int m;
		private final int d;

		/**
		 * Constructor. There is no validation, all values accepted. Yes, years are
		 * represented as long.
		 * 
		 * @param y
		 *            years
		 * @param m
		 *            months
		 * @param d
		 *            days
		 */
		public YMD(long y, int m, int d) {
			super();
			this.y = y;
			this.m = m;
			this.d = d;
		}

		/**
		 * @return years
		 */
		public long y() {
			return y;
		}

		/**
		 * @return months
		 */
		public int m() {
			return m;
		}

		/**
		 * @return days
		 */
		public int d() {
			return d;
		}

		@Override
		public String toString() {
			return String.format("%d %d %d", y, m, d);
		}
	}

	/**
	 * Immutable object encapsulating hours, minutes, seconds and sub-second units.
	 *
	 */
	public static class HMSU {

		private final int h;
		private final int m;
		private final int s;
		private final int u;

		/**
		 * Constructor.
		 * There is no validation, all values accepted.
		 * 
		 * @param h hours
		 * @param m minutes
		 * @param s seconds 
		 * @param u sub-second units
		 */
		public HMSU(int h, int m, int s, int u) {
			super();
			this.h = h;
			this.m = m;
			this.s = s;
			this.u = u;
		}

		/**
		 * @return hours
		 */
		public int h() {
			return h;
		}

		/**
		 * @return minutes
		 */
		public int m() {
			return m;
		}

		/**
		 * @return seconds
		 */
		public int s() {
			return s;
		}

		/**
		 * @return sub-second units
		 */
		public int u() {
			return u;
		}

		@Override
		public String toString() {
			return String.format("%d %d %d %d", h, m, s, u);
		}

	}
	
	
	/**
	 * An immutable object representing a time zone offset. A time zone offset is
	 * created by {@link TimeScanner#scan(String)} and passed to a {@link TimeParts}
	 * to be processed by {@link TimeTools#makeRawIndex}.
	 * <p>
	 * In a time zone offset, components are either all positive or all negative.
	 * The sign is determined by a parameter passed to the constructor. All
	 * components are set using positive values.
	 * <p>
	 * With a positive offset, the components will be subtracted from the given
	 * time, and with a negative offset they will be added to it. For example in the
	 * time representation <code>2001-06-15T02:00+05:00</code>, there is a positive
	 * offset of five hours and the method {@link TimeZoneOffset#getHour()} returns
	 * 5. Applying the offset results in the time <code>2001-06-14T21:00</code>.
	 */
	public static class TimeZoneOffset {
		private final int hour;
		private final int min;
		private final int sec;
		private final int usec;
		private final int sign;
		
		/**
		 * Constructor.
		 * 
		 * @param negative negative time zone offset
		 * @param hour offset hours
		 * @param min offset minutes
		 * @param sec offset seconds
		 * @param usec offset microseconds
		 * @throws T2Exception
		 */
		public TimeZoneOffset(boolean negative, int hour, int min, int sec, int usec) throws T2Exception {
			this.sign = negative ? -1 : 1;
			if (hour < 0 || hour > 11)
				throw T2Msg.exception(K.T1018, hour);
			this.hour = sign * hour;
			if (min < 0 || min > 59)
				throw T2Msg.exception(K.T1021, min);
			this.min = sign * min;
			if (sec < 0 || sec > 59)
				throw T2Msg.exception(K.T1023, sec);
			this.sec = sign * sec;
			if (usec < 0 || usec > 999999)
				throw T2Msg.exception(K.T1027, usec);
			this.usec = sign * usec;
		}

		/**
		 * Return true is offset is negative.
		 * 
		 * @return true if offset is negative
		 */
		public boolean isNegative() {
			return sign < 0;
		}
		
		/**
		 * Return the number to subtract from the hour.
		 * 
		 * @return positive or negative number to subtract from the hour
		 */
		public int getHour() {
			return hour;
		}

		/**
		 * Return the number to subtract from the minute. 
		 * 
		 * @return positive or negative number to subtract from the minute
		 */
		public int getMin() {
			return min;
		}

		/**
		 * Return the number to subtract from the second. 
		 * 
		 * @return positive or negative number to subtract from the second
		 */
		public int getSec() {
			return sec;
		}

		/**
		 * Return the number to subtract from the microsecond. 
		 * 
		 * @return positive or negative number to subtract from the microsecond
		 */
		public int getUsec() {
			return usec;
		}

		@Override
		public String toString() {
			return String.format("%d:%d:%d.%d", hour, min, sec, usec);
		}

	}
	
	private final long year;
	private final int month;
	private final int day;
	private final int hour;
	private final int min;
	private final int sec;
	private final int usec;
	private final TimeZoneOffset timeZoneOffset;
	
	/**
	 * Constructor. Any value can passed to parameters, no validation is done.
	 * 
	 * @param year
	 *            the year
	 * @param month
	 *            the month
	 * @param day
	 *            the day
	 * @param hour
	 *            the hour
	 * @param min
	 *            the minute
	 * @param sec
	 *            the second
	 * @param usec
	 *            the microsecond
	 * @param tzOffset
	 *            the time zone offset
	 */
	public TimeParts(long year, int month, int day, int hour, int min, int sec, int usec, TimeZoneOffset tzOffset) {
		super();
		this.year = year;
		this.month = month;
		this.day = day;
		this.hour = hour;
		this.min = min;
		this.sec = sec;
		this.usec = usec;
		this.timeZoneOffset = tzOffset;
	}

	/**
	 * Constructor with a default time zone offset. Any value can passed to
	 * parameters, no validation is done.
	 * 
	 * @param year
	 *            the year
	 * @param month
	 *            the month
	 * @param day
	 *            the day
	 * @param hour
	 *            the hour
	 * @param min
	 *            the minute
	 * @param sec
	 *            the second
	 * @param usec
	 *            the microsecond
	 */
	public TimeParts(long year, int month, int day, int hour, int min, int sec, int usec) {
		this(year, month, day, hour, min, sec, usec, null);
	}

	/**
	 * Return the year. A valid year is non-negative, but there is no guarantee
	 * that the value returned will be valid.
	 * 
	 * @return the year
	 */
	public long getYear() {
		return year;
	}
	
	/**
	 * Return the month. A valid month is in the range [1, 12], but there is no
	 * guarantee that the value returned will be valid.
	 * 
	 * @return the year
	 */
	public int getMonth() {
		return month;
	}


	/**
	 * Return the day. A valid day is in the range [1, n], with n the number of
	 * days in the month, but there is no guarantee that the value returned will
	 * be valid.
	 * 
	 * @return the year
	 */
	public int getDay() {
		return day;
	}

	/**
	 * Return the hour. A valid hour is in the range [0, 23], but there is no
	 * guarantee that the value returned will be valid.
	 * 
	 * @return the year
	 */
	public int getHour() {
		return hour;
	}

	/**
	 * Return the minute. A valid minute is in the range [0, 59], but there is
	 * no guarantee that the value returned will be valid.
	 * 
	 * @return the year
	 */
	public int getMin() {
		return min;
	}

	/**
	 * Return the second. A valid second is in the range [0, 59], but there is
	 * no guarantee that the value returned will be valid.
	 * 
	 * @return the year
	 */
	public int getSec() {
		return sec;
	}

	/**
	 * Return the microsecond. A valid microsecond is in the range [0, 999999],
	 * but there is no guarantee that the value returned will be valid.
	 * 
	 * @return the year
	 */
	public int getUsec() {
		return usec;
	}

	/**
	 * Return the time zone offset. The result can be null.
	 * 
	 * @return a time zone offset or null
	 */
	public TimeZoneOffset getTZOffset() {
		return timeZoneOffset;
	}
	
	@Override
	public String toString() {
		return String.format("%04d-%02d-%02d %02d:%02d:%02d.%06d", getYear(), getMonth(), getDay(), getHour(), getMin(), getSec(), getUsec());
	}

}
