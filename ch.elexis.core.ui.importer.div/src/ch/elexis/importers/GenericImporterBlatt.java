/*******************************************************************************
 * Copyright (c) 2007-2008, D. Lutz and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    D. Lutz - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.importers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.Artikel;
import ch.elexis.core.data.Kontakt;
import ch.elexis.core.data.Organisation;
import ch.elexis.core.data.PersistentObject;
import ch.elexis.core.data.PersistentObjectFactory;
import ch.elexis.core.data.Person;
import ch.elexis.core.data.Query;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.SWTHelper;
import ch.rgw.tools.StringTool;

/**
 * A class to import any data from different sources. The user can choose the type of data and how
 * to map the fields. He can specify a key file for mapping the data to existing objects.
 * 
 * @author Daniel Lutz <danlutz@watz.ch>
 */
public class GenericImporterBlatt extends Composite {
	static final String FILENAME_KEY = "ImporterPage/" + GenericImporter.TITLE + "/filename"; //$NON-NLS-1$ //$NON-NLS-2$
	static final String LASTFIELDS_KEY = "ImporterPage/" + GenericImporter.TITLE + "/lastfields"; //$NON-NLS-1$ //$NON-NLS-2$
	
	static final String[] METHODS = new String[] {
		"XLS" //$NON-NLS-1$
	};
	private static final int XLS = 0;
	
	int method = XLS;
	String filename = StringConstants.EMPTY;
	
	Label lbFileName;
	Combo cbMethods;
	
	private Logger log = LoggerFactory.getLogger(this.getClass().getName());
	
	public GenericImporterBlatt(Composite parent){
		super(parent, SWT.NONE);
		setLayout(new GridLayout(2, false));
		new Label(this, SWT.NONE).setText(Messages.GenericImporterBlatt_FileType);
		new Label(this, SWT.NONE).setText(Messages.GenericImporterBlatt_File);
		cbMethods = new Combo(this, SWT.SINGLE);
		cbMethods.setItems(METHODS);
		cbMethods.select(0);
		cbMethods.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0){
				method = cbMethods.getSelectionIndex();
			}
		});
		
		Button bLoad = new Button(this, SWT.PUSH);
		
		bLoad.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				FileDialog fd = new FileDialog(getShell(), SWT.OPEN);
				String file = fd.open();
				lbFileName.setText(StringTool.isNothing(file) ? StringConstants.EMPTY : file);
				filename = lbFileName.getText();
				CoreHub.localCfg.set(FILENAME_KEY, filename);
			}
		});
		bLoad.setText(Messages.GenericImporterBlatt_SelectFile);
		lbFileName = new Label(this, SWT.NONE);
		lbFileName.setLayoutData(SWTHelper.getFillGridData(2, true, 1, true));
		
		String file = CoreHub.localCfg.get(FILENAME_KEY, StringConstants.EMPTY);
		if (!StringTool.isNothing(file)) {
			filename = file;
			lbFileName.setText(file);
		} else {
			filename = StringConstants.EMPTY;
			lbFileName.setText(Messages.GenericImporterBlatt_PleaseSelectFile);
		}
	}
	
	public boolean doImport(){
		UiDesk.getDisplay().syncExec(new Runnable() {
			public void run(){
				GenericImporterWizard wizard = new GenericImporterWizard(method, filename);
				
				WizardDialog dlg =
					new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						.getShell(), wizard) {
						{
							int style = getShellStyle();
							setShellStyle(style | SWT.MAX);
							
						}
					};
				
				dlg.open();
			}
		});
		return true;
	}
	
	/**
	 * Wizard for importing generic data. For now, we only support Excel.
	 */
	class GenericImporterWizard extends Wizard {
		private int method;
		private String filename;
		
		MappingPage mappingPage;
		SyncPage syncPage;
		
		ExcelWrapper excel;
		PersistentObject template;
		
		GenericImporterWizard(int method, String filename){
			this.method = method;
			this.filename = filename;
			
			mappingPage = new MappingPage();
			syncPage = new SyncPage();
			
			addPage(mappingPage);
			addPage(syncPage);
			
			setWindowTitle(Messages.GenericImporterBlatt_GeneralFileImport);
			
			initInput();
		}
		
		public boolean performFinish(){
			if (excel != null) {
				// TODO close excel sheet
			}
			return true;
		}
		
		public boolean performCancel(){
			// the same action as Finish
			return performFinish();
		}
		
		/**
		 * prepares the input document
		 */
		private void initInput(){
			excel = null;
			
			if ((!StringTool.isNothing(filename)) && method == XLS) {
				excel = new ExcelWrapper();
				if (!excel.load(filename, 0)) {
					excel = null;
				}
			}
		}
		
		class MappingPage extends WizardPage {
			private ComboViewer typesViewer;
			
			private ComboViewer inputAvailableFieldsViewer;
			private TableViewer inputChosenFieldsViewer;
			private ComboViewer dbAvailableFieldsViewer;
			private TableViewer dbChosenFieldsViewer;
			
			private List<Field> inputAvailableFields;
			private List<Field> dbAvailableFields;
			
			// accessed by SyncPage
			private List<Field> inputChosenFields = new ArrayList<Field>();
			private List<Field> dbChosenFields = new ArrayList<Field>();
			private HashMap<String, Integer> inputFieldIndices = new HashMap<String, Integer>();
			
			MappingPage(){
				super("Mapping", "Mapping", null); //$NON-NLS-1$ //$NON-NLS-2$
				String description =
					Messages.GenericImporterBlatt_PleaseSelectType
						+ Messages.GenericImporterBlatt_LowerFieldSelectedAutoamtcally
						+ Messages.GenericImporterBlatt_ChangeIfNeeded;
				setDescription(description);
			}
			
			/**
			 * Set the requested object type. Intialize available fields according to the type.
			 * 
			 * @param type
			 *            a template of the requested type
			 */
			private void setType(){
				PersistentObject type = getSelectedType();
				
				// clear old data
				clearAllFields();
				
				// query available excel and db fields
				loadAvailableFields();
				
				// try to map
				setChosenFields();
				
				inputAvailableFieldsViewer.setInput(inputAvailableFields);
				if (inputAvailableFields.size() > 0) {
					inputAvailableFieldsViewer.setSelection(new StructuredSelection(
						inputAvailableFieldsViewer.getElementAt(0)));
				}
				
				inputChosenFieldsViewer.setInput(inputChosenFields);
				
				dbAvailableFieldsViewer.setInput(dbAvailableFields);
				if (dbAvailableFields.size() > 0) {
					dbAvailableFieldsViewer.setSelection(new StructuredSelection(
						dbAvailableFieldsViewer.getElementAt(0)));
				}
				
				dbChosenFieldsViewer.setInput(dbChosenFields);
				
				checkCompleteness();
				
			}
			
			private void clearAllFields(){
				if (inputAvailableFields != null) {
					inputAvailableFields.clear();
				}
				if (inputChosenFields != null) {
					inputChosenFields.clear();
				}
				if (inputFieldIndices != null) {
					inputFieldIndices.clear();
				}
				
				if (dbAvailableFields != null) {
					dbAvailableFields.clear();
				}
				if (dbChosenFields != null) {
					dbChosenFields.clear();
				}
			}
			
			/**
			 * Populates inputAvailableFields and dbAvailableFields, and initialize
			 * inputFieldIndices
			 */
			private void loadAvailableFields(){
				PersistentObject type = getSelectedType();
				
				inputAvailableFields = new ArrayList<Field>();
				if (type != null) {
					if (excel != null) {
						int firstRowIndex = excel.getFirstRow();
						List<String> excelFields = excel.getRow(firstRowIndex);
						if (excelFields != null) {
							for (int i = 0; i < excelFields.size(); i++) {
								String name = excelFields.get(i);
								inputAvailableFields.add(new Field(name));
								inputFieldIndices.put(name, new Integer(i));
							}
						}
						
					}
				}
				
				dbAvailableFields = getAvailableFields(getSelectedType());
			}
			
			// choose initial set of fields
			private void setChosenFields(){
				if (inputAvailableFields != null && dbAvailableFields != null) {
					ArrayList<Field> inputFields = new ArrayList<Field>();
					ArrayList<Field> dbFields = new ArrayList<Field>();
					for (Field inputField : inputAvailableFields) {
						Field dbField = findDbField(inputField);
						if (dbField != null) {
							inputFields.add(inputField);
							dbFields.add(dbField);
							// synchronize initial key status
							inputField.isKey = dbField.isKey;
						}
					}
					inputAvailableFields.removeAll(inputFields);
					inputChosenFields.addAll(inputFields);
					dbAvailableFields.removeAll(dbFields);
					dbChosenFields.addAll(dbFields);
					
					inputAvailableFieldsViewer.refresh();
					inputChosenFieldsViewer.refresh();
					dbAvailableFieldsViewer.refresh();
					dbChosenFieldsViewer.refresh();
					
					checkCompleteness();
				}
			}
			
			private Field findDbField(Field excelField){
				for (Field dbField : dbAvailableFields) {
					if (dbField.name.equals(excelField.name)) {
						
						return dbField;
					}
				}
				
				// not found
				return null;
			}
			
			/**
			 * Check if the mapping is complete - Fields are chosen - There are the same size of
			 * input and db fields chosen - There is at least one key field defined
			 */
			private void checkCompleteness(){
				boolean complete = false;
				
				PersistentObject type = getSelectedType();
				if (type != null) {
					int inputSize = inputChosenFields.size();
					int dbSize = dbChosenFields.size();
					if (inputSize > 0 && dbSize > 0 && inputSize == dbSize) {
						// compare keys
						complete = true;
						boolean hasKeys = false;
						for (int i = 0; i < inputSize; i++) {
							Field inputField = inputChosenFields.get(i);
							Field dbField = dbChosenFields.get(i);
							// is there a key?
							if (inputField.isKey) {
								hasKeys = true;
							}
							// are both fields defined the same way?
							if (inputField.isKey != dbField.isKey) {
								complete = false;
								break;
							}
						}
						complete = (complete & hasKeys);
					}
				}
				
				setPageComplete(complete);
			}
			
			private List<PersistentObject> getTypes(){
				List<PersistentObject> types = new ArrayList<PersistentObject>();
				// TODO return all types available
				// (use the plugins PersistentObjectFactories)
				
				PersistentObjectFactory factory = new PersistentObjectFactory();
				
				types.add(factory.createTemplate(Person.class));
				types.add(factory.createTemplate(Organisation.class));
				types.add(factory.createTemplate(Artikel.class));
				
				return types;
			}
			
			private List<Field> getAvailableFields(PersistentObject template){
				List<Field> fields = new ArrayList<Field>();
				Field field;
				
				if (template instanceof Person) {
					fields.add(new Field(Person.NAME, true));
					fields.add(new Field(Person.FIRSTNAME, true));
					fields.add(new Field("Zusatz")); //$NON-NLS-1$
					fields.add(new Field(Person.BIRTHDATE, true));
					fields.add(new Field(Person.SEX));
					fields.add(new Field(Kontakt.FLD_STREET));
					fields.add(new Field(Kontakt.FLD_ZIP));
					fields.add(new Field(Kontakt.FLD_PLACE));
					fields.add(new Field(Kontakt.FLD_COUNTRY));
					fields.add(new Field(Kontakt.FLD_PHONE1));
					fields.add(new Field(Kontakt.FLD_PHONE2));
					fields.add(new Field(Kontakt.FLD_MOBILEPHONE));
					fields.add(new Field(Kontakt.FLD_FAX));
					fields.add(new Field(Kontakt.FLD_E_MAIL));
					fields.add(new Field(Kontakt.FLD_WEBSITE));
					fields.add(new Field(Person.TITLE));
					fields.add(new Field(Person.FLD_SHORT_LABEL));
					fields.add(new Field(Person.FLD_REMARK));
				} else if (template instanceof Organisation) {
					fields.add(new Field(Organisation.FLD_NAME1));
					fields.add(new Field("Zusatz1")); //$NON-NLS-1$
					fields.add(new Field("Ansprechperson")); //$NON-NLS-1$
					fields.add(new Field("Tel. direkt")); //$NON-NLS-1$
					fields.add(new Field(Organisation.FLD_SHORT_LABEL));
					fields.add(new Field(Organisation.FLD_STREET));
					fields.add(new Field(Organisation.FLD_ZIP));
					fields.add(new Field(Organisation.FLD_PLACE));
					fields.add(new Field(Organisation.FLD_COUNTRY));
					fields.add(new Field(Organisation.FLD_PHONE1));
					fields.add(new Field(Organisation.FLD_PHONE2));
					fields.add(new Field(Organisation.FLD_E_MAIL));
					fields.add(new Field(Organisation.FLD_WEBSITE));
					fields.add(new Field(Organisation.FLD_FAX));
					fields.add(new Field(Organisation.FLD_REMARK));
				} else if (template instanceof Artikel) {
					fields.add(new Field(Artikel.FLD_SUB_ID, true));
					fields.add(new Field(Artikel.FLD_NAME));
					fields.add(new Field(Artikel.MAXBESTAND));
					fields.add(new Field(Artikel.MINBESTAND));
					fields.add(new Field(Artikel.ISTBESTAND));
					fields.add(new Field(Artikel.FLD_TYP));
					fields.add(new Field(Artikel.FLD_CODECLASS));
					fields.add(new Field(Artikel.FLD_LIEFERANT_ID));
				} else {
					// unknown type
				}
				
				return fields;
			}
			
			private PersistentObject getSelectedType(){
				IStructuredSelection sel = (IStructuredSelection) typesViewer.getSelection();
				if (sel != null) {
					Object obj = sel.getFirstElement();
					if (obj instanceof PersistentObject) {
						return (PersistentObject) obj;
					}
				}
				
				return null;
			}
			
			private Field getViewerSelection(StructuredViewer viewer){
				IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
				Object obj = sel.getFirstElement();
				if (obj instanceof Field) {
					return (Field) obj;
				}
				
				return null;
			}
			
			private void addInputField(){
				Field field = getViewerSelection(inputAvailableFieldsViewer);
				inputAvailableFields.remove(field);
				inputChosenFields.add(field);
				
				inputAvailableFieldsViewer.refresh();
				inputChosenFieldsViewer.refresh();
				
				inputChosenFieldsViewer.setSelection(new StructuredSelection(field));
				
				checkCompleteness();
			}
			
			private void delInputField(){
				Field field = getViewerSelection(inputChosenFieldsViewer);
				field.isKey = false;
				inputChosenFields.remove(field);
				inputAvailableFields.add(field);
				
				inputAvailableFieldsViewer.refresh();
				inputChosenFieldsViewer.refresh();
				
				checkCompleteness();
			}
			
			private void upInputField(){
				Field field = getViewerSelection(inputChosenFieldsViewer);
				int index = inputChosenFields.indexOf(field);
				if (index > 0) {
					int newIndex = index - 1;
					Field other = inputChosenFields.get(newIndex);
					inputChosenFields.set(newIndex, field);
					inputChosenFields.set(index, other);
					inputChosenFieldsViewer.refresh();
					inputChosenFieldsViewer.setSelection(new StructuredSelection(field));
				}
				
				checkCompleteness();
			}
			
			private void downInputField(){
				Field field = getViewerSelection(inputChosenFieldsViewer);
				int index = inputChosenFields.indexOf(field);
				if (index < inputChosenFields.size() - 1) {
					int newIndex = index + 1;
					Field other = inputChosenFields.get(newIndex);
					inputChosenFields.set(newIndex, field);
					inputChosenFields.set(index, other);
					inputChosenFieldsViewer.refresh();
					inputChosenFieldsViewer.setSelection(new StructuredSelection(field));
				}
				
				checkCompleteness();
			}
			
			private void keyInputField(){
				Field field = getViewerSelection(inputChosenFieldsViewer);
				field.isKey = !field.isKey;
				
				// set the corresponding peer field to the same value
				if (inputChosenFields.size() == dbChosenFields.size()) {
					int index = inputChosenFields.indexOf(field);
					Field other = dbChosenFields.get(index);
					if (other != null) {
						other.isKey = field.isKey;
					}
				}
				
				inputChosenFieldsViewer.refresh();
				dbChosenFieldsViewer.refresh();
				
				checkCompleteness();
			}
			
			private void addDbField(){
				Field field = getViewerSelection(dbAvailableFieldsViewer);
				dbAvailableFields.remove(field);
				dbChosenFields.add(field);
				
				dbAvailableFieldsViewer.refresh();
				dbChosenFieldsViewer.refresh();
				
				dbChosenFieldsViewer.setSelection(new StructuredSelection(field));
				
				checkCompleteness();
			}
			
			private void delDbField(){
				Field field = getViewerSelection(dbChosenFieldsViewer);
				field.isKey = false;
				dbChosenFields.remove(field);
				dbAvailableFields.add(field);
				
				dbAvailableFieldsViewer.refresh();
				dbChosenFieldsViewer.refresh();
				
				checkCompleteness();
			}
			
			private void upDbField(){
				Field field = getViewerSelection(dbChosenFieldsViewer);
				int index = dbChosenFields.indexOf(field);
				if (index > 0) {
					int newIndex = index - 1;
					Field other = dbChosenFields.get(newIndex);
					dbChosenFields.set(newIndex, field);
					dbChosenFields.set(index, other);
					dbChosenFieldsViewer.refresh();
					dbChosenFieldsViewer.setSelection(new StructuredSelection(field));
				}
				
				checkCompleteness();
			}
			
			private void downDbField(){
				Field field = getViewerSelection(dbChosenFieldsViewer);
				int index = dbChosenFields.indexOf(field);
				if (index < dbChosenFields.size() - 1) {
					int newIndex = index + 1;
					Field other = dbChosenFields.get(newIndex);
					dbChosenFields.set(newIndex, field);
					dbChosenFields.set(index, other);
					dbChosenFieldsViewer.refresh();
					dbChosenFieldsViewer.setSelection(new StructuredSelection(field));
				}
				
				checkCompleteness();
			}
			
			private void keyDbField(){
				Field field = getViewerSelection(dbChosenFieldsViewer);
				field.isKey = !field.isKey;
				
				// set the corresponding peer field to the same value
				if (inputChosenFields.size() == dbChosenFields.size()) {
					int index = dbChosenFields.indexOf(field);
					Field other = inputChosenFields.get(index);
					if (other != null) {
						other.isKey = field.isKey;
					}
				}
				
				inputChosenFieldsViewer.refresh();
				dbChosenFieldsViewer.refresh();
				
				checkCompleteness();
			}
			
			public void createControl(Composite parent){
				ArrayContentProvider arrayContentProvider = new ArrayContentProvider();
				LabelProvider typeLabelProvider = new LabelProvider() {
					public String getText(Object element){
						if (element instanceof PersistentObject) {
							// TODO persistent objects should provide a human
							// readable description of themselves
							return ((PersistentObject) element).getClass().getSimpleName();
						} else {
							return element.toString();
						}
					}
				};
				LabelProvider fieldLabelProvider = new LabelProvider() {
					public String getText(Object element){
						String text = null;
						
						if (element instanceof Field) {
							Field field = (Field) element;
							if (field.isKey) {
								text = field.toString() + "*"; //$NON-NLS-1$
							} else {
								text = field.toString();
							}
						}
						
						return text;
					}
				};
				
				Label label;
				
				Composite composite = new Composite(parent, SWT.NONE);
				composite.setLayout(new GridLayout(1, false));
				
				Composite topArea = new Composite(composite, SWT.NONE);
				topArea.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
				topArea.setLayout(new GridLayout(2, false));
				
				label = new Label(topArea, SWT.NONE);
				label.setText("Typ:"); //$NON-NLS-1$
				
				typesViewer = new ComboViewer(topArea, SWT.DROP_DOWN | SWT.READ_ONLY);
				typesViewer.setContentProvider(arrayContentProvider);
				typesViewer.setLabelProvider(typeLabelProvider);
				List<Object> types = new ArrayList<Object>();
				types.add(Messages.GenericImporterBlatt_PleaseSelect);
				types.addAll(getTypes());
				typesViewer.setInput(types);
				typesViewer.setSelection(new StructuredSelection(typesViewer.getElementAt(0)));
				typesViewer.addSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent event){
						setType();
					}
				});
				
				Composite bottomArea = new Composite(composite, SWT.NONE);
				bottomArea.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
				bottomArea.setLayout(new GridLayout(2, true));
				
				label = new Label(bottomArea, SWT.NONE);
				label.setText(Messages.GenericImporterBlatt_FieldsExcel);
				
				label = new Label(bottomArea, SWT.NONE);
				label.setText(Messages.GenericImporterBlatt_FieldsElexis);
				
				FormData fd;
				
				Button addButton;
				Button delButton;
				Button upButton;
				Button downButton;
				Button keyButton;
				Control availableFieldsControl;
				Control chosenFieldsControl;
				
				// left area
				
				Composite leftArea = new Composite(bottomArea, SWT.NONE);
				leftArea.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
				leftArea.setLayout(new FormLayout());
				
				addButton = new Button(leftArea, SWT.PUSH);
				addButton.setText("+"); //$NON-NLS-1$
				addButton.addSelectionListener(new SelectionListener() {
					public void widgetSelected(SelectionEvent e){
						addInputField();
					}
					
					public void widgetDefaultSelected(SelectionEvent e){}
				});
				
				delButton = new Button(leftArea, SWT.PUSH);
				delButton.setText("-"); //$NON-NLS-1$
				delButton.addSelectionListener(new SelectionListener() {
					public void widgetSelected(SelectionEvent e){
						delInputField();
					}
					
					public void widgetDefaultSelected(SelectionEvent e){}
				});
				
				upButton = new Button(leftArea, SWT.PUSH);
				upButton.setText("^"); //$NON-NLS-1$
				upButton.addSelectionListener(new SelectionListener() {
					public void widgetSelected(SelectionEvent e){
						upInputField();
					}
					
					public void widgetDefaultSelected(SelectionEvent e){}
				});
				
				downButton = new Button(leftArea, SWT.PUSH);
				downButton.setText("v"); //$NON-NLS-1$
				downButton.addSelectionListener(new SelectionListener() {
					public void widgetSelected(SelectionEvent e){
						downInputField();
					}
					
					public void widgetDefaultSelected(SelectionEvent e){}
				});
				
				keyButton = new Button(leftArea, SWT.PUSH);
				keyButton.setText("K"); //$NON-NLS-1$
				keyButton.addSelectionListener(new SelectionListener() {
					public void widgetSelected(SelectionEvent e){
						keyInputField();
					}
					
					public void widgetDefaultSelected(SelectionEvent e){}
				});
				
				inputAvailableFieldsViewer =
					new ComboViewer(leftArea, SWT.DROP_DOWN | SWT.READ_ONLY);
				availableFieldsControl = inputAvailableFieldsViewer.getControl();
				
				inputChosenFieldsViewer = new TableViewer(leftArea, SWT.BORDER);
				chosenFieldsControl = inputChosenFieldsViewer.getControl();
				
				fd = new FormData();
				fd.left = new FormAttachment(0);
				fd.top = new FormAttachment(availableFieldsControl, 0, SWT.CENTER);
				addButton.setLayoutData(fd);
				
				fd = new FormData();
				fd.top = new FormAttachment(0);
				fd.left = new FormAttachment(addButton);
				fd.right = new FormAttachment(100);
				availableFieldsControl.setLayoutData(fd);
				
				fd = new FormData();
				fd.left = new FormAttachment(0);
				fd.top = new FormAttachment(availableFieldsControl, 5);
				delButton.setLayoutData(fd);
				
				fd = new FormData();
				fd.left = new FormAttachment(0);
				fd.top = new FormAttachment(delButton, 5);
				upButton.setLayoutData(fd);
				
				fd = new FormData();
				fd.left = new FormAttachment(0);
				fd.top = new FormAttachment(upButton);
				downButton.setLayoutData(fd);
				
				fd = new FormData();
				fd.left = new FormAttachment(0);
				fd.top = new FormAttachment(downButton);
				keyButton.setLayoutData(fd);
				
				fd = new FormData();
				fd.left = new FormAttachment(availableFieldsControl, 0, SWT.LEFT);
				fd.top = new FormAttachment(availableFieldsControl, 5);
				fd.right = new FormAttachment(100);
				fd.bottom = new FormAttachment(100);
				chosenFieldsControl.setLayoutData(fd);
				
				// right area
				
				Composite rightArea = new Composite(bottomArea, SWT.NONE);
				rightArea.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
				rightArea.setLayout(new FormLayout());
				
				addButton = new Button(rightArea, SWT.PUSH);
				addButton.setText("+"); //$NON-NLS-1$
				addButton.addSelectionListener(new SelectionListener() {
					public void widgetSelected(SelectionEvent e){
						addDbField();
					}
					
					public void widgetDefaultSelected(SelectionEvent e){}
				});
				
				delButton = new Button(rightArea, SWT.PUSH);
				delButton.setText("-"); //$NON-NLS-1$
				delButton.addSelectionListener(new SelectionListener() {
					public void widgetSelected(SelectionEvent e){
						delDbField();
					}
					
					public void widgetDefaultSelected(SelectionEvent e){}
				});
				
				upButton = new Button(rightArea, SWT.PUSH);
				upButton.setText("^"); //$NON-NLS-1$
				upButton.addSelectionListener(new SelectionListener() {
					public void widgetSelected(SelectionEvent e){
						upDbField();
					}
					
					public void widgetDefaultSelected(SelectionEvent e){}
				});
				
				downButton = new Button(rightArea, SWT.PUSH);
				downButton.setText("v"); //$NON-NLS-1$
				downButton.addSelectionListener(new SelectionListener() {
					public void widgetSelected(SelectionEvent e){
						downDbField();
					}
					
					public void widgetDefaultSelected(SelectionEvent e){}
				});
				
				keyButton = new Button(rightArea, SWT.PUSH);
				keyButton.setText("K"); //$NON-NLS-1$
				keyButton.addSelectionListener(new SelectionListener() {
					public void widgetSelected(SelectionEvent e){
						keyDbField();
					}
					
					public void widgetDefaultSelected(SelectionEvent e){}
				});
				
				dbAvailableFieldsViewer = new ComboViewer(rightArea, SWT.DROP_DOWN | SWT.READ_ONLY);
				availableFieldsControl = dbAvailableFieldsViewer.getControl();
				
				dbChosenFieldsViewer = new TableViewer(rightArea, SWT.BORDER);
				chosenFieldsControl = dbChosenFieldsViewer.getControl();
				
				fd = new FormData();
				fd.left = new FormAttachment(0);
				fd.top = new FormAttachment(availableFieldsControl, 0, SWT.CENTER);
				addButton.setLayoutData(fd);
				
				fd = new FormData();
				fd.top = new FormAttachment(0);
				fd.left = new FormAttachment(addButton);
				fd.right = new FormAttachment(100);
				availableFieldsControl.setLayoutData(fd);
				
				fd = new FormData();
				fd.left = new FormAttachment(0);
				fd.top = new FormAttachment(availableFieldsControl, 5);
				delButton.setLayoutData(fd);
				
				fd = new FormData();
				fd.left = new FormAttachment(0);
				fd.top = new FormAttachment(delButton, 5);
				upButton.setLayoutData(fd);
				
				fd = new FormData();
				fd.left = new FormAttachment(0);
				fd.top = new FormAttachment(upButton);
				downButton.setLayoutData(fd);
				
				fd = new FormData();
				fd.left = new FormAttachment(0);
				fd.top = new FormAttachment(downButton);
				keyButton.setLayoutData(fd);
				
				fd = new FormData();
				fd.left = new FormAttachment(availableFieldsControl, 0, SWT.LEFT);
				fd.top = new FormAttachment(availableFieldsControl, 5);
				fd.right = new FormAttachment(100);
				fd.bottom = new FormAttachment(100);
				chosenFieldsControl.setLayoutData(fd);
				
				// viewers configuration
				
				inputAvailableFieldsViewer.setContentProvider(arrayContentProvider);
				inputAvailableFieldsViewer.setLabelProvider(fieldLabelProvider);
				
				inputChosenFieldsViewer.setContentProvider(arrayContentProvider);
				inputChosenFieldsViewer.setLabelProvider(fieldLabelProvider);
				
				dbAvailableFieldsViewer.setContentProvider(arrayContentProvider);
				dbAvailableFieldsViewer.setLabelProvider(fieldLabelProvider);
				
				dbChosenFieldsViewer.setContentProvider(arrayContentProvider);
				dbChosenFieldsViewer.setLabelProvider(fieldLabelProvider);
				
				// set intial type selection
				setType();
				
				setControl(composite);
			}
		}
		
		class SyncPage extends WizardPage {
			private static final String PLUGIN_ID = "ch.elexis.importer.div"; //$NON-NLS-1$
			
			private static final int IMAGE_INPUT_ONLY = 1;
			private static final int IMAGE_DB_ONLY = 2;
			private static final int IMAGE_DIFF = 3;
			private static final int IMAGE_EQUAL = 4;
			
			private static final int IMAGE_COLUMN_WIDTH = 24;
			
			private TableViewer mainViewer;
			private TableViewer diff1Viewer;
			private TableViewer diff2Viewer;
			
			private Button importNewButton;
			private Button updateButton;
			
			private List<SyncElement> syncElements = new ArrayList<SyncElement>();
			private SyncElement currentSyncElement = null;
			
			SyncPage(){
				super("Synchronize", "Synchronize", null); //$NON-NLS-1$ //$NON-NLS-2$
				setDescription(Messages.GenericImporterBlatt_MatchData);
			}
			
			private Image getImage(int id){
				switch (id) {
				case IMAGE_INPUT_ONLY:
					return PlatformUI.getWorkbench().getSharedImages()
						.getImage(ISharedImages.IMG_OBJ_FILE);
				case IMAGE_DB_ONLY:
					return Images.IMG_DATABASE.getImage();
				case IMAGE_DIFF:
					return Images.IMG_CONFLICT.getImage();
				case IMAGE_EQUAL:
					return Images.IMG_OK.getImage();
				}
				
				return null;
			}
			
			public void createControl(Composite parent){
				Composite composite = new Composite(parent, SWT.NONE);
				composite.setLayout(new GridLayout(1, false));
				
				Button refreshButton = new Button(composite, SWT.PUSH);
				refreshButton.setText(Messages.GenericImporterBlatt_update);
				refreshButton.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e){
						refresh();
					}
				});
				
				mainViewer =
					new TableViewer(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
				Control mainControl = mainViewer.getControl();
				mainControl.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
				
				mainViewer.addSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent event){
						setSyncElement();
					}
				});
				
				mainViewer.setLabelProvider(new ITableLabelProvider() {
					public Image getColumnImage(Object element, int columnIndex){
						Image image = null;
						
						if (element instanceof SyncElement) {
							SyncElement syncElement = (SyncElement) element;
							if (columnIndex == 0) {
								switch (syncElement.state) {
								case SyncElement.INPUT_ONLY:
									image = getImage(IMAGE_INPUT_ONLY);
									break;
								case SyncElement.DB_ONLY:
									image = getImage(IMAGE_DB_ONLY);
									break;
								case SyncElement.DIFF:
									image = getImage(IMAGE_DIFF);
									break;
								case SyncElement.EQUAL:
									image = getImage(IMAGE_EQUAL);
									break;
								}
							} else {
								// mark differences
								if (syncElement.state == SyncElement.DIFF) {
									int fieldIndex = columnIndex - 1;
									HashMap<String, String> inputObject = syncElement.inputObject;
									PersistentObject dbObject = syncElement.dbObject;
									
									String inputKey =
										mappingPage.inputChosenFields.get(fieldIndex).name;
									String dbKey = mappingPage.dbChosenFields.get(fieldIndex).name;
									String inputValue =
										PersistentObject.checkNull(inputObject.get(inputKey))
											.trim();
									String dbValue =
										PersistentObject.checkNull(dbObject.get(dbKey)).trim();
									if (inputValue != null) {
										// compare
										if (inputValue.equals(dbValue)) {
											// equal values
											image = null;
										} else {
											// values differ, show conflict
											image = getImage(IMAGE_DIFF);
										}
									} else {
										if (dbValue != null) {
											// only dbValue available, show conflict
											image = getImage(IMAGE_DIFF);
										} else {
											// no value available
											image = null;
										}
									}
								}
							}
						}
						
						return image;
					}
					
					public String getColumnText(Object element, int columnIndex){
						String text = ""; //$NON-NLS-1$
						
						// commonly used variables
						int fieldIndex;
						HashMap<String, String> inputObject;
						PersistentObject dbObject;
						String key;
						
						if (columnIndex > 0 && element instanceof SyncElement) {
							// first column is image
							fieldIndex = columnIndex - 1;
							
							SyncElement syncElement = (SyncElement) element;
							switch (syncElement.state) {
							case SyncElement.INPUT_ONLY:
								inputObject = syncElement.inputObject;
								key = mappingPage.inputChosenFields.get(fieldIndex).name;
								text = inputObject.get(key);
								break;
							case SyncElement.DB_ONLY:
								dbObject = syncElement.dbObject;
								key = mappingPage.dbChosenFields.get(fieldIndex).name;
								text = dbObject.get(key);
								break;
							case SyncElement.DIFF:
								inputObject = syncElement.inputObject;
								dbObject = syncElement.dbObject;
								String inputKey =
									mappingPage.inputChosenFields.get(fieldIndex).name;
								String dbKey = mappingPage.dbChosenFields.get(fieldIndex).name;
								String inputValue =
									PersistentObject.checkNull(inputObject.get(inputKey)).trim();
								String dbValue =
									PersistentObject.checkNull(dbObject.get(dbKey)).trim();
								if (inputValue != null) {
									// compare
									if (inputValue.equals(dbValue)) {
										// equal values, only show one of them
										text = inputValue;
									} else {
										// values differ, show both values
										if (dbValue != null) {
											text = inputValue + "/" + dbValue; //$NON-NLS-1$
										} else {
											text = inputValue + "/"; //$NON-NLS-1$
										}
									}
								} else {
									if (dbValue != null) {
										// only dbValue available
										text = "/" + dbValue; //$NON-NLS-1$
									} else {
										// no value available
										text = "/"; //$NON-NLS-1$
									}
								}
								
								break;
							case SyncElement.EQUAL:
								// all values equal, only show inputValue
								inputObject = syncElement.inputObject;
								key = mappingPage.inputChosenFields.get(fieldIndex).name;
								text = inputObject.get(key);
								break;
							}
						}
						
						return text;
					}
					
					public void addListener(ILabelProviderListener listener){
						// nothing to do
					}
					
					public void dispose(){
						// nothing to do
					}
					
					public boolean isLabelProperty(Object element, String property){
						return false;
					}
					
					public void removeListener(ILabelProviderListener listener){
						// nothing to do
					}
				});
				mainViewer.setContentProvider(new IStructuredContentProvider() {
					public Object[] getElements(Object inputElement){
						return syncElements.toArray();
					}
					
					public void dispose(){
						// nothing to do
					}
					
					public void inputChanged(Viewer viewer, Object oldInput, Object newInput){
						// nothing to do
					}
				});
				
				// mainViewer gets input from syncElements
				mainViewer.setInput(this);
				
				GridData gd;
				
				diff1Viewer = new TableViewer(composite, SWT.BORDER);
				Control diff1Control = diff1Viewer.getControl();
				gd = SWTHelper.getFillGridData(1, true, 1, false);
				gd.heightHint = diff1Viewer.getTable().getItemHeight();
				diff1Control.setLayoutData(gd);
				
				diff2Viewer = new TableViewer(composite, SWT.BORDER);
				Control diff2Control = diff2Viewer.getControl();
				gd.heightHint = diff2Viewer.getTable().getItemHeight();
				diff2Control.setLayoutData(gd);
				
				// table configuration
				
				mainViewer.getTable().setHeaderVisible(true);
				mainViewer.getTable().setLinesVisible(true);
				
				diff1Viewer.getTable().setLinesVisible(true);
				diff2Viewer.getTable().setLinesVisible(true);
				
				// content provider
				DiffViewerContentProvider dvcp = new DiffViewerContentProvider();
				diff1Viewer.setContentProvider(dvcp);
				diff2Viewer.setContentProvider(dvcp);
				
				// label provider
				diff1Viewer.setLabelProvider(new DiffViewerLabelProvider(
					DiffViewerLabelProvider.INPUT));
				diff2Viewer
					.setLabelProvider(new DiffViewerLabelProvider(DiffViewerLabelProvider.DB));
				
				// import new elements button
				new Label(composite, SWT.NONE)
					.setText(Messages.GenericImporterBlatt_ImportAllDataNew);
				importNewButton = new Button(composite, SWT.PUSH);
				importNewButton.setText(Messages.GenericImporterBlatt_ImportAllDataNewCaption);
				importNewButton.setImage(getImage(IMAGE_INPUT_ONLY));
				importNewButton.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e){
						importNew();
					}
				});
				// TODO disabled for now
				importNewButton.setEnabled(true);
				
				// import new elements button
				new Label(composite, SWT.NONE)
					.setText(Messages.GenericImporterBlatt_UpdateSelectedData);
				updateButton = new Button(composite, SWT.PUSH);
				updateButton.setText(Messages.GenericImporterBlatt_ImportAllValues);
				updateButton.setImage(getImage(IMAGE_DIFF));
				updateButton.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e){
						updateSelected();
					}
				});
				// initial state: nothing selected
				updateButton.setEnabled(false);
				
				setControl(composite);
				
			}
			
			private void refresh(){
				createColumns();
				sync();
			}
			
			private void createColumns(){
				createColumns(mainViewer);
				createColumns(diff1Viewer);
				createColumns(diff2Viewer);
			}
			
			private void createColumns(TableViewer viewer){
				Table table = viewer.getTable();
				
				// remove existing columns
				TableColumn[] oldColumns = table.getColumns();
				if (oldColumns != null) {
					for (TableColumn column : oldColumns) {
						column.dispose();
					}
				}
				
				// first column: image
				TableColumn imageColumn = new TableColumn(table, SWT.LEFT);
				imageColumn.setWidth(IMAGE_COLUMN_WIDTH);
				imageColumn.setText(""); //$NON-NLS-1$
				
				if (mappingPage.isPageComplete()) {
					int columnsCount = mappingPage.inputChosenFields.size();
					int columnSize =
						(table.getBounds().width - imageColumn.getWidth()) / columnsCount;
					
					TableColumn[] columns = new TableColumn[columnsCount];
					
					for (int i = 0; i < columnsCount; i++) {
						columns[i] = new TableColumn(table, SWT.LEFT);
						columns[i].setWidth(columnSize);
						String inputName = mappingPage.inputChosenFields.get(i).name;
						String dbName = mappingPage.dbChosenFields.get(i).name;
						String name = dbName;
						if (!inputName.equals(dbName)) {
							name = inputName + "/" + name; //$NON-NLS-1$
						}
						columns[i].setText(name);
					}
					
					// correct rounding errors (prevent scrollbars)
					if (columnsCount > 0) {
						int lastIndex = columnsCount - 1;
						columns[lastIndex].setWidth(columns[lastIndex].getWidth() - 5);
					}
				}
			}
			
			private void sync(){
				// clear old data
				syncElements.clear();
				
				// initialize input/db field mapping
				HashMap<String, String> dbFieldNames = new HashMap<String, String>();
				for (int i = 0; i < mappingPage.inputChosenFields.size(); i++) {
					String inputName = mappingPage.inputChosenFields.get(i).name;
					String dbName = mappingPage.dbChosenFields.get(i).name;
					dbFieldNames.put(inputName, dbName);
				}
				
				// load objects from database
				/*
				 * bad performance. we actually don't require to load all objects from the database
				 * Query<PersistentObject> query = new Query<PersistentObject>(template.getClass());
				 * List<PersistentObject> dbObjects = query.execute(); if (dbObjects == null) {
				 * dbObjects = new ArrayList<PersistentObject>(); }
				 */
				
				// load objects from excel, start with second row (first row is header)
				List<HashMap<String, String>> inputObjects =
					new ArrayList<HashMap<String, String>>();
				for (int i = excel.getFirstRow() + 1; i <= excel.getLastRow(); i++) {
					List<String> row = excel.getRow(i);
					if (row != null) {
						HashMap<String, String> rowMap = new HashMap<String, String>();
						for (Field field : mappingPage.inputChosenFields) {
							int index = mappingPage.inputFieldIndices.get(field.name).intValue();
							String key = field.name;
							String value;
							if (row.size() > index) {
								// get value from excel, eliminate leading and trailing white space
								value = row.get(index).trim();
							} else {
								value = ""; //$NON-NLS-1$
							}
							rowMap.put(key, value);
						}
						
						inputObjects.add(rowMap);
					}
				}
				
				// get key fields
				List<KeyFields> keyFields = new ArrayList<KeyFields>();
				for (int i = 0; i < mappingPage.inputChosenFields.size(); i++) {
					Field inputField = mappingPage.inputChosenFields.get(i);
					Field dbField = mappingPage.dbChosenFields.get(i);
					if (inputField.isKey && dbField.isKey) {
						keyFields.add(new KeyFields(inputField.name, dbField.name));
					}
				}
				
				PersistentObject template = mappingPage.getSelectedType();
				
				// sync over all input objects
				for (HashMap<String, String> rowMap : inputObjects) {
					// PersistentObject dbObject = findDbObject(rowMap, dbObjects, keyFields);
					PersistentObject dbObject =
						findDbObject(rowMap, keyFields, template.getClass());
					
					SyncElement syncElement = new SyncElement(rowMap, dbObject, dbFieldNames);
					if (syncElement.state == SyncElement.INPUT_ONLY
						|| syncElement.state == SyncElement.DIFF) {
						syncElements.add(syncElement);
					}
				}
				
				// show sync result in viewer
				mainViewer.refresh();
				
				setSyncElement();
			}
			
			private PersistentObject findDbObject(HashMap<String, String> inputObject,
				List<KeyFields> keyFields, Class typ){
				Query<PersistentObject> query = new Query<PersistentObject>(typ);
				for (KeyFields keyField : keyFields) {
					String name = keyField.dbName;
					String value = inputObject.get(keyField.inputName);
					query.add(name, "=", value); //$NON-NLS-1$
				}
				List<PersistentObject> dbObjects = query.execute();
				if (dbObjects != null && dbObjects.size() > 0) {
					// found
					return dbObjects.get(0);
				}
				
				// not found
				return null;
			}
			
			/*
			 * private PersistentObject findDbObject(HashMap<String, String> inputObject,
			 * List<PersistentObject> dbObjects, List<KeyFields> keyFields) { for (PersistentObject
			 * dbObject : dbObjects) { boolean found = true; for (KeyFields keyField : keyFields) {
			 * String inputValue = inputObject.get(keyField.inputName); String dbValue =
			 * dbObject.get(keyField.dbName); if (dbValue == null || !dbValue.equals(inputValue)) {
			 * found = false; break; } } if (found) { return dbObject; } }
			 * 
			 * // not found return null; }
			 */
			
			private void setSyncElement(){
				// this element is shown when multiple elements are selected in the mainViewer
				final SyncElement UNKNOWN_SYNC_ELEMENT = new SyncElement();
				
				// get current selection
				IStructuredSelection selection = (IStructuredSelection) mainViewer.getSelection();
				
				if (selection.size() == 0) {
					// nothing selected, show nothing
					currentSyncElement = null;
				} else if (selection.size() == 1) {
					// one element selected, show this element
					Object element = selection.getFirstElement();
					if (element instanceof SyncElement) {
						currentSyncElement = (SyncElement) element;
					} else {
						currentSyncElement = null;
					}
				} else {
					currentSyncElement = UNKNOWN_SYNC_ELEMENT;
				}
				
				diff1Viewer.setInput(currentSyncElement);
				diff2Viewer.setInput(currentSyncElement);
				
				updateButton.setEnabled(isSelectionUpdatable());
			}
			
			private boolean isSelectionUpdatable(){
				boolean updatable = false;
				
				IStructuredSelection selection = (IStructuredSelection) mainViewer.getSelection();
				if (selection.size() > 0) {
					updatable = true;
					
					// iterate over all elements and set updateable to false if we
					// find any not updatable element
					for (Object element : selection.toList()) {
						if (element instanceof SyncElement) {
							SyncElement syncElement = (SyncElement) element;
							if (syncElement == null || syncElement.state != SyncElement.DIFF) {
								updatable = false;
							}
						} else {
							// incompatible element
							updatable = false;
						}
					}
				}
				
				return updatable;
			}
			
			private void importNew(){
				List<SyncElement> imported = new ArrayList<SyncElement>();
				for (SyncElement syncElement : syncElements) {
					// don't import existing objects here
					if (syncElement.state != SyncElement.INPUT_ONLY) {
						continue;
					}
					
					// create new element in DB
					HashMap<String, String> inputObject = syncElement.inputObject;
					if (inputObject != null) {
						// warning: call to MappingPage
						Class typ = mappingPage.getSelectedType().getClass();
						
						String[] fields = new String[mappingPage.dbChosenFields.size()];
						for (int i = 0; i < fields.length; i++) {
							fields[i] = mappingPage.dbChosenFields.get(i).name;
						}
						
						String[] values = new String[fields.length];
						for (int i = 0; i < fields.length; i++) {
							String key = mappingPage.inputChosenFields.get(i).name;
							values[i] = inputObject.get(key);
						}
						
						PersistentObject po =
							new PersistentObjectFactory().create(typ, fields, values);
						
						imported.add(syncElement);
					}
				}
				syncElements.removeAll(imported);
				mainViewer.refresh();
				// deselect all elemenets
				mainViewer.setSelection(new StructuredSelection());
			}
			
			private void updateSelected(){
				IStructuredSelection selection = (IStructuredSelection) mainViewer.getSelection();
				for (Object element : selection.toList()) {
					if (element instanceof SyncElement) {
						SyncElement syncElement = (SyncElement) element;
						updateSyncElement(syncElement);
					}
				}
				
				mainViewer.refresh();
				setSyncElement();
			}
			
			/**
			 * Update a single SyncElement to the database. Remove the element form the current list
			 * (since its now of type EQUAL).
			 * 
			 * @param syncElement
			 *            SyncElement of type DIFF
			 */
			private void updateSyncElement(SyncElement syncElement){
				if (syncElement == null || syncElement.state != SyncElement.DIFF) {
					
					return;
				}
				
				// create new element in DB
				HashMap<String, String> inputObject = syncElement.inputObject;
				PersistentObject dbObject = syncElement.dbObject;
				
				if (inputObject != null && dbObject != null) {
					String[] fields = new String[mappingPage.dbChosenFields.size()];
					for (int i = 0; i < fields.length; i++) {
						fields[i] = mappingPage.dbChosenFields.get(i).name;
					}
					
					String[] values = new String[fields.length];
					for (int i = 0; i < fields.length; i++) {
						String key = mappingPage.inputChosenFields.get(i).name;
						values[i] = inputObject.get(key);
					}
					
					dbObject.set(fields, values);
					
					syncElements.remove(syncElement);
				}
			}
			
			private class DiffViewerContentProvider implements IStructuredContentProvider {
				public Object[] getElements(Object inputElement){
					if (inputElement instanceof SyncElement) {
						return new Object[] {
							inputElement
						};
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
			
			private class DiffViewerLabelProvider implements ITableLabelProvider {
				static final int INPUT = 1;
				static final int DB = 2;
				
				private int type;
				
				public DiffViewerLabelProvider(int type){
					this.type = type;
				}
				
				public Image getColumnImage(Object element, int columnIndex){
					Image image = null;
					
					// commonly used variables
					int fieldIndex;
					HashMap<String, String> inputObject;
					PersistentObject dbObject;
					String key;
					
					if (columnIndex > 0 && element instanceof SyncElement) {
						// first column is image
						fieldIndex = columnIndex - 1;
						
						SyncElement syncElement = (SyncElement) element;
						switch (syncElement.state) {
						case SyncElement.DIFF:
							inputObject = syncElement.inputObject;
							dbObject = syncElement.dbObject;
							boolean equal = false;
							if (inputObject != null && dbObject != null) {
								String inputKey =
									mappingPage.inputChosenFields.get(fieldIndex).name;
								String dbKey = mappingPage.dbChosenFields.get(fieldIndex).name;
								String inputValue = inputObject.get(inputKey).trim();
								String dbValue = dbObject.get(dbKey).trim();
								if (inputValue.equals(dbValue)) {
									equal = true;
								}
							}
							if (!equal) {
								image = getImage(IMAGE_DIFF);
							}
							break;
						}
					}
					
					return image;
				}
				
				public String getColumnText(Object element, int columnIndex){
					String text = ""; //$NON-NLS-1$
					
					// commonly used variables
					int fieldIndex;
					HashMap<String, String> inputObject;
					PersistentObject dbObject;
					String key;
					
					if (columnIndex > 0 && element instanceof SyncElement) {
						// first column is image
						fieldIndex = columnIndex - 1;
						
						SyncElement syncElement = (SyncElement) element;
						switch (syncElement.state) {
						case SyncElement.INPUT_ONLY:
							if (type == INPUT) {
								inputObject = syncElement.inputObject;
								key = mappingPage.inputChosenFields.get(fieldIndex).name;
								text = inputObject.get(key);
							} else {
								text = ""; //$NON-NLS-1$
							}
							break;
						case SyncElement.DB_ONLY:
							if (type == DB) {
								dbObject = syncElement.dbObject;
								key = mappingPage.dbChosenFields.get(fieldIndex).name;
								text = dbObject.get(key);
							} else {
								text = ""; //$NON-NLS-1$
							}
							break;
						case SyncElement.DIFF:
							if (type == INPUT) {
								inputObject = syncElement.inputObject;
								key = mappingPage.inputChosenFields.get(fieldIndex).name;
								text = inputObject.get(key);
							} else {
								dbObject = syncElement.dbObject;
								key = mappingPage.dbChosenFields.get(fieldIndex).name;
								text = dbObject.get(key);
							}
							break;
						case SyncElement.EQUAL:
							if (type == INPUT) {
								inputObject = syncElement.inputObject;
								key = mappingPage.inputChosenFields.get(fieldIndex).name;
								text = inputObject.get(key);
							} else {
								dbObject = syncElement.dbObject;
								key = mappingPage.dbChosenFields.get(fieldIndex).name;
								text = dbObject.get(key);
							}
							break;
						case SyncElement.UNKNOWN:
							text = "?"; //$NON-NLS-1$
							break;
						}
					}
					
					return text;
				}
				
				public void addListener(ILabelProviderListener listener){
					// nothing to do
				}
				
				public void dispose(){
					// nothing to do
				}
				
				public boolean isLabelProperty(Object element, String property){
					return false;
				}
				
				public void removeListener(ILabelProviderListener listener){
					// nothing to do
				}
			}
			
			// TODO dbFieldNames mapping may become outdated
			class SyncElement {
				static final int UNKNOWN = 0;
				static final int EQUAL = 1;
				static final int DIFF = 2;
				static final int INPUT_ONLY = 3;
				static final int DB_ONLY = 4;
				
				int state = UNKNOWN;
				
				HashMap<String, String> inputObject;
				PersistentObject dbObject;
				HashMap<String, String> dbFieldNames;
				
				/**
				 * Creates a new sync element.
				 * 
				 * @param inputObject
				 * @param dbObject
				 * @param dbFieldNames
				 */
				SyncElement(HashMap<String, String> inputObject, PersistentObject dbObject,
					HashMap<String, String> dbFieldNames){
					this.inputObject = inputObject;
					this.dbObject = dbObject;
					
					this.dbFieldNames = dbFieldNames;
					
					sync();
				}
				
				/**
				 * Creates a dummy sync element of type UNKNOWN
				 */
				SyncElement(){
					this(null, null, null);
				}
				
				void sync(){
					state = UNKNOWN;
					
					// handle simple cases
					if (inputObject == null && dbObject == null) {
						state = UNKNOWN;
						return;
					}
					if (dbObject == null) {
						state = INPUT_ONLY;
						return;
					}
					if (inputObject == null) {
						state = DB_ONLY;
						return;
					}
					
					// both objects are available, compare all fields
					boolean equal = true;
					for (String inputName : inputObject.keySet()) {
						String dbName = dbFieldNames.get(inputName);
						
						// TODO workaround: the db may return values padded with spaces, so we do a
						// trim()
						String inputValue =
							PersistentObject.checkNull(inputObject.get(inputName)).trim();
						String dbValue = PersistentObject.checkNull(dbObject.get(dbName)).trim();
						if (!inputValue.equals(dbValue)) {
							equal = false;
							// enough seen
							break;
						}
					}
					if (equal) {
						state = EQUAL;
					} else {
						state = DIFF;
					}
					return;
				}
			}
		}
		
		class Field {
			String name;
			boolean isKey;
			
			Field(String name){
				this(name, false);
			}
			
			Field(String name, boolean isKey){
				this.name = name;
				this.isKey = isKey;
			}
			
			public String toString(){
				return name;
			}
			
			public boolean equals(Object o){
				if (o instanceof Field) {
					Field other = (Field) o;
					
					if (other.name.equals(this.name) && other.isKey == this.isKey) {
						return true;
					}
				}
				
				return false;
			}
		}
		
		class KeyFields {
			String inputName;
			String dbName;
			
			KeyFields(String inputName, String dbName){
				this.inputName = inputName;
				this.dbName = dbName;
			}
		}
		
	}
}
