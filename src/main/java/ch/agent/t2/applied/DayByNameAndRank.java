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
package ch.agent.t2.applied;

import ch.agent.t2.T2Exception;
import ch.agent.t2.T2Msg;
import ch.agent.t2.T2Msg.K;
import ch.agent.t2.time.DayOfWeek;
import ch.agent.t2.time.Resolution;

/**
 * DayByNameAndRank defines a day by its name and its rank within a month or a
 * year. This allows to define for example the 3d Friday of the month, or the
 * last Monday of the year. Other base periods units are not meaningful, and are
 * not allowed. The rank is a non-zero number between -5 and +5 for months and
 * -53 and +53 for years. With a negative rank, days are counted from the end,
 * with -1 for the last day.
 * 
 * @author Jean-Paul Vetterli
 */
public class DayByNameAndRank {

	private static final int MONTH_MAX_RANK = 5;
	private static final int YEAR_MAX_RANK = 53;

	private DayOfWeek day;
	private int rank;
	
	/**
	 * Construct day definition for the given day of week and rank.
	 * 
	 * @param day non-null day of week
	 * @param rank a non-zero number in [-5, 5] or [-53, 53] depending on the context
	 */
	public DayByNameAndRank(DayOfWeek day, int rank) {
		this.day = day;
		this.rank = rank;
	}

	/**
	 * Return the maximum rank even if it is not always achievable. For
	 * example, the maximum rank for a day in a month is 5, but in many months
	 * it is only 4.
	 * 
	 * @param unit the resolution
	 * @return the maximum rank for the given resolution
	 * @throws T2Exception
	 */
	protected int getMaxRank(Resolution unit) throws T2Exception {
		switch (unit) {
		case MONTH:
			return MONTH_MAX_RANK;
		case YEAR:
			return YEAR_MAX_RANK;
		default:
			throw T2Msg.exception(K.T1052, unit.name());
		}
	}
	
	/**
	 * Verify the validity of the rank for the given resolution.
	 * 
	 * @param rank a number 
	 * @param unit a non-null resolution
	 * @throws T2Exception
	 */
	protected void checkRank(int rank, Resolution unit) throws T2Exception {
		int m = getMaxRank(unit);
		if (rank == 0 || rank < -m || rank > m)
			throw T2Msg.exception(K.T1051, rank, -m, m);
	}
	
	/**
	 * Return the day of week.
	 * 
	 * @return the day of week
	 */
	public DayOfWeek getDayOfWeek() {
		return day;
	}

	/**
	 * Return the rank.
	 * @return the rank
	 */
	public int getRank() {
		return rank;
	}

	@Override
	public String toString() {
		return day.name() + "#" + rank;
	}

	@Override
	public int hashCode() {
		int h = rank;
		return 31 * h + day.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof DayByNameAndRank))
			return false;
		if (rank != ((DayByNameAndRank) obj).rank)
			return false;
		return day.equals(((DayByNameAndRank) obj).day);
	}
	
}
