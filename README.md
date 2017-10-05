time2lib : The Time2 Library
============================

	Copyright 2011-2017 Hauser Olsson GmbH.
	
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

The Time2 Library is a Java library providing generic time series with 
configurable time domains. __Version 2__ is not plug-compatible with version 1,
because factories and singletons have been removed. Time2 applications must
now use injection, which increases reliability and testability. More details 
about what has changed is available from the release notes distributed with 
the software.

Distribution
------------

The distribution consists of a binary JAR with compiled classes, of a javadoc 
JAR and of a source JAR. For version x.y.z:

	t2-x.y.z.jar
	t2-x.y.z-javadoc.jar
	t2-x.y.z-sources.jar

For versions earlier than 1.1.6, there is no javadoc JAR. For versions earlier 
than 1.1.5, the suffix of the source JAR is `.source` instead of `-sources`. 

For Maven users
---------------

Starting with version 1.1.6, the software is available from the 
[Maven central repository](http://repo.maven.apache.org/maven2/ch/agent/t2/).
To use version x.y.z, insert the following dependency into your `pom.xml` file:

    <dependency>
      <groupId>ch.agent</groupId>
      <artifactId>t2</artifactId>
      <version>x.y.z</version>
      <scope>compile</scope>
    </dependency>

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


A simple example using the library
----------------------------------

Here is a simple example of an application using the Time2 Library. 
It's called `Olympics.java`:

    import ch.agent.t2.time.Cycle;
    import ch.agent.t2.time.Resolution;
    import ch.agent.t2.time.TimeDomain;
    import ch.agent.t2.time.TimeDomainDefinition;
    import ch.agent.t2.timeseries.Observation;
    import ch.agent.t2.timeseries.RegularTimeSeries;
    import ch.agent.t2.timeseries.TimeAddressable;

    /**
     * Olympics is a mini demo for the Time2 library.
     *
     * @author Jean-Paul Vetterli
     */
    public class Olympics {

    	public static void main(String[] args) {
    		try {
    			// create a time domain with one point every 4th year starting in year 0000
    			TimeDomain year4 = new TimeDomainDefinition(
    					"year4",
    					Resolution.YEAR,
    					0L,
    					new Cycle(true, false, false, false)
    			).asTimeDomain();
    			
    			// define "missing value" for String (else, the default is null)
    			String missingValue = "(missing)";
    			TimeAddressable<String> olympics = new RegularTimeSeries<String>(String.class, year4, missingValue);
    			
    			olympics.put(year4.time("1896"), new String[] {"Athens", "Paris", "Saint-Louis", "London", "Stockholm"});
    			olympics.put(year4.time("1920"), new String[] {"Antwerp", "Paris", "Amsterdam", "Los Angeles", "Berlin"});
    			
    			// notice: no games in 1916, during WW I
    			
    			for (Observation<String> oly : olympics) {
    				System.out.println(oly.toString());
    			}
    			
    		} catch (Exception e) {
    			System.err.println("Oops...\n" + e.getMessage());
    		}
    	}
    }

Create file `Olympics.java` in some directory and put a copy of 
`t2-2.0.0.jar` (or a later version) in the same directory.

Compile:

	$ javac -cp t2-2.0.0.jar Olympics.java

Execute:

	$ java -cp .:t2-2.0.0.jar Olympics
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

The source is available on [GitHub](http://github.com/jpvetterli/time2lib.git).

Finding more information
------------------------

More information on the Time2 Library is available at the 
[project website](http://agent.ch/timeseries/t2/).

<small>Updated: 2017-10-04/jpv</small>

<link rel="stylesheet" type="text/css" href="README.css"/>

