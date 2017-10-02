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

import ch.agent.t2.T2Exception;
import ch.agent.t2.T2Msg;
import ch.agent.t2.T2Msg.K;
import ch.agent.t2.time.TimeParts.HMSF;
import ch.agent.t2.time.TimeParts.YMD;

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
 * @see Resolution
 * @see BasePeriodPattern
 * @see SubPeriodPattern
 */
public class TimeFactory implements TimeDomain, TimePacker, TimeFormatter, TimeScanner {

	
	private int hash = 0; // the object is immutable, so hash must be computed only once

	private String label;
	
	private Resolution baseUnit;

	private long origin;

	private BasePeriodPattern basePeriodPattern;
	
	private SubPeriodPattern subPeriodPattern;
	
	private TimeIndex minTime, maxTime, minOffsetCompatibleTime, maxOffsetCompatibleTime;
	
	private long min, max; // we must keep them 
	
	private final TimeFormatter formatter;
	private final TimeScanner scanner;
	
	/**
	 * Construct a TimeFactory for the given time domain.
	 * 
	 * @param def
	 *            a non-null time domain definition
	 * @param formatter
	 *            a non-null time formatter
	 * @param scanner
	 *            a non-null time scanner
	 */
	public TimeFactory(TimeDomainDefinition def, TimeFormatter formatter, TimeScanner scanner) {
		super();
		if (def == null)
			throw new IllegalArgumentException("def null");
		if (formatter == null)
			throw new IllegalArgumentException("formatter null");
		this.label = def.getLabel();
		this.baseUnit = def.getBaseUnit();
		this.origin = def.getOrigin();
		this.basePeriodPattern = def.getBasePeriodPattern();
		if (this.basePeriodPattern != null && !this.basePeriodPattern.effective())
			basePeriodPattern = null;
		this.subPeriodPattern = def.getSubPeriodPattern();
		min = 0;
		max = findMaxIndex(this.basePeriodPattern, this.subPeriodPattern);
		this.formatter = formatter;
		this.scanner = scanner;
		
		// derived data:
		minTime = new Time2(this, min);
		maxTime = new Time2(this, max);
		minOffsetCompatibleTime = new Time2(this, getOrigin());
		maxOffsetCompatibleTime = new Time2(this, Integer.MAX_VALUE + getOrigin());
	}
	
	/**
	 * Constructor providing a default time formatter and time scanner.
	 * 
	 * @param def
	 *            a non-null time domain definition
	 */
	public TimeFactory(TimeDomainDefinition def) {
		this(def, new DefaultTimeFormatter(), new DefaultTimeScanner());
	}
	
	/**
	 * Constructor providing a default time scanner.
	 * 
	 * @param def
	 *            a non-null time domain definition
	 * @param formatter
	 *            a non-null time formatter
	 */
	public TimeFactory(TimeDomainDefinition def, TimeFormatter formatter) {
		this(def, formatter, new DefaultTimeScanner());
	}
	
	@Override
	public TimePacker getPacker() {
		return this;
	}

	@Override
	public TimeFormatter getFormatter() {
		return this;
	}

	@Override
	public TimeScanner getScanner() {
		return this;
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
	public void requireEquality(TimeDomain domain) throws T2Exception {
		if (!equals(domain))
			throw T2Msg.exception(K.T1074, getLabel(), domain.getLabel());
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
	
	private boolean equalsIgnoringLabel(TimeFactory domain) {
		if (!domain.baseUnit.equals(baseUnit))
			return false;
		if (domain.origin != origin)
			return false;
		if (!equals(basePeriodPattern, domain.basePeriodPattern))
			return false;
		return equals(subPeriodPattern, domain.subPeriodPattern);
	}
	
	@Override
	public boolean similar(TimeDomain domain) {
		if (this == domain)
			return true;
		if (domain == null)
			return false;
		if (getClass() != domain.getClass())
			return false;
		return equalsIgnoringLabel((TimeFactory) domain);
	}
	
	public int hashCode() {
		if (hash == 0) {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((basePeriodPattern == null) ? 0 : basePeriodPattern.hashCode());
			result = prime * result + ((baseUnit == null) ? 0 : baseUnit.hashCode());
			result = prime * result + ((label == null) ? 0 : label.hashCode());
			result = prime * result + (int) (origin ^ (origin >>> 32));
			result = prime * result + ((subPeriodPattern == null) ? 0 : subPeriodPattern.hashCode());
			hash = result;
		}
		return hash;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		if (!equals(label, ((TimeFactory) obj).label))
			return false;
		return equalsIgnoringLabel((TimeFactory) obj);
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
			int sec, int usec, Adjustment adjust) throws T2Exception {
		return new Time2(this, year, month, day, hour, min, sec, usec, adjust);
	}

	@Override
	public TimeIndex time(String date) throws T2Exception {
		return new Time2(this, date, Adjustment.NONE);
	}
	
	@Override
	public TimeIndex time(String date, Adjustment adjust) throws T2Exception {
		return new Time2(this, date, adjust);
	}

	@Override
	public TimeIndex minTime() {
		return minTime(false);
	}

	@Override
	public TimeIndex maxTime() {
		return maxTime(false);
	}
	
	@Override
	public TimeIndex minTime(boolean offsetCompatible) {
		return offsetCompatible ? minOffsetCompatibleTime : minTime;
	}

	@Override
	public TimeIndex maxTime(boolean offsetCompatible) {
		return offsetCompatible ? maxOffsetCompatibleTime : maxTime;
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
	public boolean valid(long t, boolean testOnly) throws T2Exception {
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
		if (t >= min && t <= max)
			return true;
		else {
			if (testOnly)
				return false;
			else
				throw T2Msg.exception(K.T1070, t);
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
	public TimeParts scan(Resolution unit, String time) throws T2Exception {
		return scanner.scan(unit, time);
	}

	@Override
	public String format(Resolution unit, TimeParts timeParts) {
		return formatter.format(unit, timeParts);
	}

	@Override
	public long pack(TimeParts tp, Adjustment adjust) throws T2Exception {
		try {
			// input values validated in asRawIndex
			long time = TimeTools.makeRawIndex(this.baseUnit, tp);
			if (subPeriodPattern == null)
				time = compress(time, adjust);
			else {
				// adjustments apply only to sub period
				time = compress(time, Adjustment.NONE);
				time = subPeriodPattern.adjustForSubPeriod(time, adjust, tp);
			}
			return time;
		} catch (T2Exception e) {
			String messageKey = subPeriodPattern == null ? K.T1068 : K.T1069;
			throw T2Msg.exception(e, messageKey, tp.toString(), getLabel());
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
		
		Resolution unit = this.baseUnit;
		YMD ymd = null;
		HMSF hmsu = null;
		switch (unit) {
		case YEAR:
			ymd = new YMD(time, 1, 1);
			break;
		case MONTH:
			long year = time / 12;
			ymd = new YMD(year, (int) (time - year * 12) + 1, 1);
			break;
		case DAY:
			ymd = TimeTools.computeYMD(time);
			break;
		case HOUR:
			long days = time / 24;
			hmsu = new HMSF((int) (time - days * 24), 0, 0, 0);
			ymd = TimeTools.computeYMD(days);
			break;
		case MIN:
			days = time/ (24 * 60);
			long minutes = time - days * 24 * 60;
			int hours = (int)(minutes / 60);
			hmsu = new HMSF(hours, (int) (minutes - hours * 60), 0, 0);
			ymd = TimeTools.computeYMD(days);
			break;
		case SEC:
			days = time / (24 * 60 * 60);
			long seconds = time - days * 24L * 60L * 60L;
			ymd = TimeTools.computeYMD(days);
			hmsu = TimeTools.computeHMS(seconds);
			break;
		case MSEC:
			days = time / (24L * 60L * 60L * 1000L);
			long millis = time - days * 24L * 60L * 60L * 1000L;
			seconds = millis / 1000L;
			ymd = TimeTools.computeYMD(days);
			hmsu = TimeTools.computeHMS(seconds, (int) (millis - seconds * 1000L));
			break;
		case USEC:
			days = time / (24L * 60L * 60L * 1000000L);
			long micros = time - days * 24L * 60L * 60L * 1000000L;
			seconds = micros / 1000000L;
			ymd = TimeTools.computeYMD(days);
			hmsu = TimeTools.computeHMS(seconds, (int) (micros - seconds * 1000000L));
			break;
		case NSEC:
			days = time / (24L * 60L * 60L * 1000000000L);
			long nanos = time - days * 24L * 60L * 60L * 1000000000L;
			seconds = nanos / 1000000000L;
			ymd = TimeTools.computeYMD(days);
			hmsu = TimeTools.computeHMS(seconds, (int) (nanos - seconds * 1000000000L));
			break;
		default:
			throw new RuntimeException("bug: " + unit.name());
		}
		
		TimeParts tp = makeTimeParts(unit, ymd, hmsu);
		if (subPeriodPattern != null) {
			// there is something to do even when subPeriod = 0
			tp = subPeriodPattern.fillInSubPeriod(subPeriod, tp);
		}
		
		return tp;
	}
	
	private TimeParts makeTimeParts(Resolution unit, YMD ymd, HMSF hmsu) {
		return hmsu == null ? new TimeParts(unit, ymd.y(), ymd.m(), ymd.d(), 0, 0, 0, 0) :
			new TimeParts(unit, ymd.y(), ymd.m(), ymd.d(), hmsu.h(), hmsu.m(), hmsu.s(), hmsu.f());
	}

	@Override
	public DayOfWeek getDayOfWeek(TimeIndex time) throws T2Exception {
		if (subPeriodPattern != null) {
			if (compareResolutionTo(Resolution.DAY) <= 0) {
				long numTime = TimeTools.makeRawIndex(Resolution.DAY, ((Time2) time).getTimeParts());
				return TimeTools.getDayOfWeek(Resolution.DAY, numTime);
			} else
				throw T2Msg.exception(K.T1060, getResolution());
		} else {
			long t;
			if (basePeriodPattern == null)
				t = time.asLong();
			else
				t = basePeriodPattern.expandIndex(time.asLong());
			return TimeTools.getDayOfWeek(getResolution(), t);
		}
	}
	
	@Override
	public int getBasePeriodSize() {
		return subPeriodPattern == null ? 1 : subPeriodPattern.getSize();
	}

	@Override
	public TimeIndex getBasePeriodStart(TimeIndex t) {
		if (t == null)
			throw new IllegalArgumentException("t null");
		if (!t.getTimeDomain().equals(this))
			throw new IllegalArgumentException(new T2Msg(K.T0017, t.getTimeDomain().getLabel(), getLabel()).toString());
		long size = this.getPacker().getBasePeriodSize();
		long boundary = (t.asLong() / size) * size;
		return boundary == t.asLong() ? t : time(boundary);
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
	 * @throws T2Exception
	 */
	private long compress(long time, Adjustment adjust) throws T2Exception {
		if (basePeriodPattern != null) {
			if (time < 0) // not yet compressed, so don't use domain.invalid()
				throw T2Msg.exception(K.T1070, time);
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
					} catch (T2Exception e) {
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
	 * @throws T2Exception
	 */
	private long adjust(long time, boolean up) throws T2Exception {
		long result = time;
		if (up)
			result++;
		else
			result--;
		// overflow?
		if (result < 0 && time > 0 || result > 0 && time < 0)
			throw T2Msg.exception(K.T1072);
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
				} catch (T2Exception e) {
					continue;
				}
			}
			throw new RuntimeException("bug: " + maxIndex);
		}
		return maxIndex;
	}

}
