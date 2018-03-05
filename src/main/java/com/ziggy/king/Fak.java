package com.ziggy.king;

import java.math.BigInteger;

public class Fak {

	public static void main(String[] args) {
		run(10028);
	}

	private static BigInteger stop = new BigInteger("1000000000000000000000000000000000000000000000000");

	private static BigInteger spuds = new BigInteger("0");

	private static BigInteger farmers = new BigInteger("0");
	private static BigInteger farmersMultiplier = new BigInteger("51840");

	private static BigInteger communes = new BigInteger("1674000000000000000000");
	private static BigInteger communesMultiplier = new BigInteger("1440");

	private static BigInteger collectives = new BigInteger("534000000000000");
	private static BigInteger collectivesMultiplier = new BigInteger("1440");

	private static BigInteger plantations = new BigInteger("452000000");
	private static BigInteger plantationsMultiplier = new BigInteger("1440");

	private static BigInteger hives = new BigInteger("1144");
	private static BigInteger hivesMultiplier = new BigInteger("1440");

	public static void longRun(long max) {
		// for (long run = 1L; run < max; run += 100) {
		// run(run);
		// }
		run(1144L);
	}

	public static void run(long seed) {
		// spuds = new BigInteger("0");
		// farmers = new BigInteger("0");
		// communes = new BigInteger("0");
		// collectives = new BigInteger("0");
		// plantations = new BigInteger(Long.valueOf(seed).toString());
		// hives = new BigInteger("0");

		System.out.println("time,time_readable,spuds,farmers,communes,collectives,plantations");
		for (long l = 1; l < Long.MAX_VALUE; ++l) {
			spuds = spuds.add(produce(l, 2, farmers, farmersMultiplier));
			farmers = farmers.add(produce(l, 6, communes, communesMultiplier));
			communes = communes.add(produce(l, 18, collectives, collectivesMultiplier));
			collectives = collectives.add(produce(l, 54, plantations, plantationsMultiplier));
			plantations = plantations.add(produce(l, 162, hives, hivesMultiplier));
			// hives = hives.add(produce(l, 60 * 60, new BigInteger("1000000"), new
			// BigInteger("1")));
			if (l % 10000 == 0) {
				System.out.println(l + "," + t(l) + "," + p(spuds) + "," + p(farmers) + "," + p(communes) + "," + p(collectives) + "," + p(plantations) + "," + p(hives));
			}

			if (spuds.compareTo(stop) == 1) {
				System.out.println(l + "," + t(l) + "," + p(spuds) + "," + p(farmers) + "," + p(communes) + "," + p(collectives) + "," + p(plantations) + "," + p(hives));
				System.out.println(seed + "," + l + "," + t(l));
				break;
			}
		}
	}

	// return produce count
	public static BigInteger farmers(long time) {
		if (time % 2 == 0) {
			return farmers.multiply(farmersMultiplier);
		} else {
			return BigInteger.ZERO;
		}
	}

	// returns produce count if time % timeMod == 0
	public static BigInteger produce(long time, long timeMod, BigInteger producers, BigInteger multiplier) {
		if (time % timeMod == 0) {
			return producers.multiply(multiplier);
		} else {
			return BigInteger.ZERO;
		}
	}

	private static BigInteger k = new BigInteger("1000");
	private static BigInteger m = new BigInteger("1000000");
	private static BigInteger b = new BigInteger("1000000000");
	private static BigInteger t = new BigInteger("1000000000000");
	private static BigInteger aa = new BigInteger("1000000000000000");
	private static BigInteger bb = new BigInteger("1000000000000000000");
	private static BigInteger cc = new BigInteger("1000000000000000000000");
	private static BigInteger dd = new BigInteger("1000000000000000000000000");
	private static BigInteger ee = new BigInteger("1000000000000000000000000000");
	private static BigInteger ff = new BigInteger("1000000000000000000000000000000");
	private static BigInteger gg = new BigInteger("1000000000000000000000000000000000");
	private static BigInteger hh = new BigInteger("1000000000000000000000000000000000000");
	private static BigInteger ii = new BigInteger("1000000000000000000000000000000000000000");
	private static BigInteger jj = new BigInteger("1000000000000000000000000000000000000000000");
	private static BigInteger kk = new BigInteger("1000000000000000000000000000000000000000000000");
	private static BigInteger ll = new BigInteger("1000000000000000000000000000000000000000000000000");

	public static String p(BigInteger num) {
		if (num.compareTo(ll) == 1) {
			return pp(num, ll, "LL");
		} else if (num.compareTo(kk) == 1) {
			return pp(num, kk, "KK");
		} else if (num.compareTo(jj) == 1) {
			return pp(num, jj, "JJ");
		} else if (num.compareTo(ii) == 1) {
			return pp(num, ii, "II");
		} else if (num.compareTo(hh) == 1) {
			return pp(num, hh, "HH");
		} else if (num.compareTo(gg) == 1) {
			return pp(num, gg, "GG");
		} else if (num.compareTo(ff) == 1) {
			return pp(num, ff, "FF");
		} else if (num.compareTo(ee) == 1) {
			return pp(num, ee, "EE");
		} else if (num.compareTo(dd) == 1) {
			return pp(num, dd, "DD");
		} else if (num.compareTo(cc) == 1) {
			return pp(num, cc, "CC");
		} else if (num.compareTo(bb) == 1) {
			return pp(num, bb, "BB");
		} else if (num.compareTo(aa) == 1) {
			return pp(num, aa, "AA");
		} else if (num.compareTo(t) == 1) {
			return pp(num, t, "T");
		} else if (num.compareTo(b) == 1) {
			return pp(num, b, "B");
		} else if (num.compareTo(m) == 1) {
			return pp(num, m, "M");
		} else if (num.compareTo(k) == 1) {
			return pp(num, k, "K");
		} else {
			return num.toString();
		}
	}

	public static String pp(BigInteger num, BigInteger big, String str) {
		BigInteger rem = num.mod(big).multiply(k).divide(big);
		return fs(num.divide(big).toString(), 3, " ") + "." + fs(rem.toString(), 3, "0") + str;
	}

	public static String s(Object so, int c, String p) {
		String s = so.toString();
		String sstr = s.substring(0, Math.min(s.length(), c));
		while (sstr.length() < c) {
			sstr += p;
		}
		return sstr;
	}

	public static String fs(Object so, int c, String p) {
		String s = so.toString();
		while (s.length() < c) {
			s = p + s;
		}
		return s;
	}

	public static String t(long l) {
		String s = fs(l % 60, 2, "0");
		String m = fs((l / 60) % 60, 2, "0");
		String h = fs((l / (60 * 60)) % 24, 2, " ");
		String d = fs((l / (60 * 60 * 24)), 2, " ");
		return d + "d " + h + "h " + m + "m " + s + "s";
	}

}
