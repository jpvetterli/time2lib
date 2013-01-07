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

/**
 * UpdateReviewer provides an interface for implementing update policies for time series.
 *
 * @author Jean-Paul Vetterli
 * @param <T>
 */
public interface UpdateReviewer<T> {

	/**
	 * Returns true if the new value is acceptable as a replacement for the old one.
	 * The method does not perform the update itself.
	 * 
	 * @param series the series to be updated
	 * @param index the index of the value to be updated
	 * @param newValue the new value
	 * @return true if the update is accepted
	 * @throws Exception
	 */
	boolean accept(TimeAddressable<T> series, long index, T newValue) throws Exception;

}
