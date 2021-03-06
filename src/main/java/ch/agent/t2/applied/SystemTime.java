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
package ch.agent.t2.applied;

import ch.agent.t2.T2Exception;
import ch.agent.t2.T2Msg;
import ch.agent.t2.T2Msg.K;
import ch.agent.t2.time.Adjustment;
import ch.agent.t2.time.Resolution;
import ch.agent.t2.time.Time2;
import ch.agent.t2.time.TimeDomain;
import ch.agent.t2.time.TimeDomainDefinition;
import ch.agent.t2.time.TimeIndex;

/**
 * A SystemTime is a {@link Time2} time with millisecond resolution and the
 * origin at 1970-01-01. It corresponds to the time used internally by
 * {@link java.util.Date}. For times before 1970-01-01,
 * {@link TimeIndex#asOffset()} will return negative values. The domain label is
 * <b>systemtime</b>.
 * 
 * @author Jean-Paul Vetterli
 */
public class SystemTime extends Time2 {

	private static final long ORIGIN = TimeDomain.DAYS_TO_19700101 * 24 * 60 * 60 * 1000;

	/**
	 * A constant holding the domain label.
	 */
	public static final String LABEL = "systemtime";
	
	/**
	 * A constant holding the domain.
	 */
	public static final TimeDomain DOMAIN = new TimeDomainDefinition(LABEL, Resolution.MSEC, ORIGIN).asTimeDomain();

	/**
	 * Construct a <q>systemtime</q> time from another time object. 
	 * @param time a non-null time in the domain <q>systemtime</q>
	 * @throws T2Exception
	 */
	public SystemTime(TimeIndex time) throws T2Exception {
		super(DOMAIN, time.asLong());
		if (DOMAIN != time.getTimeDomain())
			throw T2Msg.exception(K.T1073, time.getTimeDomain().getLabel(), DOMAIN.getLabel());
	}
	
	/**
	 * Constructs the current time in the <q>systemtime</q> domain.
	 * 
	 * @throws T2Exception
	 */
	public SystemTime() throws T2Exception {
		super(DOMAIN, ORIGIN + System.currentTimeMillis());
	}
	
	/**
	 * Construct a <q>systemtime</q> from a Java fast time.
	 * A Java fast time is returned for example by
	 * <pre>
	 * System.currentTimeMillis()
	 * </pre>
	 * 
	 * To get a Java fast time from a <q>systemtime</q> do
	 * <pre>
	 * SystemTime t;
	 * ...
	 * long javaTime = t - SystemTime.DOMAIN.getOrigin();
	 * </pre>
	 * 
	 * @param millis a number of milliseconds since the epoch
	 */
	public SystemTime(long millis) {
		super(DOMAIN, ORIGIN + millis);
	}

	/**
	 * Construct a <q>systemtime</q> time from a string.
	 *  
	 * @param date a non-null string
	 * @throws T2Exception
	 */
	public SystemTime(String date) throws T2Exception {
		super(DOMAIN, date);
	}
	
	/**
	 * Construct a <q>systemtime</q> time from the given time components.
	 * 
	 * @param year a non-negative number
	 * @param month a number in [1-12]
	 * @param day a number between 1 and the last day of the month
	 * @param hour a number in [0-23]
	 * @param min a number in [0-59]
	 * @param sec a number in [0-59]
	 * @param msec a msec in [0-999]
	 * @throws T2Exception
	 */
	public SystemTime(long year, int month, int day, int hour, int min, int sec, int msec) throws T2Exception { 
		super(DOMAIN, year, month, day, hour, min, sec, msec * 1000, Adjustment.NONE);
	}
	
	/**
	 * Get the fast Java time corresponding to this
	 * <q>systemtime</q>. The result can be passed to the java.util.Date
	 * constructor.
	 * 
	 * @return a long representing a fast Java time
	 */
	public long asFastJavaTime() {
		return asLong() - ORIGIN;
	}

}
