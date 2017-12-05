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
package ch.elexis.core.ui.article.dialogs;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.importer.div.importers.ExcelWrapper;
import ch.elexis.core.ui.article.service.ArticleServiceHolder;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.SWTHelper;

/**
 * Eine Buchung zu einem Patientenkonto zufügen
 * 
 * @author gerry
 * 
 */
public class ImportArticleDialog extends TitleAreaDialog {

	
	public ImportArticleDialog(Shell parentShell){
		super(parentShell);
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayout(new GridLayout());
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		new Label(ret, SWT.NONE).setText(ArticleServiceHolder.getStoreIds().toString()); //$NON-NLS-1$
		return ret;
	}
	
	@Override
	public void create(){
		super.create();
		setTitle("Artikel Importieren"); //$NON-NLS-1$
		setMessage("Bitte wählen Sie die Quelle aus, aus dem Sie die Artikel importieren möchten."); //$NON-NLS-1$
		getShell().setText("Artikel Import"); //$NON-NLS-1$
		getShell().setImage(Images.IMG_IMPORT.getImage());
	}
	
	@Override
	protected void okPressed(){
		ExcelWrapper w = new ExcelWrapper();
		super.okPressed();
	}
	
}
