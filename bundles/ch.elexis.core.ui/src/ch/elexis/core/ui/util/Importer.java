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

import java.util.List;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.data.util.Extensions;

/**
 * This Dialog will open on the Global Action importAction (normally linked to File-Import It will
 * find all importer-Plugins that use the specified extension and display them in a tabbed folder.
 * 
 * @author gerry
 * 
 */
public class Importer extends TitleAreaDialog {
	private CTabFolder ctab;
	private String ext;
	
	/**
	 * Create an Importer environment for plugins at the specified point
	 * 
	 * @param extension
	 *            where to look for the plugins to load
	 */
	public Importer(Shell parentShell, String extension){
		super(parentShell);
		ext = extension;
	}
	
	/**
	 * Create the Dialog and the tabbed folder and let the plugins create their own ImporterPages
	 */
	@SuppressWarnings("unchecked")//$NON-NLS-1$
	@Override
	protected Control createDialogArea(Composite parent){
		ctab = new CTabFolder(parent, SWT.BOTTOM);
		List<ImporterPage> importers = Extensions.getClasses(ext, "Class"); //$NON-NLS-1$
		for (ImporterPage p : importers) {
			if (p != null) {
				CTabItem item = new CTabItem(ctab, SWT.NONE);
				item.setText(p.getTitle());
				item.setControl(p.createPage(ctab));
				item.setData(p);
			}
		}
		ctab.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		ctab.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e){
				CTabItem top = ctab.getSelection();
				if (top != null) {
					ImporterPage p = (ImporterPage) top.getData();
					setMessage(p.getDescription());
					top.getControl().setFocus();
				}
			}
			
		});
		return ctab;
	}
	
	/**
	 * Run the import method of the topmost plugin
	 */
	@Override
	protected void okPressed(){
		CTabItem top = ctab.getSelection();
		if (top != null) {
			ImporterPage page = (ImporterPage) top.getData();
			page.collect();
			page.run(false);
		}
		super.okPressed();
	}
	
	/**
	 * focuses the tab with the given name if it can be found
	 * 
	 * @param title
	 *            of the tab
	 */
	public void setFocusedTab(String title){
		for (CTabItem cTabItem : ctab.getItems()) {
			if (cTabItem.getText().equals(title)) {
				ctab.setSelection(cTabItem);
			}
		}
	}
}
