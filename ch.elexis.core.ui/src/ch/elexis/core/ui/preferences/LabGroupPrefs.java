/*******************************************************************************
 * Copyright (c) 2007-2010, D. Lutz and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    D. Lutz - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.core.ui.preferences;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

import ch.elexis.core.data.LabGroup;
import ch.elexis.core.data.LabItem;
import ch.elexis.core.data.Query;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.viewers.DefaultLabelProvider;

public class LabGroupPrefs extends PreferencePage implements IWorkbenchPreferencePage {
	
	private LabGroup actGroup = null;
	
	private ComboViewer groupsViewer;
	private ListViewer itemsViewer;
	
	Button newButton;
	Button removeButton;
	Button addItemButton;
	Button removeItemButton;
	
	public LabGroupPrefs(){
		super(Messages.LabGroupPrefs_groups);
	}
	
	protected Control createContents(Composite parent){
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		composite.setLayout(new GridLayout(1, false));
		
		Label label;
		GridLayout layout;
		
		Composite topArea = new Composite(composite, SWT.NONE);
		topArea.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		layout = new GridLayout(3, false);
		layout.verticalSpacing = 20;
		topArea.setLayout(layout);
		
		label = new Label(topArea, SWT.NONE);
		label.setText(Messages.LabGroupPrefs_ExplanationsLine1
			+ Messages.LabGroupPrefs_ExplanationsLine2 + Messages.LabGroupPrefs_ExplanationsLine3);
		label.setLayoutData(SWTHelper.getFillGridData(3, true, 1, false));
		
		GridData gd;
		
		label = new Label(topArea, SWT.NONE);
		label.setText(Messages.LabGroupPrefs_group);
		gd = SWTHelper.getFillGridData(1, false, 1, false);
		gd.verticalAlignment = GridData.BEGINNING;
		label.setLayoutData(gd);
		
		groupsViewer = new ComboViewer(topArea, SWT.READ_ONLY);
		gd = SWTHelper.getFillGridData(1, true, 1, false);
		gd.verticalAlignment = GridData.BEGINNING;
		groupsViewer.getControl().setLayoutData(gd);
		
		groupsViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event){
				IStructuredSelection sel = (IStructuredSelection) groupsViewer.getSelection();
				Object element = sel.getFirstElement();
				if (element instanceof LabGroup) {
					actGroup = (LabGroup) element;
					
					itemsViewer.refresh();
				}
				
				updateButtonsState();
			}
		});
		
		groupsViewer.setContentProvider(new GroupsContentProvider());
		groupsViewer.setLabelProvider(new DefaultLabelProvider());
		
		groupsViewer.setInput(this);
		
		Composite groupButtonArea = new Composite(topArea, SWT.PUSH);
		layout = new GridLayout(1, false);
		layout.marginHeight = 0;
		groupButtonArea.setLayout(layout);
		
		newButton = new Button(groupButtonArea, SWT.PUSH);
		newButton.setText(Messages.LabGroupPrefs_new);
		newButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e){
				InputDialog dialog =
					new InputDialog(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						Messages.LabGroupPrefs_newLabGroup,
						Messages.LabGroupPrefs_selectNameForLabGroup, "", null); //$NON-NLS-1$
				int rc = dialog.open();
				if (rc == Window.OK) {
					String name = dialog.getValue();
					LabGroup group = new LabGroup(name, null);
					
					groupsViewer.refresh();
					groupsViewer.setSelection(new StructuredSelection(group));
				}
			}
			
			public void widgetDefaultSelected(SelectionEvent e){
				widgetSelected(e);
			}
		});
		
		removeButton = new Button(groupButtonArea, SWT.PUSH);
		removeButton.setText(Messages.LabGroupPrefs_delete);
		removeButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e){
				if (actGroup != null) {
					if (SWTHelper.askYesNo(
						Messages.LabGroupPrefs_deleteGroup,
						MessageFormat.format(Messages.LabGroupPrefs_reallyDeleteGroup,
							actGroup.getLabel())))
						;
					{
						
						actGroup.delete();
						actGroup = null;
						groupsViewer.refresh();
						itemsViewer.refresh();
						selectFirstGroup();
					}
				}
			}
			
			public void widgetDefaultSelected(SelectionEvent e){
				widgetSelected(e);
			}
		});
		
		Composite bottomArea = new Composite(composite, SWT.NONE);
		bottomArea.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		bottomArea.setLayout(new GridLayout(1, false));
		
		label = new Label(bottomArea, SWT.NONE);
		label.setText(Messages.LabGroupPrefs_containingLabItems);
		
		itemsViewer =
			new ListViewer(bottomArea, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		itemsViewer.getControl().setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		
		itemsViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event){
				updateItemButtonsState();
			}
		});
		
		itemsViewer.setContentProvider(new GroupItemsContentProvider());
		itemsViewer.setLabelProvider(new ItemsLabelProvider());
		
		itemsViewer.setInput(this);
		
		Composite buttonArea = new Composite(bottomArea, SWT.NONE);
		buttonArea.setLayoutData(SWTHelper.getFillGridData(1, false, 1, false));
		buttonArea.setLayout(new GridLayout(2, true));
		
		addItemButton = new Button(buttonArea, SWT.PUSH);
		addItemButton.setText(Messages.LabGroupPrefs_add);
		addItemButton.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		
		removeItemButton = new Button(buttonArea, SWT.PUSH);
		removeItemButton.setText(Messages.LabGroupPrefs_remove);
		removeItemButton.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		
		addItemButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e){
				if (actGroup != null) {
					ItemsSelectionDialog dialog =
						new ItemsSelectionDialog(PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getShell(), actGroup);
					if (dialog.open() == ItemsSelectionDialog.OK) {
						itemsViewer.refresh();
					}
				}
			}
			
			public void widgetDefaultSelected(SelectionEvent e){
				widgetSelected(e);
			}
		});
		
		removeItemButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e){
				if (actGroup != null) {
					IStructuredSelection sel = (IStructuredSelection) itemsViewer.getSelection();
					for (Object obj : sel.toList()) {
						if (obj instanceof LabItem) {
							LabItem item = (LabItem) obj;
							actGroup.removeItem(item);
						}
					}
					
					itemsViewer.refresh();
				}
			}
			
			public void widgetDefaultSelected(SelectionEvent e){
				widgetSelected(e);
			}
		});
		
		selectFirstGroup();
		
		return composite;
	}
	
	public void init(IWorkbench workbench){
		// nothing to do
	}
	
	private void selectFirstGroup(){
		Object element = groupsViewer.getElementAt(0);
		if (element != null) {
			groupsViewer.setSelection(new StructuredSelection(element));
		}
	}
	
	private void updateButtonsState(){
		updateGroupButtonsState();
		updateItemButtonsState();
	}
	
	private void updateGroupButtonsState(){
		if (actGroup != null) {
			removeButton.setEnabled(true);
		} else {
			removeButton.setEnabled(false);
		}
	}
	
	private void updateItemButtonsState(){
		if (actGroup != null) {
			addItemButton.setEnabled(true);
			
			IStructuredSelection sel = (IStructuredSelection) itemsViewer.getSelection();
			Object element = sel.getFirstElement();
			if (element instanceof LabItem) {
				removeItemButton.setEnabled(true);
			} else {
				removeItemButton.setEnabled(false);
			}
		} else {
			addItemButton.setEnabled(false);
			removeItemButton.setEnabled(false);
		}
	}
	
	static class GroupsContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement){
			Query<LabGroup> query = new Query<LabGroup>(LabGroup.class);
			query.orderBy(false, new String[] {
				"Name"}); //$NON-NLS-1$
			
			List<LabGroup> groups = query.execute();
			if (groups == null) {
				groups = new ArrayList<LabGroup>();
			}
			
			return groups.toArray();
		}
		
		public void dispose(){
			// nothing to do
		}
		
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput){
			// nothing to do
		}
	}
	
	static class ItemsLabelProvider extends DefaultLabelProvider {
		@Override
		public String getText(Object element){
			if (element instanceof LabItem) {
				LabItem item = (LabItem) element;
				
				StringBuffer sb = new StringBuffer();
				sb.append(item.getGroup());
				sb.append(" - "); //$NON-NLS-1$
				sb.append(item.get("titel")); //$NON-NLS-1$
				
				return sb.toString();
			} else {
				return element.toString();
			}
		}
	}
	
	class GroupItemsContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement){
			if (actGroup != null) {
				List<LabItem> items = actGroup.getItems();
				Collections.sort(items);
				return items.toArray();
			} else {
				return new Object[] {};
			}
		}
		
		public void dispose(){
			// nothing to do
		}
		
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput){
			// nothing to do
		}
	}
	
	class ItemsSelectionDialog extends TitleAreaDialog {
		private LabGroup group;
		
		private ListViewer viewer;
		
		ItemsSelectionDialog(Shell parentShell, LabGroup group){
			super(parentShell);
			this.group = group;
		}
		
		protected Control createContents(Composite parent){
			Control contents = super.createContents(parent);
			
			setMessage(Messages.LabGroupPrefs_pleaseSelectLabItems);
			setTitle(Messages.LabGroupPrefs_selectLabItems);
			
			return contents;
		}
		
		protected Control createDialogArea(Composite parent){
			Composite composite = (Composite) super.createDialogArea(parent);
			composite.setLayout(new GridLayout(1, false));
			
			Label label = new Label(composite, SWT.NONE);
			label.setText(Messages.LabGroupPrefs_group1 + group.getName());
			
			viewer =
				new ListViewer(composite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
			viewer.getControl().setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			
			viewer.setContentProvider(new ItemsContentProvider());
			viewer.setLabelProvider(new ItemsLabelProvider());
			
			viewer.setInput(this);
			
			return composite;
		}
		
		protected void buttonPressed(int buttonId){
			if (buttonId == OK) {
				IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
				
				// list of existing items
				List<LabItem> existingItems = group.getItems();
				
				List<LabItem> items = new ArrayList<LabItem>();
				for (Object obj : sel.toList()) {
					if (obj instanceof LabItem) {
						LabItem item = (LabItem) obj;
						if (!existingItems.contains(item)) {
							items.add(item);
						}
					}
				}
				
				group.addItems(items);
			}
			
			setReturnCode(buttonId);
			close();
		}
		
		class ItemsContentProvider implements IStructuredContentProvider {
			public Object[] getElements(Object inputElement){
				Query<LabItem> query = new Query<LabItem>(LabItem.class);
				query.orderBy(false, new String[] {
					"Gruppe", "prio", "titel"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				
				List<LabItem> items = query.execute();
				if (items == null) {
					items = new ArrayList<LabItem>();
				}
				
				// remove items already contained in the group
				List<LabItem> contained = group.getItems();
				items.removeAll(contained);
				
				return items.toArray();
			}
			
			public void dispose(){
				// nothing to do
			}
			
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput){
				// nothing to do
			}
		}
	}
}
