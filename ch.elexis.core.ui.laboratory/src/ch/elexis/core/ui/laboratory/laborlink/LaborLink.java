/*******************************************************************************
 * Copyright (c) 2006-2010, D. Lutz and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    D. Lutz - initial implementation
 *    G. Weirich - Adapted for API changes
 * 
 *******************************************************************************/

package ch.elexis.core.ui.laboratory.laborlink;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.data.Anwender;
import ch.elexis.core.data.LabGroup;
import ch.elexis.core.data.LabItem;
import ch.elexis.core.data.LabResult;
import ch.elexis.core.data.Patient;
import ch.elexis.core.data.Query;
import ch.elexis.core.data.Reminder;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.UiResourceConstants;
import ch.elexis.core.ui.laboratory.views.LaborView;
import ch.elexis.core.ui.text.IRichTextDisplay;
import ch.elexis.core.ui.util.IKonsExtension;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.viewers.DefaultLabelProvider;
import ch.rgw.tools.TimeTool;

public class LaborLink implements IKonsExtension {
	public static final String PROVIDER_ID = "laborlink";
	
	private static final String LABOR_COLOR = "ffc8c8";
	
	IRichTextDisplay textField;
	
	@Override
	public String connect(IRichTextDisplay textField){
		this.textField = textField;
		return PROVIDER_ID;
	}
	
	public boolean doLayout(StyleRange n, String provider, String id){
		n.background = UiDesk.getColorFromRGB(LABOR_COLOR);
		return true;
	}
	
	public boolean doXRef(String refProvider, String refID){
		// update LaborView and show it
		LaborView laborView =
			(LaborView) Hub.plugin.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.findView(UiResourceConstants.LaborView_ID);
		if (laborView != null) {
			laborView.rebuild();
			Hub.plugin.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.activate(laborView);
		}
		
		return true;
	}
	
	public IAction[] getActions(){
		IAction[] ret = new IAction[1];
		ret[0] = new Action("Labor verordnen") {
			@Override
			public void run(){
				Patient patient = ElexisEventDispatcher.getSelectedPatient();
				if (patient == null) {
					return;
				}
				TimeTool date = new TimeTool();
				
				// insert XRef
				textField.insertXRef(-1, "Labor", PROVIDER_ID, "");
				
				LaborVerordnungDialog dialog =
					new LaborVerordnungDialog(UiDesk.getTopShell(), patient, date);
				dialog.open();
			}
		};
		return ret;
	}
	
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
		throws CoreException{
		// TODO Auto-generated method stub
		
	}
	
	public void removeXRef(String refProvider, String refID){
		// nothing to do
	}
	
	public void insert(Object o, int pos){
		// TODO Auto-generated method stub
		
	}
	
	static class LaborVerordnungDialog extends TitleAreaDialog {
		// height of laborViewer
		private static final int LINES_TO_SHOW = 20;
		
		private static final String LAST_SELECTED_USER = PROVIDER_ID + "/last_selected_user";
		
		private Patient patient = null;
		private TimeTool date = null;
		
		private TreeViewer laborViewer = null;
		private ComboViewer userViewer = null;
		
		// Gruppen von Laboritems
		private Hashtable<String, Group> hGroups;
		// Rueckverlinkung hGroups
		private Hashtable<LabItem, Group> hLabItems;
		// Alphabetische Gruppenliste
		private List<String> lGroupNames;
		
		private List<Group> customGroups;
		
		public LaborVerordnungDialog(Shell parentShell, Patient patient, TimeTool date){
			super(parentShell);
			this.patient = patient;
			this.date = date;
		}
		
		/**
		 * Liste der Laboritems, Gruppiert nach groups und Sequenznummer aufbauen
		 * 
		 */
		@SuppressWarnings("unchecked")
		private void loadItems(){
			hGroups = new Hashtable<String, Group>();
			hLabItems = new Hashtable<LabItem, Group>();
			lGroupNames = new ArrayList<String>();
			
			Query<LabItem> query = new Query<LabItem>(LabItem.class);
			List<LabItem> lItems = query.execute();
			if (lItems == null) {
				// error
				return;
			}
			
			for (LabItem it : lItems) {
				String groupName = it.getGroup();
				Group group = hGroups.get(groupName);
				if (group == null) {
					group = new Group(groupName, new ArrayList<LabItem>());
					hGroups.put(groupName, group);
					
					// sortiert in die Gruppenliste einfuegen
					int i = 0;
					for (i = 0; i < lGroupNames.size(); i++) {
						if (groupName.compareTo(lGroupNames.get(i)) < 0) {
							break;
						}
					}
					lGroupNames.add(i, groupName);
				}
				List<LabItem> lGroupItems = group.items;
				lGroupItems.add(it);
				Collections.sort(lGroupItems);
				hLabItems.put(it, group);
			}
		}
		
		/**
		 * Load User-defined LabGroups
		 */
		private void loadCustomGroups(){
			customGroups = new ArrayList<Group>();
			
			Query<LabGroup> query = new Query<LabGroup>(LabGroup.class);
			query.orderBy(false, "Name");
			List<LabGroup> labGroups = query.execute();
			if (labGroups != null) {
				for (LabGroup labGroup : labGroups) {
					Group group = new Group(labGroup);
					customGroups.add(group);
				}
			}
		}
		
		/**
		 * Selects already measured values. The current selection is preserved.
		 * 
		 */
		@SuppressWarnings("unchecked")
		private void selectMeasured(){
			if (laborViewer != null && date != null) {
				Query<LabResult> query = new Query<LabResult>(LabResult.class);
				query.add("PatientID", "=", patient.getId());
				query.add("Datum", "=", date.toString(TimeTool.DATE_ISO));
				List<LabResult> results = query.execute();
				if (results != null && results.size() > 0) {
					// there are alread measured values
					List<LabItem> items = new ArrayList<LabItem>();
					for (LabResult result : results) {
						items.add(result.getItem());
					}
					
					List<TreePath> treePaths = new ArrayList<TreePath>();
					for (LabItem item : items) {
						Group group = hLabItems.get(item);
						if (group != null) {
							TreePath treePath = new TreePath(new Object[] {
								group, item
							});
							treePaths.add(treePath);
						}
					}
					TreePath[] treePathsArray = new TreePath[treePaths.size()];
					for (int i = 0; i < treePathsArray.length; i++) {
						treePathsArray[i] = treePaths.get(i);
					}
					
					TreeSelection newSelection = new TreeSelection(treePathsArray);
					laborViewer.setSelection(newSelection, false);
				}
			}
		}
		
		private void selectLastSelectedUser(){
			String id = CoreHub.userCfg.get(LAST_SELECTED_USER, "");
			Anwender user = Anwender.load(id);
			if (user != null && user.exists()) {
				StructuredSelection newSelection = new StructuredSelection(user);
				userViewer.setSelection(newSelection);
			}
		}
		
		private void saveLastSelectedUser(){
			Anwender user = getSelectedUser();
			String id = "";
			if (user != null) {
				id = user.getId();
			}
			CoreHub.userCfg.set(LAST_SELECTED_USER, id);
		}
		
		@Override
		protected Control createDialogArea(Composite parent){
			loadItems();
			loadCustomGroups();
			
			Composite composite = (Composite) super.createDialogArea(parent);
			composite.setLayout(new GridLayout(1, false));
			
			Label label;
			
			label = new Label(composite, SWT.NONE);
			label.setText("Laborwerte:");
			
			laborViewer =
				new TreeViewer(composite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
			GridData gd = SWTHelper.getFillGridData(1, true, 1, true);
			// initially, show 10 lines
			gd.heightHint = laborViewer.getTree().getItemHeight() * LINES_TO_SHOW;
			laborViewer.getControl().setLayoutData(gd);
			
			label = new Label(composite, SWT.NONE);
			label.setText("Verantwortliche Person:");
			
			userViewer =
				new ComboViewer(composite, SWT.SINGLE | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL
					| SWT.BORDER);
			userViewer.getControl().setLayoutData(SWTHelper.getFillGridData(1, false, 1, false));
			
			laborViewer.setContentProvider(new ITreeContentProvider() {
				public Object[] getElements(Object inputElement){
					int size = lGroupNames.size() + customGroups.size();
					Group[] elements = new Group[size];
					
					int base1 = 0;
					int base2 = lGroupNames.size();
					
					for (int i = 0; i < lGroupNames.size(); i++) {
						String groupName = lGroupNames.get(i);
						elements[base1 + i] = hGroups.get(groupName);
					}
					
					for (int i = 0; i < customGroups.size(); i++) {
						elements[base2 + i] = customGroups.get(i);
					}
					
					return elements;
				}
				
				public Object[] getChildren(Object parentElement){
					if (parentElement instanceof Group) {
						Group group = (Group) parentElement;
						return group.items.toArray();
					} else {
						return null;
					}
				}
				
				public boolean hasChildren(Object element){
					return (element instanceof Group);
				}
				
				public Object[] getParent(Object element){
					return null;
				}
				
				public void dispose(){
					// nothing to do
				}
				
				public void inputChanged(Viewer viewer, Object oldInput, Object newInput){
					// nothing to do
				}
			});
			
			laborViewer.setLabelProvider(new DefaultLabelProvider());
			
			laborViewer.setInput(this);
			
			userViewer.setContentProvider(new IStructuredContentProvider() {
				public Object[] getElements(Object inputElement){
					Query<Anwender> query = new Query<Anwender>(Anwender.class);
					List<Anwender> users = query.execute();
					if (users != null) {
						return users.toArray();
					} else {
						// error, return empty list
						return new Object[] {};
					}
				}
				
				public void dispose(){
					// nothing to do
				}
				
				public void inputChanged(Viewer viewer, Object oldInput, Object newInput){
					// nothing to do
				}
			});
			
			userViewer.setLabelProvider(new DefaultLabelProvider());
			
			userViewer.setInput(this);
			
			selectMeasured();
			selectLastSelectedUser();
			
			// exand all and make first element be displayed
			laborViewer.expandAll();
			String firstGroupName = lGroupNames.get(0);
			if (firstGroupName != null) {
				Group group = hGroups.get(firstGroupName);
				if (group != null) {
					laborViewer.reveal(group);
				}
			}
			
			return composite;
		}
		
		@Override
		protected Control createContents(Composite parent){
			Control contents = super.createContents(parent);
			setTitle("Labor Verordnen");
			setMessage("Bitte wählen Sie die gewünschten Laborwerte und die"
				+ " verantwortliche Person aus. (Der verantwortlichen Person"
				+ " wird eine Pendenz zugeordnet.)");
			getShell().setText("Labor Verordnen");
			return contents;
		}
		
		private boolean hasResult(LabItem labItem, TimeTool date){
			LabResult result = LabResult.getForDate(patient, date, labItem);
			if (result != null) {
				return true;
			} else {
				return false;
			}
		}
		
		private void createLabItems(List<LabItem> items){
			if (items != null) {
				for (LabItem labItem : items) {
					if (!hasResult(labItem, date)) {
						new LabResult(patient, date, labItem, "?", "");
					}
				}
			}
		}
		
		private void createReminder(Anwender user){
			Reminder reminder =
				new Reminder(patient, date.toString(TimeTool.DATE_ISO),
					Reminder.Typ.anzeigeTodoAll, "", "Labor");
			if (user != null) {
				reminder.set("Responsible", user.getId());
			}
		}
		
		private List<LabItem> getSelectedItems(){
			List<LabItem> labItems = new ArrayList<LabItem>();
			
			IStructuredSelection sel = (IStructuredSelection) laborViewer.getSelection();
			if (sel != null) {
				for (Object obj : sel.toArray()) {
					if (obj instanceof LabItem) {
						labItems.add((LabItem) obj);
					} else if (obj instanceof Group) {
						Group group = (Group) obj;
						labItems.addAll(group.items);
					}
				}
			}
			
			return labItems;
		}
		
		private Anwender getSelectedUser(){
			Object sel = ((IStructuredSelection) userViewer.getSelection()).getFirstElement();
			if (sel instanceof Anwender) {
				return (Anwender) sel;
			} else {
				return null;
			}
		}
		
		@Override
		protected void okPressed(){
			createLabItems(getSelectedItems());
			createReminder(getSelectedUser());
			
			saveLastSelectedUser();
			
			super.okPressed();
		}
		
		static class Group {
			String name;
			String shortName;
			List<LabItem> items;
			
			Group(String name, List<LabItem> items){
				this.name = name;
				this.items = items;
				
				// shortname as in LaborView (without ordering number)
				String[] gn = name.split(" +");
				if (gn.length > 1) {
					shortName = gn[1];
				} else {
					shortName = "? " + name + " ?";
				}
			}
			
			Group(LabGroup labGroup){
				this.name = labGroup.getName();
				this.shortName = this.name;
				
				items = labGroup.getItems();
			}
			
			public String toString(){
				return shortName;
			}
		}
	}
}
