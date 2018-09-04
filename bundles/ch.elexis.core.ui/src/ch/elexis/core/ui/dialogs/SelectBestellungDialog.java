/*******************************************************************************
 * Copyright (c) 2013 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.ui.dialogs;

import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.dialogs.SelectionDialog;

import ch.elexis.core.data.service.CoreModelServiceHolder;
import ch.elexis.core.model.IOrder;
import ch.elexis.core.services.IQuery;
import ch.rgw.tools.TimeTool;

public class SelectBestellungDialog extends SelectionDialog {
	
	private IStructuredContentProvider fContentProvider;
	
	private ILabelProvider fLabelProvider;
	
	private Object fInput;
	
	private TableViewer fTableViewer;
	
	private boolean fAddCancelButton = true;
	
	private int widthInChars = 55;
	
	private int heightInChars = 15;
	
	public SelectBestellungDialog(Shell parent){
		super(parent);
	}
	
	protected Control createDialogArea(Composite container){
		Composite parent = (Composite) super.createDialogArea(container);
		createMessageArea(parent);
		fTableViewer = new TableViewer(parent, getTableStyle());
		fTableViewer.setContentProvider(ArrayContentProvider.getInstance());
		
		addColumns();
		
		setComparator();
		
		fTableViewer.setInput(this);
		
		fTableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event){
				if (fAddCancelButton) {
					okPressed();
				}
			}
		});
		List initialSelection = getInitialElementSelections();
		if (initialSelection != null) {
			fTableViewer.setSelection(new StructuredSelection(initialSelection));
		}
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = convertHeightInCharsToPixels(heightInChars);
		gd.widthHint = convertWidthInCharsToPixels(widthInChars);
		Table table = fTableViewer.getTable();
		table.setLayoutData(gd);
		table.setFont(container.getFont());
		table.setHeaderVisible(true);
		
		IQuery<IOrder> query = CoreModelServiceHolder.get().getQuery(IOrder.class);
		fTableViewer.setInput(query.execute());
		
		return parent;
	}
	
	private void setComparator(){
		fTableViewer.setComparator(new ViewerComparator() {
			private TimeTool t1 = new TimeTool();
			private TimeTool t2 = new TimeTool();
			
			@Override
			public int compare(Viewer viewer, Object b1, Object b2){
				setTimeTool((IOrder) b1, t1);
				setTimeTool((IOrder) b2, t2);
				if (t1.after(t2))
					return -1;
				if (t2.after(t1))
					return 1;
				return 0;
			}
			
			private void setTimeTool(IOrder order, TimeTool timeTool){
				try {
					String[] i = order.getId().split(":"); //$NON-NLS-1$
					timeTool.set(i[1]);
				} catch (Exception e) {
					timeTool.set("1.1.1970");
				}
			}
		});
	}
	
	private void addColumns(){
		TableViewerColumn closed = new TableViewerColumn(fTableViewer, SWT.NONE);
		closed.getColumn().setWidth(50);
		closed.getColumn().setText("Abg.");
		closed.setLabelProvider(new ColumnLabelProvider() {
			
			@Override
			public String getText(Object element){
				IOrder order = (IOrder) element;
				if (order.isDone()) {
					return "*";
				} else {
					return "";
				}
			}
		});
		
		TableViewerColumn time = new TableViewerColumn(fTableViewer, SWT.NONE);
		time.getColumn().setWidth(125);
		time.getColumn().setText("Datum");
		time.setLabelProvider(new ColumnLabelProvider() {
			TimeTool date = new TimeTool();
			
			@Override
			public String getText(Object element){
				IOrder order = (IOrder) element;
				String[] i = order.getId().split(":"); //$NON-NLS-1$
				
				if (i.length > 1) {
					date.set(i[1]);
					return date.toString(TimeTool.FULL_GER);
				} else {
					return "???";
				}
			}
		});
		
		TableViewerColumn title = new TableViewerColumn(fTableViewer, SWT.NONE);
		title.getColumn().setWidth(200);
		title.getColumn().setText("Titel");
		title.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				IOrder order = (IOrder) element;
				String[] i = order.getId().split(":"); //$NON-NLS-1$
				
				if (i.length > 0)
					return i[0];
				else
					return "???";
			}
		});
	}
	
	/**
	 * Return the style flags for the table viewer.
	 * 
	 * @return int
	 */
	protected int getTableStyle(){
		return SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION;
	}
	
	/*
	 * Overrides method from Dialog
	 */
	protected void okPressed(){
		// Build a list of selected children.
		IStructuredSelection selection = (IStructuredSelection) fTableViewer.getSelection();
		setResult(selection.toList());
		super.okPressed();
	}
}
