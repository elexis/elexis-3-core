/*******************************************************************************
 * Copyright (c) 2007-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     G. Weirich - initial API and implementation
 ******************************************************************************/
// $Id: StatusLine.java 23 2006-03-24 15:36:01Z rgw_ch $
/*
 * Created on 15.09.2005
 */
package ch.elexis.core.ui.actions;

import org.eclipse.ui.IViewSite;

public class StatusLine {
	
	public static void setText(IViewSite site, String text){
		site.getActionBars().getStatusLineManager().setMessage(text);
	}
	
}
