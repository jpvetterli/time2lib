<link rel="stylesheet" type="text/css" href="README.css"/>
The Time2 Library
===========================

2012-08-25/jpv

	Copyright 2011-2012 Hauser Olsson GmbH.
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
    	http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.

***

Thanks for your interest in the Time2 Library. This Java library provides 
generic time series with configurable time domains.

Building the library
-

The easiest way is to use maven (<http://maven.apache.org>). The process is easy, 
as maven takes care of locating and downloading dependencies:

	$ git clone https://github.com/jpvetterli/time2lib.git
	$ cd time2lib
	$ mvn package
	$ ls target/*.jar
	t2-1.1.4.jar

The current version number of the library will vary.

Using the library
-

This is a simple example of an application using the Time2 Library. 
The following is the text of _Olympics.java_:

	import ch.agent.t2.time.*;
	import ch.agent.t2.timeseries.*;

	/**
	 * Olympics is a small demo for the Time2 library.
	 * @author Jean-Paul Vetterli
	 */
	public class Olympics {
	
	    public static void main(String[] args) {
	        try {
	            /* define time domain "once every fourth year" */
	            TimeDomainDefinition year4def = new TimeDomainDefinition("year4", 
	                Resolution.YEAR, 0L, new Cycle(true, false, false, false));
	            TimeDomain year4 = TimeDomainManager.getFactory().get(year4def, true);
	            
	            /* define "missing value" for String (else, the default is null) */
	            String missingValue = "(missing)";
	            TimeSeriesFactory.getInstance().define(String.class, missingValue);
	
	            TimeAddressable<String> olympics = TimeSeriesFactory.make(year4, String.class);
	            olympics.put(year4.time("1896"), new String[] 
 	               {"Athens", "Paris", "Saint-Louis", "London", "Stockholm"});
	            olympics.put(year4.time("1920"), new String[] 
	                {"Antwerp", "Paris", "Amsterdam", "Los Angeles", "Berlin"});
	            
	            for (Observation<String> oly : olympics) {
	                System.out.println(oly.toString());
	            }
	        } catch (Exception e) {
	            System.err.println("Oops...\n" + e.getMessage());
        }
	    }
	}

Create file _Olympics.java_ in some directory and put a copy of _t2-1.1.4.jar_
in the same directory.

Compile:

	$ javac -cp t2-1.1.4.jar Olympics.java

Execute:

	$ java -cp .:t2-1.1.4.jar Olympics
	1896=Athens
	1900=Paris
	1904=Saint-Louis
	1908=London
	1912=Stockholm
	1916=(missing)
	1920=Antwerp
	1924=Paris
	1928=Amsterdam
	1932=Los Angeles
	1936=Berlin

Browsing the source code
-

The source is available on GitHub at <http://github.com/jpvetterli/time2lib.git>.

Finding more information
-

More information on the Time2 Library is available at <http://agent.ch/timeseries/t2>.

