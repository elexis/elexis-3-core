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

import ch.elexis.core.types.PathologicDescription;
import ch.elexis.data.LabOrder;
import ch.rgw.tools.TimeTool;

/**
 * Copy of core interface, used while refactoring.
 */
public interface ILabResult {
	
	String getId();
	
	String getRefMale();

	void setRefMale(String value);

	String getRefFemale();

	void setRefFemale(String value);

	String getUnit();

	void setUnit(String value);

	TimeTool getAnalyseTime();

	void setAnalyseTime(TimeTool value);

	TimeTool getObservationTime();

	void setObservationTime(TimeTool value);

	TimeTool getTransmissionTime();

	void setTransmissionTime(TimeTool value);

	String getResult();

	void setResult(String value);

	int getFlags();

	void setFlags(int value);

	String getComment();

	void setComment(String value);

	IContact getOriginContact();

	void setOriginContact(IContact value);

	String getDate();

	void setDate(String value);

	ILabItem getItem();

	void setItem(ILabItem value);

	PathologicDescription getPathologicDescription();

	void setPathologicDescription(PathologicDescription value);
	
	String getLabel();
	
	LabOrder getLabOrder();
}
