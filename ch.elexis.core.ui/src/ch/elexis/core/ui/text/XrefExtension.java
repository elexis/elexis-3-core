/*******************************************************************************
 * Copyright (c) 2007-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/
package ch.elexis.core.ui.text;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.ui.PartInitException;

import ch.elexis.core.text.XRefExtensionConstants;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.util.IKonsExtension;
import ch.elexis.core.ui.views.TextView;
import ch.elexis.data.Brief;
import ch.rgw.tools.ExHandler;

public class XrefExtension implements IKonsExtension {
	IRichTextDisplay tx;
	
	public String connect(IRichTextDisplay tf){
		tx = tf;
		return XRefExtensionConstants.providerID;
	}
	
	public boolean doLayout(StyleRange n, String provider, String id){
		
		n.background = UiDesk.getColor(UiDesk.COL_LIGHTBLUE);
		n.foreground = UiDesk.getColor(UiDesk.COL_GREY20);
		return true;
	}
	
	public boolean doXRef(String refProvider, String refID){
		try {
			TextView tv =
				(TextView) Hub.plugin.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.showView(TextView.ID);
			tv.openDocument(Brief.load(refID));
			return true;
		} catch (PartInitException e) {
			ExHandler.handle(e);
		}
		return false;
	}
	
	public IAction[] getActions(){
		// TODO Auto-generated method stub
		return null;
	}
	
	public void removeXRef(String refProvider, String refID){
		// TODO Auto-generated method stub
		
	}
	
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
		throws CoreException{
		// TODO Auto-generated method stub
		
	}
	
	public void insert(Object o, int pos){
		
	}
	
}
