package ch.elexis.core.findings.ui.preferences;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.slf4j.LoggerFactory;

import ch.elexis.befunde.Messwert;
import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.findings.templates.model.FindingsTemplate;
import ch.elexis.core.findings.templates.model.FindingsTemplates;
import ch.elexis.core.findings.templates.model.InputDataGroupComponent;
import ch.elexis.core.findings.ui.services.FindingsTemplateServiceComponent;
import ch.elexis.core.findings.ui.services.MigratorServiceComponent;
import ch.elexis.core.findings.util.model.TransientCoding;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.rgw.tools.JdbcLink.Stm;

public class MesswertMigrationSettings extends PreferencePage implements IWorkbenchPreferencePage {
	
	private List<MesswertFieldMapping> localMappings;
	
	private TableViewer viewerBefundeMapping;
	
	private HashMap<String, Integer> availableCode;
	
	@Override
	public void init(IWorkbench workbench){
		if (isMesswertAvailable()) {
			localMappings = MesswertUtil.getLocalMappings();
			availableCode = getAvailableCodes();
		}
	}
	
	@Override
	protected Control createContents(Composite parent){
		Composite parentComposite = new Composite(parent, SWT.NONE);
		parentComposite.setLayout(new GridLayout(2, false));
		
		Label label = new Label(parentComposite, SWT.NONE);
		if (isMesswertAvailable()) {
			label.setText("Befunde Zuordnung:");
			label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
			
			viewerBefundeMapping =
				new TableViewer(parentComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
			viewerBefundeMapping.getTable().setHeaderVisible(true);
			viewerBefundeMapping.getTable().setLinesVisible(true);
			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
			gd.heightHint = 400;
			viewerBefundeMapping.getControl().setLayoutData(gd);
			
			viewerBefundeMapping.setContentProvider(new ArrayContentProvider());
			
			TableViewerColumn column = new TableViewerColumn(viewerBefundeMapping, SWT.NONE);
			column.getColumn().setWidth(150);
			column.getColumn().setText("Messwert");
			column.setLabelProvider(new ColumnLabelProvider() {
				public String getText(Object element){
					if (element instanceof MesswertFieldMapping) {
						return ((MesswertFieldMapping) element).getLocalFieldLabel();
					}
					return "?";
				}
			});
			
			column = new TableViewerColumn(viewerBefundeMapping, SWT.NONE);
			column.getColumn().setWidth(150);
			column.getColumn().setText("Code");
			column.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public String getText(Object element){
					if (element instanceof MesswertFieldMapping) {
						return ((MesswertFieldMapping) element).getFindingsCodeLabel();
					}
					return "";
				}
			});
			column.setEditingSupport(new EditingSupport(viewerBefundeMapping) {
				
				@Override
				protected void setValue(Object element, Object value){
					Set<String> keys = availableCode.keySet();
					for (String key : keys) {
						Integer fieldValue = availableCode.get(key);
						if (fieldValue.equals(value)) {
							((MesswertFieldMapping) element).setFindigsCode(key);
						}
					}
					getViewer().update(element, null);
				}
				
				@Override
				protected Object getValue(Object element){
					if (element instanceof MesswertFieldMapping) {
						if (((MesswertFieldMapping) element).isValidMapping()) {
							String fieldLabel =
								((MesswertFieldMapping) element).getFindingsCodeLabel();
							Integer ret = availableCode.get(fieldLabel);
							if (ret != null) {
								return ret;
							}
						}
					}
					return 0;
				}
				
				@Override
				protected CellEditor getCellEditor(Object element){
					Set<String> keys = availableCode.keySet();
					String[] displayValues = new String[keys.size()];
					for (String key : keys) {
						displayValues[availableCode.get(key)] = key;
					}
					return new ComboBoxCellEditor(viewerBefundeMapping.getTable(), displayValues);
				}
				
				@Override
				protected boolean canEdit(Object element){
					return true;
				}
			});
			
			viewerBefundeMapping.setInput(localMappings);
			
			Label statistics = new Label(parentComposite, SWT.NONE);
			statistics.setText("Statistik lädt ... ");
			Executors.newSingleThreadExecutor().execute(new Runnable() {
				@Override
				public void run(){
					List<Messwert> messwerte = getAllMesswerte();
					long notMigrated = countNotMigratedMesswerte(messwerte);
					if (statistics != null && !statistics.isDisposed()) {
						Display.getDefault().syncExec(new Runnable() {
							@Override
							public void run(){
								statistics
									.setText(messwerte.size() + " Messwerte vorhanden, davon sind "
										+ notMigrated + " noch zu migrieren.");
								parentComposite.layout();
							}
						});
					}
				}
			});
			Button btn = new Button(parentComposite, SWT.PUSH);
			btn.setText("Messwerte migrieren");
			btn.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e){
					Query<Patient> query = new Query<Patient>(Patient.class);
					List<Patient> patients = query.execute();
					
					ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
					
					try {
						dialog.run(true, true, new IRunnableWithProgress() {
							
							@Override
							public void run(IProgressMonitor monitor)
								throws InvocationTargetException, InterruptedException{
								monitor.beginTask("Messwerte Migration", patients.size());
								for (int count = 0; count < patients.size(); count++) {
									Patient patient = patients.get(count);
									monitor.subTask(
										"Patient (" + count + "/" + patients.size() + ")");
									MigratorServiceComponent.getService().migratePatientsFindings(
										patient.getId(), IObservation.class, null);
									monitor.worked(1);
									if (monitor.isCanceled()) {
										break;
									}
								}
								monitor.done();
							}
						});
					} catch (InvocationTargetException | InterruptedException ex) {
						MessageDialog.openError(getShell(), "Migration fehlgeschlagen",
							"Wärend der Migration ist ein Fehler aufgetreten.\n" + ex.getMessage());
					}
				}
			});
		} else {
			label.setText("Befunde nicht installiert.");
			label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		}
		
		return parentComposite;
	}
	
	private boolean isMesswertAvailable(){
		try {
			Class<?> clazz = getClass().getClassLoader().loadClass("ch.elexis.befunde.Messwert");
			return clazz != null;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
	
	private List<Messwert> getAllMesswerte(){
		Query<Messwert> query = new Query<Messwert>(Messwert.class);
		return query.execute();
	}
	
	private long countNotMigratedMesswerte(List<Messwert> messwerte){
		long ret = 0;
		for (Messwert messwert : messwerte) {
			if (!hasMigratedObservations(messwert.storeToString())) {
				ret++;
			}
		}
		return ret;
	}
	
	private boolean hasMigratedObservations(String originuri){
		Stm stm = PersistentObject.getDefaultConnection().getStatement();
		if (stm != null) {
			try {
				ResultSet result = stm
					.query("SELECT ID FROM CH_ELEXIS_CORE_FINDINGS_OBSERVATION WHERE originuri = '"
						+ originuri + "';");
				return result.next();
			} catch (SQLException e) {
				LoggerFactory.getLogger(getClass()).error("Error on migrated lookup", e);
			} finally {
				PersistentObject.getDefaultConnection().releaseStatement(stm);
			}
		}
		return false;
	}
	
	protected HashMap<String, Integer> getAvailableCodes(){
		HashMap<String, Integer> ret = new HashMap<String, Integer>();
		List<String> codesStrings = new ArrayList<String>();
		// load the templates and collect the codes from the template titles
		FindingsTemplates templatesResource =
			FindingsTemplateServiceComponent.getService().getFindingsTemplates("Standard Vorlagen");
		EList<FindingsTemplate> templates = templatesResource.getFindingsTemplates();
		List<ICoding> codes = new ArrayList<>();
		for (FindingsTemplate findingsTemplate : templates) {
			if (findingsTemplate.getInputData() instanceof InputDataGroupComponent) {
				EList<FindingsTemplate> subTemplates =
					((InputDataGroupComponent) findingsTemplate.getInputData())
						.getFindingsTemplates();
				for (FindingsTemplate subFindingsTemplate : subTemplates) {
					codes.add(new TransientCoding(CodingSystem.ELEXIS_LOCAL_CODESYSTEM.getSystem(),
						findingsTemplate.getTitle() + "." + subFindingsTemplate.getTitle(),
						subFindingsTemplate.getTitle()));
				}
			} else {
				codes.add(new TransientCoding(CodingSystem.ELEXIS_LOCAL_CODESYSTEM.getSystem(),
					findingsTemplate.getTitle(), findingsTemplate.getTitle()));
			}
		}
		for (ICoding iCoding : codes) {
			codesStrings.add(iCoding.getCode());
		}
		codesStrings.sort(new Comparator<String>() {
			@Override
			public int compare(String arg0, String arg1){
				return arg0.compareTo(arg1);
			}
		});
		ret.put("", 0);
		for (int i = 0; i < codesStrings.size(); i++) {
			ret.put(codesStrings.get(i), new Integer(i + 1));
		}
		return ret;
	}
	
	@Override
	public boolean performOk(){
		MesswertUtil.saveMappings(localMappings);
		return super.performOk();
	}
}