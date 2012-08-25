package ch.agent.t2.time.junit;

import java.util.Random;

import junit.framework.TestCase;
import ch.agent.t2.time.Day;
import ch.agent.t2.time.TimeIndex;

public class TimeLargeTest extends TestCase {

	private static class Singleton {
		private static Random random;
		static {
			random = new Random();
		};
	}

	/**
	 * Return a random int uniformly distributed in [a, b[.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	private int nextInt(int a, int b) {
		if (a > b)
			throw new IllegalArgumentException("a > b");
		return a + Singleton.random.nextInt(b - a);
	}
	
	public void testRandomDates() {
		int errors = 0;
		int outCount = 0;
		int sample = 1000000;
		long millis = System.currentTimeMillis();
		for (int i = 0; i < sample; i++) {
			int y = nextInt(0, 3333);
			int m = nextInt(1, 12);
			int d = nextInt(1, 31);
			String date = String.format("%04d-%02d-%02d", y, m, d);
			TimeIndex t = null;
			try {
				t = new Day(date);
			} catch (Exception e) {
				// System.out.println("invalid date: " + date);
				continue;
			}
			String tString = t.toString();
			if (!date.equals(tString)) {
				errors++;
				outCount++;
				if (outCount < 11)
					System.out.println(date + " " + tString);
				else if (outCount == 11)
					System.out.println("stop printing errors (but continue testing)");
			}
		}
		millis -= System.currentTimeMillis();
		System.out.println(String.format("random sample of size %d processed in %dms", sample, -millis));
		assertEquals(0, errors);
	}

}

