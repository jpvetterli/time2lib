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
 * Package: ch.agent.t2.time
 * Type: TimeDomain
 * Version: 1.0.0
 */
package ch.agent.t2.time;

import ch.agent.core.KeyedException;

/**
 * The TimeDomain is a fundamental property of a {@link TimeIndex}. Most
 * importantly, the domain defines the <b>resolution</b> of a time index.
 * Incrementing a time index yields the next index in the domain's resolution,
 * like next year, next day, next microsecond, etc. In a more sophisticated
 * domain, like one defining the sequence of working days, incrementing a time
 * index yields the next working day, with Monday directly following Friday. The
 * available resolution units are listed in {@link Resolution}.
 * <p>
 * Another property of a time domain is the <b>origin</b>. To understand its
 * role it is necessary to know that time is internally represented by a
 * <em>long</em> and that time starts in year 0. To get an idea, the largest
 * time with microsecond resolution is
 * <q>292277-01-09 04:00:54.775807</q> (yes, in the year 292277). In some
 * applications, however, it is useful to represent time with a 32 bit integer.
 * It is here that the origin comes into play: the method
 * {@link TimeIndex#asOffset()} returns time as an integer offset from the
 * origin and the method {@link TimeDomain#timeFromOffset(long)} does the
 * reverse.
 * <p>
 * Another property of a time domain is its label. When an application uses a
 * time domain factory, for example through {@link TimeDomainManager}, the label should uniquely 
 * identify the time domain, making it safe to get a domain <q>by name</q>.
 * <p>
 * A TimeDomain is also a factory, with a choice of methods for creating
 * {@link TimeIndex} objects.
 * <p>
 * The <em>Time2</em> package implements dates and time in the spirit of the
 * ISO 8601 standard. A future version may adhere exactly to the standard. To
 * keep a long story short, the <em>t2.time</em> package applies a <a
 * href="http://en.wikipedia.org/wiki/Proleptic_Gregorian_calendar">Proleptic
 * Gregorian Calendar</a> and knows only the UTC time zone. The external format
 * of dates and times is defined by implementations of the
 * {@link ExternalTimeFormat} interface. The default is
 * {@link DefaultExternalFormat}.
 * <p>
 * Dates and times are astonishingly complex and messy. Some interesting
 * references are
 * <ul>
 * <li><a href="http://en.wikipedia.org/wiki/ISO_8601">ISO 8601</a>, at
 * Wikipedia.
 * <li><a href="http://www.ietf.org/rfc/rfc3339.txt">Date and Time on the
 * Internet (RFC 3399)</a>, by the IETF.
 * <li><a href="http://tycho.usno.navy.mil/systime.html">Systems of Time</a>, by
 * the US Naval Observatory.
 * <li><a href="http://www.exit109.com/~ghealton/y2k/yrexamples.html">The Best
 * of Dates, The Worst Of Dates</a>, by Gilbert Healton.
 * </ul>
 * 
 * The design assumes that TimeDomain is implemented as an immutable object.
 * 
 * @author Jean-Paul Vetterli
 * @version 1.0.0
 * @see TimeIndex
 * @see TimeDomainManager
 */
public interface TimeDomain {

	public static long DAYS_TO_19700101 = 719528L;
	
	public static long DAYS_TO_20000101 = 730485L;
	
	/**
	 * Throw an exception if the domain is not equal to this domain.
	 * 
	 * @param domain a time domain
	 * @throws KeyedException
	 */
	void requireEquality(TimeDomain domain) throws KeyedException;
	
	/**
	 * Return the resolution of the time domain.
	 * 
	 * @return a time unit
	 */
	Resolution getResolution();
	
	/**
	 * Return a negative, zero, positive number if the resolution is higher,
	 * the same, or lower than the argument. To make things completely clear,
	 * second resolution is higher than year resolution.
	 * 
	 * @param unit
	 *            a non-null resolution
	 * @return a negative (zero, positive) number if the resolution is higher
	 *         (same, lower) than the argument
	 */
	int compareResolutionTo(Resolution unit);
	
	/**
	 * Return the origin of the time domain.
	 * By default it is 0L.
	 * 
	 * @return a positive or negative number
	 */
	long getOrigin();

	/**
	 * Return the label of the time domain, uniquely identifying the domain when
	 * using a factory like {@link TimeDomainManager}.
	 * 
	 * @return the label of the time domain
	 */
	String getLabel();
	
	/**
	 * Return a new TimeIndex corresponding to the given index.
	 * 
	 * @param index a number
	 * @return a TimeIndex in the domain
	 */
	TimeIndex time(long index);
	
	/**
	 * Return a new TimeIndex corresponding to the given offset.
	 * 
	 * @param offset a number to be added to the domain's origin
	 * @return a TimeIndex in the domain
	 */
	TimeIndex timeFromOffset(long offset);

	/**
	 * Return a new TimeIndex giving the minimum time in this domain. 
	 * 
	 * @return a TimeIndex in the domain
	 */
	TimeIndex minTime();
	
	/**
	 * Return a new TimeIndex giving the maximum time in this domain. 
	 * 
	 * @return a TimeIndex in the domain
	 */
	TimeIndex maxTime();
	
	/**
	 * Return a new TimeIndex corresponding to the given date string. 
	 * Adjustments are not allowed. The date syntax depends on the actual
	 * {@link ExternalTimeFormat} used, the default being {@link DefaultExternalFormat}.
	 * 
	 * @param date a non-null date string
	 * @return a TimeIndex in the domain
	 * @throws KeyedException
	 */
	TimeIndex time(String date) throws KeyedException;
	
	/**
	 * Return a new TimeIndex corresponding to the given date string. 
	 * If necessary, adjustments are made as specified. An exception is
	 * thrown if an adjustment is required but not allowed.
	 * 
	 * @param date a non-null date string
	 * @param adjust a non-null adjustment mode
	 * @return a TimeIndex in the domain
	 * @throws KeyedException
	 */
	TimeIndex time(String date, Adjustment adjust) throws KeyedException;
	
	/**
	 * Return a new TimeIndex corresponding to the list of parameters. 
	 * If necessary, adjustments are made as specified. An
	 * exception is thrown if an adjustment is required but not allowed.
	 * 
	 * @param year the year
	 * @param month the month
	 * @param day the day
	 * @param hour the hour
	 * @param min the minute
	 * @param sec the second
	 * @param usec the microsecond
	 * @param adjust a non-null adjustment mode
	 * @return a TimeIndex in the domain
	 * @throws KeyedException
	 */
	TimeIndex time(long year, int month, int day, int hour, int min, int sec,
			int usec, Adjustment adjust) throws KeyedException;

}
