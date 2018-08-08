/*******************************************************************************
 * Copyright (c) 2007-2018, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    T. Huster - updated
 *******************************************************************************/
package ch.elexis.core.ui.eigendiagnosen;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import ch.elexis.core.model.IDiagnosisTree;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.util.LabeledInputField;
import ch.elexis.core.ui.util.LabeledInputField.InputData;
import ch.elexis.core.ui.views.IDetailDisplay;
import ch.rgw.tools.StringTool;

public class EigendiagnoseDetailDisplay implements IDetailDisplay {
	Form form;
	LabeledInputField.AutoForm tblPls;
	InputData[] data = new InputData[] {
		new InputData(Messages.EigendiagnoseSelector_Shortcut_Label, "code",
			InputData.Typ.STRING, null),
		new InputData(Messages.EigendiagnoseSelector_Text_Label, "text",
			InputData.Typ.STRING, null)
	
	};
	Text tComment;
	
	public Composite createDisplay(Composite parent, IViewSite site){
		form = UiDesk.getToolkit().createForm(parent);
		TableWrapLayout twl = new TableWrapLayout();
		form.getBody().setLayout(twl);
		
		tblPls = new LabeledInputField.AutoForm(form.getBody(), data);
		
		TableWrapData twd = new TableWrapData(TableWrapData.FILL_GRAB);
		twd.grabHorizontal = true;
		tblPls.setLayoutData(twd);
		TableWrapData twd2 = new TableWrapData(TableWrapData.FILL_GRAB);
		tComment = UiDesk.getToolkit().createText(form.getBody(), StringTool.leer, SWT.BORDER);
		tComment.setLayoutData(twd2);
		return form.getBody();
	}
	
	public void display(Object obj){
		if (obj instanceof IDiagnosisTree) {
			IDiagnosisTree diag = (IDiagnosisTree) obj;
			form.setText(diag.getLabel());
			tblPls.reload(diag);
			tComment.setText(diag.getDescription());
		} else {
			form.setText("");
			tblPls.reload((Identifiable) null);
			tComment.setText("");
		}
	}
	
	public Class getElementClass(){
		return IDiagnosisTree.class;
	}
	
	@Inject
	public void updateSelection(
		@Optional @Named("ch.elexis.core.ui.eigendiagnosen.selection") IDiagnosisTree diagnose){
		display(diagnose);
	}
	
	public String getTitle(){
		return Messages.Eigendiagnosen_CodeSystemName;
	}
	
}
