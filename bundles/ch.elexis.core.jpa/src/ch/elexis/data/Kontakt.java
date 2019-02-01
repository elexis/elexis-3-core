package ch.elexis.data;

import java.io.Serializable;

/**
 * This class is for compatibility reasons only! In several data types we find
 * the column "extInfo", where {@link HashMap} elements are stored. These may
 * contain elements of type Kontakt$statL which only map to a String. In order
 * to stay compatible with existing databases, and yet provide an independent
 * access we hence need to provide this compatibility class.
 *
 * @see ch.elexis.data.Kontakt in ch.elexis
 *
 */
public class Kontakt {
	public static class statL implements Comparable<statL>, Serializable {
		private static final long serialVersionUID = 10455663346456L;
		String v;
		int c;

		public statL() {
		}

		statL(String vv) {
			v = vv;
			c = 1;
		}

		public int compareTo(statL ot) {
			return ot.c - c;
		}
	}
}
