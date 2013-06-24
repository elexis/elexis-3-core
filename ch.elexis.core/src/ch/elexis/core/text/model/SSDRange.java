/*******************************************************************************
 * Copyright (c) 2010-2013, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 
 *******************************************************************************/
package ch.elexis.core.text.model;

import org.jdom.Element;

/**
 * An SSDRange is some part of a SimpleStructuredDocument. It has a position and
 * a length within the text Optionally, it can be placed outside the text flow.
 * In that case, it must provide a viewport position relative to the character
 * indicated by position. The contents of the Range is totally implementation
 * specific. It might be some text or some graphics or both.
 * 
 * @author gerry
 * 
 */
public class SSDRange {
	public static final String TYPE_MARKUP = "markup";
	public static final String TYPE_XREF = "xref";

	public static final String ELEM_NAME = "range";
	/** It is not possible to set the cursor within this range */
	private static final String ATTR_LOCKED = "locked";
	/** Identifier of this range. Used to find a matching renderer */
	private static final String ATTR_TYPENAME = "typename";
	/** ID of this range. Hint for the renderer */
	private static final String ATTR_ID = "ID";
	/**
	 * Some range types are displayed off the text flow. This is a hint in wich
	 * region of the screen to display
	 */
	public static final String ATTR_VIEWPORT = "viewport";
	/**
	 * Length of the range within the text flow in characters
	 */
	private static final String ATTR_LENGTH = "length";
	/**
	 * Position of the range as characters from text start.
	 */
	private static final String ATTR_START_OFFSET = "startOffset";
	/**
	 * Hint what to to if an application does not know this type of Range one
	 * of: prefix:(prefix), append:(append), replace:replace
	 */
	private static final String ATTR_HINT = "hint";

	public static final String STYLE_BOLD = "bold";
	public static final String STYLE_ITALIC = "italic";
	public static final String STYLE_UNDERLINE = "underline";
	public static final String STYLE_FOREGROUND = "foreground:";
	public static final String STYLE_BACKGROUND = "backgreound:";
	Object data;
	String id;
	String typename;
	int length;
	int position;
	Rectangle viewport;
	boolean bLocked;
	String hint;
	String contents;

	public SSDRange(Element el) {
		id = el.getAttributeValue(ATTR_ID);
		typename = el.getAttributeValue(ATTR_TYPENAME);
		position = Integer.parseInt(el.getAttributeValue(ATTR_START_OFFSET));
		length = Integer.parseInt(el.getAttributeValue(ATTR_LENGTH));
		// length=Integer.parseInt(el.getAttributeValue(""));
		hint = el.getAttributeValue(ATTR_HINT);
		String v = el.getAttributeValue(ATTR_VIEWPORT);
		if (v != null && v.matches("\\d+,\\d+,\\d+,\\d+")) {
			String[] koor = v.split(",");
			viewport = new Rectangle(Integer.parseInt(koor[0]),
					Integer.parseInt(koor[1]), Integer.parseInt(koor[2]),
					Integer.parseInt(koor[3]));
		}
		contents = el.getText();
	}

	public SSDRange(final int start, final int len, String typename, String id) {
		length = len;
		position = start;
		this.typename = typename;
		this.id = id;
	}

	public boolean isLocked() {
		return bLocked;
	}

	public int getLength() {
		return length;
	}

	public int getPosition() {
		return position;
	}

	public String getType() {
		return typename;
	}

	public String getID() {
		return id;
	}

	public void setLength(final int pos) {
		length = pos;
	}

	public void setPosition(final int pos) {
		position = pos;
	}

	public Rectangle getViewPort() {
		return viewport;
	}

	public void setViewPort(Rectangle r) {
		this.viewport = r;
	}

	public String getHint() {
		return hint;
	}

	public String getContents() {
		return contents;
	}

	public void setContents(String c) {
		contents = c;
	}

	private void setAttributeIfExists(Element e, String attr, String value) {
		if (attr != null && value != null) {
			e.setAttribute(attr, value);
		}
	}

	/**
	 * Link some user defined data to the object
	 * 
	 * @param data
	 */
	public void setData(Object data) {
		this.data = data;
	}

	/**
	 * return user defined fata
	 * 
	 * @return data as perviously set by setData()
	 */
	public Object getData() {
		return data;
	}

	/**
	 * @since 3.0.0 trimmed down copy of org.eclipse.swt.graphics.Rectangle to satisfy dependency
	 */
	public class Rectangle {
		int x, y;
		int width, height;

		/**
		 * Construct a new instance of this class given the x, y, width and
		 * height values.
		 * 
		 * @param x
		 *            the x coordinate of the origin of the rectangle
		 * @param y
		 *            the y coordinate of the origin of the rectangle
		 * @param width
		 *            the width of the rectangle
		 * @param height
		 *            the height of the rectangle
		 */
		public Rectangle(int x, int y, int width, int height) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}
	}
}
