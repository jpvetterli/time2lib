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

/**
 * Adjustment defines three adjustment modes used when parsing time strings or converting 
 * between time domains.
 * Sometimes a time which can be expressed using the methods provided does
 * not correspond to a valid time in the given time domain. For example
 * January 1, 2000 is a Saturday, and cannot be represented in a time domain
 * which excludes weekends. Some methods allow to specify an adjustment mode to manage
 * such situations. There are three possible modes:
 * <ol>
 * <li>Adjust downwards (to December 31, 1999 in the example)
 * <li>Adjust upwards (to Monday, January 3, 2000)
 * <li>Do not adjust and produce an error.
 * </ol>
 * 
 * @author Jean-Paul Vetterli
 */
public enum Adjustment {
	/**
	 * Requires downward adjustment.
	 */
	DOWN, /**
	 * 
	 * Requires upward adjustment.
	 */
	UP, /**
	 * 
	 * Forbids any adjustment.
	 */
	NONE
}