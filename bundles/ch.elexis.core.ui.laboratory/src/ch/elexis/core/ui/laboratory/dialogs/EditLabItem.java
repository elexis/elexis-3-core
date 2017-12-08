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
package ch.elexis.core.ui.laboratory.dialogs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.SelectionDialog;

import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.types.LabItemTyp;
import ch.elexis.core.ui.laboratory.controls.LaborMappingComposite;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.WidgetFactory;
import ch.elexis.core.ui.views.codesystems.CodeSelectorFactory;
import ch.elexis.data.Kontakt;
import ch.elexis.data.LabItem;
import ch.elexis.data.Labor;
import ch.elexis.scripting.ScriptEditor;
import ch.rgw.tools.StringTool;

public class EditLabItem extends TitleAreaDialog {
	
	private LaborMappingComposite mapping;
	
	Text iKuerzel, iTitel, iRef, iRfF, iUnit, iPrio, iComma;
	Combo cGroup, cExportTag;
	Button alph, numeric, abs, formula, document, visible;
	String formel;
	org.eclipse.swt.widgets.List labors;
	Labor actLabor;
	LabItem result;
	ArrayList<String> groups;
	ArrayList<String> exportTags;
	
	private Text loincCode;
	private Button loincCodeSelection;
	
	public EditLabItem(Shell parentShell, LabItem act){
		super(parentShell);
		
		groups = new ArrayList<String>();
		exportTags = new ArrayList<String>();
		result = act;
		if (act != null) {
			actLabor = act.getLabor();
		}
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		getShell().setText(Messages.EditLabItem_shellTitle);
		setTitle(Messages.EditLabItem_title);
		setMessage(Messages.EditLabItem_message);
		
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		ret.setLayout(new GridLayout(4, false));
		
		mapping = new LaborMappingComposite(ret, SWT.NONE);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1);
		layoutData.heightHint = 150;
		mapping.setLayoutData(layoutData);
		mapping.setLabItem(result);
		
		WidgetFactory.createLabel(ret, Messages.EditLabItem_labelShortLabel);
		iKuerzel = new Text(ret, SWT.BORDER);
		iKuerzel.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		iKuerzel.setTextLimit(80);
		
		WidgetFactory.createLabel(ret, Messages.EditLabItem_labelTitle);
		iTitel = new Text(ret, SWT.BORDER);
		iTitel.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		iTitel.setTextLimit(80);
		
		WidgetFactory.createLabel(ret, Messages.EditLabItem_labelType);
		Group grp = new Group(ret, SWT.NONE);
		grp.setLayout(new FillLayout(SWT.HORIZONTAL));
		grp.setLayoutData(SWTHelper.getFillGridData(3, true, 1, false));
		numeric = new Button(grp, SWT.RADIO);
		numeric.setText(Messages.EditLabItem_labelTypNumber);
		alph = new Button(grp, SWT.RADIO);
		alph.setText(Messages.EditLabItem_labelTypText);
		abs = new Button(grp, SWT.RADIO);
		abs.setText(Messages.EditLabItem_labelTypAbsolute);
		formula = new Button(grp, SWT.RADIO);
		formula.setText(Messages.EditLabItem_labelTypFormula);
		formula.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e){
				if (formula.getSelection()) {
					
					ScriptEditor se =
						new ScriptEditor(getShell(), formel, Messages.EditLabItem_titleScriptEditor);
					if (se.open() == Dialog.OK) {
						formel = se.getScript();
					}
				}
			}
			
		});
		document = new Button(grp, SWT.RADIO);
		document.setText(Messages.EditLabItem_labelTypDocument);
		document.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				documentSelectionChanged();
			}
		});
		WidgetFactory.createLabel(ret, Messages.EditLabItem_labelRefMale);
		
		iRef = new Text(ret, SWT.BORDER);
		iRef.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		iRef.setTextLimit(80);
		WidgetFactory.createLabel(ret, Messages.EditLabItem_labelRefFemale);
		iRfF = new Text(ret, SWT.BORDER);
		iRfF.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		iRfF.setTextLimit(80);
		WidgetFactory.createLabel(ret, Messages.EditLabItem_labelUnit);
		iUnit = new Text(ret, SWT.BORDER);
		iUnit.setLayoutData(SWTHelper.getFillGridData(3, true, 1, false));
		iUnit.setTextLimit(25);
		WidgetFactory.createLabel(ret, Messages.EditLabItem_labelGroup);
		
		List<LabItem> labItems = LabItem.getLabItems();
		groups.clear();
		exportTags.clear();
		for (LabItem li : (List<LabItem>) labItems) {
			if (li.getExport() != null && li.getExport().length() > 0
				&& !exportTags.contains(li.getExport())) {
				exportTags.add(li.getExport());
			}
			if (!groups.contains(li.getGroup())) {
				groups.add(li.getGroup());
			}
		}
		Collections.sort(groups);
		Collections.sort(exportTags);
		
		cGroup = new Combo(ret, SWT.SINGLE | SWT.DROP_DOWN);
		cGroup.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		cGroup.setToolTipText(Messages.EditLabItem_tooltipGroup);
		cGroup.setItems(groups.toArray(new String[0]));
		WidgetFactory.createLabel(ret, Messages.EditLabItem_labelGroupSequence);
		iPrio = new Text(ret, SWT.BORDER);
		iPrio.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		iPrio.setToolTipText(Messages.EditLabItem_labelGroupPosition);
		iPrio.setTextLimit(3);
		
		WidgetFactory.createLabel(ret, "LOINC"); //$NON-NLS-1$
		loincCode = new Text(ret, SWT.BORDER);
		loincCode.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		loincCode.setTextLimit(80);
		loincCode.setEnabled(false);
		loincCodeSelection = new Button(ret, SWT.PUSH);
		loincCodeSelection.setText("..."); //$NON-NLS-1$
		loincCodeSelection.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				SelectionDialog dialog =
					CodeSelectorFactory.getSelectionDialog("LOINC", getShell(), null); //$NON-NLS-1$
				if (dialog.open() == SelectionDialog.OK) {
					if (dialog.getResult() != null && dialog.getResult().length > 0) {
						ICodeElement code = (ICodeElement) dialog.getResult()[0];
						loincCode.setText(code.getCode());
					}
				}
			}
		});
		
		WidgetFactory.createLabel(ret, Messages.EditLabItem_labelDecimalPlace);
		iComma = new Text(ret, SWT.BORDER);
		iComma.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		iComma.setTextLimit(80);
		WidgetFactory.createLabel(ret, Messages.EditLabItem_labelVisible);
		visible = new Button(ret, SWT.CHECK);
		visible.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		WidgetFactory.createLabel(ret, Messages.EditLabItem_labelExportTag);
		cExportTag = new Combo(ret, SWT.SINGLE | SWT.DROP_DOWN);
		cExportTag.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		cExportTag.setToolTipText(Messages.EditLabItem_labelHintExportTag);
		cExportTag.setItems(exportTags.toArray(new String[0]));
		
		if (result != null) {
			iKuerzel.setText(result.getKuerzel());
			iTitel.setText(result.getName());
			if (result.getTyp() == LabItemTyp.NUMERIC) {
				numeric.setSelection(true);
			} else if (result.getTyp() == LabItemTyp.TEXT) {
				alph.setSelection(true);
			} else if (result.getTyp() == LabItemTyp.ABSOLUTE) {
				abs.setSelection(true);
			} else if (result.getTyp() == LabItemTyp.DOCUMENT) {
				document.setSelection(true);
				documentSelectionChanged();
			} else {
				formula.setSelection(true);
			}
			iUnit.setText(result.getEinheit());
			iRef.setText(result.get(LabItem.REF_MALE));
			iRfF.setText(result.getRefW());
			cGroup.setText(result.getGroup());
			iPrio.setText(result.getPrio());
			iComma.setText(Integer.toString(result.getDigits()));
			visible.setSelection(result.isVisible());
			loincCode.setText(result.getLoincCode());
			cExportTag.setText(result.getExport());
			formel = result.getFormula();
		}
		return ret;
	}
	
	/**
	 * Event method is called when document radio button is selected or deselected
	 */
	private void documentSelectionChanged(){
		iRef.setEnabled(!document.getSelection());
		iRfF.setEnabled(!document.getSelection());
	}
	
	@Override
	protected void okPressed(){
		LabItemTyp typ;
		// String refmin="",refmax;
		// refmax=iRef.getText();
		if (iTitel.getText().length() < 1 && iPrio.getText().length() < 1) {
			setErrorMessage(Messages.EditLabItem_errorNoTitle);
			return;
		}
		
		if (numeric.getSelection() == true) {
			typ =LabItemTyp.NUMERIC;
		} else if (abs.getSelection() == true) {
			typ = LabItemTyp.ABSOLUTE;
		} else if (formula.getSelection()) {
			typ =LabItemTyp.FORMULA;
		} else if (document.getSelection()) {
			typ = LabItemTyp.DOCUMENT;
		} else {
			typ =LabItemTyp.TEXT;
		}
		if (result == null) {
			result = 
				new LabItem(iKuerzel.getText(), iTitel.getText(), (Kontakt) null, iRef.getText(),
					iRfF.getText(), iUnit.getText(), typ, cGroup.getText(), iPrio.getText());
			mapping.persistTransientLabMappings(result);
		} else {
			String t = "0"; //$NON-NLS-1$
			if (typ == LabItemTyp.TEXT) {
				t = "1"; //$NON-NLS-1$
			} else if (typ == LabItemTyp.ABSOLUTE) {
				t = "2"; //$NON-NLS-1$
			} else if (typ == LabItemTyp.FORMULA) {
				t = "3"; //$NON-NLS-1$
			} else if (typ == LabItemTyp.DOCUMENT) {
				t = "4"; //$NON-NLS-1$
			}
			result.set(new String[] {
				LabItem.SHORTNAME, LabItem.TITLE, LabItem.LAB_ID, LabItem.REF_MALE,
				LabItem.REF_FEMALE_OR_TEXT, LabItem.UNIT, LabItem.TYPE, LabItem.GROUP,
				LabItem.PRIO, LabItem.EXPORT
			}, iKuerzel.getText(), iTitel.getText(), actLabor.getId(), iRef.getText(),
				iRfF.getText(), iUnit.getText(), t, cGroup.getText(), iPrio.getText(),
				cExportTag.getText());
		}
		result.setLoincCode(loincCode.getText());
		
		if (!iComma.getText().isEmpty()) {
			result.setDigits(Integer.parseInt(iComma.getText()));
		} else {
			result.setDigits(0);
		}
		result.setVisible(visible.getSelection());
		
		if (!StringTool.isNothing(formel)) {
			result.setFormula(formel);
		}
		super.okPressed();
	}
	
	public void setShortDescText(String string){
		iKuerzel.setText(string);
	}
	
	public void setTitelText(String string){
		if (string != null)
			iTitel.setText(string);
	}
	
	public void setRefMText(String string){
		if (string != null)
			iRef.setText(string);
	}
	
	public void setRefFText(String string){
		if (string != null)
			iRfF.setText(string);
	}
	
	public void setUnitText(String string){
		if (string != null)
			iUnit.setText(string);
	}
}
