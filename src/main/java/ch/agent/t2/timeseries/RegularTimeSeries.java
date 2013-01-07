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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import ch.agent.t2.T2Exception;
import ch.agent.t2.T2Msg;
import ch.agent.t2.T2Msg.K;
import ch.agent.t2.time.Range;
import ch.agent.t2.time.TimeDomain;

/**
 * RegularTimeSeries implements {@link TimeIndexable}.
 * <p>
 * <b>Nota bene.</b>
 * The implementation is not thread-safe.
 * 
 * @author Jean-Paul Vetterli
 * @param <T> the value type
 */
public class RegularTimeSeries<T> extends AbstractTimeSeries<T> implements TimeIndexable<T> {

	/**
	 * TimeSeriesIterator is an {@link Iterator} returning {@link Observation} objects.
	 */
	public class TimeSeriesIterator implements Iterator<Observation<T>> {

		private Iterator<T> dataIt;
		private long index;
		private TimeDomain domain;
		
		/**
		 * Construct an iterator using the index start and the data.
		 * @param start the numerical time index of the first element of data
		 * @param data the non-null list of values
		 */
		public TimeSeriesIterator(long start, List<T> data) {
			dataIt = data.iterator();
			domain = getTimeDomain();
			index = start;
		}

		@Override
		public boolean hasNext() {
			return dataIt.hasNext();
		}

		@Override
		public Observation<T> next() {
			Observation<T> obs = new Observation<T>(domain, index++, dataIt.next());
			return obs;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

	private List<T> data;
	private T[] template;
	private long start; // negative when no data
	private T[] empty;
	private int maxGap;
	
	/**
	 * The parameterless constructor is never used.
	 */
	@SuppressWarnings("unused")
	private RegularTimeSeries() {
	}
	
	/**
	 * Construct a regular time series.
	 * <p>
	 * The template argument is required for turning the generic collection into an array.
	 * 
	 * @param domain a non-null time domain
	 * @param template a non-null array, typically empty
	 * @param missingValue an object representing missing values for this value type
	 * @param maxGap the largest run of missing values allowed
	 */
	protected RegularTimeSeries(TimeDomain domain, T[] template, T missingValue, int maxGap) {
		super(domain, missingValue);
		this.data = new ArrayList<T>();
		this.start = -1;
		this.template = template;
		this.empty = new ArrayList<T>(0).toArray(template);
		this.maxGap = maxGap;
	}
	
	/**
	 * Construct a shorter view of a time series.
	 * 
	 * @param ts the non-null time series to copy 
	 * @param fromOffset start offset of the view (inclusive)
	 * @param toOffset  end offset of the view (exclusive)
	 * @throws T2Exception
	 */
	private RegularTimeSeries(RegularTimeSeries<T> ts, int fromOffset, int toOffset) {
		// no need to clone domain, template, empty, or missingValue
		super(ts.getTimeDomain(), ts.getMissingValue());
		this.template = ts.template;
		this.empty = ts.empty;
		this.maxGap = ts.maxGap;
		if (toOffset > fromOffset) {
			this.data = new ArrayList<T>(ts.data.subList(fromOffset, toOffset));
			this.start = ts.start + fromOffset;
		} else {
			this.data = new ArrayList<T>();
			this.start = -1;
		}
	}

	@Override
	public Iterator<Observation<T>> iterator() {
		return new TimeSeriesIterator(start, data);
	}
	
	@Override
	public TimeAddressable<T> get(Range range) throws T2Exception {
		getTimeDomain().requireEquality(range.getTimeDomain());
		if (range.isEmpty())
			return new RegularTimeSeries<T>(this, 0, -1);
		else
			return get(range.getFirstIndex(), range.getLastIndex());
	}

	@Override
	public TimeAddressable<T> get(long first, long last) throws T2Exception {
		if (first > last) {
			if (first == 0 && last == -1)
				return new RegularTimeSeries<T>(this, 0, -1);
			throw T2Msg.exception(K.T5016, getTimeDomain().time(first).toString(), 
					getTimeDomain().time(last).toString());
		}
		
		if (start < 0)
			return new RegularTimeSeries<T>(this, 0, -1);
		
		int fromOffset = offset(first, start);
		if (fromOffset < 0)
			fromOffset = 0;
		
		if (fromOffset >= data.size())
			return new RegularTimeSeries<T>(this, 0, -1);

		/* toOffset is the index of the element after the last one requested; 
		 * by construction, it can be cast to an int.
		 */ 
		long toOffset = fromOffset + last - first + 1; // can be < 0 due to overflow
		if (toOffset > data.size() || toOffset < 0)
			toOffset = data.size();

		// exclude missing values at the beginning of the series
		while (data.get(fromOffset) == getMissingValue()) {
			if (++fromOffset == toOffset)
				break;
		}
		if (fromOffset < toOffset) {
			// exclude missing values at the end of the series
			while (data.get((int) toOffset - 1) == getMissingValue() && fromOffset < toOffset) {
				toOffset--;
			}
		}
		
		return new RegularTimeSeries<T>(this, fromOffset, (int) toOffset);
	}
	
	@Override
	protected Observation<T> internalGetLast(long index) throws T2Exception {
		T value = get(index);
		if (isMissing(value)) {
			long last = internalGetLastIndex();
			if (last >= 0) {
				if (index > last) {
					value = get(last);
					index = last;
				} else {
					long first = internalGetFirstIndex();
					while (index > first && isMissing(value)) {
						value = get(--index);
					}
					// else a missing value is returned
				}
			}
		}
		if (isMissing(value))
			return null;
		else
			return new Observation<T>(getTimeDomain(), index, value);
	}
	
	@Override
	protected Observation<T> internalGetFirst(long index) throws T2Exception {
		T value = get(index);
		if (isMissing(value)) {
			long first = internalGetFirstIndex();
			if (first >= 0) {
				if (index < first) {
					value = get(first);
					index = first;
				} else {
					long last = internalGetLastIndex();
					while (index < last && isMissing(value)) {
						value = get(++index);
					}
					// else a missing value is returned
				}
			}
		}
		if (isMissing(value))
			return null;
		else 
			return new Observation<T>(getTimeDomain(), index, value);
	}

	@Override
	public int getValueCount() {
		T[] values = getArray();
		int valueCount = 0;
		for (T d: values)
			if (!isMissing(d))
				valueCount++;
		return valueCount;
	}

	@Override
	public boolean isIndexable() {
		return true;
	}

	@Override
	public int getMaxGap() {
		return maxGap;
	}

	@Override
	public TimeIndexable<T> makeEmptyCopy() {
		return new RegularTimeSeries<T>(getTimeDomain(), template, getMissingValue(), maxGap);
	}
	
	@Override
	public TimeIndexable<T> asIndexable() throws T2Exception {
		return this;
	}

	@Override
	public TimeIndexable<T> copy() throws T2Exception {
		TimeIndexable<T> ts = makeEmptyCopy();
		ts.put(this, null);
		return ts;
	}

	@Override
	public T[] getArray() {
		if (start < 0)
			return empty;
		return data.toArray(template);
	}
	
	@Override
	public T[] getArray(Range range) throws T2Exception {
		long first = range.getFirstIndex();
		long last = range.getLastIndex();
		if (start < 0)
			return empty;
		int firstOffset = offset(first, start);
		int lastOffset = offset(last, start);
		if (firstOffset >= data.size())
			return empty;
		if (lastOffset >= data.size())
			lastOffset = data.size() - 1;
		// subList() is a view but toArray() makes a copy
		return data.subList(firstOffset, lastOffset + 1).toArray(template);
	}
	
	@Override
	public void put(long index, T[] values) throws T2Exception {
		if (getSize() != 0)
			super.put(index, values);
		else {
			// optimized version
			// ensure all missing values are the same object and that there are no MVs at the boundaries
			int firstNonMissing = 0;
			int lastNonMissing = -1;
			for (int i = 0; i < values.length; i++) {
				values[i] = normalizeMissingValue(values[i]);
				if (isMissing(values[i])) {
					if (firstNonMissing == i)
						firstNonMissing = i + 1;
				} else
					lastNonMissing = i;
			}
			if (lastNonMissing < 0) {
				; // nothing to add
			} else if (firstNonMissing == 0 && lastNonMissing == values.length - 1) {
				data.addAll(0, Arrays.asList(values));
				start = index;
			} else {
				data.addAll(0, Arrays.asList(values).subList(firstNonMissing, lastNonMissing + 1));
				start = index + firstNonMissing;
			}
		}
	}

	@Override
	public void put(TimeAddressable<T> values, UpdateReviewer<T> reviewer) throws T2Exception {
		if (reviewer != null || getSize() != 0 || !values.isIndexable())
			super.put(values, reviewer);
		else {
			// optimized version
			data.addAll(0, ((AbstractTimeSeries<T>) values).internalGetData());
			start = ((AbstractTimeSeries<T>) values).internalGetFirstIndex();  // -1 when no data
		}
	}

	/**
	 * Append a value many times.
	 * 
	 * @param value a value
	 * @param repetitions a number
	 */
	private void append(T value, long repetitions) {
		if ((data.size() + repetitions) > Integer.MAX_VALUE)
			throw new RuntimeException("too many repetitions: " + repetitions);
		if (repetitions > 0) {
			while (repetitions > 0) {
				data.add(value);
				repetitions--;
			}
		}
	}
	
	@Override
	public int fill(T replacement, long tailLength) throws T2Exception {
		T mv = getMissingValue();
		
		if (replacement == null && mv != null)
			throw T2Msg.exception(K.T5015);
		
		if (replacement.equals(mv) && tailLength > 0) 
			throw T2Msg.exception(K.T5020);
		else
			replacement = mv;
			
		int count = 0;
		T[] val = getArray();
		if (val == null)
			return count;
		
		for (int i = 0; i < val.length; i++) {
			if (isMissing(val[i])) {
				val[i] = replacement;
				count++;
			}
		}
		if (count > 0) {
			data.clear();
			data.addAll(Arrays.asList(val));
		}
		if (tailLength > 0) {
			append(replacement, tailLength);
			count += tailLength;
		}
		return count;
	}
	
	@Override
	public int fill(long tailLength) {
		int count = 0;
		T[] val = getArray();
		if (val == null)
			return count;
		
		for (int i = 1; i < val.length; i++) {
			if (isMissing(val[i]) && !isMissing(val[i-1])) {
				val[i] = val[i-1];
				count++;
			}
		}
		if (count > 0) {
			data.clear();
			data.addAll(Arrays.asList(val));
		}
		if (tailLength > 0 && val.length > 0) {
			T replacement = val[val.length - 1];
			append(replacement, tailLength);
			count += tailLength;
		}
		return count;
	}
	
	@Override
	public int fill(Filler<T> interpolator) throws T2Exception {
		int count = 0;
		T[] val = getArray();
		if (val == null)
			return count;

		int mvStart = -1;
		for (int i = 0; i < val.length; i++) {
			if (isMissing(val[i])) {
				count++;
				if (mvStart == -1)
					mvStart = i;
			} else {
				if (mvStart > 0) {
					// i.e. don't interpolate when first element is a missing value
					try {
						interpolator.fillHole(val, mvStart - 1, i);
					} catch (Exception e) {
						Range range = new Range(getTimeDomain(), getFirstIndex() + mvStart,	getFirstIndex() + i - 1);
						throw T2Msg.exception(e, K.T5017, range.toString());
					}
					for (int j = mvStart; j < i; j++) {
						val[j] = normalizeMissingValue(val[j]);
					}
				}
				mvStart = -1;
			}
		}

		if (count > 0) {
			data.clear();
			data.addAll(Arrays.asList(val));
		}
		return count;
	}

	@Override
	protected void internalClear() {
		data.clear();
		start = -1;
	}

	@Override
	protected T internalGet(long index) throws T2Exception {
		if (start < 0)
			return getMissingValue();
		int offset = offset(index, start);
		if (offset < 0 || offset >= data.size())
			return getMissingValue();
		return data.get(offset);
	}

	@Override
	protected Collection<T> internalGetData() {
		return data;
	}

	@Override
	protected long internalGetFirstIndex() {
		return start;
	}

	@Override
	protected long internalGetLastIndex() {
		if (start < 0)
			return start;
		else
			return start + internalGetSize() - 1;
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
		
		if (start < 0) {
			if (!isMissing(value)) {
				// new series
				start = index;
				data.add(value);
			}
			return;
		}

		// series has data
		int offset = offset(index, start);
		
		if (offset >= 0 && offset < data.size()) {
			// update existing element
			// adding a missing value at the boundary reduces the range
			if (isMissing(value)) {
				if (offset == 0) {
					start++;
					data.remove(0);
					removeBeginningMissingValues();
				}
				if (offset == data.size() - 1) {
					data.remove(offset);
					removeEndingMissingValues();
				}
			} else {
				data.set(offset, value);
			}
		} else {
			// do not add missing values out of range
			if (isMissing(value)) {
				return;
			}
			// append new element to the ...
			if (offset < 0) {
				// ... left ...
				int padSize = -offset - 1;
				if (padSize > 0) {
					if (padSize > maxGap)
						throw T2Msg.exception(K.T5018, padSize, maxGap, getTimeDomain().time(index).toString());
					List<T> pad = new ArrayList<T>(padSize);
					for (int i = 0; i < padSize; i++)
						pad.add(getMissingValue());
					data.addAll(0, pad);
				}
				data.add(0, value);
				start = index;
			} else {
				// ... or to the right
				int padSize = offset - data.size();
				if (padSize > 0) {
					if (padSize > maxGap) // versions 1.26 to 1.49 had "padSize > 10* MAX_GAP" ???
						throw T2Msg.exception(K.T5019, padSize, maxGap, getTimeDomain().time(index).toString());
					List<T> pad = new ArrayList<T>(padSize);
					for (int i = 0; i < padSize; i++)
						pad.add(getMissingValue());
					data.addAll(pad);
				}
				data.add(value);
			}
		}
	}

	@Override
	protected void internalRemove(long index) throws T2Exception {
		internalPut(index, getMissingValue());
	}

	@Override
	protected void internalSetBounds(long first, long last) throws T2Exception {
		// - the range becomes smaller, so the long-to-int casts are okay by definition
		// - here it is okay to keep the subList because the original won't be used by anyone
		data = data.subList((int)(first - start), (int) (last - start + 1));
		start = first;
		removeBeginningMissingValues();
		removeEndingMissingValues();
	}
	
	/**
	 * Remove all missing values at the start of the series.
	 */
	private void removeBeginningMissingValues() {
		while(data.size() > 0 && isMissing(data.get(0))) {
			data.remove(0);
			start++;
		}
	}
	
	/**
	 * Remove all missing values at the end of the series.
	 */
	private void removeEndingMissingValues() {
		while(data.size() > 0 && isMissing(data.get(data.size() - 1))) {
			data.remove(data.size() - 1);
		}
	}
	
	/**
	 * Return a numerical time index as an offset from the start of the time series and
	 * ensure it fits in a 32 bit integer.
	 * 
	 * @param index a numerical time index
	 * @param start the numerical time index of the start of the series
	 * @return the difference between index and start
	 * @throws T2Exception
	 */
	private int offset(long index, long start) throws T2Exception {
		long offset = index - start;
		if (offset < Integer.MIN_VALUE || offset > Integer.MAX_VALUE)
			throw T2Msg.exception(K.T1058, index, start);
		return (int) offset;
	}
	
}
