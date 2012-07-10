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
 * Type: TimeParts
 * Version: 1.0.0
 */
package ch.agent.t2.time;

/**
 * TimeParts represents a time with its component parts. 
 * This class is not intended for application use but only
 * for moving data inside the library.
 * 
 * <p>
 * The components are:
 * <ol>
 * <li>year, a non-negative number
 * <li>month, in [1,12]
 * <li>day, in [1,n], where n is the last day of the month
 * <li>hour, in [0,59]
 * <li>min, in [0,59]
 * <li>sec, in [0,59]
 * <li>usec, in [0,999999] 
 * </ol>
 * 
 * Most methods are straightforward getters and setters without anything to comment.
 * 
 * @author Jean-Paul Vetterli
 * @version 1.0.0
 */
public class TimeParts {
	
	private long year; // >= 0
	private int month; // 1-12
	private int day;   // 1-last day of month
	private int hour;  // 0-23
	private int min;   // 0-59
	private int sec;   // 0-59
	private int usec;  // 0-999999
	
	public TimeParts() {
	}

	public void setYear(long year) {
		this.year = year;
	}

	public long getYear() {
		return year;
	}
	
	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public int getHour() {
		return hour;
	}

	public void setHour(int hour) {
		this.hour = hour;
	}

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;
	}

	public int getSec() {
		return sec;
	}

	public void setSec(int sec) {
		this.sec = sec;
	}

	public int getUsec() {
		return usec;
	}

	public void setUsec(int usec) {
		this.usec = usec;
	}

	/**
	 * Return true if any part negative.
	 * @return true if any part negative
	 */
	public boolean anyNegative() {
		return getYear() < 0 || getMonth() < 0 || getDay() < 0 || getHour() < 0 || getMin() < 0 || getSec() < 0 || getUsec() < 0;
	}

	@Override
	public String toString() {
		return String.format("%04d-%02d-%02d %02d:%02d:%02d.%06d", getYear(), getMonth(), getDay(), getHour(), getMin(), getSec(), getUsec());
	}

}
