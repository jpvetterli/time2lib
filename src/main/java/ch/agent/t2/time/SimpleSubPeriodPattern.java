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
 * Type: SimpleSubperiodPattern
 * Version: 1.0.1
 */
package ch.agent.t2.time;

import java.util.Arrays;

import ch.agent.t2.T2Exception;
import ch.agent.t2.T2Msg;
import ch.agent.t2.T2Msg.K;
import ch.agent.t2.time.engine.TimeTools;

/**
 * A SimpleSubPeriodPattern defines one or more sub periods within a base period.
 * All sub periods have the same resolution. They are defined by their number within
 * the base period.
 * 
 * Here is an example:
 * "on the 10th day and on the 20th of each occurring month", and here is
 * another one: "at 10:15, 14:20, and 22:40 every working day".
 * 
 * @author Jean-Paul Vetterli
 * @version 1.0.1
 * @see DayRankingSubPeriodPattern
 */
public class SimpleSubPeriodPattern implements SubPeriodPattern {
	
	private Resolution basePeriodUnit;
	private Resolution subPeriodUnit;
	private int[] ranks;
	
	public int getSize() {
		return ranks.length;
	}
	
	/**
	 * Construct a sub period pattern with the given base period and sub period resolutions, and with 
	 * the given ranks.
	 * 
	 * @param basePeriod a non-null resolution
	 * @param subPeriod a non-null resolution
	 * @param ranks a non-null non-empty array of integers
	 * @throws T2Exception
	 */
	public SimpleSubPeriodPattern(Resolution basePeriod, Resolution subPeriod, int[] ranks) throws T2Exception {
		if (basePeriod == null)
			throw new IllegalArgumentException("basePeriod null");
		if (subPeriod == null)
			throw new IllegalArgumentException("subPeriod null");
		if (ranks == null || ranks.length == 0)
			throw new IllegalArgumentException("ranks null or empty");
		this.basePeriodUnit = basePeriod;
		this.subPeriodUnit = subPeriod;
		this.ranks = ranks;
		validateRanks();
	}

	/**
	 * Validate the ranks array.
	 * 
	 * @throws T2Exception
	 */
	private void validateRanks() throws T2Exception {
		int minRank = Integer.MAX_VALUE;
		int maxRank = Integer.MIN_VALUE;
		switch (subPeriodUnit) {
		case MONTH:
			minRank = 1;
			maxRank = 12;
			break;
		case DAY:
			minRank = 1;
			maxRank = 28;
			break;
		case SEC:
			minRank = 0;
			maxRank = 86399;
			break;
		default:
			throw T2Msg.exception(K.T1112, subPeriodUnit.name());
		}
		
		for (int i = 0; i < ranks.length; i++) {
			if (ranks[i] < minRank || ranks[i] > maxRank)
				throw T2Msg.exception(K.T1113, ranks[i], minRank, maxRank);
			if (i > 0 && ranks[i] <= ranks[i-1])
				throw T2Msg.exception(K.T1114);
		}
	}
	
	@Override
	public Resolution getSubPeriod() {
		return subPeriodUnit;
	}

	@Override
	public Resolution getBasePeriod() {
		return basePeriodUnit;
	}

	@Override
	public long adjustForSubPeriod(long time, Adjustment adjust, TimeParts tp) throws T2Exception {
		time *= getSize();
		switch (basePeriodUnit) {
		case YEAR:
			switch(subPeriodUnit) {
			case MONTH:
				time = increment(time, adjust, ranks, tp.getMonth());
				break;
			default:
				throw T2Msg.exception(K.T1115, basePeriodUnit.name(), subPeriodUnit.name());
			}
			break;
		case MONTH:
			switch (subPeriodUnit) {
			case DAY:
				time = increment(time, adjust, ranks, tp.getDay());
				break;
			default:
				throw T2Msg.exception(K.T1115, basePeriodUnit.name(), subPeriodUnit.name());
			}
			break;
		case DAY:
			switch (subPeriodUnit) {
			case SEC:
				time = increment(time, adjust, ranks, tp.getHour() * 3600 + tp.getMin() * 60 + tp.getSec());
				break;
			default:
				throw T2Msg.exception(K.T1115, basePeriodUnit.name(), subPeriodUnit.name());
			}
			break;
		default:
			throw T2Msg.exception(K.T1115, basePeriodUnit.name(), subPeriodUnit.name());
		}
		
		return time;
	}
	
	/**
	 * Increment the numeric time index by the sub period
	 * index corresponding to the given period, adjusting if necessary
	 * when allowed.
	 * 
	 * @param time an unadjusted time
	 * @param adjust a non-null adjustment mode
	 * @param ranks an array of ranks
	 * @param period a number
	 * @return an adjusted time
	 * @throws T2Exception
	 */
	private long increment(long time, Adjustment adjust, int[] ranks, int period) throws T2Exception {
		int inc = 0;
		int i = Arrays.binarySearch(ranks, period);
		if (i >= 0)
			inc = i;
		else {
			switch (adjust) {
			case UP:
				inc = -i - 1;
				if (inc == ranks.length)
					inc = getSize(); // first sub period of next base period
				break;
			case DOWN:
				inc = -i - 2;
				if (inc < 0)
					inc = -1; // last sub period of previous base period
				break;
			case NONE:
				throw T2Msg.exception(K.T1117);
			default:
				throw new RuntimeException(adjust.name());
			}
		}
		return increment(time, inc);
	}

	/**
	 * Add the increment to the time and check for overflow.
	 * 
	 * @param time
	 *            a numeric time index
	 * @param inc
	 *            an increment
	 * @return the incremented numeric time index
	 * @throws T2Exception
	 */
	private long increment(long time, int inc) throws T2Exception {
		long result = time;
		time += inc;
		// overflow?
		if (result < 0 && time > 0 || result > 0 && time < 0)
			throw T2Msg.exception(K.T1116);
		return time;
	}
	
	@Override
	public void fillInSubPeriod(int subPeriod, TimeParts tp) {
		if (subPeriod < 0 || subPeriod > ranks.length)
			throw new IllegalArgumentException("subPeriod does not agree with ranks.length");
		
		switch (subPeriodUnit) {
		case MONTH:
			tp.setMonth(ranks[subPeriod]);
			break;
		case DAY:
			tp.setDay(ranks[subPeriod]);
			break;
		case SEC:
			TimeTools.computeHMS(ranks[subPeriod], tp);
			break;
		default:
			throw new RuntimeException("bug: " + subPeriodUnit.name());
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof SimpleSubPeriodPattern))
			return false;
		if (!subPeriodUnit.equals(((SimpleSubPeriodPattern) obj).subPeriodUnit))
			return false;
		return Arrays.equals(ranks, ((SimpleSubPeriodPattern) obj).ranks);
	}
	
	@Override
	public int hashCode() {
		return 31 * subPeriodUnit.hashCode() + Arrays.hashCode(ranks);
	}

	@Override
	public String toString() {
		return subPeriodUnit.toString() + Arrays.toString(ranks);
	}

}
