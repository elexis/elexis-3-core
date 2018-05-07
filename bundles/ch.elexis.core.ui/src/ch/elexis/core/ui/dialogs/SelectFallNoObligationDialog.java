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
package ch.elexis.core.ui.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.PlatformUI;

import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.interfaces.IVerrechenbar;
import ch.elexis.core.ui.actions.GlobalActions;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.data.Fall;

public class SelectFallNoObligationDialog extends TitleAreaDialog {
	
	private static Fall lastSelectedFall;
	
	private IVerrechenbar noOblCode;
	private Fall oblFall;
	private Fall fall;
	private ComboViewer noOblFallCombo;
	
	public SelectFallNoObligationDialog(Fall oblFall, IVerrechenbar code){
		super(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		this.oblFall = oblFall;
		this.noOblCode = code;
	}
	
	@Override
	public void create(){
		super.create();
		setTitle("Auf diesen Fall können nur Pflichtleitungen verrechnet werden."); //$NON-NLS-1$
		setMessage("Erstellen bzw. wählen Sie einen Fall für die Nicht-Pflichtleistung:\n" + noOblCode.getText()); //$NON-NLS-1$
		getShell().setText("Fall für Nicht-Pflichtleistungen"); //$NON-NLS-1$
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		Composite composite = (Composite) super.createDialogArea(parent);
		
		Composite areaComposite = new Composite(composite, SWT.NONE);
		areaComposite
			.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));
		
		areaComposite.setLayout(new FormLayout());
		
		Label lbl = new Label(areaComposite, SWT.NONE);
		lbl.setText("Fall erstellen");
		
		ToolBarManager tbManager = new ToolBarManager(SWT.FLAT | SWT.HORIZONTAL | SWT.WRAP);
		tbManager.add(GlobalActions.neuerFallAction);
		ToolBar toolbar = tbManager.createControl(areaComposite);
		
		FormData fd = new FormData();
		fd.top = new FormAttachment(0, 5);
		fd.left = new FormAttachment(0, 5);
		lbl.setLayoutData(fd);
		
		fd = new FormData();
		fd.top = new FormAttachment(0, 5);
		fd.left = new FormAttachment(30, 5);
		toolbar.setLayoutData(fd);
		
		lbl = new Label(areaComposite, SWT.NONE);
		lbl.setText("Fall auswählen");
		
		noOblFallCombo = new ComboViewer(areaComposite);
		
		noOblFallCombo.setContentProvider(new ArrayContentProvider());
		
		noOblFallCombo.setInput(getNoObligationFaelle());
		noOblFallCombo.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element){
				return ((Fall) element).getLabel();
			}
		});
		
		if (lastSelectedFall != null)
			noOblFallCombo.setSelection(new StructuredSelection(lastSelectedFall));
		
		ElexisEventDispatcher.getInstance().addListeners(
			new UpdateFallComboListener(noOblFallCombo, Fall.class, 0xff));
		
		fd = new FormData();
		fd.top = new FormAttachment(toolbar, 5);
		fd.left = new FormAttachment(0, 5);
		lbl.setLayoutData(fd);
		
		fd = new FormData();
		fd.top = new FormAttachment(toolbar, 5);
		fd.left = new FormAttachment(30, 5);
		fd.right = new FormAttachment(100, -5);
		noOblFallCombo.getControl().setLayoutData(fd);
		
		return areaComposite;
	}
	
	protected List<Fall> getNoObligationFaelle(){
		ArrayList<Fall> ret = new ArrayList<Fall>();
		Fall[] faelle = oblFall.getPatient().getFaelle();
		for (Fall f : faelle) {
			String gesetz = f.getConfiguredBillingSystemLaw().name();
			if (f.isOpen() && !gesetz.equalsIgnoreCase("KVG")) //$NON-NLS-1$
				ret.add(f);
		}
		return ret;
	}
	
	@Override
	public void okPressed(){
		Object obj = ((IStructuredSelection) noOblFallCombo.getSelection()).getFirstElement();
		if (obj instanceof Fall) {
			fall = (Fall) obj;
			lastSelectedFall = fall;
			super.okPressed();
		}
		if (this.getShell() != null && !this.getShell().isDisposed())
			setErrorMessage("Kein Fall ausgewählt.");
		return;
	}
	
	public Fall getFall(){
		return fall;
	}
	
	private class UpdateFallComboListener extends ElexisUiEventListenerImpl {
		
		ComboViewer viewer;
		
		UpdateFallComboListener(ComboViewer viewer, final Class<?> clazz, int mode){
			super(clazz, mode);
			this.viewer = viewer;
		}
		
		@Override
		public void runInUi(ElexisEvent ev){
			if (viewer != null && viewer.getControl() != null && !viewer.getControl().isDisposed()) {
				viewer.setInput(getNoObligationFaelle());
				viewer.refresh();
			}
		}
	}
}
