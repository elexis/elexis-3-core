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

import org.apache.commons.lang3.StringUtils;
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

import ch.elexis.core.data.interfaces.ICodeElement;
import ch.elexis.core.model.ILabItem;
import ch.elexis.core.model.LabItemConstants;
import ch.elexis.core.model.builder.ILabItemBuilder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.types.LabItemTyp;
import ch.elexis.core.ui.laboratory.controls.LaborMappingComposite;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.WidgetFactory;
import ch.elexis.core.ui.views.codesystems.CodeSelectorFactory;
import ch.elexis.scripting.ScriptEditor;
import ch.rgw.tools.StringTool;

public class EditLabItem extends TitleAreaDialog {

	private LaborMappingComposite mapping;

	Text iKuerzel, iTitel, iRef, iRfF, iUnit, iPrio, iComma;
	Combo cGroup, cExportTag;
	Button alph, numeric, abs, formula, document, visible;
	String formel;
	org.eclipse.swt.widgets.List labors;
	ILabItem actLabItem;
	ArrayList<String> groups;
	ArrayList<String> exportTags;

	private Text loincCode;
	private Button loincCodeSelection;
	Button noRefValues;

	public EditLabItem(Shell parentShell, ILabItem act) {
		super(parentShell);

		groups = new ArrayList<>();
		exportTags = new ArrayList<>();
		actLabItem = act;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText(Messages.Core_Laboratory_parameter);
		setTitle(Messages.Core_Enter_new_laboratory_parameter);
		setMessage(Messages.EditLabItem_message);

		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		ret.setLayout(new GridLayout(4, false));

		mapping = new LaborMappingComposite(ret, SWT.NONE);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1);
		layoutData.heightHint = 150;
		mapping.setLayoutData(layoutData);
		mapping.setLabItem(actLabItem);

		WidgetFactory.createLabel(ret, Messages.Core_Short_Label);
		iKuerzel = new Text(ret, SWT.BORDER);
		iKuerzel.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		iKuerzel.setTextLimit(80);

		WidgetFactory.createLabel(ret, Messages.Core_Title);
		iTitel = new Text(ret, SWT.BORDER);
		iTitel.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		iTitel.setTextLimit(80);

		WidgetFactory.createLabel(ret, Messages.Core_Type);
		Group grp = new Group(ret, SWT.NONE);
		grp.setLayout(new FillLayout(SWT.HORIZONTAL));
		grp.setLayoutData(SWTHelper.getFillGridData(3, true, 1, false));
		numeric = new Button(grp, SWT.RADIO);
		numeric.setText(Messages.Core_Number);
		alph = new Button(grp, SWT.RADIO);
		alph.setText(Messages.Core_Text);
		abs = new Button(grp, SWT.RADIO);
		abs.setText(Messages.Core_Absolute);
		formula = new Button(grp, SWT.RADIO);
		formula.setText(Messages.Core_Formula);
		formula.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (formula.getSelection()) {

					ScriptEditor se = new ScriptEditor(getShell(), formel, Messages.EditLabItem_titleScriptEditor);
					if (se.open() == Dialog.OK) {
						formel = se.getScript();
					}
				}
			}

		});
		document = new Button(grp, SWT.RADIO);
		document.setText(Messages.Core_Document);
		document.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				documentSelectionChanged();
			}
		});
		WidgetFactory.createLabel(ret, Messages.Core_Reference_Male);

		iRef = new Text(ret, SWT.BORDER);
		iRef.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		iRef.setTextLimit(80);
		iRef.setEnabled(actLabItem == null || !actLabItem.isNoReferenceValueItem());
		WidgetFactory.createLabel(ret, Messages.Core_Reference_female);
		iRfF = new Text(ret, SWT.BORDER);
		iRfF.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		iRfF.setTextLimit(80);
		iRfF.setEnabled(actLabItem == null || !actLabItem.isNoReferenceValueItem());
		WidgetFactory.createLabel(ret, Messages.Core_Unit);
		noRefValues = new Button(grp, SWT.CHECK);
		noRefValues.setText(ch.elexis.core.l10n.Messages.LabResultEvaluator_LabItemNoRefValue);
		noRefValues.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				iRef.setEnabled(!noRefValues.getSelection());
				iRfF.setEnabled(!noRefValues.getSelection());
			}
		});
		noRefValues.setSelection(actLabItem != null && actLabItem.isNoReferenceValueItem());
		iUnit = new Text(ret, SWT.BORDER);
		iUnit.setLayoutData(SWTHelper.getFillGridData(3, true, 1, false));
		iUnit.setTextLimit(25);
		WidgetFactory.createLabel(ret, Messages.Core_Group);

		List<ILabItem> labItems = CoreModelServiceHolder.get().getQuery(ILabItem.class).execute();
		groups.clear();
		exportTags.clear();
		for (ILabItem li : labItems) {
			if (li.getExport() != null && li.getExport().length() > 0 && !exportTags.contains(li.getExport())) {
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
		cGroup.setToolTipText(Messages.Core_Labgroup);
		cGroup.setItems(groups.toArray(new String[0]));
		WidgetFactory.createLabel(ret, Messages.Core_Sequence_number);
		iPrio = new Text(ret, SWT.BORDER);
		iPrio.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		iPrio.setToolTipText(Messages.Core_Sequence_inside_group);
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
			public void widgetSelected(SelectionEvent e) {
				SelectionDialog dialog = CodeSelectorFactory.getSelectionDialog("LOINC", getShell(), null); //$NON-NLS-1$
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

		WidgetFactory.createLabel(ret, Messages.Core_Export_tag);
		cExportTag = new Combo(ret, SWT.SINGLE | SWT.DROP_DOWN);
		cExportTag.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		cExportTag.setToolTipText(Messages.Core_HL7_Tags_comma_separated);
		cExportTag.setItems(exportTags.toArray(new String[0]));

		if (actLabItem != null) {
			iKuerzel.setText(actLabItem.getCode());
			iTitel.setText(actLabItem.getName());
			if (actLabItem.getTyp() == LabItemTyp.NUMERIC) {
				numeric.setSelection(true);
			} else if (actLabItem.getTyp() == LabItemTyp.TEXT) {
				alph.setSelection(true);
			} else if (actLabItem.getTyp() == LabItemTyp.ABSOLUTE) {
				abs.setSelection(true);
			} else if (actLabItem.getTyp() == LabItemTyp.DOCUMENT) {
				document.setSelection(true);
				documentSelectionChanged();
			} else {
				formula.setSelection(true);
			}
			loincCode.setText(StringUtils.defaultString(actLabItem.getLoincCode()));
			iRef.setText(StringUtils.defaultString(actLabItem.getReferenceMale()));
			iRfF.setText(StringUtils.defaultString(actLabItem.getReferenceFemale()));
			iUnit.setText(StringUtils.defaultString(actLabItem.getUnit()));
			cGroup.setText(StringUtils.defaultString(actLabItem.getGroup()));
			iPrio.setText(StringUtils.defaultString(actLabItem.getPriority()));
			iComma.setText(Integer.toString(actLabItem.getDigits()));
			visible.setSelection(actLabItem.isVisible());
			cExportTag.setText(StringUtils.defaultString(actLabItem.getExport()));
			formel = StringUtils.defaultString(actLabItem.getFormula());
		}
		return ret;
	}

	/**
	 * Event method is called when document radio button is selected or deselected
	 */
	private void documentSelectionChanged() {
		iRef.setEnabled(!document.getSelection());
		iRfF.setEnabled(!document.getSelection());
	}

	@Override
	protected void okPressed() {
		LabItemTyp typ;
		// String refmin=StringUtils.EMPTY,refmax;
		// refmax=iRef.getText();
		if (iTitel.getText().length() < 1 && iPrio.getText().length() < 1) {
			setErrorMessage(Messages.EditLabItem_errorNoTitle);
			return;
		}

		if (numeric.getSelection() == true) {
			typ = LabItemTyp.NUMERIC;
		} else if (abs.getSelection() == true) {
			typ = LabItemTyp.ABSOLUTE;
		} else if (formula.getSelection()) {
			typ = LabItemTyp.FORMULA;
		} else if (document.getSelection()) {
			typ = LabItemTyp.DOCUMENT;
		} else {
			typ = LabItemTyp.TEXT;
		}

		String refValMale = iRef.getText();
		String refValFemale = iRfF.getText();
		if (noRefValues.getSelection()) {
			refValMale = LabItemConstants.REFVAL_INCONCLUSIVE;
			refValFemale = LabItemConstants.REFVAL_INCONCLUSIVE;
		}

		if (actLabItem == null) {
			actLabItem = new ILabItemBuilder(CoreModelServiceHolder.get(), iKuerzel.getText(), iTitel.getText(),
					refValMale, refValFemale, iUnit.getText(), typ, cGroup.getText(), 0).build();
			actLabItem.setPriority(iPrio.getText());
			actLabItem.setExport(cExportTag.getText());
			CoreModelServiceHolder.get().save(actLabItem);
			mapping.persistTransientLabMappings(actLabItem);
		} else {
			actLabItem.setCode(iKuerzel.getText());
			actLabItem.setName(iTitel.getText());
			actLabItem.setReferenceMale(refValMale);
			actLabItem.setReferenceFemale(refValFemale);
			actLabItem.setUnit(iUnit.getText());
			actLabItem.setTyp(typ);
			actLabItem.setGroup(cGroup.getText());
			actLabItem.setPriority(iPrio.getText());
			actLabItem.setExport(cExportTag.getText());
		}
		actLabItem.setLoincCode(loincCode.getText());

		if (!iComma.getText().isEmpty()) {
			actLabItem.setDigits(Integer.parseInt(iComma.getText()));
		} else {
			actLabItem.setDigits(0);
		}
		actLabItem.setVisible(visible.getSelection());

		if (!StringTool.isNothing(formel)) {
			actLabItem.setFormula(formel);
		}
		CoreModelServiceHolder.get().save(actLabItem);
		super.okPressed();
	}

	public void setShortDescText(String string) {
		iKuerzel.setText(string);
	}

	public void setTitelText(String string) {
		if (string != null)
			iTitel.setText(string);
	}

	public void setRefMText(String string) {
		if (string != null)
			iRef.setText(string);
	}

	public void setRefFText(String string) {
		if (string != null)
			iRfF.setText(string);
	}

	public void setUnitText(String string) {
		if (string != null)
			iUnit.setText(string);
	}
}
