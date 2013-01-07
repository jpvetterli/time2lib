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
package ch.agent.core;

import java.text.MessageFormat;
import java.util.MissingResourceException;

/**
 * A keyed message is a text identified by a key unique within a category.
 * The text is obtained from a {@link MessageBundle}.
 * Such a message can be easily identified in a large system with thousands of
 * messages. In large systems, messages are grouped in categories with
 * corresponding textual resources. Within each category, each message has a
 * unique key. The messages themselves contain parameters following the
 * {@link MessageFormat} convention.
 * <p>
 * Messages are only prepared when fetched with {@link #getMessage()}. Preparation 
 * involves looking up messages in a resource bundle and formatting parameters.
 * Avoiding this overhead is especially useful when messages are sent to a logging 
 * facility without knowing for sure that they will actually be logged. 
 * Thanks to this feature, it is not necessary to bracket the logging calls with
 * tests of the current logging level. To force preparation of the message, for example
 * because objects passed as parameter are about to change, use {@link #getMessage()},
 * which makes a copy of the formatted string the first time it is called, and 
 * returns the copy on subsequent calls.
 * <p>
 * An application would extend this class as follows: <blockquote> 
 * <xmp>
 * public class FOOMsg extends KeyedMessage {
 * 
 *	public class M {
 *	    public static final String A123 = "A123";
 *	    public static final String X124 = "X124";
 *	}
 *	private static final MessageBundle BUNDLE = 
 *	    new MessageBundle("FOO", ResourceBundle.getBundle("ch.agent.foo.FOOMsg"));
 *
 *	public static KeyedMessage exception(String code, Object... arg) {
 *		return new KeyedMessage(new FOOMsg(code, arg));
 *	}
 *	public static KeyedMessage exception(Throwable cause, String code, Object... arg) {
 *		return new KeyedMessage(new FOOMsg(code, arg), cause);
 * 	}
 *	public FOOMsg(String code, Object... args) {
 *		super(code, BUNDLE, args);
 *	}
 * }
 * </xmp> </blockquote> The property file <tt>ch/agent/foo/FOOMsg.properties</tt> would contains lines like:
 * <blockquote>
 * <xmp>
 * A123=The answer is {0}.
 * X124=What is the question?
 * </xmp>
 * </blockquote>
 * Clients would throw exceptions and write messages so:
 * <blockquote>
 * <xmp>
 * System.error.println(new FOOMsg(M.X124));
 * throw FOOMsg.exception(M.A123, 42);
 * </xmp>
 * </blockquote>
 * The user would see:
 * <blockquote>
 * <xmp>
 * FOO.X124 - What is the question?
 * FOO.A123 - The answer is 42.
 * </xmp>
 * </blockquote>
 * 
 * @author Jean-Paul Vetterli
 */
public abstract class KeyedMessage {

	private MessageBundle bundle;
	private String key;
	private String message;
	private Object[] args;

	/**
	 * Construct a keyed message. The description is fetched from the message
	 * bundle using the key. When one or more arguments are specified, these are
	 * inserted into the text. When an argument is null, the string <q>null</q>
	 * is inserted, else the method {@link Object#toString() toString()} is
	 * applied to the argument before it is inserted. Refer to
	 * {@link MessageFormat} for details on the format of arguments.
	 * 
	 * @param key
	 *            a String identifying the text
	 * @param bundle
	 *            a {@link MessageBundle} containing the wanted text
	 * @param args
	 *            zero of more arguments
	 */
	public KeyedMessage(String key, MessageBundle bundle, Object... args) {
		this.bundle = bundle;
		this.key = key;
		this.args = args;
	}
	
	private String findMessage(MessageBundle bundle, String key, Object[] args) {
		try {
			return format(bundle.getBundle().getString(String.valueOf(key)), args);
		} catch (MissingResourceException e) {
			return key + "??";
		}
	}

	private String format(String rawMessage, Object ... args) {
		if (args.length == 0)
			return rawMessage;
		// Double.NaNs are not formatted correctly using MessageFormat.format() but Object.toString() is o.k. 
		String[] s = new String[args.length];
		for (int i = 0; i < args.length; i++) {
			if (args[i] == null)
				s[i] = "null";
			else
				s[i] = args[i].toString();
		}
		MessageFormat format = new MessageFormat(rawMessage);
		return format.format(s);
	}

	/**
	 * Return the message category. The category, typically a short string, is
	 * supplied by the message's {@link MessageBundle}.
	 * 
	 * @return the category
	 */
	public String getCategory() {
		return bundle.getCategory();
	}

	/**
	 * Return the message key.
	 * 
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Return the formatted text of the message.
	 * 
	 * @return the message text
	 */
	public String getMessage() {
		if (message == null)
			message = findMessage(bundle, key, args);
		return message;
	}

	@Override
	public String toString() {
		return String.format("%s.%s - %s", getCategory(), key, getMessage());
	}
}