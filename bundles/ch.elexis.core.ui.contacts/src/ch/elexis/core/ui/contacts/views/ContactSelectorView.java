/*******************************************************************************
 * Copyright (c) 2014 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.ui.contacts.views;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.JexlException;
import org.apache.commons.jexl2.MapContext;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.model.IContact;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.contacts.views.comparator.ContactSelectorViewerComparator;
import ch.elexis.core.ui.contacts.views.dnd.ContactSelectorDragListener;
import ch.elexis.core.ui.contacts.views.dnd.ContactSelectorDropListener;
import ch.elexis.core.ui.contacts.views.filter.KontaktAnzeigeTextFieldViewerFilter;
import ch.elexis.core.ui.contacts.views.filter.KontaktAnzeigeTypViewerFilter;
import ch.elexis.core.ui.contacts.views.provider.ContactSelectorObservableMapLabelProvider;
import ch.elexis.core.ui.contacts.views.provider.TableDecoratingLabelProvider;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.data.Patient;

/**
 * @since 3.0.0
 */
public class ContactSelectorView extends ViewPart implements ITabbedPropertySheetPageContributor {
	
	public static final String ID = "ch.elexis.core.ui.contacts.views.ContactSelectorView";
	
	private ObservableListContentProvider contentProvider = null;
	
	private TableViewer tableViewerContacts;
	private WritableList<IContact> contactList;
	
	private Text txtFilter;
	private KontaktAnzeigeTypViewerFilter filterAnzeigeTyp;
	private KontaktAnzeigeTextFieldViewerFilter filterPositionTitle;
	private LoadContactsRunnable loadContactsRunnable;
	
	private Label lblStatus;
	
	public ContactSelectorView(){
		contactList = new WritableList<IContact>();
		contentProvider = new ObservableListContentProvider();
		filterPositionTitle = new KontaktAnzeigeTextFieldViewerFilter();
		loadContactsRunnable = new LoadContactsRunnable();
	}
	
	@Override
	public void createPartControl(Composite parent){
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		
		Composite compositeSearch = new Composite(composite, SWT.NONE);
		compositeSearch.setLayout(new GridLayout(1, false));
		compositeSearch.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		txtFilter = new Text(compositeSearch, SWT.BORDER | SWT.SEARCH | SWT.CANCEL);
		txtFilter.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		tableViewerContacts = new TableViewer(composite, SWT.BORDER | SWT.FULL_SELECTION);
		Table table = tableViewerContacts.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Composite compositeStatus = new Composite(composite, SWT.NONE);
		compositeStatus.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		compositeStatus.setLayout(new GridLayout(1, false));
		
		lblStatus = new Label(compositeStatus, SWT.NONE);
		lblStatus.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		tableViewerContacts.setComparator(new ContactSelectorViewerComparator(tableViewerContacts));
		filterAnzeigeTyp = new KontaktAnzeigeTypViewerFilter(tableViewerContacts);
		ViewerFilter[] filters = new ViewerFilter[] {
			filterAnzeigeTyp, filterPositionTitle
		};
		tableViewerContacts.setFilters(filters);
		
		int operations = DND.DROP_COPY | DND.DROP_MOVE;
		Transfer[] transferTypes = new Transfer[] {
			TextTransfer.getInstance()
		};
		tableViewerContacts.addDragSupport(operations, transferTypes,
			new ContactSelectorDragListener(tableViewerContacts));
		tableViewerContacts.addDropSupport(operations, transferTypes,
			new ContactSelectorDropListener(tableViewerContacts));
		
		txtFilter.addKeyListener(new FilterKeyListener(txtFilter, tableViewerContacts));
		txtFilter.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e){
				if (e.detail == SWT.CANCEL) {
					filterPositionTitle.setSearchText(null);
					tableViewerContacts.getControl().setRedraw(false);
					tableViewerContacts.refresh();
					tableViewerContacts.getControl().setRedraw(true);
				}
			}
		});
		
		initDataBindings();
		
		MenuManager menuManager = new MenuManager();
		menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		tableViewerContacts.getTable()
			.setMenu(menuManager.createContextMenu(tableViewerContacts.getTable()));
		
		getSite().registerContextMenu(menuManager, tableViewerContacts);
		getSite().setSelectionProvider(tableViewerContacts);
		
		contactList.getRealm().asyncExec(loadContactsRunnable);
		
		tableViewerContacts
			.addSelectionChangedListener(new ContactSelectionChangedToEventDispatcher());
		tableViewerContacts.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event){
				StructuredSelection ss = (StructuredSelection) event.getSelection();
				tableViewerContacts.refresh(ss.getFirstElement());
			}
		});
	}
	
	protected DataBindingContext initDataBindings(){
		DataBindingContext bindingContext = new DataBindingContext();
		
		tableViewerContacts.setContentProvider(contentProvider);
		IObservableMap[] observeMaps = BeansObservables
			.observeMaps(contentProvider.getKnownElements(), IContact.class, new String[] {
			// removed to make compatible with NoPo implementation
			});
		ILabelDecorator decorator =
			PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator();
		tableViewerContacts.setLabelProvider(new TableDecoratingLabelProvider(
			new ContactSelectorObservableMapLabelProvider(observeMaps), decorator));
		tableViewerContacts.setInput(contactList);
		return bindingContext;
	}
	
	public void addContact(IContact contact){
		contactList.add(contact);
	}
	
	@Override
	public void setFocus(){
		txtFilter.setFocus();
		tableViewerContacts.refresh();
	}
	
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes")
	Class adapter){
		if (adapter == IPropertySheetPage.class)
			return new TabbedPropertySheetPage(this);
		return super.getAdapter(adapter);
	}
	
	@Override
	public String getContributorId(){
		return getSite().getId();
	}
	
	/**
	 * Listener for the txtFilter text control. Updates the search text filter in
	 * {@link KontaktAnzeigeTextFieldViewerFilter} on key press.
	 */
	private class FilterKeyListener extends KeyAdapter {
		private Text text;
		private StructuredViewer viewer;
		
		FilterKeyListener(Text filterTxt, StructuredViewer viewer){
			text = filterTxt;
			this.viewer = viewer;
		}
		
		@Override
		public void keyPressed(KeyEvent e){
			text.setMessage("");
		}
		
		public void keyReleased(KeyEvent ke){
			String txt = text.getText();
			
			// We have a formula, if the string starts with "="
			if (txt.startsWith("=")) {
				String formula;
				if (txt.contains(";")) {
					formula = txt.substring(1, txt.indexOf(";"));
					
					Map<String, Object> functions = new HashMap<>();
					functions.put("math", Math.class);
					JexlEngine jexl = new JexlEngine();
					jexl.setLenient(false);
					jexl.setFunctions(functions);
					
					try {
						Expression expr = jexl.createExpression(formula);
						Object result = expr.evaluate(new MapContext());
						text.setText("");
						text.setMessage(formula + "=" + result + "");
						result = null;
					} catch (JexlException e) {
						text.setText("");
						text.setMessage("Invalid expression: " + formula);
					}
				}
				return;
			}
			
			if (txt.length() > 1) {
				filterPositionTitle.setSearchText(txt);
				viewer.getControl().setRedraw(false);
				viewer.refresh();
				viewer.getControl().setRedraw(true);
			} else {
				filterPositionTitle.setSearchText(null);
				viewer.getControl().setRedraw(false);
				viewer.refresh();
				viewer.getControl().setRedraw(true);
			}
		}
	}
	
	private class LoadContactsRunnable implements Runnable {
		@Override
		public void run(){
			List<IContact> contacts =
				CoreModelServiceHolder.get().getQuery(IContact.class).execute();
			lblStatus.setText(contacts.size() + " contacts found.");
			tableViewerContacts.getControl().setRedraw(false);
			contactList.clear();
			contactList.addAll(contacts);
			tableViewerContacts.getControl().setRedraw(true);
		}
	}
	
	/**
	 * Forwards selections in the contact viewer table to the ElexisEventDispatcher
	 */
	private class ContactSelectionChangedToEventDispatcher implements ISelectionChangedListener {
		
		@Override
		public void selectionChanged(SelectionChangedEvent event){
			ISelection selection = event.getSelection();
			if (selection == null)
				return;
			IStructuredSelection strucSelection = (IStructuredSelection) selection;
			Object selectedObject = strucSelection.getFirstElement();
			if (selectedObject == null)
				return;
			if (selectedObject instanceof IContact) {
				IContact contact = (IContact) selectedObject;
				if (contact.isPatient()) {
					ElexisEventDispatcher.fireSelectionEvent(Patient.load(contact.getId()));
				}
			}
		}
	}
	
	@Optional
	@Inject
	public void setFixLayout(MPart part, @Named(Preferences.USR_FIX_LAYOUT)
	boolean currentState){
		CoreUiUtil.updateFixLayout(part, currentState);
	}
}
