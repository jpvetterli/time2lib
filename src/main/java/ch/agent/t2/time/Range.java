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
 * Type: Range
 * Version: 1.0.2
 */
package ch.agent.t2.time;

import java.util.Iterator;
import java.util.NoSuchElementException;

import ch.agent.t2.T2Exception;
import ch.agent.t2.T2Msg;
import ch.agent.t2.T2Msg.K;

/**
 * A Range is an immutable time interval in a {@link TimeDomain}. A range can be empty.
 * When not empty, both ends of the interval are well-defined. Open intervals
 * and semi-open intervals are not supported.
 * <p>
 * A Range is an immutable object.
 * 
 * @author Jean-Paul Vetterli
 * @version 1.0.2
 */
public class Range implements Iterable<TimeIndex> {

	private int hash; // a Range is immutable, so hash must be computed only once

	/**
	 * RangeIterator is a {@link Iterator} return TimeIndex objects.
	 */
	public class RangeIterator implements Iterator<TimeIndex> {

		private Range range;
		private TimeIndex next;
		public RangeIterator(Range range) {
			this.range = range;
			if (range.isEmpty())
				this.next = null;
			else
				this.next = range.getFirst(); // no worry, a time is immutable
		}
		
		@Override
		public boolean hasNext() {
			return next != null && next.asLong() <= range.getLastIndex();
		}

		@Override
		public TimeIndex next() {
			if (!hasNext())
				throw new NoSuchElementException();
			TimeIndex result = next;
			try {
				next = next.add(1);
			} catch (T2Exception e) {
				next = null;
			}
			return result;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}
	
	private TimeDomain domain;
	private long first;
	private long last;
	/**
	 * Construct for an empty range.
	 * <p>
	 * @param domain a non null domain
	 */
	public Range(TimeDomain domain) {
		if (domain == null)
			throw new IllegalArgumentException("domain null");
		this.domain = domain;
		first = 2;
		last = 1;
		normalize();
	}
	
	/**
	 * Construct a range as a copy of the argument.
	 * 
	 * @param range a non-null range
	 */
	public Range(Range range) {
		if (range == null)
			throw new IllegalArgumentException("range null");
		this.domain = range.domain;
		this.first = range.first;
		this.last = range.last;
	}
	
	/**
	 * Construct a range with bounds specified with numerical time indexes,.
	 * Specifying a first bound past the second results in an empty range.
	 * 
	 * @param domain a non-null domain
	 * @param first the lower bound
	 * @param last the upper bound
	 * @throws T2Exception
	 */
	public Range(TimeDomain domain, long first, long last) {
		this(domain);
		this.first = first;
		this.last = last;
		normalize();
	}
	
	/**
	 * Construct a range with bounds specified as times.
	 * Specifying a first bound past the second results in an empty range.
	 * 
	 * @param first a non-null lower bound
	 * @param last a non-null upper bound
	 * @throws T2Exception
	 */
	public Range(TimeIndex first, TimeIndex last) throws T2Exception {
		this(first.getTimeDomain());
		if (!domain.equals(last.getTimeDomain())) {
			throw T2Msg.exception(K.T5009, first.toString(), last.toString(),
					first.getTimeDomain().getLabel(), last.getTimeDomain().getLabel());
		}
		this.first = first.asLong();
		this.last = last.asLong();
		normalize();
	}

	/**
	 * Construct a range with bounds specified as date strings.
	 * Bounds are adjusted if necessary and if allowed by the adjust parameter.
	 * The range will be smaller (larger) ifO adjustment DOWN (UP) is specified.
	 * An exception is thrown if adjusting is required but adjustment NONE is
	 * specified.
	 * <p>
	 * Specifying a first bound past the second results in an empty range.
	 * 
	 * @param domain a non-null domain
	 * @param firstDate a non-null lower bound
	 * @param lastDate a non-null upper bound
	 * @param adjust a non-null adjustment mode
	 * @throws T2Exception
	 */
	public Range(TimeDomain domain, String firstDate, String lastDate, Adjustment adjust) throws T2Exception {
		this(domain);
		Adjustment firstAdjust = Adjustment.NONE;
		Adjustment lastAdjust = Adjustment.NONE;
		switch (adjust) {
		case NONE:
			break;
		case UP:
			// make range larger if necessary
			firstAdjust = Adjustment.DOWN;
			lastAdjust = Adjustment.UP;
			break;
		case DOWN:
			// make range smaller if necessary
			firstAdjust = Adjustment.UP;
			lastAdjust = Adjustment.DOWN;
			break;
		default:
			throw new RuntimeException("bug: " + adjust.name());
		}
		first = domain.time(firstDate, firstAdjust).asLong();
		last = domain.time(lastDate, lastAdjust).asLong();
		normalize();
	}
	
	/**
	 * Convert the range to the domain specified. The range will be made smaller
	 * (larger) if adjustment DOWN (UP) is specified. An exception is thrown if
	 * adjusting is required but adjustment NONE is specified.
	 * <p>
	 * If the domain is the same, returns this range, else a new range is
	 * created.
	 * 
	 * @param domain a non-null domain
	 * @param adjust a non-null adjustment mode
	 * @return a range
	 * @throws T2Exception
	 */
	public Range convert(TimeDomain domain, Adjustment adjust) throws T2Exception {
		if (this.domain.equals(domain))
			return this;
		Adjustment firstAdjust = Adjustment.NONE;
		Adjustment lastAdjust = Adjustment.NONE;
		switch (adjust) {
		case NONE:
			break;
		case UP:
			// make range larger if necessary
			firstAdjust = Adjustment.DOWN;
			lastAdjust = Adjustment.UP;
			break;
		case DOWN:
			// make range smaller if necessary
			firstAdjust = Adjustment.UP;
			lastAdjust = Adjustment.DOWN;
			break;
		default:
			throw new RuntimeException("bug: " + adjust.name());
		}
		Range r = new Range(domain);
		if (!isEmpty()) {
			r.first = getFirst().convert(domain, firstAdjust).asLong();
			r.last = getLast().convert(domain, lastAdjust).asLong();
		}
		return r;
	}

	@Override
	public Iterator<TimeIndex> iterator() {
		return new RangeIterator(this);
	}
	
	/**
	 * Set standard bounds for an empty range.
	 */
	private void normalize() {
		if (getSize() <= 0) {
			first = 0;
			last = -1;
		}
	}
	
	/**
	 * Return the lower bound as a numerical time index.
	 * @return the lower bound
	 */
	public long getFirstIndex() {
		return first;
	}

	/**
	 * Return the upper bound as a numerical time index.
	 * @return the upper bound
	 */
	public long getLastIndex() {
		return last;
	}

	/**
	 * Get the size of the range as an integer and check for overflow.
	 * 
	 * @return the range size as an integer
	 * @throws T2Exception
	 */
	public int getSizeAsInt() throws T2Exception {
		long size = getSize();
		if (size > Integer.MAX_VALUE)
			throw T2Msg.exception(K.T5008, domain.time(first).toString(),	domain.time(last).toString());
		return (int) size;
	}

	/**
	 * Return the range size.
	 * @return the size
	 */
	public long getSize() {
		return getLastIndex() - getFirstIndex() + 1;
	}

	/**
	 * Return the time domain.
	 * 
	 * @return the time domain
	 */
	public TimeDomain getTimeDomain() {
		return domain;
	}

	/**
	 * Return true if the range is empty, else false.
	 * 
	 * @return true if the range is empty
	 */
	public boolean isEmpty() {
		return first > last;
	}
	
	/**
	 * Return true if the numerical time index is within range.
	 * 
	 * @param index a numerical time index
	 * @return true if the time is in the range
	 */
	public boolean isInRange(long index) {
		return index >= first && index <= last;
	}
	
	/**
	 * Return true if the time is within range.
	 * The domains must be equal.
	 * 
	 * @param time a non-null time 
	 * @return true if the time is in the range
	 */
	public boolean isInRange(TimeIndex time) throws T2Exception {
		getTimeDomain().requireEquality(time.getTimeDomain());
		return isInRange(time.asLong());
	}
	
	/**
	 * Return true if another range is within range.
	 * This is the case if both ends of the other range
	 * are within this range. The domains must be equal.
	 * 
	 * @param range a non-null range
	 * @return true if the other range is in the range
	 */
	public boolean isInRange(Range range) throws T2Exception {
		getTimeDomain().requireEquality(range.getTimeDomain());
		return isInRange(range.first) && isInRange(range.last);
	}
	
	/**
	 * Return the lower bound as a time or null when the range is empty.
	 * @return the lower bound or null
	 */
	public TimeIndex getFirst() {
		if (first > last)
			return null;
		return domain.time(first);
	}
	
	/**r
	 * Return the upper bound as a time o null when the range is empty.
	 * @return the upper bound or null
	 */
	public TimeIndex getLast() {
		if (first > last)
			return null;
		return domain.time(last);
	}
	
	/**
	 * Return a new range which is the union with the argument.
	 * 
	 * @param range a non-null range
	 * @return the union of both ranges
	 * @throws T2Exception
	 */
	public Range union(Range range) throws T2Exception {
//		range = range.convert(domain); ... no magic please
		getTimeDomain().requireEquality(range.getTimeDomain());
		Range union = null;
		if (range.isEmpty())
			union = new Range(this);
		else if (isEmpty())
			union = new Range(range);
		else {
			long t1 = Math.min(first, range.getFirstIndex());
			long t2 = Math.max(last, range.getLastIndex());
			union = new Range(domain, t1, t2);
		}
		return union;
	}
	
	/**
	 * Return a new range which is the intersection with the argument. The
	 * result can be an empty range.
	 * 
	 * @param range a non-null range
	 * @return the intersection of both ranges
	 * @throws T2Exception
	 */
	public Range intersection(Range range) throws T2Exception {
//		range = range.convert(domain); ... no magic please
		getTimeDomain().requireEquality(range.getTimeDomain());
		Range inter = null;
		if (isEmpty() || range.isEmpty() || range.getFirstIndex() > last || first > range.getLastIndex()) {
			inter = new Range(domain);
		} else {
			long t1 = Math.max(first, range.getFirstIndex());
			long t2 = Math.min(last, range.getLastIndex());
			inter = new Range(domain, t1, t2);
		}
		return inter;
	}
	
	@Override
	public String toString() {
		if (isEmpty()) {
			return "[]";
		} else {
			return "[" + getFirst().toString() + ", " + getLast().toString() + "]";
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Range))
			return false;
		Range r = (Range) obj;
		if (!domain.equals(r.getTimeDomain()))
			return false;
		return (first == r.getFirstIndex() && last == r.getLastIndex());
	}
	
	@Override
	public int hashCode() {
		// note: choice of 31 inspired by String 
		if (hash == 0) {
			hash = domain.hashCode();
			hash = 31 * hash + (new Long(first)).hashCode();
			hash = 31 * hash + (new Long(last)).hashCode();
		}
		return hash;
	}

}
