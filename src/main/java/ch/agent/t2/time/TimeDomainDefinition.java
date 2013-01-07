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
package ch.agent.t2.time;


/**
 * TimeDomainDefinition is a helper encapsulating properties of a time domain.
 * It does not provide useful behavior.
 *
 * @author Jean-Paul Vetterli
 */
public class TimeDomainDefinition {
	
	private String label;
	private Resolution baseUnit;
	private long origin;
	private BasePeriodPattern basePeriodPattern;
	private SubPeriodPattern subPeriodPattern;
	
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
	 * Return the label.
	 * 
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Change the label. This is the only setter, used to set a generated label, 
	 * when the label is null.
	 * 
	 * @param label
	 */
	public void setLabel(String label) {
		this.label = label;
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
