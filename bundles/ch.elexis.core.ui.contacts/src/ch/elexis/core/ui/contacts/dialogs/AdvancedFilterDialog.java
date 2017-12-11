/*******************************************************************************
 * Copyright (c) 2012 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.ui.contacts.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class AdvancedFilterDialog extends TitleAreaDialog {
	private Text text;
	
	/**
	 * Create the dialog.
	 * 
	 * @param parentShell
	 */
	public AdvancedFilterDialog(Shell parentShell){
		super(parentShell);
	}
	
	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent){
		setMessage("Konfiguration des erweiterten Filters");
		setTitle("Erweiterter Filter");
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayout(new GridLayout(2, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label lblGespeichertenFilterLaden = new Label(container, SWT.NONE);
		lblGespeichertenFilterLaden.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false,
			1, 1));
		lblGespeichertenFilterLaden.setText("Gespeicherten Filter laden");
		
		ComboViewer comboViewer = new ComboViewer(container, SWT.NONE);
		Combo combo = comboViewer.getCombo();
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblNewLabel = new Label(container, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		
		Button btnFilterSticker = new Button(container, SWT.CHECK);
		btnFilterSticker.setText("nach Sticker filtern");
		new Label(container, SWT.NONE);
		
		Button btnFilterField = new Button(container, SWT.CHECK);
		btnFilterField.setText("nach Feldwerten filtern");
		new Label(container, SWT.NONE);
		
		Button btnBtnfilterproperties = new Button(container, SWT.CHECK);
		btnBtnfilterproperties.setText("nach Eigenschaften filtern");
		new Label(container, SWT.NONE);
		
		Label lblNewLabel_1 = new Label(container, SWT.NONE);
		lblNewLabel_1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		
		Label lblFilterSpeichernAls = new Label(container, SWT.NONE);
		lblFilterSpeichernAls
			.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblFilterSpeichernAls.setText("Filter speichern als");
		
		text = new Text(container, SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		return area;
	}
	
	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent){
		Button button_1 =
			createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		button_1.setText("Filter anwenden");
		Button btnAbbrechen =
			createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		btnAbbrechen.setText("Abbrechen");
	}
}
