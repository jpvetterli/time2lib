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

import ch.agent.t2.time.engine.AbstractTimeDomainFactory;
import ch.agent.t2.time.engine.Time2;


/**
 * TimeDomainFactory includes a set of built-in unique {@link TimeDomain} objects 
 * accessible in various ways.
 * <p>
 * The domain labels provided by this factory are:
 * <ul>
 * <li>yearly
 * <li>monthly
 * <li>daily
 * <li>datetime
 * <li>workweek
 * <li>weekly
 * <li>friday3
 * </ul>
 *
 * @author Jean-Paul Vetterli
 */
public class DefaultTimeDomainFactory extends AbstractTimeDomainFactory {

	/**
	 * Initializer class for the default time domain factory.
	 * The time domain factory is initialized simply by referencing all
	 * {@link Time2} subclasses that should be considered built-in. The
	 * registration into the factory is done implicitly, because all the
	 * subclasses have a static final field named DOMAIN which triggers
	 * registration when it is initialized at class load time. Note that it is
	 * not a good idea to reference the DOMAIN field in the factory initializer
	 * because the DOMAIN field initialization uses the factory and this
	 * constitutes a circularity. As a result, if the static initializer of the
	 * factory is triggered by the constructor of one of the classes referenced
	 * in initializer, the DOMAIN field will still be null.
	 * <p>
	 * The idiom used by this default factory is not mandatory but it works. It can be summarized
	 * as:
	 * <ul>
	 * <li>Write a subclass of {@link Time2} with two public static final fields:
	 * <ol>
	 * <li>DEF, initialized as a {@link TimeDomainDefinition}
	 * <li>DOMAIN, initialized with the following code
	 * <blockquote>
	 * <code>TimeDomainManager.getFactory().get(DEF, true);</code>
	 * </blockquote>
	 * The second parameter tells the factory to create and register the time domain if necessary. 
	 * </ol>
	 * This {@link Time2} subclass is ready for use, and, because it is automatically 
	 * registered in the time domain factory, uniqueness is guaranteed.
	 * <li>In the static initializer of the factory, declare the time domain to be built-in with the following code:
	 * <blockquote>
	 * <code>factory.declareBuiltIn(Year.DEF.getLabel());</code>
	 * </blockquote>
	 * where factory is the singleton and declareBuiltIn is a method inherited from the 
	 * factory superclass.
	 * </ul>
	 * <p>
	 * <small>
	 * Technical note:
	 * <p>
	 * Singleton implements thread-safe and lazy initialization of the
	 * TimeDomainFactory instance.
	 * <ul>
	 * <li>Static initializers are thread-safe, so no need for explicit
	 * synchronization.
	 * <li>Using a nested class results in lazy initialization.
	 * </ul>
	 * 
	 * See for example this <a href=
	 * "http://stackoverflow.com/questions/878577/are-java-static-initializers-thread-safe"
	 * >reference</a>
	 * </small>
	 */
	private static class Singleton {
		private static DefaultTimeDomainFactory factory;
		static {
			factory = new DefaultTimeDomainFactory();
			factory.declareBuiltIn(Year.DEF.getLabel());
			factory.declareBuiltIn(Month.DEF.getLabel());
			factory.declareBuiltIn(Day.DEF.getLabel());
			factory.declareBuiltIn(Week.DEF.getLabel());
			factory.declareBuiltIn(Workday.DEF.getLabel());
			factory.declareBuiltIn(DateTime.DEF.getLabel());
			factory.declareBuiltIn(ThirdFriday.DEF.getLabel());
			factory.lockBuiltIns();
		};
	}

	/**
	 * Return the DefaultTimeDomainFactory instance.
	 * @return the DefaultTimeDomainFactor instance
	 */
	public static DefaultTimeDomainFactory getInstance() {
		return Singleton.factory;
	}
	
	/**
	 * Construct a DefaultTimeDomainFactory.
	 */
	private DefaultTimeDomainFactory() {
	}

}
