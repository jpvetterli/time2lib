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

import ch.agent.t2.T2Exception;
import ch.agent.t2.T2Msg;
import ch.agent.t2.T2Msg.K;
import ch.agent.t2.time.Adjustment;
import ch.agent.t2.time.Day;
import ch.agent.t2.time.Range;
import ch.agent.t2.time.Resolution;
import ch.agent.t2.time.TimeDomain;
import ch.agent.t2.time.TimeIndex;

/**
 * A DateHolder helps applications manipulate times.
 * When necessary, times are adjusted downwards. 
 *
 * @author Jean-Paul Vetterli
 */
public class DateHolder {

	// date not set <==> time = -1 && expr == null
	
	private TimeDomain domain;
	private DayExpression expr;
	private boolean isEmptyOkay = false;
	
	/**
	 * Construct a date holder in the given time domain and initialize it
	 * with the current time.
	 * 
	 * @param domain
	 *            a non-null time domain
	 */
	public DateHolder(TimeDomain domain){
		if (domain == null)
			throw new IllegalArgumentException("domain null");
		this.domain = domain;
		this.expr = new DayExpression(Adjustment.DOWN);
		this.expr.setTime(TimeUtil.now(domain));
	}

	/**
	 * Construct a date holder in the daily domain and initialize 
	 * it with the current time.
	 */
	public DateHolder(){
		this(Day.DOMAIN);
	}
	
	/**
	 * Return the number of units for a large increment in the given resolution.
	 * 
	 * @param unit a non-null resolution
	 * @return a number of units
	 */
	protected static int getLargeIncrement(Resolution unit) {
		int incr;
		switch (unit) {
		case YEAR:
			incr = 5;
			break;
		case MONTH:
			incr = 12;
			break;
		case DAY:
			incr = 15;
			break;
		case HOUR:
			incr = 24;
			break;
		case MIN:
			incr = 24 * 60;
			break;
		case SEC:
			incr = 24 * 60 * 60;
			break;
		case MSEC:
			incr = 24 * 60 * 60 * 1000;
			break;
		case USEC:
			incr = 24 * 60 * 60 * 1000 * 1000;
			break;
		case NSEC:
			incr = 24 * 60 * 60 * 1000 * 1000 * 1000;
			break;
		default:
			throw new RuntimeException("bug: " + unit.name());
		}
		return incr;
	}
	
	/**
	 * Return the time domain.
	 * 
	 * @return the time domain of the date holder
	 */
	public TimeDomain getTimeDomain() {
		return domain;
	}
	
	/**
	 * Set a flag to allow or disallow empty dates.
	 * 
	 * @param allow if true, empty dates will be allowed
	 */
	public void allowEmptyDate(boolean allow) {
		isEmptyOkay = allow;
	}
	
	/**
	 * Change the time domain. The current day expression is retained.
	 * 
	 * @param domain the non-null new time domain
	 * 
	 * @throws T2Exception
	 */
	public void reset(TimeDomain domain) throws T2Exception {
		if (domain == null)
			throw new IllegalArgumentException("domain null");
		if (this.domain.equals(domain))
			return;
		String date = getDateText();
		this.domain = domain;
		setDate(date);
	}
	
	/**
	 * Change the date to the current time.
	 * 
	 */
	public void resetToCurrentTime() {
		if (expr == null)
			this.expr = new DayExpression(Adjustment.DOWN);
		this.expr.setTime(TimeUtil.now(domain));
	}

	/**
	 * Set the date. A null or empty date is conditionally allowed. 
	 * This is useful for signaling that no date is specified.
	 * Refer to {@link DayExpression} for the valid syntax of dates.
	 * 
	 * @param date a day expression
	 * @throws T2Exception
	 * @see DateHolder#allowEmptyDate(boolean)
	 */
	public void setDate(String date) throws T2Exception {
		if (date == null || date.length() == 0) {
			if (isEmptyOkay) {
				expr = null;
			} else
				throw T2Msg.exception(K.T7021);
		} else {
			if (expr == null) {
				expr = new DayExpression(Adjustment.DOWN);
			}
			expr.setExpression(domain, date);
		}
	}
	
	/**
	 * Increment the date by the given amount. If no date is currently set,
	 * the method has the same effect as {@link DateHolder#resetToCurrentTime() resetToToday()}.
	 * 
	 * @param increment a positive or negative number
	 */
	public void incrDate(int increment) throws T2Exception {
		if (increment == 0)
			return;
		if (expr == null)
			resetToCurrentTime();
		else
			expr.incr(increment);
	}
	
	/**
	 * Increase or decrease the date by a unit or by a large amount.
	 * What constitutes a large amount depends on the resolution of the time domain.
	 * 
	 * @param up if true increase, else decrease
	 * @param large if true use a large amount, else a single unit
	 * @throws T2Exception
	 */
	public void incrDate(boolean up, boolean large) throws T2Exception {
		int incr = 1;
		if (large) {
			if (expr.isToday())
				incr = getLargeIncrement(Resolution.DAY);
			else
				incr = getLargeIncrement(domain.getResolution());
		}
		incrDate(up ? incr : -incr);
	}

	/**
	 * Return the text of the day expression set with {@link DateHolder#setDate(String) setDate}. 
	 * If a date was never set, an empty string is returned.
	 * <p>
	 * Do get a date as a string, use <code>getDate()</code> and
	 * <code>toString()</code>.
	 * <p>
	 * The difference between the two ways becomes important when the date is a
	 * day expression: this method does not resolve the expression (and there is
	 * no risk of error caused by the presence of unresolved keywords in the day
	 * expression).
	 * <p>
	 * The method never returns null.
	 * 
	 * @return the text of the current day expression or an empty string
	 */
	public String getDateText() {
		if (expr == null)
			return "";
		else
			return expr.getExpression();
	}
	
	/**
	 * Return the date evaluated in the time domain of the value holder.  
	 * 
	 * @return a time index or null when date is set
	 */
	public TimeIndex getDate() throws T2Exception {
		if (expr == null)
			return null;
		else
			return expr.getDate(domain);
	}
	
	/**
	 * Return the date evaluated in the context of the given range.
	 * Note that depending on the day expression used in the date, 
	 * the context of a range is more than simply its time domain.
	 * 
	 * @param context a non-null range
	 * @return a time index or null when no date is set
	 * @throws T2Exception
	 */
	public TimeIndex getDate(Range context) throws T2Exception {
		if (expr == null)
			return null;
		else
			return expr.getDate(context);
	}

}

