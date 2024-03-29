/**
 * Copyright (c) 2013 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 */
package ch.elexis.core.data.interfaces;

/**
 * Copy of core interface, used while refactoring.
 */
public interface ILabOrder {

	String getId();

	ILabResult getLabResult();

	void setLabResult(ILabResult value);

	ILabItem getLabItem();

	void setLabItem(ILabItem value);

	IPatient getPatientContact();

	void setPatientContact(IPatient value);

}
