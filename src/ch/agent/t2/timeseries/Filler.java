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
 * Type: Filler
 * Version: 1.0.0
 */
package ch.agent.t2.timeseries;

/**
 * Filler defines an interface for filling holes in a time series.
 *
 * @author Jean-Paul Vetterli
 * @version 1.0.0
 * @param <T>
 */
public interface Filler<T> {

	/**
	 * Fill the hole using some procedure. The first and last indexes
	 * are guaranteed to be in the range of the val array.
	 * <p>
	 * A typical procedure for numerical time series is interpolation.
	 * 
	 * @param val
	 *            a non-null array of values with a hole
	 * @param first
	 *            index of last non-missing value before hole in val
	 * @param last
	 *            index of first non-missing value after hole in val
	 */
	void fillHole(T[] val, int first, int last);
	
}
