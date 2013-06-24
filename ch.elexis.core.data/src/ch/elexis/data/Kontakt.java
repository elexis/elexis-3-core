/*******************************************************************************
 * Copyright (c) 2013 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.data;

import java.io.Serializable;

import ch.elexis.core.data.PersistentObject;

/**
 * For compatibility reasons only, avoids unfolding errors in {@link PersistentObject#fold}
 * @since 3.0.0
 * @deprecated
 */
public class Kontakt {
	public static class statL implements Comparable<statL>, Serializable {
		private static final long serialVersionUID = 10455663346456L;
		String v;
		int c;
		
		public statL(){}
		
		statL(String vv){
			v = vv;
			c = 1;
		}
		
		public int compareTo(statL ot){
			return ot.c - c;
		}
	}
}
