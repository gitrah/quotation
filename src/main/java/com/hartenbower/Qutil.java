package com.hartenbower;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


import scala.Tuple2;

public class Qutil {
    public static NumberFormat int2 = NumberFormat.getInstance();
    public static NumberFormat decimal3 = NumberFormat.getNumberInstance();

    static {
            int2.setMinimumIntegerDigits(2);
            int2.setMaximumIntegerDigits(2);
            decimal3.setMinimumFractionDigits(3);
            decimal3.setMaximumFractionDigits(3);
    }

    public static String decimal3(double v) {
            return decimal3.format(v);
    }
    public static String int2(int v) {
            return int2.format(v);
    }

    public final static int SECOND_MS = 1000;

	public final static int MINUTE = 60;

	public final static int MINUTE_MS = MINUTE * SECOND_MS;

	public final static int HOUR = 60 * MINUTE;

	public final static int HOUR_MS = HOUR * SECOND_MS;

	public final static int DAY = 24 * HOUR;

	public final static int DAY_MS = DAY * SECOND_MS;

	public final static float F_DAY_MS = DAY * SECOND_MS * 1.f;
	 public static List<Quotation> qods = (List) FileUtil.fromFile(
	  		"./quotations.ser", Quotation.class);
	
	public static Tuple2<Integer, Integer> ldIndexOf(String domain,
			String target, int distanceThreshold) {
		int trgLen = target.length();
		int dmnLen = domain.length();
		if (trgLen > dmnLen) {
			throw new IllegalArgumentException(target + " must be >= " + domain);
		}
		// examine targ-sized pieces of source, starting at begining and
		// shifting 1
		// char
		// each pass. if ldist of two frags is below threshold, save index with
		// ldist.
		// return index with lowest ldist or -1 if no matchy.
		int lowestLDist = dmnLen;
		int idxWithLowestLDist = -1;
		int currLD;
		for (int i = 0; i < dmnLen - trgLen; i++) {
			String subD = domain.substring(i, i + trgLen);
			currLD = StringUtil.getLevenshteinDistance(subD, target);
			if (currLD == 0) {
				// exact match
				return new Tuple2<Integer, Integer>(new Integer(currLD),
						new Integer(i));
			} else if (currLD < distanceThreshold && currLD < lowestLDist) {
				lowestLDist = currLD;
				idxWithLowestLDist = i;
			}
		}
		return new Tuple2<Integer, Integer>(lowestLDist, idxWithLowestLDist);
	}

	public static List<Quotation> search(String target, int levdist) {
		List<Quotation> res = new LinkedList<Quotation>();
		Tuple2<Integer, Integer> tup;
		for (Quotation q : qods) {
			tup = ldIndexOf(q.getAuthor() + q.getBody() + q.getAttribution(),
					target, levdist);
			if (tup._2 > -1) {
				res.add(q);
			}
		}
		return res;
	}

	public static String fromSeconds(double seconds) {
		double days = 0;
		if (seconds > DAY) {
			days = (int) (seconds / DAY);
		}
		double remainder = seconds - days * DAY;
		double hours = 0;
		if (remainder > HOUR) {
			hours = (int) (remainder / HOUR);
		}
		remainder = remainder - hours * HOUR;
		double minutes = 0;
		if (remainder > MINUTE) {
			minutes = (int) (remainder / MINUTE);
		}
		remainder = remainder - minutes * MINUTE;
		String time = (days > 0 ? "" + ((int) days) + " days " : "");
		time += (hours > 0 ? "" + ((int) hours) + "h " : "");
		time += (minutes > 0 ? "" + ((int) minutes) + "m " : "");
		time += (remainder > 0 ? ""
				+ (decimal3.format(remainder)) + "s" : "");
		return time;
	}

	public static String fromMillis(long millis) {
		return fromSeconds(millis / 1000.d);
	}

}
