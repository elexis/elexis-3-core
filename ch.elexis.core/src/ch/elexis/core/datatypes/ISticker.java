/*******************************************************************************
 * Copyright (c) 2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.core.datatypes;

import java.util.List;

/**
 * An ISticker is a Sticker that can be attached to any Object
 * 
 * @author gerry
 * 
 */
public interface ISticker extends Comparable<ISticker> {
	
	public static final String IMAGE_ID = "BildID";
	public static final String BACKGROUND = "bg";
	public static final String FOREGROUND = "vg";
	public static final String NAME = "Name";
	
	public String getId();
	
	public abstract String getLabel();
	
	public abstract int getWert();
	
	public abstract void setWert(int w);
	
	public abstract boolean delete();
	
	public abstract void setClassForSticker(Class<?> clazz);
	
	public abstract void removeClassForSticker(Class<?> clazz);
	
	public abstract List<String> getClassesForSticker();
	
	public abstract int compareTo(ISticker o);
	
	/**
	 * get the current visibility of this sticker to the end-user
	 * 
	 * @return true if the sticker should be shown in the user interface, false if not
	 * @author marlovitsh
	 */
	public abstract boolean getVisibility();
	
	/**
	 * set the current visibility of this sticker to the end-user param visibility new visibility
	 * 
	 * @author marlovitsh
	 */
	public abstract void setVisibility(boolean visibility);
	
}