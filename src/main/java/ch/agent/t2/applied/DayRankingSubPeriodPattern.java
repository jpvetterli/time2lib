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
package ch.agent.t2.applied;

import java.util.Arrays;

import ch.agent.t2.T2Exception;
import ch.agent.t2.T2Msg;
import ch.agent.t2.T2Msg.K;
import ch.agent.t2.time.Adjustment;
import ch.agent.t2.time.Resolution;
import ch.agent.t2.time.SimpleSubPeriodPattern;
import ch.agent.t2.time.SubPeriodPattern;
import ch.agent.t2.time.TimeParts;
import ch.agent.t2.time.TimeTools;

/**
 * A DayRankingSubperiodPattern defines sub periods within a base period in
 * terms of named days. For a simple example of use, see {@link ThirdFriday}. It
 * is possible to define more complex patterns.
 * 
 * @author Jean-Paul Vetterli
 * @see SimpleSubPeriodPattern
 */
public class DayRankingSubPeriodPattern implements SubPeriodPattern {

	private Resolution basePeriodUnit;

	private DayByNameAndRank[] ranks;

	/**
	 * Construct a sub period pattern for the given base period and ranks. There
	 * are multiple restrictions on the dayPattern parameter. First it must
	 * specify ranks in increasing order. Then it is forbidden to have
	 * repetitions of actual days. To ensure this rule holds in all cases,
	 * negative ranks occupy two positive ranks, because a day can occur 4 or 5
	 * times in a month and 52 or 53 times in a year. For example, in a month,
	 * Fri 2 and Fri -4 correspond to the same day when there are 5 Fridays in
	 * the month and Fri 2 and -3 correspond to the same day when there are 4
	 * Fridays. So both are forbidden. On the other hand, Fri 2 and -2 is okay.
	 * A further restriction is that a given rank can be used only once. For
	 * example it is forbidden to specify Mon 1 and Fri 1. This is done to limit
	 * complexity, because in some cases Mon 1 precedes Fri 1 while in other
	 * cases it comes after. As a final restriction, all ranks must be relative
	 * to the same unit. It is not possible to mix days of month with days of
	 * year.
	 * <p>
	 * If any restriction is violated an {@link IllegalArgumentException} is
	 * thrown.
	 * 
	 * @param basePeriod
	 *            a non-null resolution, yearly or monthly
	 * @param ranks
	 *            an array of day definitions
	 * @throws T2Exception
	 */
	public DayRankingSubPeriodPattern(Resolution basePeriod, DayByNameAndRank[] ranks) {
		if (basePeriod != Resolution.YEAR && basePeriod != Resolution.MONTH)
			throw new IllegalArgumentException("basePeriod must be either YEAR or MONTH");
		if (ranks == null || ranks.length == 0)
			throw new IllegalArgumentException("ranks null or empty");
		this.basePeriodUnit = basePeriod;
		this.ranks = ranks;
		try {
			validateRanks();
		} catch (T2Exception e) {
			throw new IllegalArgumentException("ranks invalid", e);
		}
	}

	@Override
	public int getSize() {
		return ranks.length;
	}

	@Override
	public Resolution getSubPeriod() {
		return Resolution.DAY;
	}

	@Override
	public Resolution getBasePeriod() {
		return basePeriodUnit;
	}

	@Override
	public long adjustForSubPeriod(long time, Adjustment adjust, TimeParts tp)
			throws T2Exception {
		time *= getSize();
		switch (basePeriodUnit) {
		case YEAR:
			time = increment(true, time, tp.getYear(), tp.getMonth(),
					tp.getDay(), adjust, ranks);
			break;
		case MONTH:
			time = increment(false, time, tp.getYear(), tp.getMonth(),
					tp.getDay(), adjust, ranks);
			break;
		default:
			throw T2Msg.exception(K.T1118, basePeriodUnit.name(), getSubPeriod()
					.name());
		}
		return time;
	}

	@Override
	public TimeParts fillInSubPeriod(int subPeriod, TimeParts tp) {
		if (subPeriod < 0 || subPeriod > ranks.length)
			throw new IllegalArgumentException(
					"subPeriod does not agree with ranks.length");
		TimeParts result = null;
		try {
			switch (basePeriodUnit) {
			case YEAR:
				int yearDay = TimeTools.getDayByRank(tp.getYear(), 0,
						ranks[subPeriod].getDayOfWeek(),
						ranks[subPeriod].getRank());
				int[] md = TimeTools.computeMonthAndDay(tp.getYear(), yearDay);
				result = new TimeParts(getSubPeriod(), tp.getYear(), md[0], md[1], tp.getHour(), tp.getMin(), tp.getSec(), tp.getFsec(), tp.getTZOffset());
				break;
			case MONTH:
				int day = TimeTools.getDayByRank(tp.getYear(), tp.getMonth(),
						ranks[subPeriod].getDayOfWeek(),
						ranks[subPeriod].getRank());
				result = new TimeParts(getSubPeriod(), tp.getYear(), tp.getMonth(), day, tp.getHour(), tp.getMin(), tp.getSec(), tp.getFsec(), tp.getTZOffset());
				break;
			default:
				throw T2Msg.exception(K.T1118, basePeriodUnit.name(),
						getSubPeriod().name());
			}
		} catch (T2Exception e) {
			// errors not expected when unpacking, so this can only be a bug
			throw new RuntimeException("bug", e);
		}
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof DayRankingSubPeriodPattern))
			return false;
		return Arrays.equals(ranks, ((DayRankingSubPeriodPattern) obj).ranks);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(ranks);
	}

	@Override
	public String toString() {
		return Arrays.toString(ranks);
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
		try {
			return TimeTools.sum(time, inc);
		} catch (ArithmeticException e) {
			throw T2Msg.exception(K.T1116);
		}
	}

	/**
	 * Increment by trial and error the numeric time index by the sub period
	 * index corresponding to the given time components, adjusting if necessary
	 * when allowed. No less.
	 * 
	 * @param yearMode
	 *            if true increment days in year else days in month
	 * @param time
	 *            the unadjusted time
	 * @param year
	 *            the year
	 * @param month
	 *            the month of the year
	 * @param day
	 *            the day of the month
	 * @param adjust
	 *            the non-null adjustment mode
	 * @param ranks
	 *            the array of day definitions
	 * @return the adjusted time
	 * @throws T2Exception
	 */
	private long increment(boolean yearMode, long time, long year, int month,
			int day, Adjustment adjust, DayByNameAndRank[] ranks)
			throws T2Exception {
		if (yearMode) {
			day += TimeTools.daysToMonth(year, month);
			month = 0;
		}
		int inc = Integer.MAX_VALUE;
		loop: for (int i = 0; i < ranks.length; i++) {
			DayByNameAndRank rank = ranks[i];
			int rankDay = TimeTools.getDayByRank(year, month,
					rank.getDayOfWeek(), rank.getRank());
			if (rankDay == day) {
				inc = i;
				break;
			} else if (rankDay < day) {
				switch (adjust) {
				case UP:
					if (i == ranks.length - 1) {
						inc = getSize(); // first sub period of next base period
						break loop;
					}
					break; // continue loop
				case DOWN:
					if (i == ranks.length - 1) {
						inc = i;
						break loop;
					}
					break; // continue loop
				case NONE:
					throw T2Msg.exception(K.T1117);
				default:
					throw new RuntimeException("bug: " + adjust.name());
				}
			} else {
				switch (adjust) {
				case UP:
					inc = i;
					break loop;
				case DOWN:
					inc = i - 1; // inc = -1 yields the last sub period of the
									// previous base period
					break loop;
				case NONE:
					throw T2Msg.exception(K.T1117);
				default:
					throw new RuntimeException("bug: " + adjust.name());
				}
			}
		}
		assert inc != Integer.MAX_VALUE;
		return increment(time, inc);
	}

	/**
	 * Verify the validity of the ranks array. See the comment of the
	 * constructor for details.
	 * 
	 * @throws T2Exception
	 */
	private void validateRanks() throws T2Exception {
		int m = ranks[0].getMaxRank(basePeriodUnit); // 5 or 53
		int previousR = Integer.MIN_VALUE;
		boolean inUse[] = new boolean[m];
		for (DayByNameAndRank dnr : ranks) {
			int r = dnr.getRank();
			/*
			 * Note about next test. We use the maximum (m) for sizing the inUse
			 * array and for determining the potential positions when ranks are
			 * negative. But ranks equal to the maximum (or its negative) are
			 * not allowed because such days do not always exist. So we use <=
			 * and >= in the next test.
			 */
			if (r <= -m || r >= m || r == 0)
				throw T2Msg.exception(K.T1051, r, -m + 1, m - 1);
			if (r > 0) {
				if (r <= previousR)
					throw T2Msg.exception(K.T1055);
				if (inUse[r - 1])
					throw T2Msg.exception(K.T1053, r);
				inUse[r - 1] = true;
				previousR = r;
			} else {
				int virtualR1 = m + 1 + r;
				if (virtualR1 <= previousR)
					throw T2Msg.exception(K.T1055);
				int virtualR2 = m + r;
				if (inUse[virtualR1 - 1] || inUse[virtualR2 - 1])
					throw T2Msg.exception(K.T1054, r, virtualR1, virtualR2);
				inUse[virtualR1 - 1] = true;
				inUse[virtualR2 - 1] = true;
				previousR = virtualR1;
			}
		}
	}

}
