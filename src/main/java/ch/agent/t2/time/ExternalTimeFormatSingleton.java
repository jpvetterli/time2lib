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

import ch.agent.t2.T2Msg;
import ch.agent.t2.T2Msg.K;

/**
 * Provide access to the time scanner and formatter. The name of the
 * {@link ExternalTimeFormat} class to use can be specified with the system
 * property {@code ExternalTimeFormat}. In case none is specified, the default is
 * {@link DefaultExternalFormat}.
 *
 * @author Jean-Paul Vetterli
 */
public class ExternalTimeFormatSingleton {

	private static final String PROP = "ExternalTimeFormat";
	
	private static final ExternalTimeFormat externalTimeFormat = externalTimeFormat();
	
	private static ExternalTimeFormat externalTimeFormat() {
		
		String className = System.getProperty(PROP);
		if (className == null)
			return new DefaultExternalFormat();
		else {
			try {
				return (ExternalTimeFormat) Class.forName(className).newInstance();
			} catch (Exception e) {
				Exception cause = T2Msg.exception(e, K.T0012, PROP, className);
				throw T2Msg.runtimeException(K.T0001, cause);
			}
		}
	}
	
	/**
	 * Return the time scanner and formatter.
	 * 
	 * @return an ExternalTimeFormat
	 */
	public static ExternalTimeFormat instance() {
		return externalTimeFormat;
	}
	
	/**
	 * No need to instantiate.
	 */
	private ExternalTimeFormatSingleton() {
	}
	
}
