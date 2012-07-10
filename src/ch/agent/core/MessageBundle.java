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
 * Package: ch.agent.core
 * Type: MessageBundle
 * Version: 1.0.0
 */
package ch.agent.core;

import java.util.ResourceBundle;

/**
 * A message bundle encapsulates a {@link ResourceBundle} and a category. The category
 * typically identifies an application or a project with a short string, and the
 * resource bundle contains a list of messages identified by a unique string.
 * <p>
 * If care is taken to select distinct category strings for different projects
 * composing a system, it will be possible to identify each message uniquely.
 * 
 * @author Jean-Paul Vetterli
 * @version 1.0.0
 */
public class MessageBundle {

	private String category;
	private ResourceBundle bundle;
	
	/**
	 * Construct a message bundle and assign it a category. The category is
	 * typically a short string identifying a project or an application. It is a
	 * good idea to use a unique string for each bundle.
	 * 
	 * @param category
	 *            a short string
	 * @param bundle
	 *            a {@link ResourceBundle}
	 */
	public MessageBundle(String category, ResourceBundle bundle) {
		if (category == null || bundle == null)
			throw new IllegalArgumentException("category or bundle null");
		this.category = category;
		this.bundle = bundle;
	}

	/**
	 * Return the category.
	 * 
	 * @return the category
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * Return the resource bundle.
	 * 
	 * @return the bundle
	 */
	public ResourceBundle getBundle() {
		return bundle;
	}

}