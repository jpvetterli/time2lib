package ch.agent.t2.timeutil.junit;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;

import junit.framework.TestCase;
import ch.agent.core.KeyedException;
import ch.agent.t2.T2Msg.K;
import ch.agent.t2.time.Adjustment;
import ch.agent.t2.time.DateTime;
import ch.agent.t2.time.Day;
import ch.agent.t2.time.Month;
import ch.agent.t2.time.Resolution;
import ch.agent.t2.time.SimpleSubPeriodPattern;
import ch.agent.t2.time.TimeDomain;
import ch.agent.t2.time.TimeDomainDefinition;
import ch.agent.t2.time.TimeDomainManager;
import ch.agent.t2.time.TimeIndex;
import ch.agent.t2.time.Workday;
import ch.agent.t2.time.Year;
import ch.agent.t2.time.junit.CalendarUtil;
import ch.agent.t2.timeutil.JavaDateUtil;


public class JavaDateUtilTest extends TestCase {

	private GregorianCalendar gcal;
	private DateFormat fullDateFormat;
	
	public JavaDateUtilTest(String name) {
		super(name);
	}
	
	private TimeDomain usec() {
		return TimeDomainManager.getFactory().get(
				new TimeDomainDefinition("time_usec", Resolution.USEC, 0L), true);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		// use a time zone with no offset and no daylight savings time
		SimpleTimeZone z = new SimpleTimeZone(0, "Dummy zone");
		gcal = new GregorianCalendar(z);
		fullDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		fullDateFormat.setCalendar(gcal);
	}

	public void testJavaDate1() {
		try {
			Date date = new Date();
			TimeIndex time = JavaDateUtil.fromJavaDate(date, usec());
			String timeString = time.toString();
			String dateString = new CalendarUtil().format(date);
			System.out.println(timeString + " " + dateString);
			assertEquals(true, timeString.startsWith(dateString));
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception");
		}
	}

	public void testJavaDate2() {
		try {
			Date date = new Date();
			TimeIndex time = JavaDateUtil.fromJavaDate(date, usec(), Adjustment.NONE, false);
			String timeString = time.toString();
			String dateString = new CalendarUtil().format(date, "yyyy-MM-dd HH:mm:ss", false);
			System.out.println(timeString + " " + dateString);
			assertEquals(true, timeString.startsWith(dateString));
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception");
		}
	}
	
	public void testJavaDate3() {
		try {
			Date d = new Date(0);
			TimeIndex time = JavaDateUtil.fromJavaDate(d, usec());
			String timeString = time.toString();
			assertEquals(true, timeString.startsWith(new CalendarUtil().format(d)));
			System.out.println(timeString);
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception");
		}
	}

	public void testJavaDate4() {
		try {
			TimeIndex t = new Day("1964-01-01");
			Date date = JavaDateUtil.toJavaDate(t);
			System.out.println("testJavaDate4: " + t + " " + t.asLong() + " " + JavaDateUtil.toJavaDate(t) + " " + 
				new CalendarUtil().format(date));
			assertEquals(true, new CalendarUtil().format(date).startsWith(t.toString()));
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception");
		}
	}

	public void testAsDate1() {
		try {
			TimeIndex time = usec().time("1970-01-01");
			Date date = JavaDateUtil.toJavaDate(time);
			assertEquals("1970-01-01 00:00:00.000", fullDateFormat.format(date));
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}
	
	public void testAsDate1a() {
		try {
			TimeIndex time = usec().time("1970-01-02");
			Date date = JavaDateUtil.toJavaDate(time);
			assertEquals("1970-01-02 00:00:00.000", fullDateFormat.format(date));
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}
	
	public void testAsDate1b() {
		try {
			TimeIndex time = usec().time("1970-01-02 10:11:12.345678");
			Date date = JavaDateUtil.toJavaDate(time);
			assertEquals("1970-01-02 10:11:12.345", fullDateFormat.format(date));
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}
	
	public void testAsDate2() {
		try {
			TimeIndex time = new Day("1956-09-14");
			Date date = JavaDateUtil.toJavaDate(time);
			assertEquals("1956-09-14 00:00:00.000", fullDateFormat.format(date));
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}
	
	public void testAsDate3() {
		try {
			TimeIndex time = new Day("1986-09-14");
			Date date = JavaDateUtil.toJavaDate(time);
			assertEquals("1986-09-14 00:00:00.000", fullDateFormat.format(date));
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testAsDate4() {
		try {
			TimeIndex time = new Year("1986");
			Date date = JavaDateUtil.toJavaDate(time);
			assertEquals("1986-01-01 00:00:00.000", fullDateFormat.format(date));
		} catch (Exception e) {
			e.printStackTrace();
			assertEquals(null, e);
		}
	}
	
	public void testAsDate4a() {
		try {
			TimeIndex time = new Month("1986-09");
			System.out.println(time.toString());
			Date date = JavaDateUtil.toJavaDate(time);
			assertEquals("1986-09-01 00:00:00.000", fullDateFormat.format(date));
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testAsDate5() {
		try {
			TimeIndex time = new Day("0986-09-14");
			JavaDateUtil.toJavaDate(time);
		} catch (KeyedException e) {
			assertEquals(K.T7023, e.getMsg().getKey());
		}
	}
	
	public void testAsDate5a() {
		try {
			TimeIndex time = new Day("1582-10-14");
			JavaDateUtil.toJavaDate(time);
			fail("exception expected");
		} catch (KeyedException e) {
			assertEquals(K.T7023, e.getMsg().getKey());
			assertEquals("T2.T7023 - Cannot convert date 1582-10-14 to a Java date, because it is older than the Gregorian cutover.",
					e.getMsg().toString());
		}
	}
	public void testAsDate5b() {
		try {
			TimeIndex time = new Day("1582-10-15");
			Date date = JavaDateUtil.toJavaDate(time);
			assertEquals("1582-10-15 00:00:00.000", fullDateFormat.format(date));
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}

	public void testAsDate6() {
		try {
			TimeIndex time = new Workday("1986-09-15");
			Date date = JavaDateUtil.toJavaDate(time);
			assertEquals("1986-09-15 00:00:00.000", fullDateFormat.format(date));
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}
	
	public void testAsDate7() {
		try {
			TimeDomain d = TimeDomainManager.getFactory().get(
					new TimeDomainDefinition("time_sec", Resolution.SEC, 0L), true);
			TimeIndex time = d.time("1986-09-15 12:15:42");
			Date date = JavaDateUtil.toJavaDate(time);
			assertEquals("1986-09-15 12:15:42.000", fullDateFormat.format(date));
		} catch (Exception e) {
			assertEquals(null, e);
		}
	}
	
	public void testAsDate8() {
		try {
			TimeDomain d = TimeDomainManager.getFactory().get(
					new TimeDomainDefinition("abc", Resolution.MONTH, 0L,
					null, new SimpleSubPeriodPattern(Resolution.MONTH, Resolution.DAY, new int[]{10,20})), 
					true);
			TimeIndex t = d.time("2008-06-05", Adjustment.UP); // expect 2008-06-10
			Date date = JavaDateUtil.toJavaDate(t);
			assertEquals("2008-06-10 00:00:00.000", fullDateFormat.format(date));
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}
	
	public void testAsDate9() {
		try {
			TimeDomain d = TimeDomainManager.getFactory().get(
					new TimeDomainDefinition("abcd", Resolution.DAY, 0L,
					null, new SimpleSubPeriodPattern(Resolution.DAY, Resolution.SEC, new int[]{36000, 54000, 64800})), 
					true);
			TimeIndex t = d.time("2008-06-25 11:00:00", Adjustment.UP); // expect 2008-06-25 15:00:00
			Date date = JavaDateUtil.toJavaDate(t);
			assertEquals("2008-06-25 15:00:00.000", fullDateFormat.format(date));
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}
	
	@SuppressWarnings("deprecation")
	public void testDate1() {
		// test "no time zone and no DST in Time2"
		try {
			String date1 = "Jan 1, 2000, 12:15:00 EST";
			Date jdate1 = new Date(date1);
			TimeIndex t1 = JavaDateUtil.fromJavaDate(jdate1, DateTime.DOMAIN);
			System.out.println(date1 + " ---> " + jdate1 + " ---> " + t1);
			assertEquals("Sat Jan 01 18:15:00 CET 2000", jdate1.toString());
			assertEquals("2000-01-01 17:15:00", t1.toString());
			
			String date2 = "Jul 1, 2000, 12:15:00 EST";
			Date jdate2 = new Date(date2);
			TimeIndex t2 = JavaDateUtil.fromJavaDate(jdate2, DateTime.DOMAIN);
			System.out.println(date2 + " ---> " + jdate2 + " ---> " + t2);
			assertEquals("Sat Jul 01 19:15:00 CEST 2000", jdate2.toString());
			assertEquals("2000-07-01 17:15:00", t2.toString());

		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}
	
	/**
	 * Because Date is always in the current time zone
	 * toJavaDate of fromJavaDate is not a round trip. 
	 */
	public void testDate2() {
		try {
			@SuppressWarnings("deprecation")
			Date date1 = new Date(100, 0, 1);
			TimeIndex t = JavaDateUtil.fromJavaDate(date1, Day.DOMAIN, Adjustment.NONE, false);
			assertEquals("2000-01-01", t.toString());
			Date date2 = JavaDateUtil.toJavaDate(t);
			@SuppressWarnings("deprecation")
			Date date3 = new Date(date2.getTime() + date2.getTimezoneOffset() * 60000);
			assertEquals(date1, date3);
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

}
