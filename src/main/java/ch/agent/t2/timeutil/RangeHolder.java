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
 * Package: ch.agent.t2.timeutil
 * Type: RangeHolder
 * Version: 1.0.1
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
 * A RangeHolder helps applications manipulate time ranges.
 * <p>
 * If necessary, dates are adjusted. Adjustment is upwards for the 
 * start of the range and downwards for the end.
 * 
 * @author Jean-Paul Vetterli
 * @version 1.0.1
 */
public class RangeHolder {

	private TimeDomain domain;
	private DayExpression beginExpr; // beginExpr == null <==> endExpr == null
	private DayExpression endExpr;
	private Adjustment beginAdj = Adjustment.UP;
	private Adjustment endAdj = Adjustment.DOWN;
	
	/**
	 * Construct a range holder in the given time domain and initialized
	 * with the given time. If the given time is null the range will be empty.
	 * 
	 * @param domain
	 *            a non-null time domain
	 * @param time
	 *            a time index or null
	 */
	private RangeHolder(TimeDomain domain, TimeIndex time) {
		this.domain = domain;
		if (time != null) {
			this.newExpr();
			this.beginExpr.setTime(time);
			this.endExpr.setTime(time);
		} else 
			setEmpty();
	}
	
	/**
	 * Construct a range holder with the given time domain and 
	 * initialized with the current time.
	 * 
	 * @param domain
	 *            a non-null time domain
	 */
	public RangeHolder(TimeDomain domain) {
		this(domain, TimeUtil.now(domain));
	}

	/**
	 * Construct a range holder in the daily time domain and 
	 * initialized with the current time.
	 */
	public RangeHolder() {
		this(Day.DOMAIN);
	}

	/**
	 * Construct a range holder for the given range.
	 * 
	 * @param range a non-null range
	 */
	public RangeHolder(Range range){
		this(range.getTimeDomain(), null);
		reset(range);
	}
	
	/**
	 * Construct a range holder as a copy of the given model.
	 * 
	 * @param model a non-null range holder
	 */
	public RangeHolder(RangeHolder model) {
		domain = model.domain;
		beginExpr = model.beginExpr == null ? null : new DayExpression(model.beginExpr);
		endExpr = model.endExpr == null ? null : new DayExpression(model.endExpr);
		beginAdj = model.beginAdj;
		endAdj = model.endAdj;
	}
	
	private void setEmpty() {
		beginExpr = null;
		endExpr = null;
	}
	
	private void newExpr() {
		if (beginExpr == null) {
			beginExpr = new DayExpression(beginAdj);
			endExpr = new DayExpression(endAdj);
		}
	}
	
	/**
	 * Return the time domain.
	 * 
	 * @return the time domain of the range
	 */
	public TimeDomain getTimeDomain() {
		return domain;
	}
	
	/**
	 * Change the range to the given range.
	 * 
	 * @param range a non-null range
	 */
	public void reset(Range range){
		domain = range.getTimeDomain();
		if (range.isEmpty())
			setEmpty();
		else {
			newExpr();
			beginExpr.setTime(range.getFirst());
			endExpr.setTime(range.getLast());
		}
	}
	
	/**
	 * Change the time domain of the range holder. The day expressions of 
	 * the current bounds are retained.
	 * 
	 * @param domain a non-null domain
	 * @throws T2Exception
	 */
	public void reset(TimeDomain domain) throws T2Exception {
		if (domain == null)
			throw new IllegalArgumentException("domain null");
		String begin = getBeginText();
		String end = getEndText();
		this.domain = domain;
		setBegin(begin, false);
		setEnd(end);
	}

	/**
	 * Set the date for the range beginning. A null or empty date is allowed. 
	 * This is useful for signaling that no date is specified.
	 * The method has a side-effect on the other extremity of the range.
	 * Refer to {@link DayExpression} for the valid syntax of dates.
	 * 
	 * @param date a day expression
	 * @throws T2Exception
	 */
	public void setBegin(String date) throws T2Exception {
		setBegin(date, true);
	}
	
	private void setBegin(String date, boolean enforceValidRange) throws T2Exception {
		DayExpression previousBegin = beginExpr == null ? null : new DayExpression(beginExpr);
		if (date == null || date.length() == 0)
			setEmpty();
		else {
			newExpr();
			beginExpr.setExpression(domain, date);
			if (enforceValidRange)
				enforceValidRange(true, previousBegin);
		}
	}
	
	/**
	 * Set the date for the range end. A null or empty date is allowed. 
	 * This is useful for signaling that no date is specified.
	 * The method has a side-effect on the other extremity of the range.
	 * Refer to {@link DayExpression} for the valid syntax of dates.
	 * 
	 * @param date a day expression
	 * @throws T2Exception
	 */
	public void setEnd(String date) throws T2Exception {
		DayExpression previousEnd = endExpr == null ? null : new DayExpression(endExpr);
		if (date == null || date.length() == 0)
			setEmpty();
		else {
			newExpr();
			endExpr.setExpression(domain, date);
			enforceValidRange(false, previousEnd);
		}
	}
	
	/**
	 * Increment the beginning of the range by the given amount. If no date is
	 * currently set, the method has the same effect as setting the beginning to
	 * the current time.
	 * The method has a side-effect on the other extremity of the range.
	 * 
	 * @param increment
	 *            a positive or negative number
	 */
	public void incrBegin(int increment) throws T2Exception {
		if (increment == 0)
			return;
		DayExpression previousBegin = null;
		if (beginExpr == null)
			setBegin(TimeUtil.now(domain).toString());
		else {
			previousBegin = new DayExpression(beginExpr);
			beginExpr.incr(increment);
		}
		enforceValidRange(true, previousBegin);
	}
	
	/**
	 * Increase or decrease the beginning of the range by a unit or by a large amount.
	 * What constitutes a large amount depends on the resolution of the time domain.
	 * The method has a side-effect on the other extremity of the range.
	 * 
	 * @param up if true increase, else decrease
	 * @param large if true use a large amount, else a single unit
	 * @throws T2Exception
	 */
	public void incrBegin(boolean up, boolean large) throws T2Exception {
		int incr = 1;
		if (large) {
			if (beginExpr.isToday())
				incr = DateHolder.getLargeIncrement(Resolution.DAY);
			else
				incr = DateHolder.getLargeIncrement(domain.getResolution());
		}
		incrBegin(up ? incr : -incr);
	}
	
	/**
	 * Increment the end of the range by the given amount. If no date is
	 * currently set, the method has the same effect as setting the beginning to
	 * the current time.
	 * The method has a side-effect on the other extremity of the range.
	 * 
	 * @param increment
	 *            a positive or negative number
	 */
	public void incrEnd(int increment) throws T2Exception {
		if (increment == 0)
			return;
		DayExpression previousEnd = null;
		if (endExpr == null)
			setEnd(TimeUtil.now(domain).toString());
		else {
			previousEnd = new DayExpression(endExpr);
			endExpr.incr(increment);
		}
		enforceValidRange(false, previousEnd);
	}
	
	/**
	 * Increase or decrease the end of the range by a unit or by a large amount.
	 * What constitutes a large amount depends on the resolution of the time domain.
	 * The method has a side-effect on the other extremity of the range.
	 * 
	 * @param up if true increase, else decrease
	 * @param large if true use a large amount, else a single unit
	 * @throws T2Exception
	 */
	public void incrEnd(boolean up, boolean large) throws T2Exception {
		int incr = 1;
		if (large) {
			if (endExpr.isToday())
				incr = DateHolder.getLargeIncrement(Resolution.DAY);
			else
				incr = DateHolder.getLargeIncrement(domain.getResolution());
		}
		incrEnd(up ? incr : -incr);
	}

	/**
	 * Return the text of the day expression set with {@link RangeHolder#setBegin(String) setBegin}. 
	 * If a date was never set, an empty string is returned.
	 * To get the resolved date, use one of the {@link RangeHolder#getRange getRange} methods.
	 * The method never returns null.
	 * 
	 * @return the text of the current day expression for the beginning of the range or an empty string
	 */
	public String getBeginText() {
		if (beginExpr == null)
			return "";
		else
			return beginExpr.getExpression();
	}
	
	/**
	 * Return the text of the day expression set with {@link RangeHolder#setEnd(String) setEnd}. 
	 * If a date was never set, an empty string is returned.
	 * To get the resolved date, use one of the {@link RangeHolder#getRange getRange} methods.
	 * The method never returns null.
	 * 
	 * @return the text of the current day expression for the end of the range or an empty string
	 */
	public String getEndText() {
		if (endExpr == null)
			return "";
		else
			return endExpr.getExpression();
	}
	
	/**
	 * Get the range evaluated in the time domain of the range holder.
	 * 
	 * @return a range
	 * @throws T2Exception
	 */
	public Range getRange() throws T2Exception {
		return getRange(domain);
	}
	
	/**
	 * Get the range evaluated in the context of the given domain.
	 * 
	 * @param context a non-null time domain
	 * @return a range
	 * @throws T2Exception
	 */
	public Range getRange(TimeDomain context) throws T2Exception {
		if (beginExpr == null)
			return new Range(context);
		else
			return new Range(beginExpr.getDate(context), endExpr.getDate(context));
	}
	
	/**
	 * Get the range evaluated in the context of the given range.
	 * If the holder does not contain any info, an empty range is returned. If
	 * the domain needs conversion, steps are taken to avoid invalid
	 * ranges. For example, when because of adjustments the daily range
	 * [1980-11-22, 1980-11-22] would be turned into the Monday to Friday range
	 * [1980-11-24, 1980-11-21], the method fixes it to [1980-11-21,
	 * 1980-11-21].
	 * 
	 * @param context non-null range
	 * @return a range
	 * @throws T2Exception
	 */
	public Range getRange(Range context) throws T2Exception {
		TimeDomain contextDomain = context.getTimeDomain();
		if (beginExpr == null) {
			return new Range(contextDomain);
		} else {
			// be more permissive with context and fix ranges invalidated by conversion
			TimeIndex begin = beginExpr.getDate(context);
			TimeIndex end = endExpr.getDate(context);
			if (begin == null || end == null) // (consequence of empty context range when context needed)
				return new Range(contextDomain);
			else if (begin.compareTo(end) > 0 && (beginExpr.needContext() || endExpr.needContext()))
				return new Range(contextDomain);
			else {
				/* 
				 * There is a possibly that a valid range has been converted 
				 * into an invalid one. Typical case: daily(Sat, Sat) turned 
				 * into MoFr(Mon,Fri) because of adjustments. As this is not 
				 * the intended effect of adjustments, the problem is fixed 
				 * silently here.
				 */
				if (begin.compareTo(end) > 0 && beginAdj == Adjustment.UP) {
					beginExpr.setTime(end);
					begin = beginExpr.getDate(context);
				}
				return new Range(begin, end);
			}
		}
	}
	
	/**
	 * Return true if the range holder needs a context for evaluating dates.
	 * 
	 * @return true if a context is needed
	 */
	public boolean needContext() {
		return (beginExpr != null && beginExpr.needContext()) 
			|| (endExpr != null && endExpr.needContext());
	}
	
	/**
	 * This method enforces a valid range. If anything goes wrong, it resets the
	 * day expressions to their previous state. When keepBegin is true, the
	 * previous argument is the previous value of the begin day expression. When
	 * it is false, the previous argument is the previous value of the end day
	 * expression.
	 * 
	 * @param keepBegin
	 * @param previous
	 * @throws T2Exception
	 */
	private void enforceValidRange(boolean keepBegin, DayExpression previous) throws T2Exception {
		DayExpression previous2 = new DayExpression(keepBegin ? endExpr : beginExpr);
		if (!endExpr.enforceValidRange(domain, beginExpr, keepBegin)) {
			// repair
			if (keepBegin) {
				beginExpr.reset(previous);
				endExpr.reset(previous2);
			} else {
				beginExpr.reset(previous2);
				endExpr.reset(previous);
			}
			throw T2Msg.exception(K.T7027);
		}
	}
	
}

