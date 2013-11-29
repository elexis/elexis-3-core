/*******************************************************************************
 * Copyright (c) 2006-2009, G. Weirich, D. Lutz, P. Schönbucher and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/
package ch.elexis.core.ui.dialogs;

import java.io.IOException;
import java.util.List;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.KonsFilter;
import ch.elexis.core.ui.icons.ImageSize;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Fall;
import ch.elexis.data.Patient;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;

/**
 * Einstellen eines Konsultationsfilters
 * 
 * @author gerry
 * 
 */
public class KonsFilterDialog extends TitleAreaDialog {
	KonsFilter filter;
	Patient pat;
	Combo cbFaelle;
	Fall[] faelle;
	Text tBed;
	Button bCase, bRegex;
	
	public KonsFilterDialog(Patient p, KonsFilter kf){
		super(UiDesk.getTopShell());
		filter = kf;
		pat = p;
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayout(new GridLayout());
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		new Label(ret, SWT.NONE).setText(Messages.KonsFilterDialog_onlyForCase); //$NON-NLS-1$
		cbFaelle = new Combo(ret, SWT.SINGLE);
		cbFaelle.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		faelle = pat.getFaelle();
		cbFaelle.add(Messages.KonsFilterDialog_dontMind); //$NON-NLS-1$
		for (Fall f : faelle) {
			cbFaelle.add(f.getLabel());
		}
		new Label(ret, SWT.SEPARATOR | SWT.HORIZONTAL);
		new Label(ret, SWT.WRAP).setText(Messages.KonsFilterDialog_enterWords + //$NON-NLS-1$
			Messages.KonsFilterDialog_separateFilters); //$NON-NLS-1$
		
		tBed = SWTHelper.createText(ret, 4, SWT.BORDER);
		new Label(ret, SWT.SEPARATOR | SWT.HORIZONTAL);
		bCase = new Button(ret, SWT.CHECK);
		bCase.setText(Messages.KonsFilterDialog_respectCase); //$NON-NLS-1$
		bRegex = new Button(ret, SWT.CHECK);
		bRegex.setText(Messages.KonsFilterDialog_regExp); //$NON-NLS-1$
		cbFaelle.select(0);
		bCase.setSelection(true);
		return ret;
	}
	
	@Override
	public void create(){
		super.create();
		setTitle(Messages.KonsFilterDialog_konsFilter); //$NON-NLS-1$
		setMessage(Messages.KonsFilterDialog_enterFilterExpressions); //$NON-NLS-1$
		getShell().setText(Messages.KonsFilterDialog_filter); //$NON-NLS-1$
		setTitleImage(Images.IMG_LOGO.getImage(ImageSize._75x66_TitleDialogIconSize));
	}
	
	@Override
	protected void okPressed(){
		filter = new KonsFilter();
		if (cbFaelle.getSelectionIndex() > 0) {
			Fall f = faelle[cbFaelle.getSelectionIndex() - 1];
			filter.setFall(f);
		}
		filter.setCaseSensitive(bCase.getSelection());
		filter.setAsRegEx(bRegex.getSelection());
		String cc = tBed.getText();
		if (!StringTool.isNothing(cc)) {
			StringTool.tokenizer tk =
				new StringTool.tokenizer(cc, " ", StringTool.tokenizer.DOUBLE_QUOTED_TOKENS); //$NON-NLS-1$
			try {
				List<String> tokens = tk.tokenize();
				int last = 0;
				for (String t : tokens) {
					if (t.equals("OR")) { //$NON-NLS-1$
						last = KonsFilter.OR;
					} else if (t.equals("AND")) { //$NON-NLS-1$
						last = KonsFilter.AND;
					} else if (t.equals("NOT")) { //$NON-NLS-1$
						last |= KonsFilter.NOT;
					} else {
						filter.addConstraint(last, t);
					}
				}
			} catch (IOException e) {
				ExHandler.handle(e);
			}
		}
		super.okPressed();
	}
	
	public KonsFilter getResult(){
		return filter;
	}
	
}
