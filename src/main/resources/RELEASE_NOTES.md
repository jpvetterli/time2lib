T2 LIBRARY RELEASE NOTES
========================

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

*************************************************************

ABOUT VERSION NUMBERS
---------------------

Version numbers are in 3 parts: a major, a medium, and a minor number.
The major number is incremented very rarely, when the software is
modified in such a way that clients using it need to be recoded or even 
redesigned. The medium number is incremented when modifications break 
backward compatibility. For all other modifications, only the minor 
number is incremented.

*************************************************************

<a name="v2_0_0">2017-10-05/jpv</a>

Version 2.0.0 &mdash; Elimination of singletons and support for nanoseconds
---------------------------------------------------------------------------

This is a major release which breaks compatibility with version 1.
Applications moving to version 2 need to reorganize their imports.
If they use factory methods to create time domains or time series, 
some recoding is necessary.

The release consists of three JARs:

- `t2-2.0.0.jar` (binaries)
- `t2-2.0.0-javadoc.jar`
- `t2-2.0.0-sources.jar`

There is also a new version of the __Time2 Library Demos__:

- `t2-demo-2.0.0.jar` (binaries)
- `t2-demo-2.0.0-javadoc.jar`
- `t2-demo-2.0.0-sources.jar`

The following points summarize the changes.

1. Time domain factories, which were implemented as singletons, and
had some synchronized methods, have been eliminated. Similar functionality 
is now provided by `TimeDomainCatalog`. Applications create a subclass 
of `ImmutableTimeDomainCatalog` and pass it to the relevant constructor.
A catalog is provided out the box in `DefaultTimeDomainCatalog`. Refer
to `SummerWinterOlympics` in `t2-demo` for a simple example of using
a time domain catalog.

1. `ExternalTimeFormat` has been replaced by `TimeScanner` and `TimeFormatter`.

1. `TimeSeriesFactory` has been eliminated. Time series are now created
with public constructors of `RegularTimeSeries` and `SparseTimeSeries`. 
A static factory method is provided by `AbstractTimeSeries#make`.

1. A `TimeParts` object is now immutable.

1. All classes of package `ch.agent.t2.time.engine` have been moved up one
package and the package eliminated.

1. All non-essential classes in package `ch.agent.t2.time` have been moved
to package `ch.agent.t2.applied`. 

1. Nanosecond resolution is now supported (`Resolution#NSEC`). Nanosecond
time starts in year 2000 and extends a few years beyond 2200. 

1. All Maven plugins have been upgraded (`pom.xml`).

1. The release includes various other fixes and improvements. 


*************************************************************

<a name="v1_1_7">2013-01-07/jpv</a>

Version 1.1.7 &mdash; Documentation and test cleanup
----------------------------------------------------

This is a maintenance release which behaves exactly like the previous 
version and is plug-compatible for applications. Various errors in the javadoc
comments have been fixed. Unit tests have been slightly modified to avoid 
writing on standard output. The administrative information included in files is
now limited to a copyright notice. Version tags have been removed from all 
files, because their maintenance cost greatly exceeds their value. Complete 
file history is available from the SCM (git).

*************************************************************

<a name="v1_1_6">2012-09-07/jpv</a>

Version 1.1.6 &mdash; Software deployed to the central maven repository
-----------------------------------------------------------------------

This is a maintenance release which behaves exactly like the previous 
version and is plug-compatible for applications. The POM file has been
modified to agree with 
[requirements](https://docs.sonatype.org/display/Repository/Central+Sync+Requirements)
for deployment to the central maven repository. 
Various typos in the documentation have been fixed.

The release consists of three JARs:

- `t2-1.1.6.jar` (binaries)
- `t2-1.1.6-javadoc.jar`
- `t2-1.1.6-sources.jar`

There is also a new version of the __Time2 Library Demos__:

- `t2-demo-1.0.2.jar` (binaries)
- `t2-demo-1.0.2-javadoc.jar`
- `t2-demo-1.0.2-sources.jar`

*************************************************************

<a name="v1_1_5">2012-08-28/jpv</a>

Version 1.1.5 &mdash; Project migrated to Maven
-----------------------------------------------

This is a maintenance release which behaves exactly like the previous 
version and is plug-compatible for applications. The organization of source 
files has been modified to agree with the standard recommended by 
[Apache Maven](http://maven.apache.org).

The release consists of two JARs:

- `t2-1.1.5.jar` (binaries)
- `t2-1.1.5-sources.jar` (sources only)

It was necessary to increment version numbers because the JAR checksums
are different, due to small differences related to builders. Another
change is in the name of the source JAR. The suffix has been modified and
is now `-sources.jar`. Previously it was `.source.jar`.

There is also a new version of the __Time2 Library Demos__:

- `t2-demo-1.0.1.jar` (binaries)
- `t2-demo-1.0.1-sources.jar` (sources only)

*************************************************************

<a name="v1_1_4">2012-03-21/jpv</a>

Version 1.1.4 &mdash; New exception type and refactoring of diagnostic message management
-----------------------------------------------------------------------------------------

This is a maintenance release which behaves exactly like the previous version 
and is plug-compatible for applications.

Methods of the Time2 library now throw a T2Exception instead of a KeyedException. 
The new exception extends the former, so there is no compatibility issue. 
Starting with this version, messages are prepared only when (and if) actually used. 
Because preparation includes fetching the message from the resource bundle and 
formatting the text using parameters, this change has a positive effective on
performance.
Within the sofware messages are now keyed symbolically.

The release consists of two jars:

- `t2-1.1.4.jar` (binaries)
- `t2-1.1.4.source.jar` (sources only)

Compared to version 1.1.3, the following files have been modified 
(new file version number in parentheses):

- ch/agent/core/KeyedMessage.java (1.0.1)
- ch/agent/t2/T2Exception.java : new class
- ch/agent/t2/T2Msg.java (1.0.1) : 
messages now accessed using a symbolic key instead of a literal number.
- ch/agent/t2/T2Msg.properties (1.0.5): all messages have a new key.
- ch/agent/t2/time/* : most classes modified to throw T2Exception and use new message key.
- ch/agent/t2/time/engine/* : ditto
- ch/agent/t2/timeseries/* : ditto
- ch/agent/t2/timeutil/* : ditto

*************************************************************

<a name="v1_1_3">2011-11-30/jpv</a>

Version 1.1.3 &mdash; Utility class for scanning dates and times in unusual formats
-----------------------------------------------------------------------------------

A class has been added to the utility package to 
provide support for scanning dates and times in unusual formats. 

The release consists of two jars:

- `t2-1.1.3.jar` (binaries)
- `t2-1.1.3.source.jar` (sources only)

Compared to version 1.1.2, the following files have been modified 
(new file version number in parentheses):

- ch/agent/t2/T2Msg.properties (1.0.4)
	- 32144 32145 32146 32147 added

- ch/agent/t2/timeutil/DateTimeScanner.java (1.0.0)
	- New utility class.

*************************************************************

<a name="v1_1_2">2011-10-10/jpv</a>

Version 1.1.2 &mdash; Support for ISO 8601:2004 calendar dates and times
------------------------------------------------------------------------

Starting today, release notes are sorted with the most recent release first.

The new version supports the representation for calendar dates and times 
defined in the ISO 8601:2004 international standard. Dates and times can be 
input using the basic or extended format. Leap seconds are tolerated. 
Time zone offsets are supported. A complete description is available from
the class documentation of DefaultExternalFormat.

The release consists of two jars:

- `t2-1.1.2.jar` (binaries)
- `t2-1.1.2.source.jar` (sources only)

Compared to version 1.1.1, the following files have been modified 
(new file version number in parentheses):

- ch/agent/t2/T2Msg.properties (1.0.3)
	- 32100 32110 32120 removed
	- 32042 32052 32062 32064 32072 32094 32095 32096 
	  32097 32098 32099 added
	- 32020 32030 32040 32050 32060 32070 32100 modified

- ch/agent/t2/time/DefaultExternalFormat.java (1.0.1)
	- scan(String) modified. Now adheres to ISO 8601:2004.
	- format(Resolution, TimeParts) modified. Inserts a plus sign in front 
	  of the year in some cases (ISO 8601).
	- documentation improved

- ch/agent/t2/time/TimeParts.java (1.0.1)
	- inner class TimeZoneOffset added 
	- asRawIndex(Resolution) added. The method supports ISO 8601 and 
	  replaces the static method TimeTools.makeRawIndex().
	- checkTimeComponentsAndApplyTimeZoneOffset() added
	- setTimeZoneOffset(TimeZoneOffset) added
	- documentation improved

- ch/agent/t2/time/engine/Time2.java (1.0.2)
	- toString(boolean) modified. Inserts a plus sign in front of 
	  the year in some cases (ISO 8601).

- ch/agent/t2/time/engine/TimeFactory.java (1.0.2)
	- getDayOfWeek(TimeIndex) modified
	- pack(TimeParts, Adjustment) modified

- ch/agent/t2/time/engine/TimeTools.java (1.0.1)
	- makeRawIndex(Resolution, TimeParts) modified and <b>deprecated</b>. 
	  It is now an adapter for TimeParts.asRawIndex().
	  New code should use TimeParts.asRawIndex directly.

*************************************************************

<a name="v1_1_1">2011-09-21/jpv</a>

Version 1.1.1 &mdash; Times from different domains now comparable
-----------------------------------------------------------------

The new version provides a functional improvement in the comparison of times.
It also includes various bug fixes and documentation improvements.

The release consists of two jars:

- `t2-1.1.1.jar` (binaries)
- `t2-1.1.1.source.jar` (sources only)

Compared to version 1.1.0, the following files have been modified 
(new file version number in parentheses):

- ch/agent/t2/T2Msg.properties (1.0.2)
	- 32008 removed
	- 32009 added

- ch/agent/t2/time/TimeDomain.java (1.0.1)
	- maxTime(boolean) added
	- minTime(boolean) added
	- maxTime(): comment modified
	- minTime(): comment modified

- ch/agent/t2/time/TimeDomainDefinition.java (1.0.1)
	- comments modified

- ch/agent/t2/time/engine/Time2.java (1.0.1)
	- class comment: new warning note about time comparisons
	- convert(TimeDomain, Adjustment): bug fix. 
	  When converting from YEAR or MONTH to higher resolution domains,
	  the month and day are now set to 1.
	- compareTo(TimeIndex otherTime): better implementation. It is now
	  possible to compare times from different domains.

- ch/agent/t2/time/engine/TimeFactory.java (1.0.1)
	- TimeDomain.maxTime(boolean) implementation added
	- TimeDomain.minTime(boolean) implementation added

- ch/agent/t2/timeseries/RegularTimeSeries.java (1.1.1)
	- fill(T, long): bug fix. 
	  In a special case, the method was breaking the rule that
	  missing values are all represented by the same object or null.

- ch/agent/t2/time/timeutil/DayExpression.java (1.0.1)
	- class comment: typo.
	- getDate(TimeDomain): bug fix and comment update.
	  The method was producing an unintended side effect.  

*************************************************************

<a name="v1_1_0">2011-08-22/jpv</a>

Version 1.1.0 &mdash; Null elements forbidden unless they represent a missing value
-----------------------------------------------------------------------------------

There is now a consistently enforced rule that elements of time series cannot be 
null unless null has been defined to represent missing values. The rule is not new,
but its implementation has been cleaned up.

The new version of the library includes
modifications in TimeIndexable and RegularTimeSeries which are not backward compatible.
These changes are highlighted below.

Various pieces of javadoc comment have been updated.

The release consists of two jars:

- `t2-1.1.0.jar` (binaries)
- `t2-1.1.0.source.jar` (sources only)

Compared to version 1.0.0, the following files have been modified 
(new file version number in parentheses):

- ch/agent/t2/T2Msg.properties (1.0.1)
	- header with copyright added
	- 40116 modified
	- 40117, 40118, 40121, 40124 added

- ch/agent/t2/time/{Adjustment, Resolution}.java (1.0.1)
	- add @version in javadoc

- ch/agent/t2/time/Range.java (1.0.1)
	- remove @version from javadoc of a nested class

- ch/agent/t2/timeseries/AbstractTimeSeries.java (1.0.1)
	- normalizeMissingValue() modified

- ch/agent/t2/timeseries/Filler.java (1.0.1)
	- fillHole() now throws an Exception

- ch/agent/t2/timeseries/RegularTimeSeries.java (1.1.0)
	- fill(long) now defined in interface TimeIndexable
	- fill(T, long) <b>now throws a KeyedException</b> (breaks backward compatibility) 
	- fill(Filler) <b>now throws a KeyedException</b> (breaks backward compatibility) 

- ch/agent/t2/timeseries/SparseTimeSeries (1.0.1)
	- remove @version from javadoc of a nested class

- ch/agent/t2/timeseries/TimeAddressable.java (1.0.1)
	- all put() methods: javadoc modified

- ch/agent/t2/timeseries/TimeIndexable.java (1.1.0)
	- fill(long) added
	- fill(T, long) <b>now throws a KeyedException</b> (breaks backward compatibility) 
	- fill(Filler) <b>now throws a KeyedException</b> (breaks backward compatibility) 

*************************************************************

<a name="v1_0_0">2011-08-17/jpv</a>

The same version (1.0.0) of the Time2 Library is released again.
The library version number is now encoded into the names
of jars, a README file and RELEASE NOTES have been added to the library,
and line terminators in a few files have been standardized.
The release consists of two jars:

- `t2-1.0.0.jar` (binaries)
- `t2-1.0.0.source.jar` (sources only)

Compared to the 2011-07-15 release,

- 2 files have been added to the libraries:
	- `README_1st.html`
	- `RELEASE_NOTES_v1.html`

- and in 4 files, line terminators have been cleaned up (CRLFs replaced with LFs):
	- ch/agent/core/KeyedException.java
	- ch/agent/core/MessageBundle.java
	- ch/agent/core/KeyedMessage.java
	- ch/agent/t2/T2Msg.java

*************************************************************

<a name="first_release">2011-07-15/jpv</a>

The Time2 Library is released as a beta version on SourceForge
<http://time2.sourceforge.net>. 
The release consists of two jars:

- `t2.jar` (binaries)
- `t2source.jar` (sources only)

<small>Updated: 2012-08-28/jpv</small>

<link rel="stylesheet" type="text/css" href="README.css"/>
