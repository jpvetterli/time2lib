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
 * Package: ch.agent.t2.time.engine
 * Type: Time2
 * Version: 1.0.3
 */
package ch.agent.t2.time.engine;

import java.util.Formatter;

import ch.agent.t2.T2Exception;
import ch.agent.t2.T2Msg;
import ch.agent.t2.T2Msg.K;
import ch.agent.t2.time.Adjustment;
import ch.agent.t2.time.Day;
import ch.agent.t2.time.DayOfWeek;
import ch.agent.t2.time.DefaultExternalFormat;
import ch.agent.t2.time.ExternalTimeFormat;
import ch.agent.t2.time.Resolution;
import ch.agent.t2.time.ThirdFriday;
import ch.agent.t2.time.TimeDomain;
import ch.agent.t2.time.TimeDomainDefinition;
import ch.agent.t2.time.TimeDomainManager;
import ch.agent.t2.time.TimeIndex;
import ch.agent.t2.time.TimeParts;
import ch.agent.t2.time.Workday;
import ch.agent.t2.time.Year;


/**
 * Time2 implements the behavior of {@link TimeIndex} as an immutable object.
 * The design goals of <em>Time2</em> are flexibility and performance.
 * It is not a replacement for {@link java.util.Date java.util.Date} and is not
 * a competitor to <a href="http://joda-time.sourceforge.net/">Joda Time</a>.
 * <em>Time2</em> has no time zones, no daylight savings, no
 * locales, no Gregorian cutover. Dates before October 15 1582 do
 * not correspond to historical dates. The base of <em>Time2</em> time is 
 * zero microseconds into January 1st of year zero:
 * <p>
 * <blockquote>
 * <code>0000-01-01 00:00:00.000000</code>
 * </blockquote>
 * <p>
 * It corresponds to the numerical time index 0L.
 * <p>
 * <b>Warning note about time comparisons</b>
 * <p>
 * This class implements {@link Comparable} with simple
 * semantics. Basically, it considers "earlier" as "smaller".
 * When time domain differs, times are converted before performing the
 * comparison. The conversion is done in the following fashion:
 * <ul>
 * <li>If resolutions differ, the time with the lowest resolution is
 * converted to the one with the highest resolution. For example comparing a
 * {@link Day} to a {@link Year} will compare the day specified to the 
 * implicit default day of the year specified (which is January 1).
 * <li>If resolutions do not differ, the times are converted to the unrestricted
 * time domain for their resolution. For example the
 * comparison of a {@link Workday} to a {@link ThirdFriday} will convert
 * both to a {@link Day} before comparing.
 * </ul>
 * These semantics are not meaningful for all possible time domains. 
 * Applications using "exotic" time domains should consider writing a subclass
 * of Time2 and override the {@link Comparable#compareTo(Object) compareTo} method.
 * <p>
 * @author Jean-Paul Vetterli
 * @version 1.0.3
 */
public class Time2 implements TimeIndex {

	private int hash; // Time2 is immutable, so hash needs to be computed only once
	
	private TimeFactory domain;

	private long internalTime;

	private boolean internalTimeModified;

	private TimeParts tp;
	
	/**
	 * The parameterless constructor is not used.
	 */
	@SuppressWarnings("unused")
	private Time2() {
	}
	
	/**
	 * Construct a time index with the given time domain.
	 * An <b>unchecked</b> exception is thrown if the concrete time domain
	 * is not a {@link TimeFactory}.
	 * 
	 * @param domain a non-null {@link TimeFactory}
	 */
	private Time2(TimeDomain domain) {
		if (domain == null)
			throw new IllegalArgumentException("domain null");
		try {
			this.domain = (TimeFactory) domain;
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
	protected Time2(TimeDomain domain, long time) {
		this(domain);
		try {
			this.domain.valid(time, false);
			setInternalTime(time);
		} catch (T2Exception e) {
			throw new IllegalArgumentException("time", e);
		}
	}
	
	/**
	 * Construct a time index with the given time domain and parameters.
	 * An <b>unchecked</b> exception is thrown if the concrete time domain
	 * is not a {@link TimeFactory}.
	 * If necessary the time is adjusted as allowed by the last argument.
	 * 
	 * @param domain a non-null {@link TimeFactory}
	 * @param year a year, which can be unusually large, depending on the domain
	 * @param month a number between 1 and 12
	 * @param day the day in the month, starting with 1
	 * @param hour an hour in the range 0-23
	 * @param min a minute in the range 0-59
	 * @param sec a second in the range 0-59
	 * @param usec the number of microseconds in the current second in the range 0-999999
	 * @param adjust a non-null allowed adjustment mode
	 * @throws T2Exception
	 */
	protected Time2(TimeDomain domain, long year, int month, int day, int hour, int min, int sec,
			int usec, Adjustment adjust) throws T2Exception {
		this(domain);
		TimeParts tp = new TimeParts();
		tp.setYear(year);
		tp.setMonth(month);
		tp.setDay(day);
		tp.setHour(hour);
		tp.setMin(min);
		tp.setSec(sec);
		tp.setUsec(usec);
		set(tp, adjust);
	}
	
	/**
	 * Construct a time index with the given time domain and string, with possible adjustment.
	 * An <b>unchecked</b> exception is thrown if the concrete time domain
	 * is not a {@link TimeFactory}.
	 * The string is interpreted by an instance of {@link ExternalTimeFormat}
	 * configured. By default it is {@link DefaultExternalFormat}.
	 * <p>
	 * If necessary the time is adjusted as allowed by the last argument.
	 * 
	 * @param domain a non-null {@link TimeFactory}
	 * @param time a string containing a representation of a date and time
	 * @param adjustment a non-null allowed adjustment mode
	 * @throws T2Exception
	 */
	protected Time2(TimeDomain domain, String time, Adjustment adjustment) throws T2Exception {
		this(domain);
		set(time, adjustment);
	}
	
	/**
	 * Construct a time index with the given time domain and string.
	 * An <b>unchecked</b> exception is thrown if the concrete time domain
	 * is not a {@link TimeFactory}.
	 * The string is interpreted by an instance of {@link ExternalTimeFormat}
	 * configured. By default it is {@link DefaultExternalFormat}.
	 * <p>
	 * No adjustment is allowed.
	 * 
	 * @param domain a non-null {@link TimeFactory}
	 * @param time a string containing a representation of a date and time
	 * @throws T2Exception
	 */
	protected Time2(TimeDomain domain, String time) throws T2Exception {
		this(domain, time, Adjustment.NONE);
	}
	
	private Time2(TimeIndex timeIndex, long increment) throws T2Exception {
		this(timeIndex.getTimeDomain());
		long t = timeIndex.asLong();
		long incrT = t + increment;
		// overflow?
		if (t > 0 && incrT < 0 || t < 0 && incrT > 0)
			throw T2Msg.exception(K.T1075, timeIndex.toString(), increment);
		try {
			domain.valid(incrT, false);
		} catch (T2Exception e) {
			throw T2Msg.exception(e, K.T1075, timeIndex.toString(), increment);
		}
		setInternalTime(incrT);
	}
	
	@Override
	public TimeIndex convert(TimeDomain domain) throws T2Exception {
		return convert(domain, Adjustment.NONE);
	}
	
	@Override
	public TimeIndex convert(TimeDomain domain, Adjustment adjustment) throws T2Exception {
		if (getTimeDomain().equals(domain))
			return this;
		resolve();
		switch(getTimeDomain().getResolution()) {
		case YEAR:
			tp.setMonth(1);
		case MONTH:
			tp.setDay(1);
			break;
		default:
		}
		return new Time2(domain, tp.getYear(), tp.getMonth(), tp.getDay(), tp.getHour(),
				tp.getMin(), tp.getSec(), tp.getUsec(), adjustment);
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
		return new Time2(this, increment);
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
		resolve();
		return tp;
	}

	@Override
	public long getYear() {
		resolve();
		return tp.getYear();
	}

	@Override
	public int getMonth() {
		resolve();
		return tp.getMonth();
	}

	@Override
	public int getDay() {
		resolve();
		return tp.getDay();
	}

	@Override
	public int getHour() {
		resolve();
		return tp.getHour();
	}

	@Override
	public int getMinute() {
		resolve();
		return tp.getMin();
	}

	@Override
	public int getSecond() {
		resolve();
		return tp.getSec();
	}

	@Override
	public int getMicrosecond() {
		resolve();
		return tp.getUsec();
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
	
	/**
	 * Return a string representation of the time. In a <i>full</i>
	 * representation, all components are always included, from years to
	 * microseconds. In a standard representation, the formatting is delegated
	 * to {@link ExternalTimeFormat#format(Resolution, TimeParts)}.
	 * 
	 * @param full
	 *            if true, returns a full representation
	 * @return a string representation of the time
	 */
	public String toString(boolean full) {
		resolve();
		if (full) {
			StringBuilder sb = new StringBuilder();
			Formatter fmt = new Formatter(sb);
			String plus = tp.getYear() > 9999 ? "+" : "";
			fmt.format("%s%04d-%02d-%02d %02d:%02d:%02d.%06d", plus, tp.getYear(), tp.getMonth(),
					tp.getDay(), tp.getHour(), tp.getMin(), tp.getSec(), tp.getUsec());
			fmt.close();
			return sb.toString();
		} else
			return domain.format(domain.getResolution(), tp);
	}
	
	@Override
	public String toString(String format) {
		if (format == null)
			return toString();
		resolve();
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
		return toString(false);
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
				TimeDomain unrestrictedDomain = TimeDomainManager.getFactory().get(def, true);
				TimeIndex converted = convertOrThrowRTE(unrestrictedDomain, this);
				TimeIndex otherConverted = convertOrThrowRTE(unrestrictedDomain, otherTime);
				return converted.compareTo(otherConverted);
			} else if (compareResols < 0) {
				// convert  both to highest resolution
				TimeDomainDefinition def = new TimeDomainDefinition(null, otherTime.getTimeDomain().getResolution(), 0L);
				TimeDomain unrestrictedDomain = TimeDomainManager.getFactory().get(def, true);
				TimeIndex converted = convertOrThrowRTE(unrestrictedDomain, this);
				return converted.compareTo(otherTime);
			} else {
				// convert  both to highest resolution
				TimeDomainDefinition def = new TimeDomainDefinition(null, getTimeDomain().getResolution(), 0L);
				TimeDomain unrestrictedDomain = TimeDomainManager.getFactory().get(def, true);
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
	 * Set the time from a time parts object.
	 * Silently ignore elements finer than the resolution.
	 * 
	 * @param tp a non-null time parts object
	 * @param adjust a non-null allowed adjustment mode
	 * @throws T2Exception
	 */
	private void set(TimeParts tp, Adjustment adjust) throws T2Exception {
		setInternalTime(domain.pack(tp, adjust));
	}

	/**
	 * Set the time from a string.
	 * Silently ignore elements finer than the resolution.
	 * @param date a non-null string
	 * @param adjust a non-null allowed adjustment mode
	 * @throws T2Exception
	 */
	private void set(String date, Adjustment adjust) throws T2Exception {
		TimeParts tp = domain.scan(date);
		set(tp, adjust);
	}
	
	/**
	 * If not yet done, resolve a numerical time index into its constituent elements.
	 */
	private void resolve() {
		if (internalTimeModified) {
			tp = domain.unpack(getInternalTime());
			internalTimeModified = false;
		}
	}
	
	/**
	 * Return the numerical time index.
	 * 
	 * @return the numerical time index
	 */
	private long getInternalTime() {
		return internalTime;
	}
	
	/**
	 * Set the numerical time index. 
	 * 
	 * @param time the numerical time index
	 */
	private void setInternalTime(long time) {
		internalTimeModified = true;
		internalTime = time;
	}

}
