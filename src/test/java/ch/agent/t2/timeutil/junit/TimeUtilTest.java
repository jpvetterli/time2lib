package ch.agent.t2.timeutil.junit;

import java.util.Date;

import junit.framework.TestCase;
import ch.agent.t2.time.DateTime;
import ch.agent.t2.time.Day;
import ch.agent.t2.time.TimeIndex;
import ch.agent.t2.time.junit.CalendarUtil;
import ch.agent.t2.timeutil.TimeUtil;

public class TimeUtilTest extends TestCase {

	private static void dump(Object expr) {
		// System.out.println(expr);
	}

	TimeUtil util;
	
	public TimeUtilTest(String name) {
		super(name);
		util = new TimeUtil();
	}

	public void test1() {
		TimeIndex time = TimeUtil.now(Day.DOMAIN);
		Date d = new Date();
		String timeString = time.toString();
		String dateString = new CalendarUtil().format(d);
		dump(timeString + " " + dateString);
		assertEquals(true, dateString.startsWith(timeString));
	}
	
	public void test2() {
		TimeIndex time = TimeUtil.now(DateTime.DOMAIN);
		Date d = new Date();
		String timeString = time.toString();
		String dateString = new CalendarUtil().format(d);
		dump(timeString + " " + dateString);
		assertEquals(true, dateString.startsWith(timeString));
	}
}
