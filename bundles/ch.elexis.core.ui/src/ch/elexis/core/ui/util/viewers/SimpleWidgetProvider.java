/*******************************************************************************
 * Copyright (c) 2005-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.core.ui.util.viewers;

import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.core.ui.util.viewers.ViewerConfigurer.WidgetProvider;

/**
 * Einfacher Widgetprovider. Gibt ein simples Widget des im Konstruktor genannten Types zurück
 * 
 * @author gerry
 * 
 */

public class SimpleWidgetProvider implements WidgetProvider {
	int type, style;
	
	CommonViewer cv;
	public static final int TYPE_TREE = 0;
	public static final int TYPE_LIST = 1;
	public static final int TYPE_TABLE = 2;
	public static final int TYPE_LAZYLIST = 3;
	
	public SimpleWidgetProvider(int type, int style, CommonViewer parent){
		this.type = type;
		this.style = style;
		cv = parent;
	}
	
	public StructuredViewer createViewer(Composite parent){
		switch (type) {
		case TYPE_TREE:
			return new TreeViewer(parent, style);
		case TYPE_LIST:
			return new ListViewer(parent, style);
		case TYPE_TABLE:
			return new TableViewer(parent, style);
		case TYPE_LAZYLIST:
			TableViewer ret = new TableViewer(parent, style | SWT.VIRTUAL);
			
			// ret.getTable().setItemCount(0);
			return ret;
		}
		return null;
	}
	
}