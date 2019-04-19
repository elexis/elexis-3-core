/*******************************************************************************
 * Copyright (c) 2006-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/
package ch.elexis.core.ui.util;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

/**
 * Ein DecoratedsString ist ein String, der auch eine Farbe und ein Icon haben kann
 * 
 * @author gerry
 * 
 */
public class DecoratedString {
	String text;
	String value;
	Color foreground, background;
	Image icon;
	
	public DecoratedString(String t){
		this(t, (String) null);
	}
	
	public DecoratedString(String text, String value) {
		this.text = text;
		this.value = value;
	}
	
	public DecoratedString(String t, Color fore, Color back){
		text = t;
		foreground = fore;
		background = back;
	}
	
	public DecoratedString(String t, Color fore){
		text = t;
		foreground = fore;
	}
	
	public void setText(String t){
		text = t;
	}
	
	public void setForeground(Color c){
		foreground = c;
	}
	
	public String getText(){
		return text;
	}
	
	public Color getForeground(){
		return foreground;
	}
	
	public Color getBackground(){
		return background;
	}
	
	/**
	 * 
	 * @return
	 * @since 3.7 the actual value to use
	 */
	public String getValue(){
		return value;
	}
}
