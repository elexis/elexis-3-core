/*******************************************************************************
 * Copyright (c) 2007-2009, Daniel Lutz and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Daniel Lutz - initial implementation
 *    
 *  $Id: UserCasePreferences.java 5320 2009-05-27 16:51:14Z rgw_ch $
 *******************************************************************************/
package ch.elexis.core.ui.preferences;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.util.SortedList;
import ch.elexis.core.ui.dialogs.DiagnoseSelektor;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.BillingSystem;
import ch.elexis.data.Fall;
import ch.elexis.data.PersistentObject;
import ch.rgw.io.InMemorySettings;
import ch.rgw.tools.StringTool;

/**
 * User specific settings: Case defaults
 */
public class UserCasePreferences extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {
	
	public static final String ID = "ch.elexis.preferences.UserCasePreferences"; //$NON-NLS-1$
	public static final String MENUSEPARATOR = "------------------------------------"; //$NON-NLS-1$
	private static final String PREFSDELIMITER = "`^"; //$NON-NLS-1$
	private static final String PREFSDELIMITER_REGEX = "\\`\\^"; //$NON-NLS-1$
	
	Text diagnoseTxt;
	List sorterList2;
	Button btnToManual;
	Button btnToNotPresorted;
	Button btnUp;
	Button btnDown;
	
	LinkedList<String> topItemsLinkedList = new LinkedList<String>();
	
	public UserCasePreferences(){
		super(GRID);
		setPreferenceStore(new SettingsPreferenceStore(new InMemorySettings()));
		setDescription(Messages.UserCasePreferences_Cases);
	}
	
	@Override
	protected void createFieldEditors(){
		addField(new StringFieldEditor(Preferences.USR_DEFCASELABEL,
			Messages.UserCasePreferences_DefaultName, getFieldEditorParent()));
		addField(new StringFieldEditor(Preferences.USR_DEFCASEREASON,
			Messages.UserCasePreferences_DefaultReason, getFieldEditorParent()));
		addField(new StringFieldEditor(Preferences.USR_DEFLAW,
			Messages.UserCasePreferences_DefaultBillingSystem, getFieldEditorParent()));
	}
	
	public void init(IWorkbench workbench){
		getPreferenceStore().setValue(Preferences.USR_DEFCASELABEL, Fall.getDefaultCaseLabel());
		getPreferenceStore().setValue(Preferences.USR_DEFCASEREASON, Fall.getDefaultCaseReason());
		getPreferenceStore().setValue(Preferences.USR_DEFLAW, Fall.getDefaultCaseLaw());
		// read the sorting for this user form prefs, convert to LinkedList for editing
		String topItemsSortingStr = CoreHub.userCfg.get(Preferences.USR_TOPITEMSSORTING, "");
		String[] topItemsSorting = topItemsSortingStr.split(PREFSDELIMITER_REGEX);
		topItemsLinkedList = new LinkedList<String>(Arrays.asList(topItemsSorting));
	}
	
	@Override
	public boolean performOk(){
		super.performOk();
		
		CoreHub.userCfg.set(Preferences.USR_DEFCASELABEL,
			getPreferenceStore().getString(Preferences.USR_DEFCASELABEL));
		CoreHub.userCfg.set(Preferences.USR_DEFCASEREASON,
			getPreferenceStore().getString(Preferences.USR_DEFCASEREASON));
		CoreHub.userCfg.set(Preferences.USR_DEFLAW,
			getPreferenceStore().getString(Preferences.USR_DEFLAW));
		// convert LinkedList for the sorting to comma delimited list and save to prefs
		String[] topItemsSorting = new String[topItemsLinkedList.size()];
		topItemsLinkedList.toArray(topItemsSorting);
		String topItemsSortingStr = StringTool.join(topItemsSorting, PREFSDELIMITER);
		CoreHub.userCfg.set(Preferences.USR_TOPITEMSSORTING, topItemsSortingStr);
		return true;
	}
	
	@Override
	protected Control createContents(Composite parent){
		// create the field editors by calling super
		Control suParent = super.createContents(parent);
		// create a composite for selecting the default diagnose
		Composite diagnoseParent = new Composite((Composite) suParent, SWT.NULL);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.horizontalSpan = 2;
		diagnoseParent.setLayoutData(gd);
		
		diagnoseParent.setLayout(new FormLayout());
		Label diagnoseLbl = new Label(diagnoseParent, SWT.NONE);
		diagnoseLbl.setText(Messages.UserCasePreferences_DefaultDiagnose);
		diagnoseTxt = new Text(diagnoseParent, SWT.BORDER);
		diagnoseTxt.setEditable(false);
		String diagnoseId = CoreHub.userCfg.get(Preferences.USR_DEFDIAGNOSE, "");
		if (diagnoseId.length() > 1) {
			PersistentObject diagnose = CoreHub.poFactory.createFromString(diagnoseId);
			if (diagnose != null)
				diagnoseTxt.setText(diagnose.getLabel());
		}
		Button diagnoseBtn = new Button(diagnoseParent, SWT.PUSH);
		diagnoseBtn.setText("Diagnose"); //$NON-NLS-1$
		diagnoseBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				DiagnoseSelektor dsl = new DiagnoseSelektor(getShell());
				if (dsl.open() == Dialog.OK) {
					Object[] sel = dsl.getResult();
					if (sel != null && sel.length > 0) {
						PersistentObject diagnose = (PersistentObject) sel[0];
						CoreHub.userCfg.set(Preferences.USR_DEFDIAGNOSE, diagnose.storeToString());
						diagnoseTxt.setText(diagnose.getLabel());
					} else {
						CoreHub.userCfg.set(Preferences.USR_DEFDIAGNOSE, "");
						diagnoseTxt.setText("");
					}
				}
			}
		});
		Button diagnoseDelBtn = new Button(diagnoseParent, SWT.PUSH);
		diagnoseDelBtn.setImage(Images.IMG_DELETE.getImage());
		diagnoseDelBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				CoreHub.userCfg.set(Preferences.USR_DEFDIAGNOSE, "");
				diagnoseTxt.setText("");
			}
		});
		
		FormData fd = new FormData();
		fd.top = new FormAttachment(0, 5);
		fd.left = new FormAttachment(0, 0);
		diagnoseLbl.setLayoutData(fd);
		
		fd = new FormData();
		fd.top = new FormAttachment(diagnoseLbl, 0, SWT.CENTER);
		fd.left = new FormAttachment(diagnoseLbl, 5);
		fd.right = new FormAttachment(diagnoseBtn, -5);
		diagnoseTxt.setLayoutData(fd);
		
		fd = new FormData();
		fd.top = new FormAttachment(diagnoseLbl, 0, SWT.CENTER);
		fd.right = new FormAttachment(diagnoseDelBtn, -5);
		diagnoseBtn.setLayoutData(fd);
		
		fd = new FormData();
		fd.top = new FormAttachment(diagnoseLbl, 0, SWT.CENTER);
		fd.right = new FormAttachment(100, -5);
		diagnoseDelBtn.setLayoutData(fd);
		
		Composite lastConsParent = new Composite((Composite) suParent, SWT.NULL);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.horizontalSpan = 2;
		lastConsParent.setLayoutData(gd);
		
		lastConsParent.setLayout(new FormLayout());
		Label lastConsLbl = new Label(lastConsParent, SWT.NONE);
		lastConsLbl.setText(Messages.UserCasePreferences_LoadConsultationAll);
		
		final Button lastConsBtn = new Button(lastConsParent, SWT.CHECK);
		lastConsBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				if (lastConsBtn.getSelection()) {
					CoreHub.userCfg.set(Preferences.USR_DEFLOADCONSALL, true);
				} else {
					CoreHub.userCfg.set(Preferences.USR_DEFLOADCONSALL, false);
				}
			}
		});
		lastConsBtn.setSelection(CoreHub.userCfg.get(Preferences.USR_DEFLOADCONSALL, false));
		
		fd = new FormData();
		fd.top = new FormAttachment(0, 5);
		fd.left = new FormAttachment(0, 0);
		lastConsLbl.setLayoutData(fd);
		
		fd = new FormData();
		fd.top = new FormAttachment(0, 5);
		fd.right = new FormAttachment(100, -5);
		lastConsBtn.setLayoutData(fd);
		
		// ********* section for specifying sorting of billing system
		new Label(getFieldEditorParent(), SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(SWTHelper
			.getFillGridData(2, true, 1, false));
		
		Label infoTxt = new Label(getFieldEditorParent(), SWT.NONE);
		infoTxt.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
		infoTxt.setText(Messages.UserCasePreferences_InfoLabelForSortingBillingSystems);
		
		Label lllabel2 = new Label(getFieldEditorParent(), SWT.NONE);
		lllabel2.setText(""); //$NON-NLS-1$
		Composite sorterListComp = new Composite(getFieldEditorParent(), SWT.NONE);
		GridLayout sorterListLayout = new GridLayout();
		sorterListLayout.marginWidth = 0;
		sorterListLayout.marginHeight = 0;
		sorterListLayout.numColumns = 2;
		sorterListComp.setLayout(sorterListLayout);
		
		sorterList2 = new List(sorterListComp, SWT.BORDER);
		sorterList2.setItems(sortBillingSystems(BillingSystem.getAbrechnungsSysteme(), topItemsLinkedList,
			true));
		sorterList2.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e){
				setButtonEnabling();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e){}
		});
		
		Composite sorterListCompButtons = new Composite(sorterListComp, SWT.BORDER);
		GridLayout sorterListCompButtonsLayout = new GridLayout();
		sorterListLayout.marginWidth = 0;
		sorterListLayout.marginHeight = 0;
		sorterListLayout.horizontalSpacing = 0;
		sorterListLayout.verticalSpacing = 0;
		sorterListLayout.marginTop = 0;
		sorterListLayout.marginBottom = 0;
		sorterListLayout.marginLeft = 0;
		sorterListLayout.marginRight = 0;
		sorterListCompButtons.setLayout(sorterListCompButtonsLayout);
		
		GridData sorterGridData = new GridData();
		sorterGridData.verticalAlignment = SWT.TOP;
		sorterGridData.horizontalSpan = 0;
		sorterGridData.grabExcessVerticalSpace = true;
		sorterListCompButtons.setLayoutData(sorterGridData);
		
		btnUp = new Button(sorterListCompButtons, SWT.NONE);
		btnUp.setToolTipText(Messages.UserCasePreferences_MoveItemUpInList);
		btnUp.setImage(Images.IMG_ARROWUP.getImage());
		btnUp.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e){
				moveItemUpInPresorted();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e){}
		});
		
		btnDown = new Button(sorterListCompButtons, SWT.NONE);
		btnDown.setToolTipText(Messages.UserCasePreferences_MoveItemDownInList);
		btnDown.setImage(Images.IMG_ARROWDOWN.getImage());
		btnDown.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e){
				moveItemDownInPresorted();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e){}
		});
		
		btnToManual = new Button(sorterListCompButtons, SWT.NONE);
		btnToManual.setToolTipText(Messages.UserCasePreferences_MoveItemToManualSortedList);
		btnToManual.setImage(Images.IMG_MOVETOLOWERLIST.getImage());
		btnToManual.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e){
				moveItemToPresorted();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e){}
		});
		
		btnToNotPresorted = new Button(sorterListCompButtons, SWT.NONE);
		btnToNotPresorted
			.setToolTipText(Messages.UserCasePreferences_MoveItemToAlphabeticallySortedList);
		btnToNotPresorted.setImage(Images.IMG_ARROWDOWNTORECT.getImage());
		btnToNotPresorted.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e){
				moveItemToNotPresorted();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e){}
		});
		
		setButtonEnabling();
		
		return suParent;
	}
	
	void moveItemToPresorted(){
		String[] selStr = sorterList2.getSelection();
		topItemsLinkedList.add(selStr[0]);
		topItemsLinkedList.remove(""); //$NON-NLS-1$ // remove any empty items
		sorterList2.setItems(sortBillingSystems(BillingSystem.getAbrechnungsSysteme(), topItemsLinkedList,
			true));
		sorterList2.select(topItemsLinkedList.size() - 1);
		setButtonEnabling();
	}
	
	void moveItemToNotPresorted(){
		String[] selStr = sorterList2.getSelection();
		topItemsLinkedList.remove(selStr[0]);
		topItemsLinkedList.remove(""); //$NON-NLS-1$ // remove any empty items
		sorterList2.setItems(sortBillingSystems(BillingSystem.getAbrechnungsSysteme(), topItemsLinkedList,
			true));
		int newSel = sorterList2.indexOf(selStr[0]);
		sorterList2.select(newSel);
		setButtonEnabling();
	}
	
	void moveItemUpInPresorted(){
		moveItemInPresorted(-1);
	}
	
	void moveItemDownInPresorted(){
		moveItemInPresorted(+1);
	}
	
	void moveItemInPresorted(int step){
		int selIx = sorterList2.getSelectionIndex();
		if (selIx < 0)
			return;
		String[] selStr = sorterList2.getSelection();
		int newIx = selIx + step;
		topItemsLinkedList.remove(selIx);
		topItemsLinkedList.add(newIx, selStr[0]);
		topItemsLinkedList.remove(StringConstants.EMPTY); // remove any empty items
		sorterList2.setItems(sortBillingSystems(BillingSystem.getAbrechnungsSysteme(), topItemsLinkedList,
			true));
		sorterList2.select(newIx);
		setButtonEnabling();
	}
	
	void setButtonEnabling(){
		// get separator and current sel position
		int separatorPos;
		if ((topItemsLinkedList.size() > 0) && (!topItemsLinkedList.get(0).equalsIgnoreCase(""))) //$NON-NLS-1$
			separatorPos = topItemsLinkedList.size();
		else
			separatorPos = -1;
		int selIx = sorterList2.getSelectionIndex();
		
		// enable/disable presorting buttons
		if (selIx < 0) {
			btnToManual.setEnabled(false);
			btnToNotPresorted.setEnabled(false);
		} else if (selIx < separatorPos) {
			btnToManual.setEnabled(false);
			btnToNotPresorted.setEnabled(true);
		} else if (selIx > separatorPos) {
			btnToManual.setEnabled(true);
			btnToNotPresorted.setEnabled(false);
		} else {
			btnToManual.setEnabled(false);
			btnToNotPresorted.setEnabled(false);
		}
		
		// enable/disable up/down buttons
		btnUp.setEnabled(((selIx <= 0) || (selIx >= (topItemsLinkedList.size()))) ? false : true);
		btnDown.setEnabled((selIx >= (topItemsLinkedList.size() - 1)) ? false
				: ((selIx >= 0) ? true : false));
	}
	
	public static int getBillingSystemsMenuSeparatorPos(String[] input){
		// read the sorting for this user form prefs, convert to LinkedList for editing
		String topItemsSortingStr = CoreHub.userCfg.get(Preferences.USR_TOPITEMSSORTING, ""); //$NON-NLS-1$
		String[] topItemsSorting = topItemsSortingStr.split(PREFSDELIMITER_REGEX);
		LinkedList<String> lTopItemsLinkedList =
			new LinkedList<String>(Arrays.asList(topItemsSorting));
		if ((lTopItemsLinkedList.size() > 0) && (!lTopItemsLinkedList.get(0).equalsIgnoreCase(""))) //$NON-NLS-1$
			return lTopItemsLinkedList.size();
		else
			return -1;
		
	}
	
	/**
	 * sort the input: start with sorting as specified in topItemsSorting found in the user prefs,
	 * add a separator and then sort the rest alphabetically
	 * 
	 * @param input
	 *            String[] of all billing systems, unsorted
	 * @return String[] the sorted list
	 */
	public static String[] sortBillingSystems(String[] input){
		// read the sorting for this user form prefs, convert to LinkedList for editing
		String topItemsSortingStr = CoreHub.userCfg.get(Preferences.USR_TOPITEMSSORTING, ""); //$NON-NLS-1$
		String[] topItemsSorting = topItemsSortingStr.split(PREFSDELIMITER_REGEX);
		LinkedList<String> lTopItemsLinkedList =
			new LinkedList<String>(Arrays.asList(topItemsSorting));
		return sortBillingSystems(input, lTopItemsLinkedList);
	}
	
	/**
	 * sort the input: start with sorting as specified in parameter topItemsSorting, add a separator
	 * if there are topitems and then sort the rest alphabetically
	 * 
	 * @param input
	 *            String[] of all billing systems, unsorted
	 * @param topItemsSorting
	 *            LinkedList<String> Array of billing systems in the order they should appear in the
	 *            menu/combo
	 * @return String[] the sorted list
	 */
	public static String[] sortBillingSystems(String[] input, LinkedList<String> topItemsSorting){
		return sortBillingSystems(input, topItemsSorting, false);
	}
	
	/**
	 * sort the input: start with sorting as specified in parameter topItemsSorting, always add a
	 * separator and then sort the rest alphabetically
	 * 
	 * @param input
	 *            String[] of all billing systems, unsorted
	 * @param topItemsSorting
	 *            LinkedList<String> Array of billing systems in the order they should appear in the
	 *            menu/combo
	 * @param alwaysShowSeparator
	 *            boolean should the separator also be shown when NO topItems are present
	 * @return String[] the sorted list
	 */
	public static String[] sortBillingSystems(String[] input, LinkedList<String> topItemsSorting,
		boolean alwaysShowSeparator){
		// create a copy of topItemsSorting - we append the other items to the end later
		LinkedList<String> lTopItemsSorting = (LinkedList<String>) topItemsSorting.clone();
		
		// create sorted list for the other items
		SortedList<String> sortedList = new SortedList<String>(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2){
				return (o1.compareTo(o2));
			}
		});
		for (int i = 0; i < input.length; i++) {
			String item = input[i];
			if (!lTopItemsSorting.contains(item))
				sortedList.add(item);
		}
		
		// now append the sorted items to the copied top items
		if (alwaysShowSeparator
			|| ((topItemsSorting.size() > 0) && (!topItemsSorting.get(0).equalsIgnoreCase("")))) { //$NON-NLS-1$
			lTopItemsSorting.add(MENUSEPARATOR);
		}
		lTopItemsSorting.addAll(sortedList);
		lTopItemsSorting.remove(""); //$NON-NLS-1$
		
		String[] output = new String[lTopItemsSorting.size()];
		lTopItemsSorting.toArray(output);
		
		return output;
	}
	
	@Override
	protected void performDefaults(){
		this.initialize();
	}
}
