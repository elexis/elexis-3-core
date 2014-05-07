/*******************************************************************************
 * Copyright (c) 2008-2011, G. Weirich and Elexis
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

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import ch.elexis.core.model.ISticker;
import ch.elexis.core.ui.data.UiSticker;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Sticker;

public class AssignStickerDialog extends TitleAreaDialog {
	PersistentObject mine;
	TableViewer viewer;
	StickerViewerComparator comparator;
	List<Sticker> alleEtiketten;
	List<ISticker> mineEtiketten;
	
	public AssignStickerDialog(Shell shell, PersistentObject obj){
		super(shell);
		mine = obj;
		mineEtiketten = mine.getStickers();
		alleEtiketten = Sticker.getStickersForClass(mine.getClass());
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		ret.setLayout(new GridLayout());
		Label lbl = new Label(ret, SWT.WRAP);
		lbl.setText(Messages.AssignStickerDialog_PleaseConfirm); //$NON-NLS-1$
		
		viewer = new TableViewer(ret, SWT.CHECK | SWT.FULL_SELECTION);
		viewer.getTable().setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		viewer.getTable().setHeaderVisible(true);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setInput(alleEtiketten);
		createColumns();
		comparator = new StickerViewerComparator();
		viewer.setComparator(comparator);
		checkAlreadySelected();
		
		return ret;
	}
	
	private void checkAlreadySelected(){
		TableItem[] tableItems = viewer.getTable().getItems();
		for (TableItem item : tableItems) {
			if (mineEtiketten.contains(item.getData())) {
				item.setChecked(true);
			}
		}
	}
	
	private void createColumns(){
		// first column - label
		TableViewerColumn col = new TableViewerColumn(viewer, SWT.NONE);
		col.getColumn().setText(Messages.AssignStickerDialog_StickerName);
		col.getColumn().setWidth(300);
		col.getColumn().addSelectionListener(getSelectionAdapter(col.getColumn(), 0));
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				Sticker s = (Sticker) element;
				return s.getLabel();
			}
			
			@Override
			public Color getBackground(Object element){
				Sticker s = (Sticker) element;
				UiSticker uiSticker = new UiSticker(s);
				return uiSticker.getBackground();
			}
			
			@Override
			public Color getForeground(Object element){
				Sticker s = (Sticker) element;
				UiSticker uiSticker = new UiSticker(s);
				return uiSticker.getForeground();
			}
		});
		
		// second column - value
		col = new TableViewerColumn(viewer, SWT.NONE);
		col.getColumn().setText(Messages.AssignStickerDialog_StickerWert);
		col.getColumn().setWidth(50);
		col.getColumn().addSelectionListener(getSelectionAdapter(col.getColumn(), 1));
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				Sticker s = (Sticker) element;
				return s.getWert() + "";
			}
			
			@Override
			public Color getBackground(Object element){
				Sticker s = (Sticker) element;
				UiSticker uiSticker = new UiSticker(s);
				return uiSticker.getBackground();
			}
			
			@Override
			public Color getForeground(Object element){
				Sticker s = (Sticker) element;
				UiSticker uiSticker = new UiSticker(s);
				return uiSticker.getForeground();
			}
		});
	}
	
	private SelectionAdapter getSelectionAdapter(final TableColumn column, final int index){
		SelectionAdapter selectionAdapter = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				comparator.setColumn(index);
				viewer.getTable().setSortDirection(comparator.getDirection());
				viewer.getTable().setSortColumn(column);
				viewer.refresh();
			}
		};
		return selectionAdapter;
	}
	
	@Override
	public void create(){
		super.create();
		setTitle("Sticker"); //$NON-NLS-1$
		setMessage(MessageFormat
			.format(Messages.AssignStickerDialog_enterStickers, mine.getLabel())); //$NON-NLS-1$
		getShell().setText("Elexis Sticker"); //$NON-NLS-1$
	}
	
	@Override
	protected void okPressed(){
		TableItem[] tableItems = viewer.getTable().getItems();
		for (TableItem it : tableItems) {
			Sticker et = (Sticker) it.getData();
			if (it.getChecked()) {
				if (!mineEtiketten.contains(et)) {
					mine.addSticker(et);
				}
			} else {
				if (mineEtiketten.contains(et)) {
					mine.removeSticker(et);
				}
			}
		}
		super.okPressed();
	}
	
	class StickerViewerComparator extends ViewerComparator {
		private int propertyIndex;
		private boolean direction = true;
		private Sticker s1;
		private Sticker s2;
		
		public StickerViewerComparator(){
			this.propertyIndex = 0;
		}
		
		@Override
		public int compare(Viewer viewer, Object e1, Object e2){
			if (e1 instanceof Sticker && e2 instanceof Sticker) {
				s1 = (Sticker) e1;
				s2 = (Sticker) e2;
				int rc = 0;
				
				switch (propertyIndex) {
				case 0:
					String label1 = s1.getLabel().toLowerCase();
					String label2 = s2.getLabel().toLowerCase();
					rc = label1.compareTo(label2);
					break;
				case 1:
					Integer wert1 = s1.getWert();
					Integer wert2 = s2.getWert();
					rc = wert1.compareTo(wert2);
					break;
				default:
					break;
				}
				
				// If descending order, flip the direction
				if (direction) {
					rc = -rc;
				}
				return rc;
			}
			return 0;
		}
		
		/**
		 * for sort direction
		 * 
		 * @return SWT.DOWN or SWT.UP
		 */
		public int getDirection(){
			return direction ? SWT.DOWN : SWT.UP;
		}
		
		public void setColumn(int column){
			if (column == this.propertyIndex) {
				// Same column as last sort; toggle the direction
				direction = !direction;
			} else {
				// New column; do an ascending sort
				this.propertyIndex = column;
				direction = true;
			}
		}
		
	}
	
}
