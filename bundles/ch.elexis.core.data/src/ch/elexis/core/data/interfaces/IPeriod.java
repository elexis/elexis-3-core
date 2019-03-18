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

import ch.rgw.tools.TimeTool;

public interface IPeriod {
	
	TimeTool getStartTime();
	
	void setStartTime(TimeTool value);
	
	TimeTool getEndTime();
	
	void setEndTime(TimeTool value);
	
	String getLabel();
	
	String getId();
	
} // IPeriod
