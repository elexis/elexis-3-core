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
package ch.elexis.core.ui.text;

import java.util.Map;

import ch.elexis.core.text.model.SSDRange;
import ch.elexis.core.ui.util.IKonsExtension;
import ch.rgw.tools.GenericRange;

public interface IRichTextDisplay {
	
	public void addXrefHandler(String id, IKonsExtension ike);
	
	public void setXrefHandlers(Map<String, IKonsExtension> handlers);
	
	public void insertXRef(int pos, String textToDisplay, String providerId, String itemID);
	
	public void addDropReceiver(Class<?> clazz, IKonsExtension konsExtension);
	
	public void insertRange(SSDRange range);
	
	public String getWordUnderCursor();
	
	public String getContentsAsXML();
	
	public String getContentsPlaintext();
	
	public GenericRange getSelectedRange();
	
}
