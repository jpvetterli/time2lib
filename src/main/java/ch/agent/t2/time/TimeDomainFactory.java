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

import java.util.Collection;

import ch.agent.t2.T2Exception;
import ch.agent.t2.time.engine.AbstractTimeDomainFactory;

/**
 * TimeDomainFactory is an interface for defining and retrieving
 * {@link TimeDomain} objects. A time domain factory ensures that there is only
 * one domain for a given set of properties. A factory can provide
 * built-in domains guaranteed to be available whenever the factory is present.
 * They are created when the factory class is loaded. The labels of
 * such domains can be used as external identifiers.
 * <p>
 * There are ways to access a given time domain which are more direct than
 * retrieving it from the factory, but in the end the domain is still one
 * managed by the factory. The following example code shows 4 different ways to
 * get the same domain object, in this case the <em>daily</em> domain implemented by
 * {@link Day}:
 * <p>
 * <blockquote>
 * <pre>
 * TimeDomainFactory fac = TimeDomainManager.getFactory();
 * TimeDomain d1 = new Day(&quot;0102-03-04&quot;).getTimeDomain();
 * TimeDomain d2 = Day.DOMAIN;
 * TimeDomain d3 = fac.get(&quot;daily&quot;);
 * TimeDomain d4 = fac.get(new TimeDomainDefinition(null, Resolution.DAY, 0L));
 * assertSame(d1, d2);
 * assertSame(d1, d3);
 * assertSame(d1, d4);
 * </pre>
 * </blockquote>
 * <p>
 * TimeDomainFactory was designed under the following assumptions:
 * <ol>
 * <li>A time domain factory is implemented as a singleton, return by 
 * the static method <code>getInstance()</code>.
 * <li>The reason to have different factories is to provide different sets of
 * built-in domains to applications.
 * <li>A <em>Time2</em> application uses a single factory. The factory is
 * accessed via the {@link TimeDomainManager}.
 * </ol>
 * See the class comment in {@link AbstractTimeDomainFactory} for an example of a concrete
 * factory.
 * <p>
 * It is possible to bypass the factory mechanism, but this requires using non-public methods.
 * 
 * @author Jean-Paul Vetterli
 * @see TimeDomainDefinition
 * @see DefaultTimeDomainFactory
 */
public interface TimeDomainFactory {
	
	/**
	 * Return the default {@link ExternalTimeFormat}.  
	 * @return a time parser
	 */
	ExternalTimeFormat getExternalTimeFormat();
	
	/**
	 * Conditionally create and return the time domain corresponding to the definition. An
	 * <b>unchecked</b> exception is thrown if a domain matching the definition
	 * is found, but the labels differ (unless the label specified is null).
	 * <p>
	 * The second argument determines the behavior of the method when no domain
	 * matching the definition is found:
	 * <ul>
	 * <li>if <em>register</em> is true, register the definition and throw an
	 * <b>unchecked</b> exception if any error occurs, like the label being already
	 * in use by another time domain (unless the label specified is null), 
	 * <li>if <em>register</em> is false, return null.
	 * </ul>
	 * <p>
	 * Note. This method is meant for use in initializers and any error
	 * should be considered fatal.
	 * 
	 * @param definition
	 *            a non-null time domain definition
	 * @return a time domain matching the definition or null
	 */
	TimeDomain get(TimeDomainDefinition definition, boolean register);
	
	/**
	 * Return the time domain corresponding to the definition, ignoring the
	 * label. Return null when not found.
	 * 
	 * @param definition
	 *            a non-null time domain definition
	 * @return a time domain matching the definition or null
	 */
	TimeDomain get(TimeDomainDefinition definition);
	
	/**
	 * Return the time domain with the given label. Throw an exception when not
	 * found.
	 * 
	 * @param label non-null string
	 * @return a time domain
	 * @throws T2Exception
	 */
	TimeDomain get(String label) throws T2Exception;
	
	/**
	 * Return the collection of time domains currently defined in the factory.
	 * 
	 * @return the collection of time domains 
	 */
	Collection<TimeDomain> getTimeDomains();
	
	/**
	 * Return true if the domain is a built-in domain.
	 * As as side effect {@link #lockBuiltIns()} is invoked.
	 * 
	 * @param domain a time domain
	 * @return true if the domain is built-in
	 */
	boolean isBuiltIn(TimeDomain domain);
	
	/**
	 * Declare built-in the time domain identified by the label. 
	 * An <b>unchecked</b> exception is thrown if a built-in is
	 * declared after {@link #lockBuiltIns()} was invoked.
	 * 
	 * @param label a non-null string
	 */
	void declareBuiltIn(String label);
	
	/**
	 * Make it impossible to declare more built-in domains.
	 * 
	 */
	void lockBuiltIns();
	
	/**
	 * Return the collection of built-in time domains. 
	 * As side effect {@link #lockBuiltIns()} is invoked.
	 * 
	 * @return the collection of built-in time domains 
	 */
	Collection<TimeDomain> getBuiltIns();

}
