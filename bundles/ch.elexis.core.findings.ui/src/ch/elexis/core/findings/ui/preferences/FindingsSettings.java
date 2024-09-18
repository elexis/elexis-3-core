package ch.elexis.core.findings.ui.preferences;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.admin.Messages;
import ch.elexis.core.findings.IAllergyIntolerance;
import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.ICondition.ConditionCategory;
import ch.elexis.core.findings.IFamilyMemberHistory;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.IFindingsService;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.IObservation.ObservationCategory;
import ch.elexis.core.findings.IObservation.ObservationCode;
import ch.elexis.core.findings.migration.IMigratorService;
import ch.elexis.core.findings.ui.services.FindingsServiceComponent;
import ch.elexis.core.findings.ui.services.MigratorServiceComponent;
import ch.elexis.core.findings.util.model.TransientCoding;
import ch.elexis.core.ui.preferences.ConfigServicePreferenceStore;
import ch.elexis.core.ui.preferences.ConfigServicePreferenceStore.Scope;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;

public class FindingsSettings extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public static final String ROWSAREDATES = "findingsui/settings/rowsaredates";

	private BooleanFieldEditor diagStructFieldEditor;

	private BooleanFieldEditor persAnamneseStructFieldEditor;

	private BooleanFieldEditor riskFactorStructFieldEditor;

	private BooleanFieldEditor famAnamneseStructFieldEditor;

	private BooleanFieldEditor allergyIntoleranceStructFieldEditor;

	private BooleanFieldEditor diagnoseExportWordFormatEditor;

	private BooleanFieldEditor rowsAreDatesFieldEditor;

	public FindingsSettings() {
		super(GRID);
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(new ConfigServicePreferenceStore(Scope.GLOBAL));
		setMessage("Globale Befunde Einstellungen");
		// initialize the model
		if (FindingsServiceComponent.getService() != null) {
			FindingsServiceComponent.getService().findById(StringUtils.EMPTY, IObservation.class);
		} else {
			getLogger().warn("FindingsService is null - not found.");
			setErrorMessage("Befunde Service konnte nicht geladen werden.");
		}
	}

	@Override
	protected void createFieldEditors() {
		Label label = new Label(getFieldEditorParent(), SWT.NONE);
		label.setText("Anzeige Einstellungen");
		label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));

		rowsAreDatesFieldEditor = new BooleanFieldEditor(ROWSAREDATES,
				"Datum in Zeilen anzeigen (bei Änderung View neu öffnen)", getFieldEditorParent());
		addField(rowsAreDatesFieldEditor);

		label = new Label(getFieldEditorParent(), SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));

		label = new Label(getFieldEditorParent(), SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));

		label = new Label(getFieldEditorParent(), SWT.NONE);
		label.setText("Daten konvertieren und strukturierte Anzeige verwenden.");
		label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));

		diagStructFieldEditor = new BooleanFieldEditor(IMigratorService.DIAGNOSE_SETTINGS_USE_STRUCTURED,
				"Diagnosen strukturiert anzeigen", getFieldEditorParent());
		addField(diagStructFieldEditor);

		persAnamneseStructFieldEditor = new BooleanFieldEditor(IMigratorService.PERSANAM_SETTINGS_USE_STRUCTURED,
				"Persönliche Anamnese strukturiert anzeigen", getFieldEditorParent());
		addField(persAnamneseStructFieldEditor);

		riskFactorStructFieldEditor = new BooleanFieldEditor(IMigratorService.RISKFACTOR_SETTINGS_USE_STRUCTURED,
				"Risiken strukturiert anzeigen", getFieldEditorParent());
		addField(riskFactorStructFieldEditor);

		famAnamneseStructFieldEditor = new BooleanFieldEditor(IMigratorService.FAMANAM_SETTINGS_USE_STRUCTURED,
				"Familien Anamnese strukturiert anzeigen", getFieldEditorParent());
		addField(famAnamneseStructFieldEditor);

		allergyIntoleranceStructFieldEditor = new BooleanFieldEditor(
				IMigratorService.ALLERGYINTOLERANCE_SETTINGS_USE_STRUCTURED, "Allergien strukturiert anzeigen",
				getFieldEditorParent());
		addField(allergyIntoleranceStructFieldEditor);

		diagnoseExportWordFormatEditor = new BooleanFieldEditor(IMigratorService.DIAGNOSE_EXPORT_WORD_FORMAT,
				"Diagnosen im Word formatieren", getFieldEditorParent());
		addField(diagnoseExportWordFormatEditor);

		getControl().setEnabled(FindingsServiceComponent.getService() != null);

	}

	private Logger getLogger() {
		return LoggerFactory.getLogger(FindingsSettings.class);
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);
		if (event != null) {
			if (event.getSource() == diagStructFieldEditor) {
				diagPropertyChange(event);
			} else if (event.getSource() == persAnamneseStructFieldEditor) {
				persAnamnesePropertyChange(event);
			} else if (event.getSource() == riskFactorStructFieldEditor) {
				riskFactorPropertyChange(event);
			} else if (event.getSource() == famAnamneseStructFieldEditor) {
				famAnamnesePropertyChange(event);
			} else if (event.getSource() == allergyIntoleranceStructFieldEditor) {
				allergyIntolerancePropertyChange(event);
			}
		}
	}

	private void diagPropertyChange(PropertyChangeEvent event) {
		if (event.getNewValue().equals(Boolean.TRUE)) {
			if (MessageDialog.openConfirm(getShell(), "Strukturierte Diagnosen",
					"Bisher erfasste Text Diagnosen werden automatisch in strukturierte umgewandelt.\n"
							+ "Wollen Sie wirklich von nun an strukturierte Diagnosen verwenden?")) {
				ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(getShell());
				try {
					progressDialog.run(true, true, new IRunnableWithProgress() {
						public void run(IProgressMonitor monitor)
								throws InvocationTargetException, InterruptedException {
							Query<Patient> query = new Query<>(Patient.class);
							List<Patient> patients = query.execute();
							monitor.beginTask("Strukturierte Diagnosen erzeugen", patients.size());
							IFindingsService findingsService = FindingsServiceComponent.getService();
							IMigratorService migratorService = MigratorServiceComponent.getService();
							for (Patient patient : patients) {
								String diagnosen = patient.getDiagnosen();
								List<IFinding> existing = getExistingDiagnoses(patient.getId(), findingsService);
								// only migrate if there is a diagnosis and no structured diagnosis already
								// there
								if (diagnosen != null && !diagnosen.isEmpty() && existing.isEmpty()) {
									migratorService.migratePatientsFindings(patient.getId(), ICondition.class, null);
								}
								monitor.worked(1);
								if (monitor.isCanceled()) {
									break;
								}
							}
							monitor.done();
							Display.getDefault().asyncExec(new Runnable() {
								@Override
								public void run() {
									MessageDialog.openInformation(getShell(), "Strukturierte Diagnosen",
											"Strukturierte Diagnosen erfolgreich erzeugt. Bitte starten sie Elexis neu um mit den strukturierten Diagnosen zu arbeiten.");
								}
							});
						}

						private List<IFinding> getExistingDiagnoses(String patientId,
								IFindingsService findingsService) {
							return findingsService.getPatientsFindings(patientId, ICondition.class).stream()
									.filter(condition -> ((ICondition) condition)
											.getCategory() == ConditionCategory.PROBLEMLISTITEM)
									.collect(Collectors.toList());
						};
					});
				} catch (InvocationTargetException | InterruptedException e) {
					MessageDialog.openError(getShell(), "Diagnosen konvertieren",
							"Fehler beim erzeugen der strukturierten Diagnosen.");
					getLogger().error("Error creating structured diagnosis", e);
				}
			} else {
				getPreferenceStore().setValue(IMigratorService.DIAGNOSE_SETTINGS_USE_STRUCTURED, false);
				// refresh later, on immediate refresh wasSelected of FieldEditor gets
				// overwritten
				getShell().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						diagStructFieldEditor.load();
					}
				});
			}
		} else {
			if (MessageDialog.openConfirm(getShell(), "Strukturierte Diagnosen",
					"Bisher erfasste strukturierte Diagnosen werden nicht in Text umgewandelt.\n"
							+ "Wollen Sie wirklich von nun an Text Diagnosen verwenden?")) {
				MessageDialog.openInformation(getShell(), "Text Diagnosen",
						"Bitte starten sie Elexis neu um mit den Text Diagnosen zu arbeiten.");
			} else {
				getPreferenceStore().setValue(IMigratorService.DIAGNOSE_SETTINGS_USE_STRUCTURED, true);
				// refresh later, on immediate refresh wasSelected of FieldEditor gets
				// overwritten
				getShell().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						diagStructFieldEditor.load();
					}
				});
			}
		}
	}

	private void persAnamnesePropertyChange(PropertyChangeEvent event) {
		if (event.getNewValue().equals(Boolean.TRUE)) {
			if (MessageDialog.openConfirm(getShell(), "Strukturierte Persönliche Anamnese",
					"Bisher erfasste Persönliche Anamnese Einträge werden automatisch in strukturierte umgewandelt.\n"
							+ "Wollen Sie wirklich von nun an strukturierte Persönliche Anamnese Einträge verwenden?")) {
				ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(getShell());
				try {
					progressDialog.run(true, true, new IRunnableWithProgress() {
						public void run(IProgressMonitor monitor)
								throws InvocationTargetException, InterruptedException {
							Query<Patient> query = new Query<>(Patient.class);
							List<Patient> patients = query.execute();
							monitor.beginTask("Strukturierte Persönliche Anamnese erzeugen", patients.size());
							IFindingsService findingsService = FindingsServiceComponent.getService();
							IMigratorService migratorService = MigratorServiceComponent.getService();
							for (Patient patient : patients) {
								String persAnamesis = patient.getPersAnamnese();
								List<IFinding> existing = getExistingPersAnamnese(patient.getId(), findingsService);
								// only migrate if there is a pers anamnesis and no structured pers anamnesis
								// already there
								if (persAnamesis != null && !persAnamesis.isEmpty() && existing.isEmpty()) {
									migratorService.migratePatientsFindings(patient.getId(), IObservation.class,
											new TransientCoding(ObservationCode.ANAM_PERSONAL));
								}
								monitor.worked(1);
								if (monitor.isCanceled()) {
									break;
								}
							}
							monitor.done();
							Display.getDefault().asyncExec(new Runnable() {
								@Override
								public void run() {
									MessageDialog.openInformation(getShell(), "Strukturierte Persönliche Anamnese",
											"Strukturierte Persönliche Anamnese erfolgreich erzeugt. Bitte starten sie Elexis neu um mit den strukturierten Persönliche Anamnese Einträgen zu arbeiten.");
								}
							});
						}

						private List<IFinding> getExistingPersAnamnese(String patientId,
								IFindingsService findingsService) {
							return findingsService.getPatientsFindings(patientId, IObservation.class).stream()
									.filter(oberservation -> {
										if (((IObservation) oberservation)
												.getCategory() == ObservationCategory.SOCIALHISTORY) {
											for (ICoding code : ((IObservation) oberservation).getCoding()) {
												if (ObservationCode.ANAM_PERSONAL.isSame(code)) {
													return true;
												}
											}
										}
										return false;
									}).collect(Collectors.toList());
						};
					});
				} catch (InvocationTargetException | InterruptedException e) {
					MessageDialog.openError(getShell(), "Persönliche Anamnese konvertieren",
							"Fehler beim erzeugen der strukturierten Persönliche Anamnese Einträgen.");
					getLogger().error("Error creating structured anamnesis personally", e);
				}
			} else {
				getPreferenceStore().setValue(IMigratorService.PERSANAM_SETTINGS_USE_STRUCTURED, false);
				// refresh later, on immediate refresh wasSelected of FieldEditor gets
				// overwritten
				getShell().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						persAnamneseStructFieldEditor.load();
					}
				});
			}
		} else {
			if (MessageDialog.openConfirm(getShell(), "Strukturierte Persönliche Anamnese",
					"Bisher erfasste strukturierte Persönliche Anamnese werden nicht in Text umgewandelt.\n"
							+ "Wollen Sie wirklich von nun an Text Persönliche Anamnese verwenden?")) {
				MessageDialog.openInformation(getShell(), "Text Persönliche Anamnese",
						"Bitte starten sie Elexis neu um mit den Text Persönliche Anamnese zu arbeiten.");
			} else {
				getPreferenceStore().setValue(IMigratorService.PERSANAM_SETTINGS_USE_STRUCTURED, true);
				// refresh later, on immediate refresh wasSelected of FieldEditor gets
				// overwritten
				getShell().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						persAnamneseStructFieldEditor.load();
					}
				});
			}
		}
	}

	private void riskFactorPropertyChange(PropertyChangeEvent event) {
		if (event.getNewValue().equals(Boolean.TRUE)) {
			if (MessageDialog.openConfirm(getShell(), "Strukturierte Risiken",
					"Bisher erfasste Risiken werden automatisch in strukturierte umgewandelt.\n"
							+ "Wollen Sie wirklich von nun an strukturierte Risiken verwenden?")) {
				ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(getShell());
				try {
					progressDialog.run(true, true, new IRunnableWithProgress() {
						public void run(IProgressMonitor monitor)
								throws InvocationTargetException, InterruptedException {
							Query<Patient> query = new Query<>(Patient.class);
							List<Patient> patients = query.execute();
							monitor.beginTask("Strukturierte Risiken erzeugen", patients.size());
							IFindingsService findingsService = FindingsServiceComponent.getService();
							IMigratorService migratorService = MigratorServiceComponent.getService();
							for (Patient patient : patients) {
								String risk = patient.getRisk();
								List<IFinding> existing = getExistingRiskfactors(patient.getId(), findingsService);
								// only migrate if there is a risk factor and no structured risk factor already
								// there
								if (risk != null && !risk.isEmpty() && existing.isEmpty()) {
									migratorService.migratePatientsFindings(patient.getId(), IObservation.class,
											new TransientCoding(ObservationCode.ANAM_RISK));
								}
								monitor.worked(1);
								if (monitor.isCanceled()) {
									break;
								}
							}
							monitor.done();
							Display.getDefault().asyncExec(new Runnable() {
								@Override
								public void run() {
									MessageDialog.openInformation(getShell(), "Strukturierte Risiken",
											"Strukturierte Risiken erfolgreich erzeugt. Bitte starten sie Elexis neu um mit den strukturierten Risiken zu arbeiten.");
								}
							});
						}

						private List<IFinding> getExistingRiskfactors(String patientId,
								IFindingsService findingsService) {
							return findingsService.getPatientsFindings(patientId, IObservation.class).stream()
									.filter(oberservation -> {
										if (((IObservation) oberservation)
												.getCategory() == ObservationCategory.SOCIALHISTORY) {
											for (ICoding code : ((IObservation) oberservation).getCoding()) {
												if (ObservationCode.ANAM_RISK.isSame(code)) {
													return true;
												}
											}
										}
										return false;
									}).collect(Collectors.toList());
						};
					});
				} catch (InvocationTargetException | InterruptedException e) {
					MessageDialog.openError(getShell(), "Risiken konvertieren",
							"Fehler beim erzeugen der strukturierten Risiken Einträgen.");
					getLogger().error("Error creating structured risk factors", e);
				}
			} else {
				getPreferenceStore().setValue(IMigratorService.RISKFACTOR_SETTINGS_USE_STRUCTURED, false);
				// refresh later, on immediate refresh wasSelected of FieldEditor gets
				// overwritten
				getShell().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						riskFactorStructFieldEditor.load();
					}
				});
			}
		} else {
			if (MessageDialog.openConfirm(getShell(), "Strukturierte Risiken",
					"Bisher erfasste strukturierte Risiken werden nicht in Text umgewandelt.\n"
							+ "Wollen Sie wirklich von nun an Text Risiken verwenden?")) {
				MessageDialog.openInformation(getShell(), "Text Risiken",
						"Bitte starten sie Elexis neu um mit den Text Risiken zu arbeiten.");
			} else {
				getPreferenceStore().setValue(IMigratorService.RISKFACTOR_SETTINGS_USE_STRUCTURED, true);
				// refresh later, on immediate refresh wasSelected of FieldEditor gets
				// overwritten
				getShell().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						riskFactorStructFieldEditor.load();
					}
				});
			}
		}
	}

	private void famAnamnesePropertyChange(PropertyChangeEvent event) {
		if (event.getNewValue().equals(Boolean.TRUE)) {
			if (MessageDialog.openConfirm(getShell(), "Strukturierte Familien Anamnese",
					"Bisher erfasste Familien Anamnese Einträge werden automatisch in strukturierte umgewandelt.\n"
							+ "Wollen Sie wirklich von nun an strukturierte Familien Anamnese Einträge verwenden?")) {
				ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(getShell());
				try {
					progressDialog.run(true, true, new IRunnableWithProgress() {
						public void run(IProgressMonitor monitor)
								throws InvocationTargetException, InterruptedException {
							Query<Patient> query = new Query<>(Patient.class);
							List<Patient> patients = query.execute();
							monitor.beginTask("Strukturierte Familien Anamnese erzeugen", patients.size());
							IFindingsService findingsService = FindingsServiceComponent.getService();
							IMigratorService migratorService = MigratorServiceComponent.getService();
							for (Patient patient : patients) {
								String famAnamesis = patient.getFamilyAnamnese();
								List<IFamilyMemberHistory> existing = getExistingFamAnamnese(patient.getId(),
										findingsService);
								// only migrate if there is a fam anamnesis and no structured fam anamnesis
								// already there
								if (famAnamesis != null && !famAnamesis.isEmpty() && existing.isEmpty()) {
									migratorService.migratePatientsFindings(patient.getId(), IFamilyMemberHistory.class,
											null);
								}
								monitor.worked(1);
								if (monitor.isCanceled()) {
									break;
								}
							}
							monitor.done();
							Display.getDefault().asyncExec(new Runnable() {
								@Override
								public void run() {
									MessageDialog.openInformation(getShell(), "Strukturierte Familien Anamnese",
											"Strukturierte Familien Anamnese erfolgreich erzeugt. Bitte starten sie Elexis neu um mit den strukturierten Familien Anamnese Einträgen zu arbeiten.");
								}
							});
						}

						private List<IFamilyMemberHistory> getExistingFamAnamnese(String patientId,
								IFindingsService findingsService) {
							return findingsService.getPatientsFindings(patientId, IFamilyMemberHistory.class);
						};
					});
				} catch (InvocationTargetException | InterruptedException e) {
					MessageDialog.openError(getShell(), "Familien Anamnese konvertieren",
							"Fehler beim erzeugen der strukturierten Familien Anamnese Einträgen.");
					getLogger().error("Error creating structured anamnesis family", e);
				}
			} else {
				getPreferenceStore().setValue(IMigratorService.FAMANAM_SETTINGS_USE_STRUCTURED, false);
				// refresh later, on immediate refresh wasSelected of FieldEditor gets
				// overwritten
				getShell().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						famAnamneseStructFieldEditor.load();
					}
				});
			}
		} else {
			if (MessageDialog.openConfirm(getShell(), "Strukturierte Familien Anamnese",
					"Bisher erfasste strukturierte Familien Anamnese werden nicht in Text umgewandelt.\n"
							+ "Wollen Sie wirklich von nun an Text Familien Anamnese verwenden?")) {
				MessageDialog.openInformation(getShell(), "Text Familien Anamnese",
						"Bitte starten sie Elexis neu um mit den Text Familien Anamnese zu arbeiten.");
			} else {
				getPreferenceStore().setValue(IMigratorService.FAMANAM_SETTINGS_USE_STRUCTURED, true);
				// refresh later, on immediate refresh wasSelected of FieldEditor gets
				// overwritten
				getShell().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						famAnamneseStructFieldEditor.load();
					}
				});
			}
		}
	}

	private void allergyIntolerancePropertyChange(PropertyChangeEvent event) {
		if (event.getNewValue().equals(Boolean.TRUE)) {
			if (MessageDialog.openConfirm(getShell(), "Strukturierte Allergien",
					"Bisher erfasste Allergien Einträge werden automatisch in strukturierte umgewandelt.\n"
							+ "Wollen Sie wirklich von nun an strukturierte Allergien Einträge verwenden?")) {
				ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(getShell());
				try {
					progressDialog.run(true, true, new IRunnableWithProgress() {
						public void run(IProgressMonitor monitor)
								throws InvocationTargetException, InterruptedException {
							Query<Patient> query = new Query<>(Patient.class);
							List<Patient> patients = query.execute();
							monitor.beginTask("Strukturierte Allergien erzeugen", patients.size());
							IFindingsService findingsService = FindingsServiceComponent.getService();
							IMigratorService migratorService = MigratorServiceComponent.getService();
							for (Patient patient : patients) {
								String allergies = patient.getAllergies();
								List<IAllergyIntolerance> existing = getExistingAllergyIntolerance(patient.getId(),
										findingsService);
								// only migrate if there is allergies and no structured allergies already there
								if (allergies != null && !allergies.isEmpty() && existing.isEmpty()) {
									migratorService.migratePatientsFindings(patient.getId(), IAllergyIntolerance.class,
											null);
								}
								monitor.worked(1);
								if (monitor.isCanceled()) {
									break;
								}
							}
							monitor.done();
							Display.getDefault().asyncExec(new Runnable() {
								@Override
								public void run() {
									MessageDialog.openInformation(getShell(), "Strukturierte Allergien",
											"Strukturierte Allergien erfolgreich erzeugt. Bitte starten sie Elexis neu um mit den strukturierten Allergien Einträgen zu arbeiten.");
								}
							});
						}

						private List<IAllergyIntolerance> getExistingAllergyIntolerance(String patientId,
								IFindingsService findingsService) {
							return findingsService.getPatientsFindings(patientId, IAllergyIntolerance.class);
						};
					});
				} catch (InvocationTargetException | InterruptedException e) {
					MessageDialog.openError(getShell(), "Allergien konvertieren",
							"Fehler beim erzeugen der strukturierten Allergien Einträgen.");
					getLogger().error("Error creating structured allergy intolerances", e);
				}
			} else {
				getPreferenceStore().setValue(IMigratorService.ALLERGYINTOLERANCE_SETTINGS_USE_STRUCTURED, false);
				// refresh later, on immediate refresh wasSelected of FieldEditor gets
				// overwritten
				getShell().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						allergyIntoleranceStructFieldEditor.load();
					}
				});
			}
		} else {
			if (MessageDialog.openConfirm(getShell(), "Strukturierte Allergien",
					"Bisher erfasste strukturierte Allergien werden nicht in Text umgewandelt.\n"
							+ "Wollen Sie wirklich von nun an Text Allergien verwenden?")) {
				MessageDialog.openInformation(getShell(), "Text Allergien",
						"Bitte starten sie Elexis neu um mit den Text Allergien zu arbeiten.");
			} else {
				getPreferenceStore().setValue(IMigratorService.ALLERGYINTOLERANCE_SETTINGS_USE_STRUCTURED, true);
				// refresh later, on immediate refresh wasSelected of FieldEditor gets
				// overwritten
				getShell().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						allergyIntoleranceStructFieldEditor.load();
					}
				});
			}
		}
	}
}
