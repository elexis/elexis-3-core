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

import ch.elexis.core.types.LabItemTyp;


/**
 * Copy of core interface, used while refactoring.
 */
public interface ILabItem {

	String getId();
	
	LabItemTyp getTyp();

	void setTyp(LabItemTyp value);

	String getReferenceMale();

	void setReferenceMale(String value);

	String getReferenceFemale();

	void setReferenceFemale(String value);

	String getGroup();

	void setGroup(String value);

	String getPriority();

	void setPriority(String value);

	String getUnit();

	void setUnit(String value);

	String getKuerzel();

	void setKuerzel(String value);

	String getName();

	void setName(String value);

	int getDigits();

	void setDigits(int value);

	boolean isVisible();

	void setVisible(boolean value);
	
	String getLabel();
	
	boolean isNoReferenceValueItem();
}
