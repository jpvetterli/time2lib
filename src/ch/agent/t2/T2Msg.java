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
 * Package: ch.agent.t2
 * Type: T2Msg
 * Version: 1.0.0
 */
package ch.agent.t2;

import java.util.ResourceBundle;

import ch.agent.core.MessageBundle;
import ch.agent.core.KeyedException;
import ch.agent.core.KeyedMessage;

/**
 * T2Msg provides keyed messages to all ch.agent.t2.* packages.
 *
 * @author Jean-Paul Vetterli
 * @version 1.0.0
 */
public class T2Msg extends KeyedMessage {

	private static final String BUNDLE_NAME = "ch.agent.t2.T2Msg";

	private static final MessageBundle BUNDLE = new MessageBundle("T2", ResourceBundle.getBundle(BUNDLE_NAME));

	public static KeyedException exception(int key, Object... arg) {
		return new KeyedException(new T2Msg(key, arg));
	}
	
	public static KeyedException exception(Throwable cause, int key, Object... arg) {
		return new KeyedException(new T2Msg(key, arg), cause);
	}

	public T2Msg(int tag, Object... args) {
		super(String.valueOf(tag), BUNDLE, args);
	}


}