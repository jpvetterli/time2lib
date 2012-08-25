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
 * Type: BasePeriodPattern
 * Version: 1.0.1
 */
package ch.agent.t2.time;

import ch.agent.t2.T2Exception;

/**
 * A BasePeriodPattern defines a repeating pattern of time points.
 * The pattern defines which time points are ON and which are OFF in a
 * sequence of points of a well defined size.
 * A base period pattern eliminating all Saturdays and Sundays from a
 * daily domain is used in the definition of the working week time domain in
 * {@link Workday}.
 * 
 * @author Jean-Paul Vetterli
 * @version 1.0.1
 * @see TimePacker
 */
public interface BasePeriodPattern {

    /**
     * Return the number of time points in the pattern.
     * 
     * @return a positive number
     */
    int getSize();	
	
	/**
	 * Compress an unrestricted numerical time index by removing all OFF time points.
	 * Return the compressed index.
	 * 
	 * @param time an unrestricted numerical time index
	 * @return the numerical time index compressed
	 * @throws T2Exception
	 */
	long makeIndex(long time) throws T2Exception;
	
	/**
	 * Put back all removed OFF time points in a compressed numerical time index.
	 * Return the uncompresse numerical time index.
	 * 
	 * @param time a compressed numerical time index
	 * @return the numerical time index decompressed
	 */
	long expandIndex(long time);

	/**
	 * Return true it the primary pattern has any effect. If if has no effect
	 * compression and decompression will leave inputs unchanged.
	 * 
	 * @return true if the pattern has any effect
	 */
	public boolean effective();
}
