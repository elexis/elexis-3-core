/*******************************************************************************
 * Copyright (c) 2007-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     G. Weirich - initial API and implementation
 ******************************************************************************/
package ch.elexis.scripting;

import ch.elexis.data.Person;
import ch.elexis.data.Query;
import ch.rgw.tools.StringTool;

public class CorrectGender {
	
	public void run(){
		Query<Person> qbe = new Query<Person>(Person.class);
		for (Person p : qbe.execute()) {
			String g = p.get(Person.SEX);
			if (StringTool.isNothing(g)) {
				String vn = p.get(Person.FIRSTNAME);
				if (!StringTool.isNothing(vn)) {
					if (StringTool.isFemale(vn)) {
						p.set(Person.SEX, Person.FEMALE);
					} else {
						p.set(Person.SEX, Person.MALE);
						p.createStdAnschrift();
					}
				}
			} else if (g.equals(Person.MALE)) {
				p.set(Person.FLD_ANSCHRIFT, p.createStdAnschrift());
			}
		}
	}
}
