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
 * Type: SubPeriodPattern
 * Version: 1.0.1
 */
package ch.agent.t2.time;

import ch.agent.t2.T2Exception;

/**
 * A SubperiodPattern defines a set of higher resolution time points within a base unit. 
 * It is applied after the base period pattern, if any. This makes it possible
 * to define a time domain like "at 10:00, 12:30, and 16:45 on every working day" for example,
 * where the base pattern is used to model working days and the sub period pattern is used
 * to define the within day times.  
 * A sub period pattern is used in the definition of the time domain
 * implementing "the 3d Friday of the month" in {@link ThirdFriday}.
 * 
 * @author Jean-Paul Vetterli
 * @version 1.0.1
 * @see TimePacker
 */
public interface SubPeriodPattern {
	/**
	 * Return the number of sub periods.
	 * 
	 * @return a positive number
	 */
	int getSize();
	
	/**
	 * Return the sub period resolution.
	 * 
	 * @return a resolution
	 */
	Resolution getSubPeriod();
	
	/**
	 * Return the base period resolution.
	 * 
	 * @return a resolution
	 */
	Resolution getBasePeriod();
	
	/**
	 * Modify and return the numeric time index corresponding to the state of the 
	 * time parts argument. The numeric time index passed as an argument
	 * reflects those fields of the time parts argument relevant to the base unit and incorporates 
	 * the effect from the base period pattern. Remaining fields of the time parts argument are
	 * either used to determined the sub period or are ignored.
	 * This method is intended for used by {@link TimePacker#pack(TimeParts, Adjustment)}.
	 * 
	 * @param time a numeric time index
	 * @param adjust an allowed adjustment mode
	 * @param timeParts a time parts object with the complete time specification
	 * @return the numeric time index adjusted for the sub period
	 * @throws T2Exception
	 */
	long adjustForSubPeriod(long time, Adjustment adjust, TimeParts timeParts) throws T2Exception;

	/**
	 * Update the relevant field of the time parts argument for the given sub period.
	 * This method is intended for used by {@link TimePacker#unpack(long)}.
	 * 
	 * @param subPeriod a positive number
	 * @param timeParts a time parts object to be updated 
	 */
	void fillInSubPeriod(int subPeriod, TimeParts timeParts);
	
}
