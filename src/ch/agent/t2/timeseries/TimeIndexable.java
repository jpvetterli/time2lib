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
 * Package: ch.agent.t2.timeseries
 * Type: TimeIndexable
 * Version: 1.1.1
 */
package ch.agent.t2.timeseries;

import ch.agent.t2.T2Exception;
import ch.agent.t2.time.Range;

/**
 * A TimeIndexable is a {@link TimeAddressable} where the range size can be
 * stored in a 32 bit integer, and with a value for all time units in the
 * range. The values are either actual values or <em>missing values</em>.
 * The restriction on the range size makes it possible to store values in
 * structures indexed by 32 bit integers.
 * <p>
 * A TimeIndexable places restrictions on the length of a single run of missing
 * values. Any of the {@link TimeAddressable#put put} methods can throw an exception
 * when a certain threshold is exceeded. The threshold can be queried with
 * {@link TimeIndexable#getMaxGap()}. The maximum allowed gap can be modified for future
 * time series with {@link TimeSeriesFactory#setMaxGap(int)}.
 * <p>
 * The definitions of {@link #copy} and {@link TimeIndexable#makeEmptyCopy()} are
 * overridden to return a {@link TimeIndexable} instead of a {@link TimeAddressable}.
 *  
 * 
 * @author Jean-Paul Vetterli
 * @version 1.1.1
 * @param <T> the value type
 */
public interface TimeIndexable<T> extends TimeAddressable<T> {

	/**
	 * Return the maximum allowed length of a single run of missing values.
	 * 
	 * @return the maximum length of a run of missing values
	 */
	int getMaxGap();
	
	/**
	 * Return all the values in an array.
	 * 
	 * @return an array with all value
	 */
	T[] getArray();

	/**
	 * Return a subrange of values in an array.
	 * 
	 * @param range the non-null wanted range
	 * @return an array with a subrange of values
	 * @throws T2Exception
	 */
	T[] getArray(Range range) throws T2Exception;
	
	/**
	 * Fill holes in the time series with the given value and append a
	 * tail of the given length. A hole is a run of missing values. Return the
	 * number of missing values replaced. 
	 * <p>
	 * Note: the method can be used to count missing values by specify a missing
	 * value as replacement. In this case it is illegal to specify a positive
	 * tail length.
	 * 
	 * @param replacement the replacement value
	 * @param tailLength the length of the tail to append
	 * @return the number of values filled
	 * @throws T2Exception
	 */
	int fill(T replacement, long tailLength) throws T2Exception;
	
	/**
	 * Fill holes in the time series by repeating the last value before each
	 * hole and append a tail by repeating the last value a given number of
	 * times. Return the number of values filled and appended.
	 * 
	 * @param tailLength
	 *            the number of times the last value must be repeated
	 * @return the number of values added
	 */
	int fill(long tailLength);

	/**
	 * Fill holes using the given procedure.
	 * 
	 * @param filler a non-null filler
	 * @return the number of values filled
	 * @throws T2Exception
	 */
	int fill(Filler<T> filler) throws T2Exception;
	
	@Override
	TimeIndexable<T> copy() throws T2Exception;

	@Override
	TimeIndexable<T> makeEmptyCopy();

}
