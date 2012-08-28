time2lib : The Time2 Library
============================

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

The Time2 Library is a Java library providing 
generic time series with configurable time domains.

Distribution
------------

Starting with version 1.1.5, the distribution consists of a binary JAR with 
compiled classes and of a source JAR:

	t2-<version>.jar
	t2-<version>-sources.jar

In the file names `<version>` stands of course for the actual version,
`1.1.5` for example. For earlier versions, the suffix of the source JAR 
is `.source` instead of `-sources`.   

Dependencies
------------

The software is built with maven; dependencies are defined in the <q>POM</q>
file, included in the binary JAR:

	/META-INF/maven/ch.agent/t2/pom.xml

Building the software
---------------------

The recommended way is to use [git](http://git-scm.com) for accessing the
source and [maven](<http://maven.apache.org/>) for building. The procedure 
is easy, as maven takes care of locating and downloading dependencies:

	$ git clone https://github.com/jpvetterli/time2lib.git
	$ cd time2lib
	$ mvn install

This builds and installs the distribution JARs in your local maven
repository. They can also be found in the `target` directory.

Generating the documentation
----------------------------

If you are using maven, you can generate the javadocs with:

	$ mvn javadoc:jar

The documentation is packed into a JAR located in the `target` directory
and can be browsed by pointing at the file:

	target/apidocs/index.html

Using the library
-----------------

Here is a simple example of an application using the Time2 Library. 
It's called `Olympics.java`:

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

Create file `Olympics.java` in some directory and put a copy of 
`t2-1.0.0.jar` (or a later version) in the same directory.

Compile:

	$ javac -cp t2-1.0.0.jar Olympics.java

Execute:

	$ java -cp .:t2-1.0.0.jar Olympics
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
------------------------

The source is available on GitHub at 
<http://github.com/jpvetterli/time2lib.git>.

Finding more information
------------------------

More information on the Time2 Library is available at 
<http://agent.ch/timeseries/t2/>.

<small>Updated: 2012-08-28/jpv</small>

<link rel="stylesheet" type="text/css" href="README.css"/>

