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
 * Package: ch.agent.t2.timeseries
 * Type: TimeAddressable
 * Version: 1.0.1
 */
package ch.agent.t2.timeseries;

import ch.agent.core.KeyedException;
import ch.agent.t2.time.Range;
import ch.agent.t2.time.TimeDomain;
import ch.agent.t2.time.TimeIndex;

/**
 * A TimeAddressable is the most general time series, a collection of objects of
 * a given type, each one associated to a distinct time index. This definition
 * allows the elements of the collection to be irregularly spaced on the time
 * axis.
 * 
 * @author Jean-Paul Vetterli
 * @version 1.0.1
 * @param <T>
 *            the value type
 */
public interface TimeAddressable<T> extends Iterable<Observation<T>> {
	
	/**
	 * Return true if the time series implements {@link TimeIndexable}.
	 * 
	 * @return true if the time series is indexable
	 */
	boolean isIndexable();

	/**
	 * Return true if the given value is missing.
	 * 
	 * @return true if the argument is the missing value
	 */
	boolean isMissing(T value);
	
	/**
	 * Return the object representing missing values.
	 * 
	 * @return an object of the value type
	 */
	T getMissingValue();
	
	/**
	 * Return the value at the given numerical time index. Return the missing value object 
	 * when there is no value. There is no value either when the given index is out of range or
	 * when there is a hole in the range at the requested position.
	 * 
	 * @param index a number
	 * @return a value
	 * @throws KeyedException
	 */
	T get(long index) throws KeyedException;
	
	/**
	 * Return the value identified by the given time index. Return the missing value object 
	 * when there is no value. There is no value either when the given index is out of range or
	 * when there is a hole in the range at the requested position.
	 * 
	 * @param time a non-null time index 
	 * @return a value
	 * @throws KeyedException
	 */
	T get(TimeIndex time) throws KeyedException;
	
	/**
	 * Return the observation at or before the given time. When the argument
	 * is null, return the last observation. 
	 * Return null when the wanted observation cannot be found. 
	 * When not null, the result will not contain a missing value (this is a special rule
	 * of this method, not a general rule for Observation objects).
	 * 
	 * @param time a time index
	 * @return an observation or null
	 * @throws KeyedException
	 */
	Observation<T> getLast(TimeIndex time) throws KeyedException;
	
	/**
	 * Return the observation at or after the given time. When the argument
	 * is null, return the first observation. 
	 * Return null when the wanted observation cannot be found. 
	 * When not null, the result will not contain a missing value (this is a special rule
	 * of this method, not a general rule for Observation objects).
	 * 
	 * @param time a time index
	 * @return an observation or null
	 * @throws KeyedException
	 */
	Observation<T> getFirst(TimeIndex time) throws KeyedException;
	
	/**
	 * Return a new time series corresponding to the range between two numerical time indexes.
	 * The result is never null. When there is no data corresponding to the 
	 * arguments, an empty time series is returned.
	 *  
	 * @param first a numerical time index giving the lower bound of the wanted range
	 * @param last a numerical time index giving the upper bound of the wanted range
	 * @return a time series
	 * @throws KeyedException
	 */
	TimeAddressable<T> get(long first, long last) throws KeyedException;
	
	/**
	 * Return a time series constructed from the given range of this series.
	 * The result is never null. When there is no data corresponding to the 
	 * arguments, an empty time series is returned.
	 *  
	 * @param range a non-null range
	 * @return a time series
	 * @throws KeyedException
	 */
	TimeAddressable<T> get(Range range) throws KeyedException;
	
	/**
	 * Put a value identified by a time index into the time series. The value
	 * can only be null when null was defined to represent missing values in
	 * {@link TimeSeriesFactory}.
	 * 
	 * @param time
	 *            a non-null time index
	 * @param value
	 *            a value
	 * @throws KeyedException
	 */
	void put(TimeIndex time, T value) throws KeyedException;

	/**
	 * Put an array of values identified with time index of its first element
	 * into the time series. Values elements can only be null when null was
	 * defined to represent missing values in {@link TimeSeriesFactory}.
	 * 
	 * @param time
	 *            a non-null time index
	 * @param values
	 *            a non-null array of values
	 * @throws KeyedException
	 */
	void put(TimeIndex time, T[] values) throws KeyedException;
	
	/**
	 * Put a value identified by a numerical time index into the time series.
	 * The value can only be null when null was defined to represent missing
	 * values in {@link TimeSeriesFactory}.
	 * 
	 * @param time
	 *            a numerical time index
	 * @param value
	 *            a value
	 * @throws KeyedException
	 */
	void put(long time, T value) throws KeyedException;

	/**
	 * Put an array of values identified with the numerical time index of its
	 * first element into the time series. Values elements can only be null when
	 * null was defined to represent missing values in {@link TimeSeriesFactory}
	 * .
	 * 
	 * @param time
	 *            a numerical time index
	 * @param values
	 *            a non-null array of values
	 * @throws KeyedException
	 */
	void put(long time, T[] values) throws KeyedException;
	
	/**
	 * Conditionally put all values from another time series into this time series.
	 * The condition is that, if not null, the reviewer accepts all new values.
	 * With a null reviewer, all values are always accepted.
	 * <p>
	 * <b>Important note.</b> When the implementation of the <em>values</em> time series 
	 * does not explicitly represent missing values (see {@link SparseTimeSeries} for example), 
	 * this method cannot be relied upon for "deleting" values of the target.
	 * 
	 * @param values a non-null time series
	 * @param reviewer a reviewer
	 * @throws KeyedException
	 */
	void put(TimeAddressable<T> values, UpdateReviewer<T> reviewer) throws KeyedException;
	
	/**
	 * Remove the value identified by the time index. 
	 * 
	 * @param time a non-null time index
	 * 
	 * @throws KeyedException
	 */
	void remove(TimeIndex time) throws KeyedException;
	
	/**
	 * Return the number of values. Missing values are included. Note that in a sparse time
	 * series the result is not always the same as the range size.
	 * 
	 * @return the number of values
	 */
	int getSize();
	
	/**
	 * Return the number of non-missing values.
	 * 
	 * @return the number of non-missing values
	 */
	int getValueCount();
	
	/**
	 * Return the range of the time series. The range is the empty range when there is no data.
	 * Modifying the range returned by this method does not have any effect
	 * on the range of the time series.
	 * 
	 * @return the range
	 */
	Range getRange();

	/**
	 * Return the numerical time index of the first element, or -1 when there is no data.
	 * To get the time of the first element as a
	 * {@link TimeIndex}, use {@link #getRange()}.
	 * 
	 * @return the index of the first element
	 */
	long getFirstIndex();
	
	/**
	 * Return the numerical time index of the last element, or -1 when there is no data.
	 * To get the time of the last element as a
	 * {@link TimeIndex}, use {@link #getRange()}.
	 * 
	 * @return the index of the last element
	 */
	long getLastIndex();
	
	/**
	 * Set the range to the intersection of the current range with the given range. If the
	 * intersection is empty, all values are removed.  
	 * A null range parameter is interpreted as a request to clear all values.
	 * Returns true if the range was modified. 
	 * 
	 * @param range the new range will be the intersection of the current range with this one
	 * @return true if the range was modified
	 * @throws KeyedException
	 */
	boolean setRange(Range range) throws KeyedException;
	
	/**
	 * Return the time domain of the time series.
	 * 
	 * @return a time domain
	 */
	TimeDomain getTimeDomain();
	
	/**
	 * Return an indexable time series corresponding to this one. Return the
	 * time series itself if it is already indexable. If not, create an
	 * indexable time series using values of the current time series. The operation throws 
	 * an exception when a run of missing values exceeds a certain threshold, or
	 * if the range is too large to be represented with a 32 bit integer.
	 * 
	 * @return an indexable time series
	 * @throws KeyedException
	 */
	TimeIndexable<T> asIndexable() throws KeyedException;
	
	/**
	 * Return a copy of the time series.
	 * 
	 * @return a copy of the time series
	 * @throws KeyedException
	 */
	TimeAddressable<T> copy() throws KeyedException;
	
	/**
	 * Make a copy of the time series but without the values.
	 * 
	 * @return an empty copy of the time series
	 */
	TimeAddressable<T> makeEmptyCopy();

}

