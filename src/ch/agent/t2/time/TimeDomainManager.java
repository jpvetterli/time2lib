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
 * Package: ch.agent.t2.time
 * Type: TimeDomainManager
 * Version: 1.0.0
 */
package ch.agent.t2.time;

import java.lang.reflect.Method;

import ch.agent.t2.T2Msg;

/**
 * The TimeDomainManager provides a single point of access to the 
 * configured {@link TimeDomainFactory}. Using the manager adds
 * flexibility in an application.
 * The following code accesses the factory:
 * <p>
 * <blockquote>
 * <pre>TimeDomainManager.getFactory();</pre>
 * </blockquote>
 * <p>
 * When no factory has been configured, the manager uses 
 * {@link DefaultTimeDomainFactory} as a default. 
 * This is overridden by passing the name
 * of an alternative factory class as a system property with the key
 * <em>TimeDomainFactory</em>.
 *
 * @author Jean-Paul Vetterli
 * @version 1.0.0
 */
public class TimeDomainManager {

	private static final String PROP = "TimeDomainFactory";
	private static final String INSTANCE_METHOD = "getInstance";
	
	private static TimeDomainFactory factory;
	
	/**
	 * Return the TimeDomainFactory instance.
	 * This method is synchronized.
	 * 
	 * @return the TimeDomainFactory instance
	 */
	public static synchronized TimeDomainFactory getFactory() {
		if (factory == null) {
			String className = System.getProperty(PROP);
			if (className == null)
				factory = DefaultTimeDomainFactory.getInstance();
			else {
				try {
					Class<?> c = Class.forName(className);
					Method getI = c.getMethod(INSTANCE_METHOD);
					factory = (TimeDomainFactory) getI.invoke(null);
				} catch (Exception e) {
					throw new RuntimeException(new T2Msg(32000, PROP, className).toString(), e);
				}
			}
		}
		return factory;
	}
	
	/**
	 * Construct a TimeDomainManager.
	 */
	private TimeDomainManager() {
	}
	
}
