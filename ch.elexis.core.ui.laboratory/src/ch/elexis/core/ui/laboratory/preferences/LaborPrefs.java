/*******************************************************************************
 * Copyright (c) 2005-2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    M. Descher - adapted layout
 *    
 *******************************************************************************/

package ch.elexis.core.ui.laboratory.preferences;

import java.util.List;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;

import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.laboratory.commands.CreateImportMappingUi;
import ch.elexis.core.ui.laboratory.commands.CreateLabItemUi;
import ch.elexis.core.ui.laboratory.commands.CreateMappingFrom2_1_7;
import ch.elexis.core.ui.laboratory.commands.CreateMergeLabItemUi;
import ch.elexis.core.ui.laboratory.commands.EditLabItemUi;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.LabItem;
import ch.elexis.data.LabResult;
import ch.elexis.data.Query;

public class LaborPrefs extends PreferencePage implements IWorkbenchPreferencePage {
	
	// DynamicListDisplay params;
	// Composite definition;
	// FormToolkit tk;
	private TableViewer tableViewer;
	private Table table;
	int sortC = 1;
	private String[] headers = {
		Messages.LaborPrefs_name, Messages.LaborPrefs_short,
		"LOINC", //$NON-NLS-1$
		Messages.LaborPrefs_type, Messages.LaborPrefs_unit, Messages.LaborPrefs_refM,
		Messages.LaborPrefs_refF, Messages.LaborPrefs_sortmode
	};
	private int[] colwidth = {
		16, 6, 6, 6, 6, 16, 16, 16
	};
	
	public LaborPrefs(){
		super(Messages.LaborPrefs_labTitle);
		
	}
	
	protected Control createContents(Composite parn){
		noDefaultAndApplyButton();
		
		Composite tableComposite = new Composite(parn, SWT.NONE);
		GridData gd = new GridData();
		tableComposite.setLayoutData(gd);
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		tableComposite.setLayout(tableColumnLayout);
		tableViewer = new TableViewer(tableComposite, SWT.BORDER | SWT.FULL_SELECTION);
		table = tableViewer.getTable();
		
		for (int i = 0; i < headers.length; i++) {
			TableColumn tc = new TableColumn(table, SWT.LEFT);
			tc.setText(headers[i]);
			tableColumnLayout.setColumnData(tc, new ColumnWeightData(colwidth[i], true));
			tc.setData(i);
			tc.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e){
					sortC = (Integer) ((TableColumn) e.getSource()).getData();
					tableViewer.refresh(true);
				}
				
			});
		}
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		tableViewer.setContentProvider(new IStructuredContentProvider() {
			
			public Object[] getElements(Object inputElement){
				return LabItem.getLabItems().toArray();
			}
			
			public void dispose(){}
			
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput){}
			
		});
		tableViewer.setLabelProvider(new LabListLabelProvider());
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			
			public void doubleClick(DoubleClickEvent event){
				IStructuredSelection sel = (IStructuredSelection) tableViewer.getSelection();
				Object o = sel.getFirstElement();
				if (o instanceof LabItem) {
					LabItem li = (LabItem) o;
					EditLabItemUi.executeWithParams(li);
					tableViewer.refresh();
				}
			}
			
		});
		tableViewer.setSorter(new ViewerSorter() {
			
			@Override
			public int compare(Viewer viewer, Object e1, Object e2){
				LabItem li1 = (LabItem) e1;
				LabItem li2 = (LabItem) e2;
				String s1 = "", s2 = ""; //$NON-NLS-1$ //$NON-NLS-2$
				switch (sortC) {
				case 1:
					s1 = li1.getKuerzel();
					s2 = li2.getKuerzel();
					break;
				case 3:
					s1 = li1.getTyp().toString();
					s2 = li2.getTyp().toString();
					break;
				case 7:
					s1 = li1.getGroup();
					s2 = li2.getGroup();
					break;
				default:
					s1 = li1.getName();
					s2 = li2.getName();
				}
				int res = s1.compareToIgnoreCase(s2);
				if (res == 0) {
					try {
						Integer no1 = Integer.parseInt(li1.getPrio());
						Integer no2 = Integer.parseInt(li2.getPrio());
						
						return no1.compareTo(no2);
					} catch (NumberFormatException nfe) {
						return li1.getPrio().compareToIgnoreCase(li2.getPrio());
					}
				}
				return res;
			}
			
		});
		
		int operations = DND.DROP_COPY;
		Transfer[] transferTypes = new Transfer[] {
			TextTransfer.getInstance()
		};
		tableViewer.addDragSupport(operations, transferTypes, new DragSourceAdapter() {
			
			@Override
			public void dragSetData(DragSourceEvent event){
				IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
				LabItem labItem = (LabItem) selection.getFirstElement();
				if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
					event.data =
						labItem.getKuerzel() + "," + labItem.getName() + "," + labItem.getId();
				}
			}
		});
		
		tableViewer.setInput(this);
		return tableComposite;
	}
	
	static class LabListLabelProvider extends ColumnLabelProvider implements ITableLabelProvider {
		
		public String getColumnText(Object element, int columnIndex){
			LabItem li = (LabItem) element;
			
			String[] values =
				li.get(true, LabItem.TITLE, LabItem.SHORTNAME, LabItem.LOINCCODE, LabItem.UNIT,
					LabItem.REF_MALE, LabItem.REF_FEMALE_OR_TEXT, LabItem.GROUP, LabItem.PRIO);
			String name = values[0];
			String kuerzel = values[1];
			String loinccode = values[2];
			String einheit = values[3];
			String refM = values[4];
			String refF = values[5].split("##")[0];
			String groupPrio = values[6] + " - " + values[7];
			
			switch (columnIndex) {
			case 0:
				return name;
			case 1:
				return kuerzel;
			case 2:
				return loinccode;
			case 3:
				LabItem.typ typ = li.getTyp();
				if (typ == LabItem.typ.NUMERIC) {
					return Messages.LaborPrefs_numeric;
				} else if (typ == LabItem.typ.TEXT) {
					return Messages.LaborPrefs_alpha;
				} else if (typ == LabItem.typ.FORMULA) {
					return Messages.LaborPrefs_formula;
				} else if (typ == LabItem.typ.DOCUMENT) {
					return Messages.LaborPrefs_document;
				}
				return Messages.LaborPrefs_absolute;
			case 4:
				return einheit;
			case 5:
				return refM;
			case 6:
				return refF;
			case 7:
				return groupPrio; //$NON-NLS-1$
			default:
				return "?col?"; //$NON-NLS-1$
			}
		}
		
		@Override
		public Image getColumnImage(Object element, int columnIndex){
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public Color getBackground(Object element){
			LabItem li = (LabItem) element;
			if (li.isVisible()) {
				return Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
			} else {
				return Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
			}
		}
	};
	
	@Override
	protected void contributeButtons(Composite parent){
		((GridLayout) parent.getLayout()).numColumns++;
		Button bMappingFrom2_1_7 = new Button(parent, SWT.PUSH);
		bMappingFrom2_1_7.setText(Messages.LaborPrefs_mappingFrom2_1_7);
		bMappingFrom2_1_7.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				try {
					// execute the command
					IHandlerService handlerService =
						(IHandlerService) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
							.getService(IHandlerService.class);
					
					handlerService.executeCommand(CreateMappingFrom2_1_7.COMMANDID, null);
				} catch (Exception ex) {
					throw new RuntimeException(CreateMappingFrom2_1_7.COMMANDID, ex);
				}
				tableViewer.refresh();
			}
		});
		
		if (CoreHub.acl.request(AccessControlDefaults.LABITEM_MERGE) == true) {
			((GridLayout) parent.getLayout()).numColumns++;
			Button bImportMapping = new Button(parent, SWT.PUSH);
			bImportMapping.setText(Messages.LaborPrefs_mergeLabItems);
			bImportMapping.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e){
					try {
						// execute the command
						IHandlerService handlerService =
							(IHandlerService) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
								.getService(IHandlerService.class);
						
						handlerService.executeCommand(CreateMergeLabItemUi.COMMANDID, null);
					} catch (Exception ex) {
						throw new RuntimeException(CreateMergeLabItemUi.COMMANDID, ex);
					}
					tableViewer.refresh();
				}
			});
		}
		
		((GridLayout) parent.getLayout()).numColumns++;
		Button bImportMapping = new Button(parent, SWT.PUSH);
		bImportMapping.setText(Messages.LaborPrefs_importLabMapping);
		bImportMapping.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				try {
					// execute the command
					IHandlerService handlerService =
						(IHandlerService) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
							.getService(IHandlerService.class);
					
					handlerService.executeCommand(CreateImportMappingUi.COMMANDID, null);
				} catch (Exception ex) {
					throw new RuntimeException(CreateImportMappingUi.COMMANDID, ex);
				}
				tableViewer.refresh();
			}
		});
		((GridLayout) parent.getLayout()).numColumns++;
		Button bNewItem = new Button(parent, SWT.PUSH);
		bNewItem.setText(Messages.LaborPrefs_labValue);
		bNewItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				try {
					// execute the command
					IHandlerService handlerService =
						(IHandlerService) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
							.getService(IHandlerService.class);
					
					handlerService.executeCommand(CreateLabItemUi.COMMANDID, null);
				} catch (Exception ex) {
					throw new RuntimeException(CreateLabItemUi.COMMANDID, ex);
				}
				tableViewer.refresh();
			}
		});
		((GridLayout) parent.getLayout()).numColumns++;
		Button bDelItem = new Button(parent, SWT.PUSH);
		bDelItem.setText(Messages.LaborPrefs_deleteItem);
		bDelItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e){
				IStructuredSelection sel = (IStructuredSelection) tableViewer.getSelection();
				Object o = sel.getFirstElement();
				if (o instanceof LabItem) {
					LabItem li = (LabItem) o;
					Query<LabResult> qbe = new Query<LabResult>(LabResult.class);
					qbe.add("ItemID", "=", li.getId()); //$NON-NLS-1$ //$NON-NLS-2$
					List<LabResult> list = qbe.execute();
					for (LabResult po : list) {
						po.delete();
					}
					li.delete();
					tableViewer.remove(o);
				}
			}
		});
		((GridLayout) parent.getLayout()).numColumns++;
		Button bDelAllItems = new Button(parent, SWT.PUSH);
		bDelAllItems.setText(Messages.LaborPrefs_deleteAllItems);
		bDelAllItems.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e){
				if (SWTHelper.askYesNo(Messages.LaborPrefs_deleteReallyAllItems,
					Messages.LaborPrefs_deleteAllExplain)) {
					Query<LabItem> qbli = new Query<LabItem>(LabItem.class);
					List<LabItem> items = qbli.execute();
					for (LabItem li : items) {
						Query<LabResult> qbe = new Query<LabResult>(LabResult.class);
						qbe.add("ItemID", "=", li.getId()); //$NON-NLS-1$ //$NON-NLS-2$
						List<LabResult> list = qbe.execute();
						for (LabResult po : list) {
							po.delete();
						}
						li.delete();
					}
					tableViewer.refresh();
				}
			}
		});
		if (CoreHub.acl.request(AccessControlDefaults.DELETE_LABITEMS) == false) {
			bDelAllItems.setEnabled(false);
		}
	}
	
	public void init(IWorkbench workbench){
		// Nothing to initialize
	}
	
	@Override
	public Point computeSize(){
		// TODO Auto-generated method stub
		return new Point(350, 350);
	}
}
