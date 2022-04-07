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

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
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
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.ui.util.CoreUiUtil;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.rgw.tools.StringTool;

public class SearchView extends ViewPart {
	public static final String ID = "ch.elexis.views.SearchView"; //$NON-NLS-1$
	
	TableViewer viewer;
	TabFolder tabFolder;
	TabItem mainTabItem;
	Text mainSearchText;
	Button searchButton;
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
		mainTabItem.setText(Messages.SearchView_general); //$NON-NLS-1$
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
		searchTextLabel.setText(Messages.SearchView_textToSearch); //$NON-NLS-1$
		
		mainSearchText = new Text(mainInputArea, SWT.BORDER);
		mainSearchText.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		
		mainCaseCheckbox = new Button(mainInputArea, SWT.CHECK);
		mainCaseCheckbox.setText(Messages.SearchView_honorCase); //$NON-NLS-1$
		
		// search options
		Composite mainOptionsArea = new Composite(mainSearchArea, SWT.NONE);
		mainOptionsArea.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		mainOptionsArea.setLayout(new GridLayout(2, true));
		
		Group typeGroup = new Group(mainOptionsArea, SWT.SHADOW_OUT);
		typeGroup.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		typeGroup.setLayout(new RowLayout());
		typeGroup.setText(Messages.SearchView_dosearch); //$NON-NLS-1$
		
		consultationRadio = new Button(typeGroup, SWT.RADIO);
		consultationRadio.setText(Messages.SearchView_consultations); //$NON-NLS-1$
		consultationRadio.setSelection(true);
		
		Group optionsGroup = new Group(mainOptionsArea, SWT.SHADOW_OUT);
		optionsGroup.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		optionsGroup.setLayout(new RowLayout());
		optionsGroup.setText(Messages.SearchView_limitTo); //$NON-NLS-1$
		
		consultationTextRadio = new Button(optionsGroup, SWT.RADIO);
		consultationTextRadio.setText(Messages.SearchView_entry); //$NON-NLS-1$
		consultationTextRadio.setSelection(true);
		
		searchButton = new Button(mainSearchArea, SWT.PUSH);
		searchButton.setText(Messages.SearchView_searchButtonCaption); //$NON-NLS-1$
		GridData gd = SWTHelper.getFillGridData(1, true, 1, false);
		gd.horizontalAlignment = GridData.END;
		searchButton.setLayoutData(gd);
		
		searchButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e){
				viewer.refresh();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e){
				widgetSelected(e);
			}
		});
		
		viewer = new TableViewer(main);
		Table table = viewer.getTable();
		table.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		
		viewer.setContentProvider(new IStructuredContentProvider() {
			@Override
			public void dispose(){
				// nothing to do
			}
			
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput){
				// nothing to do
			}
			
			@Override
			public Object[] getElements(Object inputElement){
				return mainSearch();
			}
		});
		
		// simple default label provider
		viewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element){
				if (element instanceof PersistentObject) {
					PersistentObject po = (PersistentObject) element;
					String type = "?"; //$NON-NLS-1$
					String label = ""; //$NON-NLS-1$
					if (po instanceof Konsultation) {
						type = Messages.SearchView_consultation; //$NON-NLS-1$
						
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
	
	@Optional
	@Inject
	public void setFixLayout(MPart part, @Named(Preferences.USR_FIX_LAYOUT)
	boolean currentState){
		CoreUiUtil.updateFixLayout(part, currentState);
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
		boolean considerCases = mainCaseCheckbox.getSelection();
		if (!considerCases) {
			searchString = searchString.toLowerCase();
		}
		
		Query<Konsultation> query = new Query<Konsultation>(Konsultation.class);
		query.orderBy(false, Messages.SearchView_date); //$NON-NLS-1$
		List<Konsultation> konsultationen = query.execute();
		if (konsultationen != null) {
			for (Konsultation konsultation : konsultationen) {
				String eintrag = konsultation.getEintrag().getHead();
				if (eintrag != null) {
					if (!considerCases) {
						eintrag = eintrag.toLowerCase();
					}
					if (eintrag.contains(searchString)) {
						result.add(konsultation);
					}
				}
			}
		}
		
		return result;
	}
}
