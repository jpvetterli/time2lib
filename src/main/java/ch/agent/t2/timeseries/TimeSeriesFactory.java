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
 * Package: ch.agent.t2.timeseries
 * Type: TimeSeriesFactory
 * Version: 1.0.1
 */
package ch.agent.t2.timeseries;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

import ch.agent.t2.T2Exception;
import ch.agent.t2.T2Msg;
import ch.agent.t2.T2Msg.K;
import ch.agent.t2.time.Resolution;
import ch.agent.t2.time.TimeDomain;

/**
 * TimeSeriesFactory manages the objects representing <em>missing values</em>
 * and provides the basis for creating time series.
 * It ensures
 * that there is a single missing value object for each time series type. This
 * is a useful feature. If it were possible to specify the missing value object
 * as a parameter when creating a time series, it would be possible to create
 * two series of the same type but with different missing value objects. This
 * does not seem so problematic until values from one series are assigned to the
 * other. The originally missing values would lose their status in the
 * operation and confusion would result.
 * <p>
 * Missing values can be defined. But they must not. The defaults used are
 * Double.NaN for Double and null for all other types.
 * <p>
 * Using TimeSeriesFactory to create new time series is not the only way.
 * Using {@link TimeAddressable#makeEmptyCopy()} or
 * {@link TimeIndexable#makeEmptyCopy()} is faster but requires an available
 * time series with the wanted attributes.
 * 
 * @author Jean-Paul Vetterli
 * @version 1.0.1
 */
public class TimeSeriesFactory {

	/**
	 * Singleton implements thread-safe and lazy initialization of the TimeSeriesFactory instance.
	 */
	// See comment in TimeDomainFactory.Singleton.
	private static class Singleton {
		private static TimeSeriesFactory singleton = new TimeSeriesFactory();
	}
	
	private static final int MAXGAP = 500;
	
	private int maxGap;
	
	/**
	 * Return the TimeSeriesFactory instance. The way the instance is created
	 * ensures there will be a single one for a class loader.
	 * 
	 * @return the instance
	 */
	public static TimeSeriesFactory getInstance() {
		return Singleton.singleton;
	}
	
	/**
	 * Construct a time series of the domain and type specified. The time series
	 * returned is indexable.
	 * <p>
	 * The presence of both a type parameter and of a method parameter to pass the type may
	 * seem confusing. Usage in client code is in fact straightforward:
	 * <blockquote> 
	 * <xmp>TimeIndexable<Double> ts = TimeSeriesFactory.make(domain, Double.class);
     * </xmp>
	 * </blockquote>
	 * 
	 * @param <T> corresponds to the value type
	 * @param domain a non-null time domain
	 * @param type the value type
	 * @return an indexable time series
	 */
	public static <T>TimeIndexable<T> make(TimeDomain domain, Class<T> type) {
		return getInstance().makeRegularTimeSeries(domain, type);
	}

	/**
	 * Construct a time series of the domain and type specified. When forceParse is
	 * true, or when the domain has a hint to use a sparse series, the time
	 * series returned is addressable, else it is indexable.
	 * 
	 * @param <T> corresponds to the value type
	 * @param domain a non-null time domain
	 * @param type the value type
	 * @param forceSparse if true, always return a sparse time series
	 * @return a time series
	 */
	public static <T>TimeAddressable<T> make(TimeDomain domain, Class<T> type, boolean forceSparse) {
		return getInstance().makeRegularOrSparseTimeSeries(domain, type, forceSparse);
	}
	
	private Map<Class<?>, Object> map;
	private Object nullValue;
	
	/**
	 * Construct an instance. Intended to be used by {@link TimeSeriesFactory.Singleton}. 
	 */
	protected TimeSeriesFactory() {
		this.maxGap = MAXGAP;
		map = new HashMap<Class<?>, Object>();
		nullValue = new Object();
	}
	
	/**
	 * Set the maximum length of a single run of missing values for an
	 * indexable time series. The default is 500. For a sparse time series,
	 * there is no limit. Changing this setting has no effect on existing time
	 * series.
	 * 
	 * @param maxGap a positive number
	 */
	public void setMaxGap(int maxGap) {
		if (maxGap < 1)
			throw new IllegalArgumentException("maxGap not positive");
		this.maxGap = maxGap;
	}

	/**
	 * Define the missing value object to use for the given type.
	 * A definition can be done only once for a type.
	 * <p>
	 * This method is synchronized.
	 * 
	 * @param type the value type
	 * @param missingValue the object representing missing values
	 * @throws T2Exception
	 */
	public synchronized void define(Class<?> type, Object missingValue) throws T2Exception {
		if (map.containsKey(type))
			throw T2Msg.exception(K.T5005, type.getSimpleName());
		// store a special object to represent null
		map.put(type, missingValue == null ? nullValue : missingValue);
	}
	
	/**
	 * Return the object representing missing values for the given type.
	 * If no such object exists, create one. For the type Double, a "not a number"
	 * object, for all other types use null.
	 * 
	 * @param type a non-null value type
	 * @return the object representing missing values, possibly null
	 */
	protected synchronized Object getOrCreateMissingValue(Class<?> type) {
		if (type == null)
			throw new IllegalArgumentException("type null");
		Object mv = map.get(type);
		
		if (mv == null) {
			if (type == Double.class)
				mv = new Double(Double.NaN);
			else
				mv = nullValue;	// avoids a map.containsKey(type);
			try {
				define(type, mv);
			} catch (Exception e) {
				throw new RuntimeException("bug", e);
			}
		}
		return mv == nullValue ? null : mv;
	}
	
	/**
	 * Construct a regular time series of the given time domain and value type.
	 * 
	 * @param <T> corresponds to the value type
	 * @param domain a non-null time domain
	 * @param type a non-null value type
	 * @return a TimeIndexable of the type specified
	 */
	@SuppressWarnings("unchecked")
	protected <T>TimeIndexable<T> makeRegularTimeSeries(TimeDomain domain, Class<T> type) {
		T missingValue = (T) getOrCreateMissingValue(type);
		return new RegularTimeSeries<T>(domain, (T[])Array.newInstance(type, 0), missingValue, maxGap);
	}

	/**
	 * Construct a regular or sparse time series of the given time domain and value type.
	 * If the resolution is higher than DAY or if the argument <em>forceSparse</em> is true
	 * a sparse time series will be created. 
	 * 
	 * @param <T> corresponds to the value type
	 * @param domain a non-null time domain
	 * @param type a non-null value type
	 * @param forceSparse if true always use a sparse organization
	 * @return a TimeAddressable of the type specified
	 */
	@SuppressWarnings("unchecked")
	protected <T>TimeAddressable<T> makeRegularOrSparseTimeSeries(TimeDomain domain, Class<T> type, boolean forceSparse) {
		T missingValue = (T) getOrCreateMissingValue(type);
		if (forceSparse || domain.compareResolutionTo(Resolution.DAY) < 0)
			return new SparseTimeSeries<T>(type, domain, missingValue);
		else
			return new RegularTimeSeries<T>(domain, (T[])Array.newInstance(type, 0), missingValue, maxGap);
	}
	
}
