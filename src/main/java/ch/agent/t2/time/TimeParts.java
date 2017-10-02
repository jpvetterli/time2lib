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

import ch.agent.t2.T2Exception;
import ch.agent.t2.T2Msg;
import ch.agent.t2.T2Msg.K;

/**
 * A TimeParts object groups the components of date and time, from the year down
 * to the fraction of a second. There is no guarantee that components are in a
 * consistent state at all times. It is for example possible to have hour 55 or
 * month -378. The class is intended for use by methods of the library for
 * moving information around. It is not meant to be used directly by
 * applications.
 * <p>
 * Together with {@link DefaultTimeScanner}, TimeParts implements a subset of
 * ISO 8601, the international standard for representing dates and times. Refer
 * to {@link DefaultTimeScanner} for details.
 * 
 * @author Jean-Paul Vetterli
 */
public class TimeParts {
	
	// package private
	/**
	 * Validate fractional seconds depending on the resolution.
	 * 
	 * @param unit time resolution 
	 * @param fsec fractional seconds
	 * @return true if valid
	 */
	static boolean good(Resolution unit, int fsec) {
		boolean good = false;
		if (fsec >= 0) {
			switch (unit) {
			case MSEC:
				good = fsec < 1000;
				break;
			case USEC:
				good = fsec < 1000000;
				break;
			case NSEC:
				good = fsec < 1000000000;
				break;
			default:
				good = fsec == 0;
				break;
			}
		}
		return good;
	}
	
	/**
	 * Immutable object encapsulating years, months and days. This object is
	 * expected to be used in implementations but not applications.
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
			return String.format("%04d-%02d-%02d", y, m, d);
		}
	}

	/**
	 * Immutable object encapsulating hours, minutes, seconds and fractional
	 * seconds. This object is expected to be used in implementations but not
	 * applications.
	 */
	public static class HMSF {

		private final int h;
		private final int m;
		private final int s;
		private final int f;

		/**
		 * Constructor.
		 * There is no validation, all values accepted.
		 * 
		 * @param h hours
		 * @param m minutes
		 * @param s seconds 
		 * @param f fractional second
		 */
		public HMSF(int h, int m, int s, int f) {
			super();
			this.h = h;
			this.m = m;
			this.s = s;
			this.f = f;
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
		 * @return fractional second
		 */
		public int f() {
			return f;
		}

		@Override
		public String toString() {
			return f == 0 ? String.format("%02d:%02d:%02d", h, m, s) : String.format("%02d:%02d:%02d.%d", h, m, s, f);
		}

	}
	
	
	/**
	 * An immutable object representing a time zone offset. A time zone offset is
	 * created by {@link TimeScanner#scan} and passed to a {@link TimeParts}
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
		private final int fsec;
		private final int sign;
		
		/**
		 * Constructor. Values insignificant for the resolution are discarded.
		 * 
		 * @param unit
		 *            time unit resolution
		 * @param negative
		 *            negative time zone offset
		 * @param hour
		 *            offset hours
		 * @param min
		 *            offset minutes
		 * @param sec
		 *            offset seconds
		 * @param fsec
		 *            offset fractional seconds (millis, micros, etc. depending on the
		 *            resolution)
		 * @throws T2Exception
		 */
		public TimeZoneOffset(Resolution unit, boolean negative, int hour, int min, int sec, int fsec) throws T2Exception {
			if (unit == null)
				throw new IllegalArgumentException("unit null");
			if (unit.compareTo(Resolution.MSEC) < 0) {
				fsec = 0;
				if (unit.compareTo(Resolution.SEC) < 0) {
					sec = 0;
					if (unit.compareTo(Resolution.MIN) < 0) {
						min = 0;
						if (unit.compareTo(Resolution.HOUR) < 0) {
							hour = 0;
						}
					}
				}
			}
			
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
			if (!good(unit, fsec))
				throw T2Msg.exception(K.T1027, fsec);
			this.fsec = sign * fsec;
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
		 * Return the number to subtract from the fractional second. 
		 * 
		 * @return positive or negative number to subtract from the fractional second
		 */
		public int getFsec() {
			return fsec;
		}

		@Override
		public String toString() {
			return String.format("%d:%d:%d.%d", hour, min, sec, fsec);
		}

	}
	
	private final Resolution unit;
	private final long year;
	private final int month;
	private final int day;
	private final int hour;
	private final int min;
	private final int sec;
	private final int fsec;
	private final TimeZoneOffset timeZoneOffset;
	
	/**
	 * Constructor. Any value can passed to parameters, no validation is done.
	 * Values insignificant for the resolution are discarded. The time zone offset
	 * is discarded unless the resolution is at least hourly.
	 * 
	 * @param unit
	 *            a non-null time resolution
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
	 * @param fsec
	 *            the fractional second (millis, micros, etc. depending on the resolution)
	 * @param tzOffset
	 *            the time zone offset
	 */
	public TimeParts(Resolution unit, long year, int month, int day, int hour, int min, int sec, int fsec, TimeZoneOffset tzOffset) {
		super();
		if (unit == null)
			throw new IllegalArgumentException("unit null");
		if (unit.compareTo(Resolution.MSEC) < 0) {
			fsec = 0;
			if (unit.compareTo(Resolution.SEC) < 0) {
				sec = 0;
				if (unit.compareTo(Resolution.MIN) < 0) {
					min = 0;
					if (unit.compareTo(Resolution.HOUR) < 0) {
						hour = 0;
						tzOffset = null;
						if (unit.compareTo(Resolution.DAY) < 0) {
							day = 1;
							if (unit.compareTo(Resolution.MONTH) < 0) {
								month = 1;
							}
						}
					}
				}
			}
		}
		this.unit = unit;
		this.year = year;
		this.month = month;
		this.day = day;
		this.hour = hour;
		this.min = min;
		this.sec = sec;
		this.fsec = fsec;
		this.timeZoneOffset = tzOffset;
	}

	/**
	 * Constructor with a default time zone offset. Any value can passed to
	 * parameters, no validation is done. Values insignificant for the resolution
	 * are discarded.
	 * 
	 * @param unit
	 *            the time resolution
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
	 * @param fsec
	 *            the fractional second
	 */
	public TimeParts(Resolution unit, long year, int month, int day, int hour, int min, int sec, int fsec) {
		this(unit, year, month, day, hour, min, sec, fsec, null);
	}

	/**
	 * Return the time resolution.
	 * 
	 * @return the resolution
	 */
	public Resolution getResolution() {
		return unit;
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
	 * @return the month
	 */
	public int getMonth() {
		return month;
	}


	/**
	 * Return the day. A valid day is in the range [1, n], with n the number of
	 * days in the month, but there is no guarantee that the value returned will
	 * be valid.
	 * 
	 * @return the day
	 */
	public int getDay() {
		return day;
	}

	/**
	 * Return the hour. A valid hour is in the range [0, 23], but there is no
	 * guarantee that the value returned will be valid.
	 * 
	 * @return the hour
	 */
	public int getHour() {
		return hour;
	}

	/**
	 * Return the minute. A valid minute is in the range [0, 59], but there is
	 * no guarantee that the value returned will be valid.
	 * 
	 * @return the minute
	 */
	public int getMin() {
		return min;
	}

	/**
	 * Return the second. A valid second is in the range [0, 59], but there is
	 * no guarantee that the value returned will be valid.
	 * 
	 * @return the second
	 */
	public int getSec() {
		return sec;
	}

	/**
	 * Return the fractional second. The value must be interpreted in the context of the resolution.
	 * 
	 * @return the fractional second
	 */
	public int getFsec() {
		return fsec;
	}
	
	/**
	 * Return the fractional second with a given resolution. The method returns zero
	 * when the target resolution is less than millisecond, else it converts the
	 * fractional second. As an example assume fsec is 123456 microseconds. For a
	 * 
	 * 
	 * @param targetUnit
	 *            the resolution to convert to
	 * @return milliseconds, microseconds, or nanoseconds
	 */
	public int getFsec(Resolution targetUnit) {
		int result = fsec;
		switch (unit) {
		case MSEC:
			switch (targetUnit) {
			case MSEC:
				break;
			case USEC:
				result = fsec * 1000;
				break;
			case NSEC:
				result = fsec * 1000 * 1000;
				break;
			default:
				result = 0;
				break;
			}
			break;
		case USEC:
			switch (targetUnit) {
			case MSEC:
				result = fsec / 1000;
				break;
			case USEC:
				break;
			case NSEC:
				result = fsec * 1000;
				break;
			default:
				result = 0;
				break;
			}
			break;
		case NSEC:
			switch (targetUnit) {
			case MSEC:
				result = fsec / 1000000;
				break;
			case USEC:
				result = fsec / 1000;
				break;
			case NSEC:
				break;
			default:
				result = 0;
				break;
			}
			break;
		default:
			result = 0;
			break;
		}
		return result;
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
		return String.format("%04d-%02d-%02d %02d:%02d:%02d.%06d",
				(unit == Resolution.NSEC ? TimeDomain.BASE_YEAR_FOR_NANO : 0) + getYear(), 
				getMonth(), getDay(), getHour(), getMin(), getSec(), getFsec());
	}

}
