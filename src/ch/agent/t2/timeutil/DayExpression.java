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
 * Package: ch.agent.t2.timeutil
 * Type: DayExpression
 * Version: 1.0.0
 */
package ch.agent.t2.timeutil;

import ch.agent.core.KeyedException;
import ch.agent.t2.T2Msg;
import ch.agent.t2.time.Adjustment;
import ch.agent.t2.time.Day;
import ch.agent.t2.time.Range;
import ch.agent.t2.time.TimeDomain;
import ch.agent.t2.time.TimeIndex;
import ch.agent.t2.time.Resolution;

/**
 * A DayExpression allows to express times symbolically. The syntax of a day
 * expression has the following informal grammar: <blockquote>
 * 
 * <pre>
 * <em>expression</em> := (today|start|end|<em>date</em>)(+|-integer)*
 * <em>date</em> := yyyy-mm-dd
 * </pre>
 * 
 * </blockquote> In words, a day expression is a keyword or a date followed by
 * zero or more positive or negative offsets. The keywords are <b>today</b>,
 * <b>start</b> and <b>end</b>. To be resolved as a time index, a day expression
 * requires a context. Possible contexts a time domain or a range.
 * The keywords start and end can only be resolved in the
 * context of a range. For example, the expression <q>end-2+1</q> corresponds to
 * the upper bound of the context range, minus 1. Day expressions can be
 * incremented without being resolved. For example, incrementing the expression
 * <q>start</q> by 2 equals <q>start+2</q>.
 * <p>
 * The date part is a day in the standard daily domain. With the keywords start
 * and end, and with literal dates, the offset is applied in the target domain,
 * as given by the context argument when dates are returned. But with the
 * keyword today, the offset is applied <em>sometimes</em> in the daily domain.
 * It is applied in the daily domain only when the target domain resolution is
 * finer than daily. This is for example useful when getting the last n days of
 * a series with second resolution. In all other cases, the offset is applied in
 * the target domain.
 * <p>
 * It is possible to use incomplete dates like a year without month and day, and
 * they will be completed in the standard fashion, but offsets are only valid
 * with complete daily dates.
 * <p>
 * A new day expression has no default value. Trying to resolve a new day
 * expression which was never set is a bug and		case ERROR:
			throw new IllegalStateException(type.name());
 throws an
 * {@link IllegalStateException}.
 * 
 * @author Jean-Paul Vetterli
 * @version 1.0.0
 */
public class DayExpression {

	private enum Type {
		END, ERROR, LITERAL, START, TODAY
	}
	
	/**
	 * The keyword <em>today</em>.
	 */
	public static String TODAY = "today";
	/**
	 * The keyword <em>start</em>.
	 */
	public static String START = "start";
	/**
	 * The keyword <em>end</em>.
	 */
	public static String END = "end";
	
	private Adjustment adjustment;
	private Type type;
	private TimeIndex time; // type == LITERAL <==> time != null
	private int offset; // type == LITERAL ==> offset == 0
	
	/**
	 * Construct a day expression with the given adjustment.
	 * 
	 * @param adjustment a non-null adjustment
	 */
	public DayExpression(Adjustment adjustment) {
		if (adjustment == null)
			throw new IllegalArgumentException("adjustment null");
		this.adjustment = adjustment;
		type = Type.ERROR;
	}
	
	/**
	 * Construct a day expression as a copy of the given model.
	 * 
	 * @param model a non-null model
	 */
	public DayExpression(DayExpression model) {
		adjustment = model.adjustment;
		type = model.type;
		time = model.time;
		offset = model.offset;
	}
	
	/**
	 * Parse the expression as a date in the daily calendar and returns it as a {@link Day}.
	 * 
	 * @param expr a non-null day expression
	 * @return a Day
	 * @throws KeyedException
	 */
	public static Day parseDay(String expr) throws KeyedException {
		return new Day(parseDay(expr, Day.DOMAIN, Adjustment.NONE));
	}
	
	/**
	 * Parse the expression as a date in the daily calendar and returns it as a TimeIndex
	 * in the domain requested, adjusted as indicated.
	 * 
	 * @param expr a non-null day expression
	 * @param domain a non-null time domain
	 * @param adjustment a non-null adjustment
	 * @return a time index
	 * @throws KeyedException
	 */
	public static TimeIndex parseDay(String expr, TimeDomain domain, Adjustment adjustment) throws KeyedException {
		DayExpression dex = new DayExpression(adjustment);
		dex.parseExpression(Day.DOMAIN, expr);
		return dex.getDate(domain);
	}
	
	/**
	 * Return true if the expression includes the keyword <em>today</em>.
	 * 
	 * @return true if the expression includes today
	 */
	protected boolean isToday() {
		return type == Type.TODAY;
	}
	
	/**
	 * Change the expression to match the given model. Do nothing if the model is null.
	 * 
	 * @param model a model
	 */
	protected void reset(DayExpression model) {
		if (model == null)
			return;
		adjustment = model.adjustment;
		type = model.type;
		time = model.time;
		offset = model.offset;
	}
	
	/**
	 * Return true if the expression requires a context.
	 * 
	 * @return true if the expression requires a context
	 */
	protected boolean needContext() {
		switch (type) {
		case LITERAL:
		case TODAY:
			return false;
		case END:
		case START:
			return true;
		case ERROR:
			throw new IllegalStateException(type.name());
		default:
			throw new RuntimeException(type.name());
		}
	}
	
	/**
	 * Set the time of the expression to the given time index.
	 * 
	 * @param time a non-null time index
	 */
	protected void setTime(TimeIndex time) {
		if(time == null)
			throw new IllegalArgumentException("time null");
		this.type = Type.LITERAL;
		this.time = time; // no worry, time is immutable
		this.offset = 0;
	}
	
	/**
	 * Set the domain and the expression.
	 * @param domain a non-null domain
	 * @param expression a non-null expression
	 * 
	 * @throws KeyedException
	 */
	public void setExpression(TimeDomain domain, String expression) throws KeyedException {
		parseExpression(domain, expression);
	}

	/**
	 * Add the given increment to the expression. This operation does
	 * not resolve the expression but is applied to a separately kept offset.
	 * The offset is applied when the expression is resolved.
	 * 
	 * @param increment a positive or negative number
	 * @throws KeyedException
	 */
	public void incr(int increment) throws KeyedException {
		if (type == Type.ERROR)
			throw new IllegalStateException();
		if (increment == 0)
			return;
		long test = offset + increment;
		if (Math.abs(test) <= Integer.MAX_VALUE)
			offset = (int) test;
		applyOffset();
	}
	
	/**
	 * Return the expression.
	 * 
	 * @return the expression.
	 */
	public String getExpression() {
		String expression;
		switch (type) {
		case LITERAL:
			return time.toString();
		case TODAY:
			expression = TODAY;
			break;
		case END:
			expression = END;
			break;
		case START:
			expression = START;
			break;
		case ERROR:
			throw new IllegalStateException(type.name());
		default:
			throw new RuntimeException(type.name());
		}
		if (offset > 0)
			expression = expression + "+" + offset;
		else if (offset < 0)
			expression = expression + offset;
		return expression;
	}
	
	/**
	 * Resolve the expression in the given time domain and return the result as
	 * a time index.
	 * 
	 * @param domain
	 *            a non-null time domain
	 * @return a time index
	 * @throws KeyedException
	 */
	public TimeIndex getDate(TimeDomain domain) throws KeyedException {
		switch(type) {
		case LITERAL:
			if (!domain.equals(time.getTimeDomain()))
				time = time.convert(domain, adjustment);
			return time;
		case TODAY: {
 			/*
			 * Warning: this is too smart by half. The idea is that "today-20"
			 * should mean "twenty days ago" in the context of sub-daily
			 * domains, like datetime for transactions. This allows to mix
			 * datetime series (buy/sell series) with daily series in charts for
			 * example, or to see the last 21 days of transaction data in the
			 * series viewer. On the other hand when mixing base series with
			 * moving averages, values for the mavs are typically requested with
			 * an additional number of periods to ensure that base and derived
			 * series start at the same date; this would not work when series
			 * are Monday-to-Friday but offsets are applied in the DAILY domain.
			 * So for higher resolutions, offsets are applied in the context
			 * domain.
			 */
			if (domain.compareResolutionTo(Resolution.DAY) < 0) {
				// apply the offset in the DAILY domain
				TimeIndex t = addOffset(TimeUtil.now(Day.DOMAIN));
				return t.convert(domain, Adjustment.DOWN);
			} else {
				// apply the offset in the context domain
				return addOffset(TimeUtil.now(domain));
			}
		}
		case END:
		case START:
			throw T2Msg.exception(32104, getExpression());
		case ERROR:
			throw new IllegalStateException(type.name());
		default:
			throw new RuntimeException("bug: " + type.name());
		}
	}

	/**
	 * Resolve the expression in the context of the given range and return the
	 * result as a time index.
	 * 
	 * @param context
	 *            a non-null range
	 * @return a time index
	 * @throws KeyedException
	 */
	public TimeIndex getDate(Range context) throws KeyedException {
		switch(type) {
		case LITERAL:
		case TODAY:
			return getDate(context.getTimeDomain());
		case END:
			return addOffset(context.getLast());
		case START:
			return addOffset(context.getFirst());
		case ERROR:
			throw new IllegalStateException(type.name());
		default: // ERROR
			throw new RuntimeException(type.name());
		}
	}
	
	/**
	 * @param offset a positive or negative offset
	 */
	private void setOffset(int offset) {
		this.offset = offset;
	}
	
	/**
	 * Apply the offset to the time index and set the offset to zero.
	 * @throws KeyedException
	 */
	private void applyOffset() throws KeyedException {
		if (time != null && offset != 0) {
			time = time.add(offset);
			offset = 0;
		}
	}
	
	/**
	 * @param t a time index or null
	 * @return a time index or null if t is null
	 * @throws KeyedException
	 */
	private TimeIndex addOffset(TimeIndex t) throws KeyedException {
		if (t == null)
			return null;
		if (offset != 0)
			return t.add(offset);
		else
			return t;
	}
	
	/**
	 * @param domain a non-null domain
	 * @param expression a non-null string
	 * @throws KeyedException
	 */
	private void parseExpression(TimeDomain domain, String expression) throws KeyedException {
		DayExpression previous = new DayExpression(this);
		try {
			tryParseExpression(domain, expression);
		} catch (KeyedException e) {
			reset(previous);
			throw e;
		}
	}
	
	/**
	 * @param domain a non-null domain
	 * @param expr a non-null string
	 * @throws KeyedException
	 */
	private void tryParseExpression(TimeDomain domain, String expr) throws KeyedException {
		String modifier = "";
		time = null;
		try {
			if (expr.length() > 10) {
				// day expression must be in format yyyy-mm-dd[+/-offset] even if domain not daily
				time = domain.time(expr.substring(0, 10), adjustment);
				modifier = expr.substring(10);
			} else {
				time = domain.time(expr, adjustment);
			}
			type = Type.LITERAL;
		} catch (KeyedException e) {
			modifier = parseKeyword(expr, e);
		}
		if (modifier.length() > 0) {
			try {
				offset = parseOffset(modifier);
			} catch (NumberFormatException e) {
				// could be a time with h:m:s precision...
				if (time != null) {
					try {
						time = domain.time(expr, adjustment);
						offset = 0;
					} catch (KeyedException e2) {
						// throw an error about the wrong offset
						throw T2Msg.exception(32102, modifier, expr);
					}
				}
			}
		} else
			offset = 0;
		applyOffset();
	}
	
	/**
	 * @param expression a non-null string
	 * @param originalException a keyed exception or null
	 * @return the modifier following the keyword or an empty string
	 * @throws KeyedException
	 */
	private String parseKeyword(String expression, KeyedException originalException) throws KeyedException {
		String modifier = "";
		String expr = expression.toLowerCase();
		if (expr.startsWith(TODAY)) {
			modifier = modifier(expr, TODAY.length());
			type = Type.TODAY;
		} else if (expr.startsWith(START)) {
			modifier = modifier(expr, START.length());
			type = Type.START;
		} else if (expr.startsWith(END)) {
			modifier = modifier(expr, END.length());
			type = Type.END;
		} else {
			if (originalException != null)
				throw originalException;
		}
		return modifier;
	}

	/**
	 * @param expr a non-null string
	 * @param length a number
	 * @return the substring after the given length
	 */
	private String modifier(String expr, int length) {
		if (expr.length() > length)
			return expr.substring(length);
		else
			return "";
	}
	
	/**
	 * @param expr a non-null string
	 * @return the number following + or - or 0
	 * @throws NumberFormatException
	 */
	private static int parseOffset(String expr) throws NumberFormatException {
		int offset = 0;
		if (expr.length() > 0) {
			int sign = 1;
			char signStr = expr.charAt(0);
			if (signStr == '+')
				;
			else if (signStr == '-')
				sign = -1;
			else 
				throw new NumberFormatException(expr);
			int plusIndex = expr.indexOf('+', 1);
			int minusIndex = expr.indexOf('-', 1);
			int index = -1;
			if (plusIndex < 0 && minusIndex < 0)
				;
			else if (plusIndex < 0)
				index = minusIndex;
			else if (minusIndex < 0)
				index = plusIndex;
			else
				index = Math.min(plusIndex, minusIndex);
			
			if (index < 0) {
				offset = sign * Integer.parseInt(expr.substring(1));
			} else {
				offset = sign * Integer.parseInt(expr.substring(1, index));
				offset = offset + parseOffset(expr.substring(index));
			}
		}
		return offset;
	}
	
	/**
	 * Enforce a valid range between the given day expression and this
	 * expression. It returns false if this expression or the begin expression
	 * is in the error state. Else it returns true. Other errors result in an
	 * exception.
	 * 
	 * @param domain
	 *            a non-null domain
	 * @param begin
	 *            a non-null day expression
	 * @param keepBegin
	 *            if true modify this object else modify the argument
	 * @return true if the range is valid
	 * @throws KeyedException
	 */
	protected boolean enforceValidRange(TimeDomain domain, DayExpression begin, boolean keepBegin) throws KeyedException {
		/*
		 * handle END-END START-START TODAY-TODAY LITERAL-LITERAL TODAY-LITERAL LITERAL-TODAY
		 * if ERROR or begin.ERROR: exception
		 * all other cases: do nothing
		 */
		if (type == Type.ERROR || begin.type == Type.ERROR)
			return false;
		
		if (type == begin.type) {
			eVRSameType(begin, keepBegin);
			return true;
		}
		if (type == Type.LITERAL && begin.type == Type.TODAY) {
			eVRTodayLiteral(domain, begin, keepBegin);
			return true;
		}
		if (type == Type.TODAY && begin.type == Type.LITERAL) {
			eVRLiteralToday(domain, begin, keepBegin);
			return true;
		}
		// all other cases, do nothing
		return true;
	}
	
	/**
	 * Enforce valid range when both expressions are of the same type.
	 * 
	 * @param begin 
	 * @param keepBegin
	 */
	private void eVRSameType(DayExpression begin, boolean keepBegin) {
		if (time == null) {
			// END, START, TODAY
			if (begin.offset > offset) {
				if (keepBegin)
					setOffset(begin.offset);
				else
					begin.setOffset(offset);
			}
		} else {
			// LITERAL
			if (begin.time.compareTo(time) > 0) {
				if (keepBegin)
					setTime(begin.time);
				else
					begin.setTime(time);
			}
		}
	}
	
	/**
	 * Enforce valid range when begin is a TODAY and end is a LITERAL.
	 * 
	 * @param begin 
	 * @param keepBegin
	 */
	private void eVRTodayLiteral(TimeDomain domain, DayExpression begin, boolean keepBegin) throws KeyedException {
		TimeIndex t = begin.getDate(domain);
		if (t.compareTo(time) > 0) {
			if (keepBegin)
				setTime(t);
			else {
				long diff = t.asLong() - time.asLong();
				long test = begin.offset + diff;
				if (Math.abs(test) <= Integer.MAX_VALUE)
					begin.incr((int)diff);
				else // change from TODAY to LITERAL
					begin.setTime(time);
			}
		}
	}

	/**
	 * Enforce valid range when begin is a LITERAL and end is a TODAY.
	 * 
	 * @param begin
	 * @param keepBegin
	 */
	private void eVRLiteralToday(TimeDomain domain, DayExpression begin, boolean keepBegin) throws KeyedException {
		TimeIndex t = getDate(domain);
		if (begin.time.compareTo(t) > 0) {
			if (keepBegin) {
				long diff = begin.time.asLong() - t.asLong();
				long test = offset + diff;
				if (Math.abs(test) <= Integer.MAX_VALUE)
					incr((int) diff);
				else
					// change from TODAY to LITERAL
					setTime(t);
			} else
				begin.setTime(t);
		}
	}


}
