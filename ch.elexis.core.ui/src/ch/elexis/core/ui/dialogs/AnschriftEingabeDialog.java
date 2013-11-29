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

package ch.elexis.core.ui.dialogs;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Anschrift;
import ch.elexis.data.Kontakt;
import ch.rgw.tools.StringTool;

public class AnschriftEingabeDialog extends TitleAreaDialog {
	Text str, plz, ort, land, postanschrift;
	Kontakt k;
	Anschrift an;
	
	public AnschriftEingabeDialog(Shell parentShell, Kontakt kt){
		super(parentShell);
		k = kt;
		an = k.getAnschrift();
	}
	
	@Override
	public void create(){
		super.create();
		setTitle(Messages.AnschriftEingabeDialog_enterAddress); //$NON-NLS-1$
		setMessage(Messages.AnschriftEingabeDialog_enterData); //$NON-NLS-1$
		getShell().setText(Messages.AnschriftEingabeDialog_postalAddress); //$NON-NLS-1$
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		Composite com = new Composite(parent, SWT.NONE);
		com.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		com.setLayout(new GridLayout(2, false));
		Label l1 = new Label(com, SWT.NONE);
		l1.setText(Messages.AnschriftEingabeDialog_street); //$NON-NLS-1$
		str = new Text(com, SWT.BORDER);
		str.setText(an.getStrasse());
		str.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		
		Label l2 = new Label(com, SWT.NONE);
		l2.setText(Messages.AnschriftEingabeDialog_zip); //$NON-NLS-1$
		plz = new Text(com, SWT.BORDER);
		plz.setText(an.getPlz());
		plz.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		Label l3 = new Label(com, SWT.NONE);
		l3.setText(Messages.AnschriftEingabeDialog_city); //$NON-NLS-1$
		ort = new Text(com, SWT.BORDER);
		ort.setText(an.getOrt());
		ort.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		Label l4 = new Label(com, SWT.NONE);
		l4.setText(Messages.AnschriftEingabeDialog_country); //$NON-NLS-1$
		land = new Text(com, SWT.BORDER);
		land.setText(an.getLand());
		land.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		Button post = new Button(com, SWT.PUSH);
		post.setText(Messages.AnschriftEingabeDialog_postalAddress); //$NON-NLS-1$
		post.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e){
				an.setStrasse(str.getText());
				an.setPlz(plz.getText());
				an.setOrt(ort.getText());
				an.setLand(land.getText());
				an.write(k);
				k.set(Kontakt.FLD_ANSCHRIFT, StringTool.leer); // clear the old postal to make
																// createStdAnschrift save a new one
				postanschrift.setText(k.createStdAnschrift());
			}
			
		});
		
		postanschrift = new Text(com, SWT.MULTI | SWT.BORDER);
		GridData gd = SWTHelper.getFillGridData(1, true, 1, true);
		// at least 3 lines height
		Point size = postanschrift.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		gd.heightHint = 4 * size.y;
		postanschrift.setLayoutData(gd);
		// get Postanschrift (may be empty if it's not yet defined)
		String pa = k.get(Kontakt.FLD_ANSCHRIFT);
		pa = pa.replaceAll("[\\r\\n]\\n", StringTool.lf); //$NON-NLS-1$
		postanschrift.setText(pa);
		
		// postanschrift info message
		new Label(com, SWT.NONE); // filler
		Label l5 = new Label(com, SWT.NONE);
		l5.setText(Messages.AnschriftEingabeDialog_postalAddressInfo); //$NON-NLS-1$
		l5.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		
		return com;
	}
	
	/*
	 * (Kein Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed(){
		an.setStrasse(str.getText());
		an.setPlz(plz.getText());
		an.setOrt(ort.getText());
		an.setLand(land.getText());
		an.write(k);
		String pa = postanschrift.getText().replaceAll("\\r\\n", StringTool.lf); //$NON-NLS-1$
		k.set(Kontakt.FLD_ANSCHRIFT, pa); //$NON-NLS-1$
		super.okPressed();
	}
	
	public Anschrift getAnschrift(){
		return an;
	}
	
}
