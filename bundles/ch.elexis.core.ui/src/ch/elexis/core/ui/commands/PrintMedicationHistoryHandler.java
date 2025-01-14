package ch.elexis.core.ui.commands;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.services.IFormattedOutput;
import ch.elexis.core.services.IFormattedOutputFactory;
import ch.elexis.core.services.IFormattedOutputFactory.ObjectType;
import ch.elexis.core.services.IFormattedOutputFactory.OutputType;
import ch.elexis.data.Patient;
import ch.elexis.data.Prescription;
import ch.elexis.data.Query;
import ch.rgw.tools.TimeTool;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

public class PrintMedicationHistoryHandler extends AbstractHandler implements IHandler {

	private static final String TOOPEN = " ... "; //$NON-NLS-1$

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ProgressMonitorDialog progress = new ProgressMonitorDialog(HandlerUtil.getActiveShell(event));
		try {
			progress.run(true, false, new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("PDF erzeugen", IProgressMonitor.UNKNOWN);
					Optional<MedicationHistoryLetter> letter = getToPrint();
					if (letter.isPresent()) {
						BundleContext bundleContext = FrameworkUtil.getBundle(getClass()).getBundleContext();
						ServiceReference<IFormattedOutputFactory> serviceRef = bundleContext
								.getServiceReference(IFormattedOutputFactory.class);
						if (serviceRef != null) {
							IFormattedOutputFactory service = bundleContext.getService(serviceRef);
							IFormattedOutput outputter = service.getFormattedOutputImplementation(ObjectType.JAXB,
									OutputType.PDF);
							ByteArrayOutputStream pdf = new ByteArrayOutputStream();
							Map<String, String> parameters = new HashMap<>();
							parameters.put("current-date", //$NON-NLS-1$
									LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))); //$NON-NLS-1$

							outputter.transform(letter.get(),
									getClass().getResourceAsStream("/rsc/xslt/medhistory2fo.xslt"), pdf, parameters); //$NON-NLS-1$
							bundleContext.ungetService(serviceRef);
							// save and open the file ...
							File file = null;
							FileOutputStream fout = null;
							try {
								file = File.createTempFile("medhistory_", ".pdf"); //$NON-NLS-1$ //$NON-NLS-2$
								fout = new FileOutputStream(file);
								fout.write(pdf.toByteArray());
							} catch (IOException e) {
								Display.getDefault().syncExec(() -> {
									MessageDialog.openError(HandlerUtil.getActiveShell(event), "Fehler",
											"Fehler beim PDF anlegen.\n" + e.getMessage());
								});
								LoggerFactory.getLogger(getClass()).error("Error creating PDF", e); //$NON-NLS-1$
							} finally {
								if (fout != null) {
									try {
										fout.close();
									} catch (IOException e) {
										// ignore
									}
								}
							}
							if (file != null) {
								Program.launch(file.getAbsolutePath());
							}
						}
					} else {
						Display.getDefault().syncExec(() -> {
							MessageDialog.openInformation(HandlerUtil.getActiveShell(event), "Info",
									"Kein Patient ausgewählt, oder Patient hat keine Medikation.\n");
						});
					}
					monitor.done();
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			MessageDialog.openError(HandlerUtil.getActiveShell(event), "Fehler",
					"Fehler beim PDF erzeugen.\n" + e.getMessage());
			LoggerFactory.getLogger(getClass()).error("Error creating PDF", e); //$NON-NLS-1$
		}
		return null;
	}

	@Override
	public boolean isEnabled() {
		return isFopServiceAvailable();
	}

	private boolean isFopServiceAvailable() {
		BundleContext bundleContext = FrameworkUtil.getBundle(getClass()).getBundleContext();
		return bundleContext.getServiceReference(IFormattedOutputFactory.class) != null;
	}

	public Optional<MedicationHistoryLetter> getToPrint() {
		Patient patient = ElexisEventDispatcher.getSelectedPatient();
		if (patient != null) {
			Query<Prescription> qbe = new Query<>(Prescription.class);
			qbe.add(Prescription.FLD_PATIENT_ID, Query.EQUALS, patient.getId());
			List<Prescription> list = qbe.execute();
			if (!list.isEmpty()) {
				return Optional.of(MedicationHistoryLetter.of(patient, list));
			}
		}
		return Optional.empty();
	}

	@XmlRootElement(name = "medicationhistory")
	private static class MedicationHistoryLetter {

		private List<MedicationHistoryItem> history;
		@XmlElement
		private String patientName;
		@XmlElement
		private String patientDob;

		private MedicationHistoryLetter() {
			// needed for jaxb
		}

		private MedicationHistoryLetter(Patient patient) {
			patientName = patient.getLabel(true);
			patientDob = patient.getGeburtsdatum();

			history = new ArrayList<>();
		}

		public void setHistory(List<MedicationHistoryItem> history) {
			this.history = history;
		}

		public List<MedicationHistoryItem> getHistory() {
			return history;
		}

		public static MedicationHistoryLetter of(Patient patient, List<Prescription> list) {
			MedicationHistoryLetter ret = new MedicationHistoryLetter(patient);

			for (Prescription prescription : list) {
				if (prescription.getEntryType() != EntryType.RECIPE) {
					Map<TimeTool, String> terms = prescription.getTerms();
					TimeTool[] tts = terms.keySet().toArray(new TimeTool[0]);
					for (int i = 0; i < tts.length - 1; i++) {
						if (i < tts.length - 1) {
							ret.history.add(new MedicationHistoryItem(tts[i].toString(TimeTool.DATE_GER),
									tts[i + 1].toString(TimeTool.DATE_GER), prescription));
						} else {
							ret.history.add(new MedicationHistoryItem(tts[i].toString(TimeTool.DATE_GER), TOOPEN,
									prescription));
						}
					}
					ret.history.add(new MedicationHistoryItem(tts[tts.length - 1].toString(TimeTool.DATE_GER), TOOPEN,
							prescription));
				}
			}
			Collections.sort(ret.history);

			return ret;
		}
	}

	@XmlRootElement(name = "historyitem")
	private static class MedicationHistoryItem implements Comparable<MedicationHistoryItem> {

		@XmlTransient
		private TimeTool fromTool;

		@XmlElement
		private String from;
		@XmlElement
		private String to;
		@XmlElement
		private String article;
		@XmlElement
		private String dosage;

		private MedicationHistoryItem() {
			// needed for jaxb
		}

		public MedicationHistoryItem(final String from, final String to, final Prescription p) {
			this.from = from;
			this.fromTool = new TimeTool(from);
			this.to = to;
			if (TOOPEN.equals(to) && StringUtils.isNotBlank(p.getEndDate())) {
				this.to = p.getEndDate();
			}
			this.article = p.getArtikel() != null ? p.getArtikel().getLabel() : "?"; //$NON-NLS-1$
			this.dosage = p.getDosis();
		}

		@Override
		public int compareTo(MedicationHistoryItem other) {
			return other.fromTool.compareTo(fromTool);
		}
	}
}
