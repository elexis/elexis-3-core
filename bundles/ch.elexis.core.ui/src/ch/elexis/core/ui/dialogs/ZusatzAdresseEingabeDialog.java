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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
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

import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.types.AddressType;
import ch.elexis.core.types.LocalizeUtil;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Kontakt;
import ch.elexis.data.ZusatzAdresse;
import ch.elexis.data.dto.ZusatzAdresseDTO;
import ch.rgw.tools.StringTool;

public class ZusatzAdresseEingabeDialog extends TitleAreaDialog {
	private Text str1, str2, plz, ort, land, postanschrift;
	private ComboViewer comboAddressType;
	
	private final ZusatzAdresseDTO zusatzAdresseDTO;
	private final ZusatzAdresse zusatzAdresse;
	private boolean locked = false;

	public ZusatzAdresseEingabeDialog(Shell parentShell, Kontakt kontakt){
		this(parentShell, kontakt, null);
	}
	
	public ZusatzAdresseEingabeDialog(Shell parentShell, Kontakt kontakt,
		ZusatzAdresse paramZusatzadresse){
		super(parentShell);
		this.zusatzAdresse =
			ZusatzAdresse.load(paramZusatzadresse != null ? paramZusatzadresse.getId() : null);
		this.zusatzAdresseDTO = zusatzAdresse.getDTO();
		this.zusatzAdresseDTO.setKontaktId(kontakt.getId());
	}
	
	public ZusatzAdresseEingabeDialog(Shell parentShell, Kontakt kontakt,
		ZusatzAdresse paramZusatzadresse, boolean locked){
		this(parentShell, kontakt, paramZusatzadresse);
		this.locked = locked;
	}
	
	@Override
	public void create(){
		super.create();
		setTitle(Messages.AnschriftEingabeDialog_enterAddress); //$NON-NLS-1$
		setMessage(Messages.AnschriftEingabeDialog_enterData); //$NON-NLS-1$
		getShell().setText(Messages.AnschriftEingabeDialog_postalAddress); //$NON-NLS-1$
		
		if (locked) {
			Button btnOk = getButton(IDialogConstants.OK_ID);
			if (btnOk != null) {
				btnOk.setEnabled(false);
			}
		}
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		Composite com = new Composite(parent, SWT.NONE);
		com.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		com.setLayout(new GridLayout(2, false));
		
		Composite compAddressType = new Composite(com, SWT.NONE);
		compAddressType.setLayoutData(SWTHelper.getFillGridData(2, true, 1, true));
		compAddressType.setLayout(new GridLayout(2, false));
		Label lblAddressType = new Label(compAddressType, SWT.NONE);
		lblAddressType.setText("Type");
		comboAddressType =
			new ComboViewer(compAddressType, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
		comboAddressType.setContentProvider(ArrayContentProvider.getInstance());
		comboAddressType.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element){
				return LocalizeUtil.getLocaleText((AddressType) element);
			}
		});
		List<AddressType> comboValues = new ArrayList<>(Arrays.asList(AddressType.values()));
		comboValues.remove(AddressType.PRINCIPAL_RESIDENCE); //principal residence is defined within patient - contact relation
		comboAddressType.setInput(comboValues);
		
		Label l1 = new Label(com, SWT.NONE);
		l1.setText(Messages.AnschriftEingabeDialog_street + "1"); //$NON-NLS-1$
		str1 = new Text(com, SWT.BORDER);
		
		str1.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		
		l1 = new Label(com, SWT.NONE);
		l1.setText(Messages.AnschriftEingabeDialog_street + "2"); //$NON-NLS-1$
		str2 = new Text(com, SWT.BORDER);
		
		str2.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		
		Label l2 = new Label(com, SWT.NONE);
		l2.setText(Messages.AnschriftEingabeDialog_zip); //$NON-NLS-1$
		plz = new Text(com, SWT.BORDER);
		
		plz.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		Label l3 = new Label(com, SWT.NONE);
		l3.setText(Messages.AnschriftEingabeDialog_city); //$NON-NLS-1$
		ort = new Text(com, SWT.BORDER);
		
		ort.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		Label l4 = new Label(com, SWT.NONE);
		l4.setText(Messages.AnschriftEingabeDialog_country); //$NON-NLS-1$
		land = new Text(com, SWT.BORDER);
		
		land.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		
		Button post = new Button(com, SWT.PUSH);
		post.setText(Messages.AnschriftEingabeDialog_postalAddress); //$NON-NLS-1$
		post.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e){
				setFieldValues();
				Kontakt kontakt = Kontakt.load(zusatzAdresseDTO.getKontaktId());
				zusatzAdresseDTO.setPostalAddress(
					kontakt.getSalutation()
						+ zusatzAdresse.getEtikette(false, true, zusatzAdresseDTO.getKontaktId(),
							zusatzAdresseDTO.getStreet1(), zusatzAdresseDTO.getCountry(),
							zusatzAdresseDTO.getZip(), zusatzAdresseDTO.getPlace()));
				loadFieldValues();
			}
		});
		
		postanschrift = new Text(com, SWT.MULTI | SWT.BORDER);
		GridData gd = SWTHelper.getFillGridData(1, true, 1, true);
		// at least 3 lines height
		Point size = postanschrift.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		gd.heightHint = 4 * size.y;
		postanschrift.setLayoutData(gd);
		
		// postanschrift info message
		new Label(com, SWT.NONE); // filler
		Label l5 = new Label(com, SWT.NONE);
		l5.setText(Messages.AnschriftEingabeDialog_postalAddressInfo); //$NON-NLS-1$
		l5.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		
		loadFieldValues();
		return com;
	}
	
	private void loadFieldValues(){
		str1.setText(zusatzAdresseDTO.getStreet1());
		str2.setText(zusatzAdresseDTO.getStreet2());
		plz.setText(zusatzAdresseDTO.getZip());
		ort.setText(zusatzAdresseDTO.getPlace());
		land.setText(zusatzAdresseDTO.getCountry());
		comboAddressType.setSelection(new StructuredSelection(zusatzAdresseDTO.getAddressType()));
		postanschrift
			.setText(zusatzAdresseDTO.getPostalAddress().replaceAll("[\\r\\n]\\n", StringTool.lf));
	}
	
	private void setFieldValues(){
		zusatzAdresseDTO.setStreet1(str1.getText());
		zusatzAdresseDTO.setStreet2(str2.getText());
		zusatzAdresseDTO.setZip(StringUtils.substring(plz.getText(), 0, 6));
		zusatzAdresseDTO.setPlace(ort.getText());
		zusatzAdresseDTO.setCountry(StringUtils.substring(land.getText(), 0, 3));
		
		StructuredSelection selection = (StructuredSelection) comboAddressType.getSelection();
		if (selection != null && !selection.isEmpty()) {
			AddressType addressType = (AddressType) selection.getFirstElement();
			zusatzAdresseDTO.setAddressType(addressType);
		}
		zusatzAdresseDTO
			.setPostalAddress(postanschrift.getText().replaceAll("\\r\\n", StringTool.lf));
	}
	
	/*
	 * (Kein Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed(){
		setFieldValues();
		try {
			zusatzAdresse.persistDTO(zusatzAdresseDTO);
		} catch (ElexisException e) {
			MessageDialog.openError(getShell(), StringTool.leer,
				"Speichern nicht möglich. Bitte diesen Dialog schließen und erneut probieren.");
			return;
		}
		super.okPressed();
	}
	
	public ZusatzAdresse getZusatzAdresse(){
		return zusatzAdresse;
	}
}
