/*******************************************************************************
 * Copyright (c) 2009-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 
 *******************************************************************************/
package ch.elexis.core.ui.views.codesystems;

import java.io.FileInputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.core.ui.util.ImporterPage;
import ch.elexis.core.ui.util.SWTHelper;
import ch.rgw.tools.StringTool;

public class BlockImporter extends ImporterPage {
	
	@Override
	public Composite createPage(Composite parent){
		FileBasedImporter fbi = new FileBasedImporter(parent, this);
		fbi.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		return fbi;
	}
	
	@Override
	public IStatus doImport(IProgressMonitor monitor) throws Exception{
		
		String filename = results[0];
		if (StringTool.isNothing(filename)) {
			return new Status(Status.ERROR, "ch.elexis", "No file given"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		try {
			FileInputStream fips = new FileInputStream(filename);
			ch.elexis.core.ui.exchange.BlockImporter blc = new ch.elexis.core.ui.exchange.BlockImporter(fips);
			if (blc.finalizeImport().isOK()) {
				return Status.OK_STATUS;
			} else {
				return Status.CANCEL_STATUS;
			}
		} catch (Exception ex) {
			return new Status(Status.ERROR, "ch.elexis", "file not found: " + ex.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
	}
	
	@Override
	public String getDescription(){
		return Messages.getString("BlockImporter.importBlocks"); //$NON-NLS-1$
	}
	
	@Override
	public String getTitle(){
		return Messages.getString("BlockImporter.Blocks"); //$NON-NLS-1$
	}
	
}
