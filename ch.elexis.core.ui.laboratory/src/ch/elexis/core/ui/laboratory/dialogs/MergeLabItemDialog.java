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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
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
import org.eclipse.ui.statushandlers.StatusManager;

import ch.elexis.core.data.status.ElexisStatus;
import ch.elexis.data.LabItem;
import ch.elexis.data.LabResult;
import ch.elexis.data.PersistentObject;

public class MergeLabItemDialog extends TitleAreaDialog {
	
	private TableViewer destinationItems;
	private Text destinationFilterTxt;
	private LabItemViewerFilter destinationFilter = new LabItemViewerFilter();
	private TableViewer sourceItems;
	private Text sourceFilterTxt;
	private LabItemViewerFilter sourceFilter = new LabItemViewerFilter();
	
	public MergeLabItemDialog(Shell parentShell, LabItem act){
		super(parentShell);
		
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
		destinationItems.setLabelProvider(new LabItemLabelProvider());
		destinationItems.setSorter(new LabItemViewerSorter());
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
		sourceItems.setLabelProvider(new LabItemLabelProvider());
		sourceItems.setSorter(new LabItemViewerSorter());
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
	
	private class LabItemViewerFilter extends ViewerFilter {
		protected String searchString;
		protected LabItemLabelProvider labelProvider = new LabItemLabelProvider();
		
		public void setSearchText(String s){
			// Search must be a substring of the existing value
			this.searchString = ".*" + s + ".*"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element){
			if (searchString == null || searchString.length() == 0) {
				return true;
			}
			String label = labelProvider.getText(element);
			if (label != null && label.matches(searchString)) {
				return true;
			}
			return false;
		}
	}
	
	private class LabItemViewerSorter extends ViewerSorter {
		private LabItemLabelProvider labelProvider = new LabItemLabelProvider();
		
		@Override
		public int compare(Viewer viewer, Object e1, Object e2){
			LabItem left = (LabItem) e1;
			LabItem right = (LabItem) e2;
			
			return labelProvider.getText(left).compareTo(labelProvider.getText(right));
		}
	}
	
	private class LabItemLabelProvider extends ColumnLabelProvider {
		private StringBuilder sb = new StringBuilder();
		
		private PreparedStatement ps = PersistentObject.getConnection().prepareStatement(
			"SELECT COUNT(*) AS results FROM LABORWERTE WHERE " + LabResult.ITEM_ID + "=?"); //$NON-NLS-1$ //$NON-NLS-2$
		
		@Override
		public String getText(Object element){
			sb.setLength(0);
			if (element instanceof LabItem) {
				String refW = shortenString(((LabItem) element).getRefW());
				String refM = shortenString(((LabItem) element).getRefM());
				sb.append(((LabItem) element).getKuerzel())
					.append(", ") //$NON-NLS-1$
					.append(((LabItem) element).getName())
					.append(" - ").append(((LabItem) element).getGroup()).append(" [") //$NON-NLS-1$ //$NON-NLS-2$
					.append(((LabItem) element).getEinheit()).append("]").append(" ").append(refW) //$NON-NLS-1$ //$NON-NLS-2$
					.append(" / ").append(refM); //$NON-NLS-1$
			}
			return sb.toString();
		}
		
		private String shortenString(String string){
			if (string.length() > 15) {
				return string.substring(0, 14) + "..."; //$NON-NLS-1$
			}
			return string;
		}
		
		@Override
		public String getToolTipText(Object element){
			if (element instanceof LabItem) {
				int results = 0;
				try {
					ps.setString(1, ((LabItem) element).getId());
					if (ps.execute()) {
						ResultSet resultSet = ps.getResultSet();
						while (resultSet.next()) {
							results = resultSet.getInt("results"); //$NON-NLS-1$
						}
						resultSet.close();
					}
				} catch (SQLException e) {
					StatusManager.getManager().handle(
						new ElexisStatus(ElexisStatus.WARNING,
							"ch.elexis", //$NON-NLS-1$
							ElexisStatus.CODE_NOFEEDBACK,
							"Could not determine count of LabResult.", e)); //$NON-NLS-1$
				}
				return String.format(Messages.MergeLabItemDialog_toolTipResultsCount, results);
			}
			return null;
		}
	}
}
