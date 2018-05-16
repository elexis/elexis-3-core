/*******************************************************************************
 * Copyright (c) 2015 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.model;

public enum XidQuality {
	/** Quality value for an ID that is valid only in the context of the issuing program */
	ASSIGNMENT_LOCAL(1),
	/**
	 * Quality value for an ID that is valid within a geographic or politic context (e.g. a
	 * nationally assigned ID)
	 */
	ASSIGNMENT_REGIONAL(2),
	/** Quality value for an ID that can be used as global identifier */
	ASSIGNMENT_GLOBAL(3),
	/** Marker that the ID is a GUID (that is, guaranteed to exist only once through time and space) */
	QUALITY_GUID(4),
	/** combinations */
	ASSIGNMENT_LOCAL_QUALITY_GUID(5), 
	ASSIGNMENT_REGIONAL_QUALITY_GUID(6),
	ASSIGNMENT_GLOBAL_QUALITY_GUID(7);
	
	private final Integer value;
	
	private XidQuality(Integer value){
		this.value = value;
	}
	
	public Integer getValue(){
		return value;
	}
}
