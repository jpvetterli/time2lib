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
package ch.agent.t2.time;

import java.util.Arrays;

import ch.agent.t2.T2Exception;
import ch.agent.t2.T2Msg;
import ch.agent.t2.T2Msg.K;

/**
 * A Cycle is a {@link BasePeriodPattern} with a repeating sequence of ON and
 * OFF time points. A cycle is defined using a boolean array.
 * 
 * @author Jean-Paul Vetterli
 */
public class Cycle implements BasePeriodPattern {

	private boolean[] pattern;

	private int[] map;

	private int[] inverseMap;
	
	private int cycleLength;
	
	private int compressedLength;
	
	/**
	 * Construct a cycle using the given pattern.
	 * 
	 * @param pattern a sequence of true and false values
	 */
	public Cycle(boolean... pattern) {
		if (pattern == null || pattern.length == 0)
			throw new IllegalArgumentException("pattern null or empty");
		this.pattern = pattern;
		computeMaps();
		if (cycleLength > 0 && compressedLength == 0)
			throw new IllegalArgumentException("all false pattern");
	}

	@Override
	public int getSize() {
		return cycleLength;
	}

	@Override
	public long makeIndex(long time) throws T2Exception {
		if (time < 0)
			throw new IllegalArgumentException("time negative");
		if (pattern == null)
			return time;
		long cycles = time / cycleLength;
		int remainder = (int) (time - cycles * cycleLength);
		int offset = map[remainder];
		if (offset < 0)
			throw T2Msg.exception(K.T1071, time, remainder);
		return cycles * compressedLength + offset;
	}
	
	@Override
	public long expandIndex(long time) {
		if (pattern == null)
			return time;
		
		int remainder = (int) (time % compressedLength);
		int offset = inverseMap[remainder];
		long cycles = (time - remainder) / compressedLength;
		return cycles * cycleLength + offset;
	}

	@Override
	public boolean effective() {
		return cycleLength != compressedLength;
	}
	
	/**
	 * Return true if the given pattern equals this pattern.
	 * 
	 * @param pattern another sequence of true and false values
	 * @return true if both sequences are equal
	 */
	public boolean matches(boolean[] pattern) {
		return Arrays.equals(this.pattern, pattern);
	}
	
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		if (pattern != null) {
			for (boolean b : pattern)
				if (b) s.append('1');
				else s.append('0');
		} else 
			s.append("(no cycle)");
		return s.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(pattern);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Cycle other = (Cycle) obj;
		if (!Arrays.equals(pattern, other.pattern))
			return false;
		return true;
	}

	/**
	 * Compute the map and the reverse map. The definition is compiled into two
	 * integer arrays: a map and a reverse map. A negative element of the map means
	 * that the corresponding time point does not belong to the cycle. A
	 * non-negative element indicates the offset of the time point in the
	 * compressed time index. The length of the reverse map is the number of ON
	 * bits in the cycle pattern. The elements of the reverse map give the
	 * offsets of the time points in the uncompressed time index.
	 * <p>
	 */
	private void computeMaps() {
		cycleLength = pattern.length;
		
		map = new int[cycleLength];
		int offset = -1;
		for (int i = 0; i < cycleLength; i++) {
			if (pattern[i])
				map[i] = ++offset;
			else
				map[i] = -1;
		}
		compressedLength = offset + 1;
		inverseMap = new int[compressedLength];
		int j = -1;
		for (int i = 0; i < cycleLength; i++) {
			if (map[i] > -1)
				inverseMap[++j] = i;
		}
		return;
	}
	
}
