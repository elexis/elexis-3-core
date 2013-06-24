/*******************************************************************************
 * Copyright (c) 2005-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.rgw.tools;

public class GenericRange {
	int pos;
	int len;
	
	public GenericRange(GenericRange other){
		pos = other.pos;
		len = other.len;
	}
	
	public GenericRange(){
		pos = 0;
		len = 0;
	}
	
	public GenericRange(int pos){
		this.pos = pos;
		len = 0;
	}
	
	public GenericRange(int start, int len){
		pos = start;
		this.len = len;
	}
	
	public int getLength(){
		return len;
	}
	
	public int getPos(){
		return pos;
	}
	
	public int getEnd(){
		return pos + len - 1;
	}
	
	public void setPos(int p){
		int end = getEnd();
		pos = p;
		setEnd(end);
	}
	
	public void setLen(int l){
		len = l;
	}
	
	public void setEnd(int e){
		len = e - pos;
	}
	
	public static final int IS_BEFORE_OTHER = 1;
	public static final int IS_AFTER_OTHER = 2;
	public static final int IS_INSIDE_OTHER = 3;
	public static final int IS_AT_BEGIN_OF_OTHER = 4;
	public static final int IS_AT_END_OF_OTHER = 5;
	public static final int IS_OVER_OTHER = 6;
	public static final int IS_ZERO_LENGTH = 7;
	
	/**
	 * Feststellen, wie dise Range in Bezug auf eine andere liegt
	 * 
	 * @return <ul>
	 *         <li>IS_BEFORE-OTHER: Liegt ganz vor der anderen</li>
	 *         <li>IS_AFTER_OTHER: Liegt ganz nach der anderen</li>
	 *         <li>IS_INSIDE_OTHER: Liegt ganz innerhalb der anderen</li>
	 *         <li>IS_AT_BEGIN_OF_OTHER: überlappt den Anfang der anderen</li>
	 *         <li>IS_AT_END_OF_OTHER: überlappt das Ende der anderen</li>
	 *         <li>IS_OVER_OTHER: überlagert die andere ganz</li>
	 *         <li>IS_ZERO_LENGTH: Länge null sekunden</li>
	 *         </ul>
	 */
	public int positionTo(GenericRange other){
		if (len == 0 || (other.len == 0)) {
			return IS_ZERO_LENGTH;
		}
		if (pos <= other.pos) {
			if (getEnd() <= other.pos) {
				return IS_BEFORE_OTHER;
			} else if (getEnd() >= other.getEnd()) {
				return IS_OVER_OTHER;
			} else {
				return IS_AT_BEGIN_OF_OTHER;
			}
		} else // offset > other.offset)
		{
			if (getEnd() <= other.getEnd()) {
				return IS_INSIDE_OTHER;
			} else if (pos >= other.getEnd()) {
				return IS_AFTER_OTHER;
			} else {
				return IS_AT_END_OF_OTHER;
			}
		}
	}
	
	/**
	 * Schnitt-Range aus zwei Ranges erzeugen
	 * 
	 * @param other
	 *            die andere Range
	 * @return eine neue Range, die die Überlappung enthält oder null, wenn keine überlappung
	 *         vorliegt
	 */
	public GenericRange overlap(GenericRange other){
		GenericRange ret = null;
		switch (positionTo(other)) {
		case IS_BEFORE_OTHER:
		case IS_AFTER_OTHER:
		case IS_ZERO_LENGTH:
			return null;
		case IS_AT_BEGIN_OF_OTHER:
			ret = new GenericRange(pos);
			ret.setEnd(other.getEnd());
			return ret;
		case IS_OVER_OTHER:
			return other;
		case IS_INSIDE_OTHER:
			return other;
		case IS_AT_END_OF_OTHER:
			ret = new GenericRange(other.getEnd());
			ret.setEnd(getEnd());
			return ret;
		}
		return ret;
	}
}
