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
package ch.agent.core;

/**
 * A keyed exception is an {@link Exception} with a {@link KeyedMessage}
 * instead of a string.
 * 
 * @author Jean-Paul Vetterli
 */
public class KeyedException extends Exception {
	
	private static final long serialVersionUID = 6657865517163634816L;
	
	protected KeyedMessage msg;

	/**
	 * Construct an exception with a keyed message.
	 * 
	 * @param message a {@link KeyedMessage}
	 */
	public KeyedException(KeyedMessage message) {
		super(null, null);
		this.msg = message;
	}
	
	/**
	 * Construct an exception with a keyed message and the causing exception.
	 * 
	 * @param message a {@link KeyedMessage}
	 * @param cause a {@link Throwable}
	 */
	public KeyedException(KeyedMessage message, Throwable cause) {
		super(null, cause);
		msg = message;
	}

	@Override
	public String getMessage() {
		return msg.getMessage();
	}

	/**
	 * Return the keyed message describing the exception.
	 *  
	 * @return a {@link KeyedMessage} 
	 */
	public KeyedMessage getMsg() {
		return msg;
	}
	
	@Override
	public String toString(){
		return this.getClass().getSimpleName() + " " + msg.toString();
	}
}