/*******************************************************************************
 * Copyright (c) 2005-2009, Daniel Lutz and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Daniel Lutz - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.core.ui.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.data.Fall;
import ch.elexis.core.data.Konsultation;
import ch.elexis.core.data.Patient;
import ch.elexis.core.data.PersistentObject;
import ch.elexis.core.data.Query;
import ch.elexis.core.ui.actions.GlobalActions;
import ch.elexis.core.ui.util.SWTHelper;
import ch.rgw.tools.StringTool;

public class SearchView extends ViewPart implements ISaveablePart2 {
	public static final String ID = "ch.elexis.views.SearchView"; //$NON-NLS-1$
	
	TableViewer viewer;
	TabFolder tabFolder;
	TabItem mainTabItem;
	Text mainSearchText;
	Button mainCaseCheckbox;
	Button consultationRadio;
	Button consultationTextRadio;
	
	@Override
	public void createPartControl(Composite parent){
		parent.setLayout(new FillLayout());
		
		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new GridLayout(1, false));
		
		tabFolder = new TabFolder(main, SWT.NONE);
		tabFolder.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		mainTabItem = new TabItem(tabFolder, SWT.NONE);
		mainTabItem.setText(Messages.getString("SearchView.general")); //$NON-NLS-1$
		Composite mainSearchArea = new Composite(tabFolder, SWT.NONE);
		mainTabItem.setControl(mainSearchArea);
		
		mainSearchArea.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		mainSearchArea.setLayout(new GridLayout(1, false));
		
		// text input box
		Composite mainInputArea = new Composite(mainSearchArea, SWT.NONE);
		mainInputArea.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		mainInputArea.setLayout(new GridLayout(2, false));
		
		Label searchTextLabel = new Label(mainInputArea, SWT.NONE);
		searchTextLabel.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		searchTextLabel.setText(Messages.getString("SearchView.textToSearch")); //$NON-NLS-1$
		
		mainSearchText = new Text(mainInputArea, SWT.BORDER);
		mainSearchText.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		
		mainCaseCheckbox = new Button(mainInputArea, SWT.CHECK);
		mainCaseCheckbox.setText(Messages.getString("SearchView.honorCase")); //$NON-NLS-1$
		
		// search options
		Composite mainOptionsArea = new Composite(mainSearchArea, SWT.NONE);
		mainOptionsArea.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		mainOptionsArea.setLayout(new GridLayout(2, true));
		
		Group typeGroup = new Group(mainOptionsArea, SWT.SHADOW_OUT);
		typeGroup.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		typeGroup.setLayout(new RowLayout());
		typeGroup.setText(Messages.getString("SearchView.dosearch")); //$NON-NLS-1$
		
		consultationRadio = new Button(typeGroup, SWT.RADIO);
		consultationRadio.setText(Messages.getString("SearchView.consultations")); //$NON-NLS-1$
		consultationRadio.setSelection(true);
		
		Group optionsGroup = new Group(mainOptionsArea, SWT.SHADOW_OUT);
		optionsGroup.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		optionsGroup.setLayout(new RowLayout());
		optionsGroup.setText(Messages.getString("SearchView.limitTo")); //$NON-NLS-1$
		
		consultationTextRadio = new Button(optionsGroup, SWT.RADIO);
		consultationTextRadio.setText(Messages.getString("SearchView.entry")); //$NON-NLS-1$
		consultationTextRadio.setSelection(true);
		
		Button searchButton = new Button(mainSearchArea, SWT.PUSH);
		searchButton.setText(Messages.getString("SearchView.searchButtonCaption")); //$NON-NLS-1$
		GridData gd = SWTHelper.getFillGridData(1, true, 1, false);
		gd.horizontalAlignment = GridData.END;
		searchButton.setLayoutData(gd);
		
		searchButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e){
				viewer.refresh();
			}
			
			public void widgetDefaultSelected(SelectionEvent e){
				widgetSelected(e);
			}
		});
		
		viewer = new TableViewer(main);
		Table table = viewer.getTable();
		table.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		
		viewer.setContentProvider(new IStructuredContentProvider() {
			public void dispose(){
				// nothing to do
			}
			
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput){
				// nothing to do
			}
			
			public Object[] getElements(Object inputElement){
				return mainSearch();
			}
		});
		
		// simple default label provider
		viewer.setLabelProvider(new LabelProvider() {
			public String getText(Object element){
				if (element instanceof PersistentObject) {
					PersistentObject po = (PersistentObject) element;
					String type = "?"; //$NON-NLS-1$
					String label = ""; //$NON-NLS-1$
					if (po instanceof Konsultation) {
						type = Messages.getString("SearchView.consultation"); //$NON-NLS-1$
						
						Konsultation konsultation = (Konsultation) po;
						Fall fall = konsultation.getFall();
						Patient pat = fall.getPatient();
						label = pat.getLabel() + " - " + fall.getLabel() + " - " //$NON-NLS-1$ //$NON-NLS-2$
							+ konsultation.getLabel();
					} else {
						label = po.getLabel();
					}
					
					return type + " - " + label; //$NON-NLS-1$
				} else {
					return super.getText(element);
				}
			}
		});
		
		viewer.setInput(this.getSite());
	}
	
	@Override
	public void setFocus(){}
	
	@Override
	public void dispose(){
		super.dispose();
	}
	
	/*
	 * Die folgenden 6 Methoden implementieren das Interface ISaveablePart2 Wir benötigen das
	 * Interface nur, um das Schliessen einer View zu verhindern, wenn die Perspektive fixiert ist.
	 * Gibt es da keine einfachere Methode?
	 */
	public int promptToSaveOnClose(){
		return GlobalActions.fixLayoutAction.isChecked() ? ISaveablePart2.CANCEL
				: ISaveablePart2.NO;
	}
	
	public void doSave(IProgressMonitor monitor){ /* leer */}
	
	public void doSaveAs(){ /* leer */}
	
	public boolean isDirty(){
		return true;
	}
	
	public boolean isSaveAsAllowed(){
		return false;
	}
	
	public boolean isSaveOnCloseNeeded(){
		return true;
	}
	
	private Object[] mainSearch(){
		String searchString = mainSearchText.getText();
		if (!StringTool.isNothing(searchString)) {
			return searchForKonsultationText(searchString).toArray();
		} else {
			return new Object[0];
		}
	}
	
	private List<Konsultation> searchForKonsultationText(String searchString){
		List<Konsultation> result = new ArrayList<Konsultation>();
		
		Query<Konsultation> query = new Query<Konsultation>(Konsultation.class);
		query.orderBy(false, Messages.getString("SearchView.date")); //$NON-NLS-1$
		List<Konsultation> konsultationen = query.execute();
		if (konsultationen != null) {
			for (Konsultation konsultation : konsultationen) {
				String eintrag = konsultation.getEintrag().getHead();
				if (eintrag != null) {
					if (eintrag.contains(searchString)) {
						result.add(konsultation);
					}
				}
			}
		}
		
		return result;
	}
}
