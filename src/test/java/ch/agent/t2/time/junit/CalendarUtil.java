package ch.agent.t2.time.junit;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.agent.core.KeyedException;

/**
 * A utility used by some tests
 * @author jp
 *
 */
public class CalendarUtil {

	private Calendar utcCalendar;
	private TimeZone utcTimeZone;
	private Matcher dateTimeMatcher;
	
	public CalendarUtil() {
	}
	
	public Calendar getUTCCalendar() {
		if (utcCalendar == null) {
			utcCalendar = Calendar.getInstance(getUTCTimeZone());
			utcCalendar.setLenient(false);
		}
		return utcCalendar;
	}
	
	public TimeZone getUTCTimeZone() {
		if (utcTimeZone == null)
			utcTimeZone = TimeZone.getTimeZone("UTC");
		return utcTimeZone;
	}
	
	/**
	 * Return a Date in UTC.
	 * 
	 * @param year
	 * @param month
	 * @param day
	 * @param hour
	 * @param min
	 * @param sec
	 * @return
	 * @throws KeyedException
	 */
	public Date dateUTC(int year, int month, int day, int hour, int min, int sec) throws Exception {
		try {
			Calendar c = getUTCCalendar();
			c.set(year, month - 1, day, hour, min, sec);
			c.set(Calendar.MILLISECOND, 0);
			return new Date(c.getTimeInMillis());
		} catch (Exception e) {
			throw new Exception(String.format("Cannot construct a date/time from %d-%d-%d %d:%d:%d",
					year, month, day, hour, min, sec));
		}
	}
	
	/**
	 * Converts a string to a Date in UTC.
	 * The input format is mandatory:
	 * <xmp>yyyy-mm-dd hh:mm:ss</xmp>
	 * 
	 * @param dateAndTime
	 * @return
	 */
	public Date dateUTC(String dateAndTime) throws Exception {
		if (dateTimeMatcher == null)
			dateTimeMatcher = Pattern.compile
				("(\\d\\d\\d\\d)-(\\d\\d)-(\\d\\d) (\\d\\d):(\\d\\d):(\\d\\d)").matcher("");
		dateTimeMatcher.reset(dateAndTime);
		if (dateTimeMatcher.matches())
			return dateUTC(
				Integer.parseInt(dateTimeMatcher.group(1)), 
				Integer.parseInt(dateTimeMatcher.group(2)), 
				Integer.parseInt(dateTimeMatcher.group(3)), 
				Integer.parseInt(dateTimeMatcher.group(4)), 
				Integer.parseInt(dateTimeMatcher.group(5)), 
				Integer.parseInt(dateTimeMatcher.group(6)));
		else
			throw new Exception(String.format(
					"Cannot construct a date/time from \"%s\" " +
							"(expecting pattern \"yyyy-MM-dd hh:mm:ss\")", dateAndTime));			
	}

	/**
	 * Format the time given. The format can be specified using Java Date
	 * syntax. The formatting can be done in UTC or in the current time zone and
	 * DST.
	 * 
	 * @param time
	 * @param format
	 * @param UTC
	 * @return
	 */
	public String format(Date time, String format, boolean UTC) {
		SimpleDateFormat fmt = new SimpleDateFormat(format);
		if (UTC)
			fmt.setTimeZone(getUTCTimeZone());
		return fmt.format(time);
	}
	
	/**
	 * Format the date using the format yyyy-MM-dd HH:mm:ss in UTC. Note that
	 * Date.toString() formats in the current time zone.
	 * 
	 * @param time
	 * @return
	 */
	public String format(Date time) {
		return format(time, "yyyy-MM-dd HH:mm:ss", true);
	}
	
}
