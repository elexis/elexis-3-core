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
import org.eclipse.swt.program.Program;
import org.eclipse.ui.statushandlers.StatusManager;

import ch.elexis.core.data.status.ElexisStatus;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.util.IKonsExtension;

public class ExternalLink implements IKonsExtension {
	public static final String ID = "ch.elexis.text.ExternalLink"; //$NON-NLS-1$
	
	// EnhancedTextField mine;
	public String connect(IRichTextDisplay tf){
		tf.addXrefHandler(ID, this);
		return ID;
	}
	
	public boolean doLayout(StyleRange n, String provider, String id){
		n.underline = true;
		n.foreground = UiDesk.getColor(UiDesk.COL_BLUE);
		return true;
	}
	
	public boolean doXRef(String refProvider, String refID){
		try {
			int r = refID.lastIndexOf('.');
			String ext = ""; //$NON-NLS-1$
			if (r != -1) {
				ext = refID.substring(r + 1);
			}
			Program proggie = Program.findProgram(ext);
			if (proggie != null) {
				proggie.execute(refID);
			} else {
				if (Program.launch(refID) == false) {
					Runtime.getRuntime().exec(refID);
				}
			}
		} catch (Exception ex) {
			ElexisStatus status =
				new ElexisStatus(ElexisStatus.ERROR, Hub.PLUGIN_ID, ElexisStatus.CODE_NONE,
					Messages.ExternalLink_CouldNotStartFile, ex);
			StatusManager.getManager().handle(status);
		}
		
		return true;
	}
	
	public IAction[] getActions(){
		return null;
	}
	
	public void insert(Object o, int pos){}
	
	public void removeXRef(String refProvider, String refID){}
	
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
		throws CoreException{}
	
}
