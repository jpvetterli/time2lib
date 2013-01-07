/*
 *   Copyright 2011-2013 Hauser Olsson GmbH
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
package ch.agent.t2.timeseries;

import java.util.Collection;
import java.util.Iterator;

import ch.agent.t2.T2Exception;
import ch.agent.t2.T2Msg;
import ch.agent.t2.T2Msg.K;
import ch.agent.t2.time.Range;
import ch.agent.t2.time.TimeDomain;
import ch.agent.t2.time.TimeIndex;


/**
 * AbstractTimeseries is the basis for {@link RegularTimeSeries} and {@link SparseTimeSeries}.
 *
 * @author Jean-Paul Vetterli
 * @param <T>
 */
public abstract class AbstractTimeSeries<T> implements TimeAddressable<T> {

	private TimeDomain domain;
	
	private T missingValue; // for each type, there is a single object (can use ==)

	/**
	 * The parameterless constructor always throws an UnsupportedOperationException.
	 */
	protected AbstractTimeSeries() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Construct a time series with the given time domain and missing value.
	 * 
	 * @param domain a non-null time domain
	 * @param missingValue the object to use for representing missing values
	 */
	public AbstractTimeSeries(TimeDomain domain, T missingValue) {
		if (domain == null)
			throw new IllegalArgumentException("calendar null");
		this.domain = domain;
		this.missingValue = missingValue;
	}

	@Override
	public boolean isMissing(T value) {
		return value == missingValue;
	}

	@Override
	public TimeAddressable<T> copy() throws T2Exception {
		TimeAddressable<T> ts = makeEmptyCopy();
		ts.put(this, null);
		return ts;
	}
	
	@Override
	public T get(long t) throws T2Exception {
		return internalGet(t);
	}
	
	@Override
	public abstract TimeAddressable<T> get(long first, long last) throws T2Exception;
	
	@Override
	public T get(TimeIndex t) throws T2Exception {
		if (!domain.equals(t.getTimeDomain()))
			throw T2Msg.exception(K.T5011);
		return get(t.asLong());
	}

	@Override
	public TimeDomain getTimeDomain() {
		return domain;
	}

	@Override
	public Range getRange() {
		// do not cache the (immutable) range object, the actual range can change
		if (getSize() < 1) 
			return new Range(domain);
		else
			return new Range(domain, internalGetFirstIndex(), internalGetLastIndex()); 
	}
	
	@Override
	public long getFirstIndex() {
		return internalGetFirstIndex();
	}

	@Override
	public long getLastIndex() {
		return internalGetLastIndex();
	}
	
	@Override
	public Observation<T> getLast(TimeIndex t) throws T2Exception {
		if (t == null) {
			long l = internalGetLastIndex();
			return l >= 0 ? internalGetLast(l) : null;
		} else {
			if (!t.getTimeDomain().equals(getTimeDomain()))
				throw T2Msg.exception(K.T5013, t.toString(), t.getTimeDomain().getLabel(), 
						getTimeDomain().getLabel());
			return internalGetLast(t.asLong());
		}
	}
	
	@Override
	public Observation<T> getFirst(TimeIndex t) throws T2Exception {
		if (t == null) {
			long f = internalGetFirstIndex();
			return f >= 0 ? internalGetFirst(f) : null;
		} else {
			if (!t.getTimeDomain().equals(getTimeDomain()))
				throw T2Msg.exception(K.T5013, t.toString(), t.getTimeDomain().getLabel(), 
						getTimeDomain().getLabel());
			return internalGetFirst(t.asLong());
		}
	}

	@Override
	public int getSize() {
		return internalGetSize();
	}
	
	@Override
	public void put(long index, T value) throws T2Exception {
		internalPut(index, value);

	}
	
	@Override
	public void put(long index, T[] values) throws T2Exception {
		if (values.length == 0)
			return;
		// add last value first so resizing occurs at most once 
		internalPut(index + values.length - 1, values[values.length - 1]);
		for (int i = 0; i < values.length - 1; i++) {
			internalPut(index++, values[i]);
		}
	}
	
	@Override
	public void put(TimeAddressable<T> values, UpdateReviewer<T> reviewer) throws T2Exception {
		// check for domain compatibility, then for Integer overflow 
		getRange().union(values.getRange()).getSizeAsInt();
		
		int rejectCount = accept(values, reviewer);
		if (rejectCount > 0)
			throw T2Msg.exception(K.T5012, rejectCount, values.getSize());
		
		if (values.isIndexable()) {
			put(values.getFirstIndex(), ((TimeIndexable<T>)values).getArray());
		} else {
			Iterator<Observation<T>> it = values.iterator();
			while (it.hasNext()) {
				Observation<T> obs = it.next();
				internalPut(obs.getIndex(), obs.getValue());
			}
		}
	}
	
	@Override
	public void put(TimeIndex t, T value) throws T2Exception {
		if (!domain.equals(t.getTimeDomain()))
			throw T2Msg.exception(K.T5011);
		internalPut(t.asLong(), value);
	}

	@Override
	public void put(TimeIndex t, T[] values) throws T2Exception {
		if (!domain.equals(t.getTimeDomain()))
			throw T2Msg.exception(K.T5011);
		put(t.asLong(), values);
	}

	@Override
	public void remove(TimeIndex t) throws T2Exception {
		if (!domain.equals(t.getTimeDomain()))
			throw T2Msg.exception(K.T5011);
		internalRemove(t.asLong());
	}

	@Override
	public boolean setRange(Range range) throws T2Exception {
		Range current = getRange();
		if (current.isEmpty())
			return false;
		if (current.equals(range))
			return false;
		if (range == null) {
			internalClear();
			return true;
		}
		Range inter = current.intersection(range);
		if (inter.isEmpty()) {
			internalClear();
			return true;
		} else if (inter.equals(current)) {
			return false;
		} else {
			internalSetBounds(inter.getFirstIndex(), inter.getLastIndex());
			return true;
		}

	}
	
	@Override
	public T getMissingValue() {
		return missingValue;
	}

	@Override
	public String toString() {
		StringBuffer b = new StringBuffer();
		long last = getLastIndex();
		int count = 0;
		int max = 3;
		for (Observation<T> o : this) {
			if (count < max || o.getIndex() == last) {
				b.append(o.toString());
				if (o.getIndex() != last)
					b.append(", ");
			} else if (count == max) {
				b.append("..., ");
			}
			++count;
		}
		return b.toString();
	}

	/**
	 * Replace all objects equal to the missing value with the "official"
	 * missing value object.
	 * <p>
	 * Null values are illegal and produce an exception, unless they represent
	 * missing values.
	 * 
	 * @param value
	 *            the value to normalize
	 * @return the value, normalized
	 * @throws T2Exception
	 */
	protected T normalizeMissingValue(T value) throws T2Exception {
		if (value == null) {
			if (missingValue == null)
				return value;
			else
				throw T2Msg.exception(K.T5014);
		} else {
			if (value.equals(missingValue))
				return missingValue;
			else
				return value;
		}
	}

	/**
	 * Return the number of updates rejected by the reviewer. A result of 0 means all
	 * updates have been accepted
	 * 
	 * @param updates a non-null time series of updates
	 * @param reviewer a non-null reviewer
	 * @return the number of updates rejected
	 * @throws T2Exception
	 */
	protected int accept(TimeAddressable<T> updates, UpdateReviewer<T> reviewer) throws T2Exception {
		int rejected = 0;
		if (reviewer != null) {
			for (Observation<T> update : updates) {
				try {
					if (!reviewer.accept(this, update.getIndex(), update.getValue()))
						rejected++;
				} catch (Exception e) {
					throw T2Msg.exception(e, K.T5031, update);
				}
			}
		}
		return rejected;
	}
	
	/**
	 * Return the numerical time index of the first element.
	 *  
	 * @return a numerical time index
	 */
	protected abstract long internalGetFirstIndex();
	
	/**
	 * Return the numerical time index of the last element.
	 *  
	 * @return a numerical time index
	 */
	protected abstract long internalGetLastIndex();

	/**
	 * Return the observation at or after the given numerical time index. Return
	 * null when there is no such observation. When the result is not null, its
	 * value will not be a missing value (this is not a general rule for
	 * {@link Observation}, only a special rule for this method).
	 * 
	 * @param index a number
	 * @return an observation or null
	 * @throws T2Exception
	 */
	protected abstract Observation<T> internalGetFirst(long index) throws T2Exception;
	
	/**
	 * Return the observation at or before the given numerical time index. Return
	 * null when there is no such observation. When the result is not null, its
	 * value will not be a missing value (this is not a general rule for
	 * {@link Observation}, only a special rule for this method).
	 * 
	 * @param time a numerical time index
	 * @return an observation or null
	 * @throws T2Exception
	 */
	protected abstract Observation<T> internalGetLast(long time) throws T2Exception;

	/**
	 * Remove all values from the time series.
	 */
	protected abstract void internalClear();
	
	/**
	 * Get the value identified by the given numerical time index.
	 * 
	 * @param time a numerical time index
	 * @return a value
	 * @throws T2Exception
	 */
	protected abstract T internalGet(long time) throws T2Exception;
	
	/**
	 * Return the used size of the data structure where values are stored. 
	 * @return the used size of the data structure with the values 
	 */
	protected abstract int internalGetSize();

	/**
	 * Put the value into the series at the given numerical time index.
	 * <p>
	 * The method silently ignores missing values added out of range. Adding
	 * missing values at the boundary shrinks the range.
	 * 
	 * @param time a numerical time index
	 * @param value a value
	 * @throws T2Exception
	 */
	protected abstract void internalPut(long time, T value) throws T2Exception;
	
	/**
	 * Remove the value at the given numerical time index.
	 * 
	 * @param time a numerical time index
	 * @throws T2Exception
	 */
	protected abstract void internalRemove(long time) throws T2Exception;
	
	/**
	 * Shrink the range to the one given by the two numerical time indexes. The
	 * method assumes that the new range lies within the current range. Not
	 * playing by the rules is likely to result in a RuntimeException being
	 * thrown.
	 * 
	 * @param first
	 *            a numerical time index giving the lower bound of the new range
	 * @param last
	 *            a numerical time index giving the upper bound of the new range
	 * @throws T2Exception
	 */
	protected abstract void internalSetBounds(long first, long last) throws T2Exception;

	/**
	 * Return all values as a {@link Collection}.
	 * 
	 * @return all values as a collection
	 */
	protected abstract Collection<T> internalGetData();

}
