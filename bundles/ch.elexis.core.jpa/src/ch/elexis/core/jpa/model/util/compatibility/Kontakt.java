package ch.elexis.core.jpa.model.util.compatibility;

import java.io.Serializable;

public class Kontakt {
	public static class statL implements Comparable<statL>, Serializable {
		private static final long serialVersionUID = 10455663346456L;
		public String v;
		public int c;
		
		public statL(){}
		
		public statL(String vv){
			v = vv;
			c = 1;
		}
		
		public int compareTo(statL ot){
			return ot.c - c;
		}
	}
}
