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
package ch.agent.t2.timeseries;

import ch.agent.t2.time.TimeDomain;
import ch.agent.t2.time.TimeIndex;

/**
 * An Observation is an element of a time series. It associates a value with a
 * time index. Depending on the implementation, time series iterators skip
 * missing values or return Observations where the value is a special object
 * signaling a missing value. This special object can be null.
 * <p>
 * The name <q>observation</q> is traditionally used to denote an element of a time
 * series in statistics.
 * 
 * @author Jean-Paul Vetterli
 * @param <T> the value type
 */
public class Observation<T> {

	private TimeDomain domain;
	private long index;
	private T value;
	
	/**
	 * Construct an observation.
	 * 
	 * @param domain a non-null domain
	 * @param index a non-negative number
	 * @param value a value 
	 */
	protected Observation(TimeDomain domain, long index, T value) {
		this.domain = domain;
		this.index = index;
		this.value = value;
	}
	
	/**
	 * Construct an observation with a time index.
	 * 
	 * @param time a non-null time index
	 * @param value a value
	 */
	public Observation(TimeIndex time, T value) {
		this.domain = time.getTimeDomain();
		this.index = time.asLong();
		this.value = value;
	}
	
	/**
	 * Return the numerical time index.
	 * 
	 * @return a number
	 */
	public long getIndex() {
		return index;
	}
	
	/**
	 * Return the time index.
	 * 
	 * @return the time index
	 */
	public TimeIndex getTime() {
		return domain.time(index);
	}
	
	/**
	 * Return the time domain.
	 * 
	 * @return the time domain
	 */
	public TimeDomain getDomain() {
		return domain;
	}

	/**
	 * Return the value.
	 * 
	 * @return the value
	 */
	public T getValue() {
		return value;
	}

	@Override
	public String toString() {
		return getTime().toString() + "=" + (value == null ? value : value.toString());
	}
	
}
