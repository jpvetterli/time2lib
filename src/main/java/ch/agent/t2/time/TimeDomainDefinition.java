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

import ch.agent.t2.time.engine.TimeFactory;

/**
 * TimeDomainDefinition is a helper class encapsulating properties of a time
 * domain. Time domain definitions are immutable.
 *
 * @author Jean-Paul Vetterli
 */
public class TimeDomainDefinition {
	
	private final String label;
	private final Resolution baseUnit;
	private final long origin;
	private final BasePeriodPattern basePeriodPattern;
	private final SubPeriodPattern subPeriodPattern;
	
	private TimeDomain cached;
	
	/**
	 * Construct a definition with the given label and properties.
	 * If the label is null, one will be generated.
	 *  
	 * @param label a string or null
	 * @param baseUnit a resolution
	 * @param origin a number 
	 * @param basePattern base period pattern
	 * @param subPattern a sub period pattern
	 */
	public TimeDomainDefinition(String label, Resolution baseUnit, long origin, 
			BasePeriodPattern basePattern, SubPeriodPattern subPattern) {
		
		if (baseUnit == null)
			throw new IllegalArgumentException("resolution null");
		if (subPattern != null && !baseUnit.equals(subPattern.getBasePeriod()))
			throw new IllegalArgumentException("resolution of sub pattern differs base resolution");
		
		this.label = label;
		this.baseUnit = baseUnit;
		this.origin = origin;
		this.basePeriodPattern = basePattern;
		this.subPeriodPattern = subPattern;
	}

	/**
	 * Construct a definition with the given label and properties.
	 *  
	 * @param label a string or null
	 * @param baseUnit a resolution
	 * @param origin a number 
	 * @param basePattern base period pattern
	 */
	public TimeDomainDefinition(String label, Resolution baseUnit, long origin, 
			BasePeriodPattern basePattern) {
		this(label, baseUnit, origin, basePattern, null);
	}
	
	/**
	 * Construct a definition with the given label and properties.
	 *  
	 * @param label a string or null
	 * @param baseUnit a resolution
	 * @param origin a number 
	 */
	public TimeDomainDefinition(String label, Resolution baseUnit, long origin) {
		this(label, baseUnit, origin, null, null);
	}
	
	/**
	 * For subclasses.
	 * 
	 * @return a time domain or null
	 */
	protected TimeDomain getCached() {
		return cached;
	}

	/**
	 * For subclasses.
	 * 
	 * @param cached a time domain
	 */
	protected void setCached(TimeDomain cached) {
		this.cached = cached;
	}

	/**
	 * Construct the time domain corresponding to this definition. This method
	 * creates a time domain with a default {@link ExternalTimeFormat}.
	 * Any problem with the definition will be detected the first time this method is executed.
	 * 
	 * @return a time domain
	 */
	public TimeDomain asTimeDomain() {
		if (getCached() == null)
			setCached(new TimeFactory(this));
		return getCached();
	}
	
	/**
	 * Return the label.
	 * 
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Return the origin.
	 * 
	 * @return the origin
	 */
	public long getOrigin() {
		return origin;
	}

	/**
	 * Return the resolution.
	 * 
	 * @return the resolution
	 */
	public Resolution getBaseUnit() {
		return baseUnit;
	}
	
	/** 
	 * Return the base period pattern.
	 * 
	 * @return the sub period pattern
	 */
	public BasePeriodPattern getBasePeriodPattern() {
		return basePeriodPattern;
	}
	
	/**
	 * Return the sub period pattern.
	 * 
	 * @return the sub period pattern
	 */
	public SubPeriodPattern getSubPeriodPattern() {
		return subPeriodPattern;
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

}
