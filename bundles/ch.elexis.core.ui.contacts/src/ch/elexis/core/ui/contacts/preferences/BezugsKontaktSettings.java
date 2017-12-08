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

package ch.elexis.core.ui.contacts.preferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.types.LocalizeUtil;
import ch.elexis.core.types.RelationshipType;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.contacts.dialogs.BezugsKontaktAuswahl;
import ch.elexis.core.ui.contacts.views.Patientenblatt2;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.preferences.SettingsPreferenceStore;
import ch.elexis.core.ui.views.Messages;
import ch.elexis.data.BezugsKontaktRelation;

public class BezugsKontaktSettings extends PreferencePage implements IWorkbenchPreferencePage {

	private TableViewer tableViewer;
	private Table tableBezugsKontaktRelations;
	
	private Set<String> updateExistingEntriesIds = new HashSet<>();
	private List<BezugsKontaktRelation> initalValues = new ArrayList<>();
	
	private final boolean allowEditing;
	
	public BezugsKontaktSettings(){
		noDefaultAndApplyButton();
		setTitle(Messages.Bezugskontakt_Title);
		this.allowEditing = CoreHub.acl.request(AccessControlDefaults.ADMIN);
	}
	
	@Override
	protected Control createContents(Composite parent){
		updateExistingEntriesIds.clear();
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new GridLayout(1, false));
		
		Group group = new Group(container, SWT.NONE);
		group.setLayout(new GridLayout(1, false));
		group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		group.setText(Messages.Bezugskontakt_Definition);
		
		Composite composite = new Composite(group, SWT.NONE);
		GridData gd_composite = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_composite.heightHint = 300;
		composite.setLayoutData(gd_composite);
		TableColumnLayout tcl_composite = new TableColumnLayout();
		composite.setLayout(tcl_composite);
		
		tableViewer = new TableViewer(composite, SWT.BORDER | SWT.FULL_SELECTION);
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		
		tableBezugsKontaktRelations = tableViewer.getTable();
		tableBezugsKontaktRelations.setHeaderVisible(true);
		tableBezugsKontaktRelations.setLinesVisible(true);
		
		if (allowEditing) {
			Menu menu = new Menu(tableBezugsKontaktRelations);
			tableBezugsKontaktRelations.setMenu(menu);
		
			MenuItem mntmAddBezugsKontaktRelation = new MenuItem(menu, SWT.NONE);
			mntmAddBezugsKontaktRelation.setText(Messages.Bezugskontakt_Add);
			mntmAddBezugsKontaktRelation.setImage(Images.IMG_NEW.getImage());
			mntmAddBezugsKontaktRelation.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e){
					BezugsKontaktRelation bezugsKontaktRelation = new BezugsKontaktRelation("",
						RelationshipType.AGENERIC, RelationshipType.AGENERIC);
					tableViewer.add(bezugsKontaktRelation);
					tableViewer.setSelection(new StructuredSelection(bezugsKontaktRelation));
				}
			});
			
			MenuItem mntmRemoveBezugsKontaktRelation = new MenuItem(menu, SWT.NONE);
			mntmRemoveBezugsKontaktRelation.setText(Messages.Bezugskontakt_Delete);
			mntmRemoveBezugsKontaktRelation.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e){
					int selectionsIdx = tableViewer.getTable().getSelectionIndex();
					if (selectionsIdx != -1) {
						boolean ret = MessageDialog.openQuestion(UiDesk.getTopShell(),
							Messages.Bezugskontakt_ConfirmDelete,
							Messages.Bezugskontakt_ConfirmDeleteText);
						if (ret) {
							tableViewer.getTable().remove(selectionsIdx);
						}
					}
				}
			});
		}
		
		TableViewerColumn viewCol = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn col = viewCol.getColumn();
		tcl_composite.setColumnData(col, new ColumnWeightData(1, 140));
		col.setText(Messages.BezugsKonktat_Reference);
		col.setToolTipText(Messages.Bezugskontakt_ReferenceTooltip);
		viewCol.setLabelProvider(new CellLabelProvider() {
			
			@Override
			public void update(ViewerCell cell){
				BezugsKontaktRelation s = (BezugsKontaktRelation) cell.getElement();
				if (s == null)
					return;
				cell.setText(s.getName());
			}
		});
		viewCol.setEditingSupport(new EditingSupport(tableViewer) {
			
			@Override
			protected void setValue(Object element, Object value){
				if (element instanceof BezugsKontaktRelation) {
					String newName = String.valueOf(value);
					BezugsKontaktRelation tableData = null;
					for (TableItem tableItem : tableViewer.getTable().getItems()) {
						tableData = (BezugsKontaktRelation) tableItem.getData();
						if (tableData != null && !tableData.equals(element)
							&& !tableData.getName().isEmpty()
							&& newName.equalsIgnoreCase(tableData.getName())) {
							MessageDialog.openError(UiDesk.getTopShell(), "",
								Messages.Bezugskontakt_NameMustBeUnique);
							return;
						}
					}
					BezugsKontaktRelation bezugsKontaktRelation = (BezugsKontaktRelation) element;
					if (!bezugsKontaktRelation.getName().equals(newName)) {
						bezugsKontaktRelation.setName(newName);
						getViewer().update(bezugsKontaktRelation, null);
						openConfirmUpdateExistingData(bezugsKontaktRelation);
					}
				}
				
			}
			
			@Override
			protected Object getValue(Object element){
				if (element instanceof BezugsKontaktRelation) {
					return ((BezugsKontaktRelation) element).getName();
				}
				return null;
			}
			
			@Override
			protected CellEditor getCellEditor(Object element){
				return new TextCellEditor(tableViewer.getTable());
			}
			
			@Override
			protected boolean canEdit(Object element){
				return allowEditing;
			}
		});
		
		viewCol = new TableViewerColumn(tableViewer, SWT.NONE);
		col = viewCol.getColumn();
		tcl_composite.setColumnData(col, new ColumnWeightData(0, 140));
		col.setText(Messages.Bezugskontakt_RelationFrom);
		viewCol.setLabelProvider(new CellLabelProvider() {
			
			@Override
			public void update(ViewerCell cell){
				BezugsKontaktRelation s = (BezugsKontaktRelation) cell.getElement();
				if (s == null)
					return;
				cell.setText(LocalizeUtil.getLocaleText(s.getDestRelationType()));
			}
		});
		viewCol.setEditingSupport(new EditingSupport(tableViewer) {
			
			@Override
			protected void setValue(Object element, Object value){
				if (element instanceof BezugsKontaktRelation && value instanceof Integer) {
					BezugsKontaktRelation bezugsKontaktRelation = (BezugsKontaktRelation) element;
					RelationshipType[] allRelationshipTypes = RelationshipType.values();
					if ((int) value != -1 && !bezugsKontaktRelation.getDestRelationType()
						.equals(allRelationshipTypes[(int) value])) {
						bezugsKontaktRelation
							.setDestRelationType(allRelationshipTypes[(int) value]);
						getViewer().update(bezugsKontaktRelation, null);
						openConfirmUpdateExistingData(bezugsKontaktRelation);
					}
				}
				
			}
			
			@Override
			protected Object getValue(Object element){
				if (element instanceof BezugsKontaktRelation) {
					BezugsKontaktRelation bezugsKontaktRelation = (BezugsKontaktRelation) element;
					RelationshipType relationshipType = bezugsKontaktRelation.getDestRelationType();
					if (relationshipType != null) {
						return relationshipType.getValue();
					}
				}
				
				return 0;
			}
			
			@Override
			protected CellEditor getCellEditor(Object element){
				return new ComboBoxCellEditor(tableViewer.getTable(),
					BezugsKontaktAuswahl.getBezugKontaktTypes(), SWT.NONE);
			}
			
			@Override
			protected boolean canEdit(Object element){
				return allowEditing;
			}
		});
		
		viewCol = new TableViewerColumn(tableViewer, SWT.NONE);
		col = viewCol.getColumn();
		tcl_composite.setColumnData(col, new ColumnWeightData(0, 140));
		col.setText(Messages.Bezugskontakt_RelationTo);
		
		viewCol.setLabelProvider(new CellLabelProvider() {
			
			@Override
			public void update(ViewerCell cell){
				BezugsKontaktRelation s = (BezugsKontaktRelation) cell.getElement();
				if (s == null)
					return;
				cell.setText(LocalizeUtil.getLocaleText(s.getSrcRelationType()));
			}
		});
		viewCol.setEditingSupport(new EditingSupport(tableViewer) {
			

			@Override
			protected void setValue(Object element, Object value){
				if (element instanceof BezugsKontaktRelation && value instanceof Integer) {
					BezugsKontaktRelation bezugsKontaktRelation = (BezugsKontaktRelation) element;
					RelationshipType[] allRelationshipTypes = RelationshipType.values();
					if ((int) value != -1 && !bezugsKontaktRelation.getSrcRelationType()
						.equals(allRelationshipTypes[(int) value])) {
						bezugsKontaktRelation.setSrcRelationType(allRelationshipTypes[(int) value]);
						getViewer().update(bezugsKontaktRelation, null);
						openConfirmUpdateExistingData(bezugsKontaktRelation);
					}
				}
			}
			
			@Override
			protected Object getValue(Object element){
				if (element instanceof BezugsKontaktRelation) {
					BezugsKontaktRelation bezugsKontaktRelation = (BezugsKontaktRelation) element;
					RelationshipType relationshipType = bezugsKontaktRelation.getSrcRelationType();
					if (relationshipType != null) {
						return relationshipType.getValue();
					}
				}
				
				return 0;
			}
			
			@Override
			protected CellEditor getCellEditor(Object element){
				return new ComboBoxCellEditor(tableViewer.getTable(),
					BezugsKontaktAuswahl.getBezugKontaktTypes(), SWT.NONE);
			}
			
			@Override
			protected boolean canEdit(Object element){
				return allowEditing;
			}
		});
		;
		tableViewer.setInput(loadBezugKonkaktTypes(
			CoreHub.globalCfg.get(Patientenblatt2.CFG_BEZUGSKONTAKTTYPEN, "")));
		return container;
	}
	
	

	private List<BezugsKontaktRelation> loadBezugKonkaktTypes(String cfgBezugKonktaks){
		List<BezugsKontaktRelation> bezugsKontaktRelations = new ArrayList<>();
		initalValues.clear();
		for (String cfgPart : cfgBezugKonktaks.split(Patientenblatt2.SPLITTER)) {
			BezugsKontaktRelation bezugsKontaktRelation = new BezugsKontaktRelation();
			bezugsKontaktRelation.loadValuesByCfg(cfgPart);
			initalValues.add(bezugsKontaktRelation);
			bezugsKontaktRelations.add(new BezugsKontaktRelation(bezugsKontaktRelation));
		}
		return bezugsKontaktRelations;
	}
	
	@Override
	public void init(IWorkbench workbench){
		setPreferenceStore(new SettingsPreferenceStore(CoreHub.globalCfg));
		
	}
	
	@Override
	protected void performApply(){
		setErrorMessage(null);
		super.performApply();
	}
	
	@Override
	public boolean performOk(){
		if (allowEditing) {
			StringBuffer cfg = new StringBuffer();
			BezugsKontaktRelation bezugsKontaktRelation = null;
			boolean firstElement = true;
			for (TableItem tableItem : tableViewer.getTable().getItems()) {
				bezugsKontaktRelation = (BezugsKontaktRelation) tableItem.getData();
				if (bezugsKontaktRelation != null) {
					if (firstElement) {
						firstElement = false;
					} else {
						cfg.append(Patientenblatt2.SPLITTER);
					}
					cfg.append(bezugsKontaktRelation.getCfgString());
					if (updateExistingEntriesIds.contains(bezugsKontaktRelation.getId())) {
						updateExistingEntries(bezugsKontaktRelation);
					}
				}
			}
			CoreHub.globalCfg.set(Patientenblatt2.CFG_BEZUGSKONTAKTTYPEN, cfg.toString());
		}
		return super.performOk();
	}
	
	private void updateExistingEntries(BezugsKontaktRelation newBezugsKontaktRelation){
		BezugsKontaktRelation initalBezugKonktaktRelation =
			getInitalBezugKontakt(newBezugsKontaktRelation.getId());
		if (initalBezugKonktaktRelation != null) {
			initalBezugKonktaktRelation.updateToNewBezugKontakt(newBezugsKontaktRelation);
		}
	}
	
	private BezugsKontaktRelation getInitalBezugKontakt(String id){
		for (BezugsKontaktRelation initalBezugKonktaktRelation : initalValues) {
			if (initalBezugKonktaktRelation.getId().equals(id)) {
				return initalBezugKonktaktRelation;
			}
		}
		return null;
	}
	
	private void openConfirmUpdateExistingData(BezugsKontaktRelation bezugsKontaktRelation){
		if (!updateExistingEntriesIds.contains(bezugsKontaktRelation.getId())) {
			BezugsKontaktRelation initalBezugKonktaktRelation =
				getInitalBezugKontakt(bezugsKontaktRelation.getId());
			if (initalBezugKonktaktRelation != null) {
				boolean ret = MessageDialog.openQuestion(UiDesk.getTopShell(), "",
					Messages.Bezugskontakt_ConfirmUpdateExisting);
				if (ret) {
					updateExistingEntriesIds.add(bezugsKontaktRelation.getId());
				}
			}
		}
		
	}
}
