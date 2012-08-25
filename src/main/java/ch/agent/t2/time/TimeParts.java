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
 * Package: ch.agent.t2.time
 * Type: TimeParts
 * Version: 1.0.2
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
 * Together with {@link DefaultExternalFormat}, TimeParts implements a subset of
 * ISO 8601, the international standard for representing dates and times. Refer
 * to {@link DefaultExternalFormat} for details.
 * 
 * @author Jean-Paul Vetterli
 * @version 1.0.2
 */
public class TimeParts {
	
	/**
	 * A TimeZoneOffset groups all components of a time zone offset. In a typical
	 * implementation, a TimeZoneOffset is created by
	 * {@link ExternalTimeFormat#scan(String)} and passed to a {@link TimeParts}
	 * to be processed by
	 * {@link TimeParts#checkTimeComponentsAndApplyTimeZoneOffset()
	 * checkTimeComponentsAndApplyTimeZoneOffset()}.
	 * <p>
	 * In a time zone offset, components are either all positive or all
	 * negative. The sign is determined by a parameter passed to the
	 * constructor. All components are set using positive values.
	 * <p>
	 * With a positive offset, the components will be subtracted from the given
	 * time, and with a negative offset they will be added to it.
	 * For example in the time representation <code>2001-06-15T02:00+05:00</code>, there is a positive
	 * offset of five hours and the method {@link TimeZoneOffset#getHour()} returns 5.
	 * Applying the offset results in the time <code>2001-06-14T21:00</code>.
	 */
	public class TimeZoneOffset {
		private int hour;
		private int min;
		private int sec;
		private int usec;
		private int sign;
		
		public TimeZoneOffset(boolean negative) {
			this.sign = negative ? -1 : 1;
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
		 * Set the hour offset.
		 * 
		 * @param hour a number in [0, 11]
		 * @throws T2Exception
		 */
		public void setHour(int hour) throws T2Exception {
			if (hour < 0 || hour > 11)
				throw T2Msg.exception(K.T1018, hour);
			this.hour = sign * hour;
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
		 * Set the minute offset.
		 * 
		 * @param min a number in [0, 59]
		 * @throws T2Exception
		 */
		public void setMin(int min) throws T2Exception {
			if (min < 0 || min > 59)
				throw T2Msg.exception(K.T1021, min);
			this.min = sign * min;
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
		 * Set the second offset.
		 * 
		 * @param sec a number in [0, 59]
		 * @throws T2Exception
		 */
		public void setSec(int sec) throws T2Exception {
			if (sec < 0 || sec > 59)
				throw T2Msg.exception(K.T1023, sec);
			this.sec = sign * sec;
		}

		/**
		 * Return the number to subtract from the microsecond. 
		 * 
		 * @return positive or negative number to subtract from the microsecond
		 */
		public int getUsec() {
			return usec;
		}

		/**
		 * Set the usec offset.
		 * 
		 * @param usec a number in [0, 999999]
		 * @throws T2Exception
		 */
		public void setUsec(int usec) throws T2Exception {
			if (usec < 0 || usec > 999999)
				throw T2Msg.exception(K.T1027, usec);
			this.usec = sign * usec;
		}
	}
	
	private long year;
	private int month = 1;
	private int day = 1;
	private int hour;
	private int min;
	private int sec;
	private int usec;
	private TimeZoneOffset timeZoneOffset;
	
	/**
	 * Construct a TimeParts object with month and day initialized to 1 and all
	 * other components to 0.
	 */
	public TimeParts() {
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
	 * Set the year. Any value is accepted.
	 * 
	 * @param year any number
	 */
	public void setYear(long year) {
		this.year = year;
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
	 * Set the month. Any value is accepted.
	 * 
	 * @param month any number
	 */
	public void setMonth(int month) {
		this.month = month;
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
	 * Set the day. Any value is accepted.
	 * 
	 * @param day any number
	 */
	public void setDay(int day) {
		this.day = day;
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
	 * Set the hour. Any value is accepted.
	 * 
	 * @param hour any number
	 */
	public void setHour(int hour) {
		this.hour = hour;
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
	 * Set the minute. Any value is accepted.
	 * 
	 * @param min any number
	 */
	public void setMin(int min) {
		this.min = min;
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
	 * Set the second. Any value is accepted.
	 * 
	 * @param sec any number
	 */
	public void setSec(int sec) {
		this.sec = sec;
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
	 * Set the microsecond. Any value is accepted.
	 * 
	 * @param usec any number
	 */
	public void setUsec(int usec) {
		this.usec = usec;
	}

	/**
	 * Set a time zone offset.
	 * 
	 * @param timeZoneOffset
	 */
	public void setTimeZoneOffset(TimeZoneOffset timeZoneOffset) {
		this.timeZoneOffset = timeZoneOffset;
	}

	/**
	 * Apply the time zone offset to time components and resolve all overflows.
	 * Return 1 if there was an hour overflow, -1 if there was an underflow, and
	 * 0 in all other cases. Applying the time zone offset more than once has no
	 * effect. As a side effect, the method verifies the validity of all time
	 * components. Leap seconds and 24 hour notation for midnight are not
	 * supported.
	 * 
	 * @return 1 if hours overflowed, -1 if they underflowed, else return 0
	 * @throws T2Exception
	 */
	protected int checkTimeComponentsAndApplyTimeZoneOffset() throws T2Exception {
		
		if (hour < 0 || hour > 23)
			throw T2Msg.exception(K.T1017, hour);
		if (min < 0 || min > 59)
			throw T2Msg.exception(K.T1019, min);
		if (sec < 0 || sec > 59)
			throw T2Msg.exception(K.T1022, sec);
		if (usec < 0 || usec > 999999)
			throw T2Msg.exception(K.T1026, usec);
		
		int overflow = 0;
		
		if (timeZoneOffset != null) {
			if (timeZoneOffset.isNegative()) {
				usec -= timeZoneOffset.usec;
				if (usec > 999999) {
					usec -= 1000000;
					sec += 1;
				}
				sec -= timeZoneOffset.sec;
				if (sec > 59) {
					sec -= 60;
					min += 1;
				}
				min -= timeZoneOffset.min;
				if (min > 59) {
					min -= 60;
					hour += 1;
				}
				hour -= timeZoneOffset.hour;
				if (hour > 23) {
					hour -= 24;
					overflow = 1;
				}
			} else {
				usec -= timeZoneOffset.usec;
				if (usec < 0) {
					usec += 1000000;
					sec -= 1;
				}
				sec -= timeZoneOffset.sec;
				if (sec < 0) {
					sec += 60;
					min -= 1;
				}
				min -= timeZoneOffset.min;
				if (min < 0) {
					min += 60;
					hour -= 1;
				}
				hour -= timeZoneOffset.hour;
				if (hour < 0) {
					hour += 24;
					overflow = -1;
				}
			}
			timeZoneOffset = null;
		}
		
		return overflow;
	}

	/**
	 * Return a long number representing the time components. The method
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
	 * @return a numeric time index
	 * @throws T2Exception
	 */
	public long asRawIndex(Resolution unit) throws T2Exception {
		long time = 0;
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
					time += checkTimeComponentsAndApplyTimeZoneOffset();
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
	
	/**
	 * Return true if any part negative.
	 * @return true if any part negative
	 */
	public boolean anyNegative() {
		return getYear() < 0 || getMonth() < 0 || getDay() < 0 || getHour() < 0 || getMin() < 0 || getSec() < 0 || getUsec() < 0;
	}

	@Override
	public String toString() {
		return String.format("%04d-%02d-%02d %02d:%02d:%02d.%06d", getYear(), getMonth(), getDay(), getHour(), getMin(), getSec(), getUsec());
	}

}
