/*
 *   Copyright 2012 Hauser Olsson GmbH
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
 * Package: ch.agent.t2
 * Type: T2Exception
 * Version: 1.0.0
 */
package ch.agent.t2;

import ch.agent.core.KeyedException;
import ch.agent.core.KeyedMessage;

/**
 * T2Exception is thrown by methods of the Time2 library when they need to throw
 * a checked exception. Such exceptions are so-called "business exceptions"
 * corresponding to logical problems occurring during normal usage of an
 * application. As an example, attempting to force a Sunday date on a time
 * domain where only working days are defined will result in a T2Exception being
 * thrown. The application is supposed to catch the exception and present the
 * accompanying message to the user.
 * 
 * @author Jean-Paul Vetterli
 * @version 1.0.0
 */
public class T2Exception extends KeyedException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5871582235647420421L;

	/**
	 * Construct an exception with a keyed message.
	 * 
	 * @param message a {@link KeyedMessage}
	 */
	public T2Exception(KeyedMessage message) {
		super(message);
	}
	
	/**
	 * Construct an exception with a keyed message and the causing exception.
	 * 
	 * @param message a {@link KeyedMessage}
	 * @param cause a {@link Throwable}
	 */
	public T2Exception(KeyedMessage message, Throwable cause) {
		super(message, cause);
	}

}