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
import java.util.SortedMap;
import java.util.TreeMap;

import ch.agent.t2.T2Exception;
import ch.agent.t2.time.Range;
import ch.agent.t2.time.TimeDomain;

/**
 * SparseTimeSeries implements {@link TimeAddressable}. Missing values are never
 * stored in a sparse time series, but nulls can be stored, unless they are used
 * for representing missing values.
 * <p>
 * This implementation provides no synchronization.
 * 
 * @author Jean-Paul Vetterli
 * @param <T>
 */
public class SparseTimeSeries<T> extends AbstractTimeSeries<T> implements TimeAddressable<T> {

	/**
	 * IrregularTimeSeriesIterator is an {@link Iterator} returning
	 * {@link Observation} objects.
	 */
	public class IrregularTimeSeriesIterator implements Iterator<Observation<T>> {

		private Iterator<Long> keysIt;
		private TimeDomain domain;
		
		/**
		 * Construct a IrregularTimeSeriesIterator.
		 */
		public IrregularTimeSeriesIterator() {
			keysIt = data.keySet().iterator();
			domain = getTimeDomain();
		}

		@Override
		public boolean hasNext() {
			return keysIt.hasNext();
		}

		@Override
		public Observation<T> next() {
			Long k = keysIt.next();
			Observation<T> obs = new Observation<T>(domain, k, data.get(k));
			return obs;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	private SortedMap<Long,T> data;
	
	/**
	 * Construct a sparse time series with the given type, time domain and missing
	 * value.
	 * 
	 * @param type
	 *            a non-null data type
	 * @param domain
	 *            a non-null time domain
	 * @param missingValue
	 *            an object representing missing values
	 */
	public SparseTimeSeries(Class<T> type, TimeDomain domain, T missingValue) {
		super(type, domain, missingValue);
		data = new TreeMap<Long,T>();
	}
	
	/**
	 * Construct a sparse time series with the given type and time domain. The
	 * missing value object is {@link Double#NaN} for type {@link Double} and null
	 * for all other types.
	 * 
	 * @param type
	 *            a non-null data type
	 * @param domain
	 *            a non-null time domain
	 */
	@SuppressWarnings("unchecked")
	public SparseTimeSeries(Class<T> type, TimeDomain domain) {
		this(type, domain, (T) (type == Double.class ? Double.NaN : null));
	}
	
	/**
	 * Construct a sparse time series as a subrange of another sparse series 
	 * specified by two numerical time indexes. 
	 * @param ts a sparse time series
	 * @param first a numerical time index giving the start of the range
	 * @param last a numerical time index giving the end of the range
	 */
	private SparseTimeSeries(SparseTimeSeries<T> ts, long first, long last) {
		super(ts.getType(), ts.getTimeDomain(), ts.getMissingValue());
		// reallocate to avoid submap problems when inserting out of range
		// take care of wrap around, last = Long#MAX_VALUE is used to indicate open range
		if (last + 1 < last)
			this.data =  new TreeMap<Long, T>(ts.data.tailMap(first));
		else
			this.data =  new TreeMap<Long, T>(ts.data.subMap(first, last + 1));
	}
	
	@Override
	public Iterator<Observation<T>> iterator() {
		return new IrregularTimeSeriesIterator();
	}
	
	@Override
	public TimeAddressable<T> makeEmptyCopy() {
		return new SparseTimeSeries<T>(getType(), getTimeDomain(), getMissingValue());
	}

	@Override
	public TimeIndexable<T> asIndexable() throws T2Exception {
		TimeIndexable<T> ts = new RegularTimeSeries<T>(getType(), getTimeDomain(), getMissingValue());
		ts.put(this, null);
		return ts;
	}

	@Override
	public TimeAddressable<T> get(long first, long last) {
		if (first > last)
			last = first - 1; // to get an empty result instead of an exception
		return new SparseTimeSeries<T>(this, first, last);
	}
		
	@Override
	public TimeAddressable<T> get(Range range) throws T2Exception {
		getTimeDomain().requireEquality(range.getTimeDomain());
		if (range.isEmpty())
			return new SparseTimeSeries<T>(getType(), getTimeDomain(), getMissingValue());
		else
			return get(range.getFirstIndex(), range.getLastIndex()); 
	}

	@Override
	protected Observation<T> internalGetLast(long index) throws T2Exception {
		if (data.isEmpty() || data.firstKey() > index)
			return null;
		// headMap does not include the high point in the result
		Long upperBound = index + 1;
		// CAUTION: if data is already a submap, the key must lie within, else exception
		if (upperBound > data.lastKey())
			upperBound = data.lastKey() + 1;
		SortedMap<Long, T> subMap = data.headMap(upperBound);
		if (subMap.size() == 0)
			return null;
		else {
			Long last = subMap.lastKey();
			return new Observation<T>(getTimeDomain(), last, subMap.get(last));
		}
	}
	
	@Override
	protected Observation<T> internalGetFirst(long index) throws T2Exception {
		if (data.isEmpty() || data.lastKey() < index)
			return null;
		Long lowerBound = index;
		// Caution: if data is already a submap, the key must lie within
		if (lowerBound < data.firstKey())
			lowerBound = data.firstKey();
		SortedMap<Long, T> subMap = data.tailMap(lowerBound);
		if (subMap.size() == 0)
			return null;
		else {
			Long last = subMap.firstKey();
			return new Observation<T>(getTimeDomain(), last, subMap.get(last));
		}
	}

	@Override
	public int getValueCount() {
		return getSize(); // no concept of missing value here
	}

	@Override
	public boolean isIndexable() {
		return false;
	}
	
	@Override
	protected void internalClear() {
		data.clear();
	}

	@Override
	protected T internalGet(long index) throws T2Exception {
		T result = data.get(index);
		if (result == null)
			return  getMissingValue();
		else
			return result;
	}

	@Override
	protected Collection<T> internalGetData() {
		return data.values();
	}

	@Override
	protected long internalGetFirstIndex() {
		if (data.size() == 0)
			return -1;
		else
			return data.firstKey();
	}

	@Override
	protected long internalGetLastIndex() {
		if (data.size() == 0)
			return -1;
		else
			return data.lastKey();
	}
	@Override
	protected int internalGetSize() {
		return data.size();
	}
	
	@Override
	protected void internalPut(long index, T value) throws T2Exception {
		if (index < 0)
			throw new IllegalArgumentException("index < 0");
		value = normalizeMissingValue(value);
		if (isMissing(value))
			internalRemove(index);
		else
			data.put(index, value);
	}

	@Override
	protected void internalRemove(long index) throws T2Exception {
		data.remove(index);
	}

	@Override
	protected void internalSetBounds(long first, long last) throws T2Exception {
		// reallocate to avoid submap problems when inserting out of range
		data = new TreeMap<Long, T>(data.subMap(first, last + 1));
	}
}
