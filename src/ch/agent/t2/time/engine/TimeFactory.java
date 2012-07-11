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
 * Package: ch.agent.t2.time.engine
 * Type: TimeFactory
 * Version: 1.0.2
 */
package ch.agent.t2.time.engine;

import ch.agent.core.KeyedException;
import ch.agent.t2.T2Msg;
import ch.agent.t2.time.Adjustment;
import ch.agent.t2.time.BasePeriodPattern;
import ch.agent.t2.time.DayOfWeek;
import ch.agent.t2.time.ExternalTimeFormat;
import ch.agent.t2.time.SubPeriodPattern;
import ch.agent.t2.time.TimeDomain;
import ch.agent.t2.time.TimeDomainDefinition;
import ch.agent.t2.time.TimeIndex;
import ch.agent.t2.time.TimePacker;
import ch.agent.t2.time.TimeParts;
import ch.agent.t2.time.Resolution;

/**
 * A TimeFactory makes {@link TimeIndex} objects and
 * acts as an immutable {@link TimeDomain}.
 * 
 * A time domain is defined by the following 4 properties:
 * <ol>
 * <li> the resolution, which is never null,
 * <li> the origin, which is long offset from the base time of January 1 year zero,
 * <li> a base period pattern, which can be null,
 * <li> a sub period pattern, which can be null.
 * </ol>
 * <p>
 *
 * @author Jean-Paul Vetterli
 * @version 1.0.2
 * @see Resolution
 * @see BasePeriodPattern
 * @see SubPeriodPattern
 */
public class TimeFactory implements TimeDomain, TimePacker, ExternalTimeFormat {

	
	private int hash; // the object is immutable, so hash must be computed only once

	private String label;
	
	private Resolution baseUnit;

	private long origin;

	private ExternalTimeFormat externalFormat;
	
	private BasePeriodPattern basePeriodPattern;
	
	private SubPeriodPattern subPeriodPattern;
	
	private int serialNumber = -1; // -1 for non-built-ins, should be accessible only from classes in package
	
	private TimeIndex minTime, maxTime, minOffsetCompatibleTime, maxOffsetCompatibleTime;
	private long minNumericTime, maxNumericTime;
	
	/**
	 * Construct a TimeFactory for the given time domain definition and external time format.
	 * 
	 * @param def a non-null time domain definition
	 * @param externalFormat a non-null external format
	 */
	protected TimeFactory(TimeDomainDefinition def, ExternalTimeFormat externalFormat) {
		super();
		this.label = def.getLabel();
		this.baseUnit = def.getBaseUnit();
		this.origin = def.getOrigin();
		this.basePeriodPattern = def.getBasePeriodPattern();
		if (this.basePeriodPattern != null && !this.basePeriodPattern.effective())
			basePeriodPattern = null;
		this.subPeriodPattern = def.getSubPeriodPattern();
		this.externalFormat = externalFormat;
		minNumericTime = 0; // coming soon: negative times
		maxNumericTime = findMaxIndex(this.basePeriodPattern, this.subPeriodPattern);
	}
	
	/**
	 * Return true if the domain is built-in.
	 * 
	 * @return true if the domain is built-in
	 */
	boolean isBuiltIn() {
		return serialNumber >= 0;
	}

	/**
	 * Marks the domain as built-in and set its serial number. 
	 * This number gives the position of the domain in the sequence of declarations. 
	 * The sequence of invocations of this method should begin with an argument 
	 * of 0, and proceed with arguments incremented by 1.
	 * 
	 * @param serialNumber
	 *            a non-negative number
	 */
	void markBuiltIn(int serialNumber) {
		if (serialNumber < 0)
			throw new IllegalArgumentException("serialNumber negative");
		this.serialNumber = serialNumber;
	}

	/**
	 * Return the serial number of the built-in time domain. 
	 * Return -1 if domain is not built-in.
	 * 
	 * @return the serial number
	 */
	int getSerialNumber() {
		return serialNumber;
	}

	@Override
	public String getLabel() {
		return label != null ? label : toString();
	}

	@Override
	public long getOrigin() {
		return origin;
	}

	@Override
	public Resolution getResolution() {
		/*
		 * The resolution is the effective unit of the time. It corresponds to
		 * the base unit or sub unit.
		 */
		if (subPeriodPattern != null)
			return subPeriodPattern.getSubPeriod();
		else
			return baseUnit;
	}
	
	@Override
	public int compareResolutionTo(Resolution unit) {
		return -getResolution().compareTo(unit);
	}

	@Override
	public void requireEquality(TimeDomain domain) throws KeyedException {
		if (!equals(domain))
			throw T2Msg.exception(32230, getLabel(), domain.getLabel());
	}

	/**
	 * Returns true if o1 and o2 are null, or if o1 is not null and equals o2 according
	 * to {@link Object#equals(Object)}. Specify the object most likely to be null first.
	 * 
	 * @param o1 the object most likely to be null
	 * @param o2 the other object
	 * @return true if both are null or if they are equal
	 */
	private boolean equals(Object o1, Object o2) {
		return o1 == null && o2 == null || o1 != null && o1.equals(o2);
	}
	
	/**
	 * This method tests if the given domain properties match those of
	 * this domain.
	 * 
	 * @param baseUnit a resolution
	 * @param origin a number 
	 * @param basePattern a base period pattern
	 * @param subPattern a sub period pattern
	 * @return true if all these properties match those of this domain
	 */
	public boolean matches(Resolution baseUnit, long origin, BasePeriodPattern basePattern, SubPeriodPattern subPattern) {
		/*
		 * IMPORTANT: the method relies on equals being overridden.
		 */
		if (!this.baseUnit.equals(baseUnit))
			return false;
		if (origin != this.origin)
			return false;
		if (!equals(basePeriodPattern, basePattern))
			return false;
		return equals(subPeriodPattern, subPattern);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof TimeFactory))
			return false;
		if (!((TimeFactory) obj).baseUnit.equals(baseUnit))
			return false;
		if (((TimeFactory) obj).origin != origin)
			return false;
		if (!equals(basePeriodPattern, ((TimeFactory) obj).basePeriodPattern))
			return false;
		return equals(subPeriodPattern, ((TimeFactory) obj).subPeriodPattern);
	}
	
	@Override
	public int hashCode() {
		int h = hash;
		if (h == 0) {
			h = (new Long(origin)).hashCode();
			h = 31 * h + baseUnit.hashCode(); // why 31?
			if (basePeriodPattern != null)
				h = 31 * h + basePeriodPattern.hashCode(); // why not?
			if (subPeriodPattern != null)
				h = 31 * h + subPeriodPattern.hashCode(); // saw it in java.util.String
			hash = h;
		}
		return h;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("L=" + label);
		s.append(" O=" + origin);
		s.append(" U=" + baseUnit);
		s.append(" P=" + basePeriodPattern);
		s.append(" S=" + subPeriodPattern);
		return s.toString();
	}

	@Override
	public TimeIndex time(long year, int month, int day, int hour, int min,
			int sec, int usec, Adjustment adjust) throws KeyedException {
		return new Time2(this, year, month, day, hour, min, sec, usec, adjust);
	}

	@Override
	public TimeIndex time(String date) throws KeyedException {
		return new Time2(this, date, Adjustment.NONE);
	}
	
	@Override
	public TimeIndex time(String date, Adjustment adjust) throws KeyedException {
		return new Time2(this, date, adjust);
	}

	@Override
	public TimeIndex minTime() {
		if (minTime == null)
			minTime = new Time2(this, minNumericTime);
		return minTime;
	}

	@Override
	public TimeIndex maxTime() {
		return maxTime(false);
	}
	
	@Override
	public TimeIndex minTime(boolean offsetCompatible) {
		if (offsetCompatible) {
			if (minOffsetCompatibleTime == null)
				minOffsetCompatibleTime = new Time2(this, Integer.MIN_VALUE + getOrigin());
			return minOffsetCompatibleTime;
		} else {
			if (minTime == null)
				minTime = new Time2(this, minNumericTime);
			return minTime;
		}
	}

	@Override
	public TimeIndex maxTime(boolean offsetCompatible) {
		if (offsetCompatible) {
			if (maxOffsetCompatibleTime == null)
				maxOffsetCompatibleTime = new Time2(this, Integer.MAX_VALUE + getOrigin());
			return maxOffsetCompatibleTime;
		} else {
			if (maxTime == null)
				maxTime = new Time2(this, maxNumericTime);
			return maxTime;
		}
	}

	@Override
	public TimeIndex time(long index) {
		return new Time2(this, index);
	}

	@Override
	public TimeIndex timeFromOffset(long offset) {
		return new Time2(this, offset + getOrigin());
	}

	@Override
	public boolean valid(long t, boolean testOnly) throws KeyedException {
		/*
		 * In the first version, time overflow was detected here when t was
		 * negative, because incrementing the maximum long value by 1 wrapped
		 * around to negative. It is planned to allow negative times, and now
		 * testing is done against minNumericTime. This minimum is currently 0,
		 * so nothing has changed yet.
		 * 
		 * Please do not remove this note until negative numeric times have been
		 * implemented. And tested.
		 */
		if (t >= minNumericTime && t <= maxNumericTime)
			return true;
		else {
			if (testOnly)
				return false;
			else
				throw T2Msg.exception(32151, t);
		}
	}

	////// TimePacker //////
	
	@Override
	public BasePeriodPattern getBasePeriodPattern() {
		return basePeriodPattern;
	}
	
	@Override
	public SubPeriodPattern getSubPeriodPattern() {
		return subPeriodPattern;
	}

	@Override
	public TimeParts scan(String time) throws KeyedException {
		return externalFormat.scan(time);
	}

	@Override
	public String format(Resolution unit, TimeParts timeParts) {
		return externalFormat.format(unit, timeParts);
	}

	@Override
	public long pack(TimeParts tp, Adjustment adjust) throws KeyedException {
		try {
			// input values validated in asRawIndex
			long time = tp.asRawIndex(this.baseUnit);
			if (subPeriodPattern == null)
				time = compress(time, adjust);
			else {
				// adjustments apply only to sub period
				time = compress(time, Adjustment.NONE);
				time = subPeriodPattern.adjustForSubPeriod(time, adjust, tp);
			}
			return time;
		} catch (KeyedException e) {
			int message = subPeriodPattern == null ? 32148 : 32149;
			throw T2Msg.exception(e, message, tp.toString(), getLabel());
		}
	}

	@Override
	public TimeParts unpack(long time) {
		int subPeriod = 0;
		if (subPeriodPattern != null) {
			int sz = subPeriodPattern.getSize();
			long orig = time;
			time = time / sz;
			subPeriod = (int) (orig - time * sz); // can cast because getSize is int
		}
		if (basePeriodPattern != null)
			time = basePeriodPattern.expandIndex(time);
		
		TimeParts tp = new TimeParts();
		Resolution unit = this.baseUnit;
		switch (unit) {
		case YEAR:
			tp.setYear(time);
			break;
		case MONTH:
			tp.setYear(time / 12);
			tp.setMonth((int) (time - tp.getYear() * 12) + 1);
			break;
		case DAY:
			TimeTools.computeYMD(time, tp);
			break;
		case HOUR:
			long days = time / 24;
			tp.setHour((int) (time - days * 24));
			TimeTools.computeYMD(days, tp);
			break;
		case MIN:
			days = time/ (24 * 60);
			long minutes = time - days * 24 * 60;
			tp.setHour((int)(minutes / 60));
			tp.setMin((int) (minutes - tp.getHour() * 60));
			TimeTools.computeYMD(days, tp);
			break;
		case SEC:
			days = time / (24 * 60 * 60);
			long seconds = time - days * 24L * 60L * 60L;
			TimeTools.computeYMD(days, tp);
			TimeTools.computeHMS(seconds, tp);
			break;
		case MSEC:
			days = time / (24L * 60L * 60L * 1000L);
			long millis = time - days * 24L * 60L * 60L * 1000L;
			seconds = millis / 1000L;
			tp.setUsec((int) (millis - seconds * 1000L) * 1000);
			TimeTools.computeYMD(days, tp);
			TimeTools.computeHMS(seconds, tp);
			break;
		case USEC:
			days = time / (24L * 60L * 60L * 1000000L);
			long micros = time - days * 24L * 60L * 60L * 1000000L;
			seconds = micros / 1000000L;
			tp.setUsec((int) (micros - seconds * 1000000L));
			TimeTools.computeYMD(days, tp);
			TimeTools.computeHMS(seconds, tp);
			break;
		default:
			throw new RuntimeException("bug: " + unit.name());
		}
		
		if (subPeriodPattern != null) {
			// there is something to do even when subPeriod = 0
			subPeriodPattern.fillInSubPeriod(subPeriod, tp);
		}
		
		// make sure nothing is negative
		if (tp.anyNegative())
			throw new RuntimeException(String.format("(bug) time=%d %s", time, tp.toString()));
			
		return tp;
	}

	@Override
	public DayOfWeek getDayOfWeek(TimeIndex time) throws KeyedException {
		if (subPeriodPattern != null) {
			if (compareResolutionTo(Resolution.DAY) <= 0) {
				long numTime = ((Time2) time).getTimeParts().asRawIndex(Resolution.DAY);
				return TimeTools.getDayOfWeek(Resolution.DAY, numTime);
			} else
				throw T2Msg.exception(32140, getResolution());
		} else {
			long t;
			if (basePeriodPattern == null)
				t = time.asLong();
			else
				t = basePeriodPattern.expandIndex(time.asLong());
			return TimeTools.getDayOfWeek(getResolution(), t);
		}
	}
	
	/**
	 * Return the raw index compressed with the base pattern. When the
	 * base pattern has no effect, the raw index is simply tested for
	 * validity.
	 * 
	 * @param time
	 *            the raw numeric time index 
	 * @param adjust
	 *            the type of adjustment allowed, if any
	 * @return the compressed numeric time index
	 * @throws KeyedException
	 */
	private long compress(long time, Adjustment adjust) throws KeyedException {
		if (basePeriodPattern != null) {
			if (time < 0) // not yet compressed, so don't use domain.invalid()
				throw T2Msg.exception(32151, time);
			if (adjust == Adjustment.NONE)
				time = basePeriodPattern.makeIndex(time);
			else {
				/*
				 * If the pattern cycle is short, the loop should be short too.
				 * If not, then the checks in adjust() will ensure eventual termination.
				 */
				while (true) {
					try {
						time = basePeriodPattern.makeIndex(time);
						break;
					} catch (KeyedException e) {
						time = adjust(time, adjust == Adjustment.UP);
					}
				}
			}
		} else
			valid(time, false);
		
		return time;
	}

	/**
	 * Adjust and test for overflow.
	 * 
	 * @param time the numeric time index to adjust
	 * @param up true if adjustment is up
	 * @return the adjusted numeric time index
	 * @throws KeyedException
	 */
	private long adjust(long time, boolean up) throws KeyedException {
		long result = time;
		if (up)
			result++;
		else
			result--;
		// overflow?
		if (result < 0 && time > 0 || result > 0 && time < 0)
			throw T2Msg.exception(32155);
		return result;
	}

	/**
	 * Return the maximum numeric time for this domain. When patterns have no
	 * effect, this maximum is the maximal long. When there is only a base
	 * pattern, the maximum is the value which uncompresses to the unrestricted
	 * maximum. With a sub pattern, time indexes are inflated by the size of the
	 * sub period pattern. A few positions are lost in the division
	 * maxIndex/sub_pattern_size. It is conceptually okay to lose them because
	 * they correspond to an incomplete last base period.
	 * 
	 * @param basePattern a base period pattern
	 * @param subPattern a sub period pattern
	 * @return the maximum numeric time index for this domain
	 */
	private long findMaxIndex(BasePeriodPattern basePattern,
			SubPeriodPattern subPattern) {
		/*
		 * TODO:
		 * Is it really conceptually o.k. to "lose a few positions"? 
		 * My feeling is that the maximum should be the maximum.
		 */
		long maxIndex = Long.MAX_VALUE;
		if (subPattern != null)
			maxIndex /= subPattern.getSize();
		// note 2: for a calm day: find a better algorithm than trial and error
		if (basePattern != null) {
			for (int i = 0; i < basePattern.getSize(); i++) {
				try {
					return basePattern.makeIndex(maxIndex - i);
				} catch (KeyedException e) {
					continue;
				}
			}
			throw new RuntimeException("bug: " + maxIndex);
		}
		return maxIndex;
	}

}
