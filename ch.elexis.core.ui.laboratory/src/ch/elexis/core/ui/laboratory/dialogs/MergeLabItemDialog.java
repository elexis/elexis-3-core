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

import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.elexis.data.LabItem;

public class MergeLabItemDialog extends TitleAreaDialog {
	
	private TableViewer destinationItems;
	private Text destinationFilterTxt;
	private LabItemViewerFilter destinationFilter;
	private TableViewer sourceItems;
	private Text sourceFilterTxt;
	private LabItemViewerFilter sourceFilter;
	
	private LabItemLabelProvider labelProvider;
	
	public MergeLabItemDialog(Shell parentShell, LabItem act){
		super(parentShell);
		labelProvider = new LabItemLabelProvider(true);
		destinationFilter = new LabItemViewerFilter(labelProvider);
		sourceFilter = new LabItemViewerFilter(labelProvider);
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		getShell().setText(Messages.MergeLabItemDialog_title);
		setTitle(Messages.MergeLabItemDialog_title);
		setMessage(Messages.MergeLabItemDialog_pleaseMergeParam);
		
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayoutData(new GridData(GridData.FILL_BOTH));
		ret.setLayout(new GridLayout(1, false));
		
		Label lbl = new Label(ret, SWT.NONE);
		lbl.setText(Messages.MergeLabItemDialog_labelMergeTo);
		
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1);
		layoutData.heightHint = 150;
		
		destinationFilterTxt = new Text(ret, SWT.BORDER);
		destinationFilterTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		destinationFilterTxt.setMessage("Filter"); //$NON-NLS-1$
		destinationFilterTxt.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e){
				if (destinationFilterTxt.getText().length() > 1) {
					destinationFilter.setSearchText(destinationFilterTxt.getText());
					destinationItems.refresh();
				} else {
					destinationFilter.setSearchText(""); //$NON-NLS-1$
					destinationItems.refresh();
				}
			}
		});
		
		destinationItems = new TableViewer(ret, SWT.BORDER);
		destinationItems.getTable().setLayoutData(layoutData);
		destinationItems.setContentProvider(new ArrayContentProvider());
		destinationItems.setLabelProvider(new LabItemLabelProvider(true));
		destinationItems.setSorter(new LabItemViewerSorter(labelProvider));
		destinationItems.addFilter(destinationFilter);
		
		ColumnViewerToolTipSupport.enableFor(destinationItems, ToolTip.NO_RECREATE);
		
		lbl = new Label(ret, SWT.NONE);
		lbl.setText(Messages.MergeLabItemDialog_labelMergeFrom);
		
		sourceFilterTxt = new Text(ret, SWT.BORDER);
		sourceFilterTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		sourceFilterTxt.setMessage("Filter"); //$NON-NLS-1$
		sourceFilterTxt.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e){
				if (sourceFilterTxt.getText().length() > 1) {
					sourceFilter.setSearchText(sourceFilterTxt.getText());
					sourceItems.refresh();
				} else {
					sourceFilter.setSearchText(""); //$NON-NLS-1$
					sourceItems.refresh();
				}
			}
		});
		
		sourceItems = new TableViewer(ret, SWT.BORDER);
		sourceItems.getTable().setLayoutData(layoutData);
		sourceItems.setContentProvider(new ArrayContentProvider());
		sourceItems.setLabelProvider(new LabItemLabelProvider(true));
		sourceItems.setSorter(new LabItemViewerSorter(labelProvider));
		sourceItems.addFilter(sourceFilter);
		
		ColumnViewerToolTipSupport.enableFor(sourceItems, ToolTip.NO_RECREATE);
		
		List<LabItem> allItems = LabItem.getLabItems();
		destinationItems.setInput(allItems);
		sourceItems.setInput(allItems);
		
		return ret;
	}
	
	@Override
	protected void okPressed(){
		StructuredSelection selection = (StructuredSelection) destinationItems.getSelection();
		if (selection.isEmpty()) {
			setErrorMessage(Messages.MergeLabItemDialog_errorNoToLabItemSelected);
			return;
		}
		LabItem destination = (LabItem) selection.getFirstElement();
		
		selection = (StructuredSelection) sourceItems.getSelection();
		if (selection.isEmpty()) {
			setErrorMessage(Messages.MergeLabItemDialog_errorNoFromLabItemSelected);
			return;
		}
		LabItem source = (LabItem) selection.getFirstElement();
		
		if (source == destination) {
			setErrorMessage(Messages.MergeLabItemDialog_errorSameSelected);
			return;
		}
		
		boolean confirm =
			MessageDialog.openConfirm(getShell(), Messages.MergeLabItemDialog_titleWarningDialog,
				Messages.MergeLabItemDialog_messageWarningDialog);
		
		if (confirm) {
			destination.mergeWith(source);
			source.delete();
		} else {
			return;
		}
		
		super.okPressed();
	}
}
