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
package ch.agent.t2.time.engine;

import java.util.Formatter;

import ch.agent.t2.T2Exception;
import ch.agent.t2.T2Msg;
import ch.agent.t2.T2Msg.K;
import ch.agent.t2.time.Adjustment;
import ch.agent.t2.time.Day;
import ch.agent.t2.time.DayOfWeek;
import ch.agent.t2.time.DefaultTimeScanner;
import ch.agent.t2.time.Resolution;
import ch.agent.t2.time.ThirdFriday;
import ch.agent.t2.time.TimeDomain;
import ch.agent.t2.time.TimeDomainDefinition;
import ch.agent.t2.time.TimeFormatter;
import ch.agent.t2.time.TimeIndex;
import ch.agent.t2.time.TimeParts;
import ch.agent.t2.time.Workday;
import ch.agent.t2.time.Year;


/**
 * Time2 implements the behavior of {@link TimeIndex} as an immutable object. It
 * requires the implementation of the {@link TimeDomain} object passed to
 * constructors to be a {@link TimeFactory} object.
 * <p>
 * The design goals of <em>Time2</em> are flexibility and performance. It is not
 * a replacement for {@link java.util.Date java.util.Date} and is not a
 * competitor to <a href="http://joda-time.sourceforge.net/">Joda Time</a>.
 * <em>Time2</em> has no time zones, no daylight savings, no locales, no
 * Gregorian cutover. Dates before October 15 1582 do not correspond to
 * historical dates. The base of <em>Time2</em> time is zero microseconds into
 * January 1st of year zero:
 * <p>
 * <blockquote> <code>0000-01-01 00:00:00.000000</code> </blockquote>
 * <p>
 * It corresponds to the numerical time index 0L.
 * <p>
 * <b>Warning note about time comparisons</b>
 * <p>
 * This class implements {@link Comparable} with simple semantics. Basically, it
 * considers "earlier" as "smaller". When time domain differs, times are
 * converted before performing the comparison. The conversion is done in the
 * following fashion:
 * <ul>
 * <li>If resolutions differ, the time with the lowest resolution is converted
 * to the one with the highest resolution. For example comparing a {@link Day}
 * to a {@link Year} will compare the day specified to the implicit default day
 * of the year specified (which is January 1).
 * <li>If resolutions do not differ, the times are converted to the unrestricted
 * time domain for their resolution. For example the comparison of a
 * {@link Workday} to a {@link ThirdFriday} will convert both to a {@link Day}
 * before comparing.
 * </ul>
 * These semantics are not meaningful for all possible time domains.
 * Applications using "exotic" time domains should consider writing a subclass
 * of Time2 and override the {@link Comparable#compareTo(Object) compareTo}
 * method.
 * <p>
 * 
 * @author Jean-Paul Vetterli
 */
public class Time2 implements TimeIndex {

	private int hash; // Time2 is immutable, so hash needs to be computed only once
	
	private TimeFactory domain;

	private final long time;

	private TimeParts timeParts;
	
	private static TimeFactory asTimeFactory(TimeDomain domain) {
		if (domain == null)
			throw new IllegalArgumentException("domain null");
		try {
			return (TimeFactory) domain;
		} catch (ClassCastException e) {
			throw new IllegalArgumentException("domain " + domain.getLabel() + " is not a " +  TimeFactory.class.getSimpleName());
		}
	}
	
	/**
	 * Construct a time index with the given time domain and numerical time index.
	 * An <b>unchecked</b> exception is thrown if the concrete time domain
	 * is not a {@link TimeFactory} or if the numerical time index is
	 * out of range. The range depends on the domain but is very large. 
	 * 
	 * @param domain a non-null {@link TimeFactory}
	 * @param time a valid numerical time index
	 */
	public Time2(TimeDomain domain, long time) {
		this.domain = asTimeFactory(domain);
		try {
			this.domain.valid(time, false);
		} catch (T2Exception e) {
			throw new IllegalArgumentException("time", e);
		}
		this.time = time;
	}
	
	/**
	 * Construct a time index with the given time domain and parameters. An
	 * <b>unchecked</b> exception is thrown if the concrete time domain is not a
	 * {@link TimeFactory}. If necessary the time is adjusted as allowed by the last
	 * argument.
	 * 
	 * @param domain
	 *            a non-null {@link TimeFactory}
	 * @param year
	 *            a year, which can be unusually large, depending on the domain
	 * @param month
	 *            a number between 1 and 12
	 * @param day
	 *            the day in the month, starting with 1
	 * @param hour
	 *            an hour in the range 0-23
	 * @param min
	 *            a minute in the range 0-59
	 * @param sec
	 *            a second in the range 0-59
	 * @param usec
	 *            the number of microseconds in the current second in the range
	 *            0-999999
	 * @param adjust
	 *            a non-null allowed adjustment mode
	 * @throws T2Exception
	 */
	public Time2(TimeDomain domain, long year, int month, int day, int hour, int min, int sec,			int usec, Adjustment adjust) throws T2Exception {
		this(domain, asTimeFactory(domain).pack(new TimeParts(year, month, day, hour, min, sec, usec), adjust));
	}
	
	/**
	 * Constructor a time index from a time parts object with an adjustment
	 * parameter.
	 * 
	 * @param domain
	 *            the time domain
	 * @param timeParts
	 *            the time parts
	 * @param adjust
	 *            an adjustment
	 * @throws T2Exception
	 *             on failure
	 */
	public Time2(TimeDomain domain, TimeParts timeParts, Adjustment adjust) throws T2Exception {
		this(domain, asTimeFactory(domain).pack(timeParts, adjust));
	}
	
	/**
	 * Construct a time index with the given time domain and string, with possible
	 * adjustment. An <b>unchecked</b> exception is thrown if the concrete time
	 * domain is not a {@link TimeFactory}. The string is interpreted by the timer
	 * scanner defined in the time factory. By default it is
	 * {@link DefaultTimeScanner}.
	 * <p>
	 * If necessary the time is adjusted as allowed by the last argument.
	 * 
	 * @param domain
	 *            a non-null {@link TimeFactory}
	 * @param time
	 *            a string containing a representation of a date and time
	 * @param adjustment
	 *            a non-null allowed adjustment mode
	 * @throws T2Exception
	 */
	public Time2(TimeDomain domain, String time, Adjustment adjustment) throws T2Exception {
		this(domain, asTimeFactory(domain).scan(time), adjustment);
	}
	
	/**
	 * Construct a time index with the given time domain and string. An
	 * <b>unchecked</b> exception is thrown if the concrete time domain is not a
	 * {@link TimeFactory}. The string is interpreted by the timer scanner defined
	 * in the time factory. By default it is {@link DefaultTimeScanner}. *
	 * <p>
	 * No adjustment is allowed.
	 * 
	 * @param domain
	 *            a non-null {@link TimeFactory}
	 * @param time
	 *            a string containing a representation of a date and time
	 * @throws T2Exception
	 */
	public Time2(TimeDomain domain, String time) throws T2Exception {
		this(domain, time, Adjustment.NONE);
	}
	
	@Override
	public TimeIndex convert(TimeDomain domain) throws T2Exception {
		return convert(domain, Adjustment.NONE);
	}
	
	@Override
	public TimeIndex convert(TimeDomain domain, Adjustment adjustment) throws T2Exception {
		if (getTimeDomain().equals(domain))
			return this;
		return new Time2(domain, getTP(), adjustment);
	}

	@Override
	public TimeDomain getTimeDomain() {
		return domain;
	}

	@Override
	public long asLong() {
		return getInternalTime();
	}
	
	@Override
	public int asOffset() throws T2Exception {
		long time = getInternalTime() - domain.getOrigin();
		if (time < Integer.MIN_VALUE || time > Integer.MAX_VALUE)
			throw T2Msg.exception(K.T1076, domain.getResolution(), time);
		return (int) time;
	}
	
	@Override
	public TimeIndex add(long increment) throws T2Exception {
		long before = asLong();
		long after = before + increment;
		// overflow? (detected with numeric wraparound)
		if (increment > 0 && after < before || increment < 0 && after > before)
			throw T2Msg.exception(K.T1075, toString(), increment);
		try {
			domain.valid(after, false);
		} catch (T2Exception e) {
			throw T2Msg.exception(e, K.T1075, toString(), increment);
		}
		return new Time2(domain, after);
	}
	
	@Override
	public long sub(TimeIndex time) throws T2Exception {
		if (getTimeDomain().equals(time.getTimeDomain())) {
			return asLong() - time.asLong();
		} else
			throw T2Msg.exception(K.T1077, time.toString(), toString(), 
					time.getTimeDomain().getLabel(), getTimeDomain().getLabel());
	}
	
	/**
	 * Return the time as a time parts object.
	 * 
	 * @return the time as a time parts object
	 */
	TimeParts getTimeParts() {
		// package private
		return getTP();
	}

	@Override
	public long getYear() {
		return getTP().getYear();
	}

	@Override
	public int getMonth() {
		return getTP().getMonth();
	}

	@Override
	public int getDay() {
		return getTP().getDay();
	}

	@Override
	public int getHour() {
		return getTP().getHour();
	}

	@Override
	public int getMinute() {
		return getTP().getMin();
	}

	@Override
	public int getSecond() {
		return getTP().getSec();
	}

	@Override
	public int getMicrosecond() {
		return getTP().getUsec();
	}

	@Override
	public DayOfWeek getDayOfWeek() throws T2Exception {
		return domain.getDayOfWeek(this);
	}

	@Override
	public TimeIndex getDayByRank(Resolution basePeriod, DayOfWeek day, int rank) throws T2Exception {
		switch(basePeriod) {
		case MONTH:
			return TimeTools.getDayOfMonthByRank(this, day, rank);
		case YEAR:
			return TimeTools.getDayOfYearByRank(this, day, rank);
		default:
			throw T2Msg.exception(K.T1052, basePeriod.name());
		}
	}
	
	@Override
	public String toString(TimeFormatter formatter) {
		return formatter == null ? domain.format(domain.getResolution(), getTP())
				: formatter.format(domain.getResolution(), getTP());
	}
	
	@Override
	public String toString(String format) {
		if (format == null)
			return toString();
		TimeParts tp = getTP();
		if (format.length() == 0) {
			// TODO: should be localized (e.g. m/d yy)
			String yy = tp.getYear() + "";
			if (yy.length() >= 3)
				yy = yy.substring(2);
			return String.format("%d.%d.%s", tp.getDay(), tp.getMonth(), yy);
		}
		StringBuilder sb = new StringBuilder();
		Formatter fmt = new Formatter(sb);
		fmt.format(format, tp.getYear(), tp.getMonth(), tp.getDay(), tp.getHour(), tp.getMin(), tp.getSec(), tp.getUsec());
		fmt.close();
		return sb.toString();
	}

	@Override
	public String toString() {
		return toString((TimeFormatter) null);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Time2))
			return false;
		return (asLong() == ((Time2)obj).asLong()) && domain.equals(((Time2)obj).getTimeDomain());
	}

	@Override
	public int hashCode() {
		if (hash == 0)
			hash = 31 * domain.hashCode() + (new Long(asLong())).hashCode();
		return hash;
	}

	@Override
	public int compareTo(TimeIndex otherTime) {
		if (otherTime == null)
			throw new IllegalArgumentException("t null");
		if (otherTime.getTimeDomain().equals(getTimeDomain())) {
			long l1 = asLong();
			long l2 = otherTime.asLong();
			if (l1 < l2)
				return -1;
			else if (l1 > l2)
				return 1;
			else
				return 0;
		} else {
			int compareResols = getTimeDomain().getResolution().compareTo(otherTime.getTimeDomain().getResolution());
			if (compareResols == 0) {
				// convert  both to unrestricted domain
				TimeDomainDefinition def = new TimeDomainDefinition(null, getTimeDomain().getResolution(), 0L);
				TimeDomain unrestrictedDomain = new TimeFactory(def);
				TimeIndex converted = convertOrThrowRTE(unrestrictedDomain, this);
				TimeIndex otherConverted = convertOrThrowRTE(unrestrictedDomain, otherTime);
				return converted.compareTo(otherConverted);
			} else if (compareResols < 0) {
				// convert  both to highest resolution
				TimeDomainDefinition def = new TimeDomainDefinition(null, otherTime.getTimeDomain().getResolution(), 0L);
				TimeDomain unrestrictedDomain = new TimeFactory(def);
				TimeIndex converted = convertOrThrowRTE(unrestrictedDomain, this);
				return converted.compareTo(otherTime);
			} else {
				// convert  both to highest resolution
				TimeDomainDefinition def = new TimeDomainDefinition(null, getTimeDomain().getResolution(), 0L);
				TimeDomain unrestrictedDomain = new TimeFactory(def);
				TimeIndex otherConverted = convertOrThrowRTE(unrestrictedDomain, otherTime);
				return compareTo(otherConverted);
			}
		}
	}
	
	/**
	 * Convert the time to the given time domain and turn any checked exception
	 * to an unchecked one.
	 * 
	 * @param domain
	 *            non-null time domain
	 * @param time
	 *            non-null time index
	 * @return the time converted to the time domain
	 */
	private TimeIndex convertOrThrowRTE(TimeDomain domain, TimeIndex time) {
		try {
			return time.convert(domain);
		} catch (T2Exception e) {
			Exception cause = T2Msg.exception(e, K.T0005, time.toString(), domain); 
			throw T2Msg.runtimeException(K.T0001, cause); 
		}
	}
	
	/**
	 * Return the numerical time index.
	 * 
	 * @return the numerical time index
	 */
	private long getInternalTime() {
		return time;
	}
	
	private TimeParts getTP() {
		if (timeParts == null) {
			// lazy (and expensive), only necessary when formatting or converting the time
			timeParts = domain.unpack(getInternalTime());
		}
		return timeParts;
	}

}
