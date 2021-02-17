package ch.elexis.core.spotlight.ui.controls.detail;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ch.elexis.core.model.IAccountTransaction;
import ch.elexis.core.model.IAppointment;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.ILabResult;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPrescription;
import ch.elexis.core.model.ISticker;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.services.IEncounterService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.INamedQuery;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.IQuery.ORDER;
import ch.elexis.core.services.IStickerService;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.spotlight.ISpotlightResultEntry;
import ch.elexis.core.spotlight.ISpotlightResultEntry.Category;
import ch.elexis.core.spotlight.ui.ISpotlightResultEntryDetailComposite;
import ch.elexis.core.spotlight.ui.controls.AbstractSpotlightResultEntryDetailComposite;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.rgw.tools.Money;

public class PatientDetailComposite extends AbstractSpotlightResultEntryDetailComposite
		implements ISpotlightResultEntryDetailComposite {
	
	@Inject
	private IStickerService stickerService;
	@Inject
	private IEncounterService encounterService;
	
	private static final String PATIENT_LABEL_FONT = "patient-label-font";
	
	private IModelService coreModelService;
	private PatientDetailCompositeUtil util;
	
	private Label lblPatientlabel;
	private Composite stickerComposite;
	private Label lblStammarzt;
	private Label lblInsuranceKVG;
	private Composite appointmentComposite;
	private Label lblAppointments;
	private Label lblLastEncounter;
	private Composite encounterComposite;
	private Label lblLastEncounterText;
	private Label lblPendenzen;
	private Label lblBalance;
	private Label lblFixedMedication;
	private Composite fixedMedicationComposite;
	private Label lblLastLaboratory;
	private Composite lastLaboratoryComposite;
	
	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	public PatientDetailComposite(Composite parent, int style){
		super(parent, style);
		GridLayout gridLayout = new GridLayout(4, true);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		setLayout(gridLayout);
		
		util = new PatientDetailCompositeUtil();
		coreModelService = CoreModelServiceHolder.get();
		
		// Patient Stickers
		stickerComposite = new Composite(this, SWT.NONE);
		FillLayout fl_stickerComposite = new FillLayout(SWT.HORIZONTAL);
		fl_stickerComposite.spacing = 5;
		stickerComposite.setLayout(fl_stickerComposite);
		GridData gd_stickerComposite = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
		gd_stickerComposite.heightHint = 16;
		stickerComposite.setLayoutData(gd_stickerComposite);
		
		Composite pendenzenComposite = new Composite(this, SWT.NONE);
		pendenzenComposite.setLayout(new FillLayout(SWT.HORIZONTAL));
		pendenzenComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		
		lblPendenzen = new Label(pendenzenComposite, SWT.NONE);
		
		// Patient Label
		Composite patientLabelComposite = new Composite(this, SWT.NONE);
		patientLabelComposite.setLayout(new FillLayout(SWT.VERTICAL));
		patientLabelComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		
		lblPatientlabel = new Label(patientLabelComposite, SWT.NONE);
		Font patientLabelFont;
		if (JFaceResources.getFontRegistry().hasValueFor(PATIENT_LABEL_FONT)) {
			patientLabelFont = JFaceResources.getFontRegistry().get(PATIENT_LABEL_FONT);
		} else {
			FontData[] fontData = lblPatientlabel.getFont().getFontData();
			fontData[0].setHeight(fontData[0].getHeight() + 2);
			JFaceResources.getFontRegistry().put(PATIENT_LABEL_FONT, fontData);
			patientLabelFont = JFaceResources.getFontRegistry().getBold(PATIENT_LABEL_FONT);
		}
		lblPatientlabel.setFont(patientLabelFont);
		new Label(patientLabelComposite, SWT.NONE);
		lblStammarzt = new Label(patientLabelComposite, SWT.NONE);
		lblInsuranceKVG = new Label(patientLabelComposite, SWT.NONE);
		
		Composite balanceComposite = new Composite(this, SWT.NONE);
		balanceComposite.setLayout(new FillLayout(SWT.HORIZONTAL));
		balanceComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
		
		lblBalance = new Label(balanceComposite, SWT.NONE);
		lblBalance.setAlignment(SWT.RIGHT);
		
		appointmentComposite = new Composite(this, SWT.NONE);
		appointmentComposite.setLayout(new FillLayout(SWT.VERTICAL));
		GridData gd_appointmentComposite = new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1);
		gd_appointmentComposite.heightHint = 60;
		appointmentComposite.setLayoutData(gd_appointmentComposite);
		
		lblAppointments = new Label(appointmentComposite, SWT.NONE);
		Font boldDefaultFont = JFaceResources.getFontRegistry().getBold("default");
		lblAppointments.setText("Termine");
		lblAppointments.setFont(boldDefaultFont);
		
		encounterComposite = new Composite(this, SWT.NONE);
		GridLayout gl_encounterComposite = new GridLayout(1, false);
		gl_encounterComposite.marginHeight = 0;
		gl_encounterComposite.marginWidth = 0;
		encounterComposite.setLayout(gl_encounterComposite);
		encounterComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 4, 1));
		
		lblLastEncounter = new Label(encounterComposite, SWT.NONE);
		lblLastEncounter.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		lblLastEncounter.setText("Letzte Konsultation");
		lblLastEncounter.setFont(boldDefaultFont);
		
		lblLastEncounterText = new Label(encounterComposite, SWT.WRAP | SWT.LEFT);
		GridData gd_lblLastEncounterText = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		gd_lblLastEncounterText.heightHint = 60;
		lblLastEncounterText.setLayoutData(gd_lblLastEncounterText);
		
		fixedMedicationComposite = new Composite(this, SWT.NONE);
		fixedMedicationComposite.setLayout(new FillLayout(SWT.VERTICAL));
		fixedMedicationComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		
		lblFixedMedication = new Label(fixedMedicationComposite, SWT.NONE);
		lblFixedMedication.setText("Fixmedikation");
		lblFixedMedication.setFont(boldDefaultFont);
		
		lastLaboratoryComposite = new Composite(this, SWT.NONE);
		lastLaboratoryComposite.setLayout(new FillLayout(SWT.VERTICAL));
		lastLaboratoryComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		
		lblLastLaboratory = new Label(lastLaboratoryComposite, SWT.NONE);
		lblLastLaboratory.setText("Letztes Labor");
		lblLastLaboratory.setFont(boldDefaultFont);
		
	}
	
	@Override
	public boolean setFocus(){
		return lblPatientlabel.setFocus();
	}
	
	@Override
	protected void checkSubclass(){
		// Disable the check that prevents subclassing of SWT components
	}
	
	@Override
	public void setSpotlightEntry(ISpotlightResultEntry resultEntry){
		
		IPatient patient = null;
		if (resultEntry != null) {
			Optional<Object> object = resultEntry.getObject();
			if (!object.isPresent()) {
				String patientId = resultEntry.getLoaderString();
				patient = coreModelService.load(patientId, IPatient.class).orElse(null);
			} else {
				patient = (IPatient) object.get();
			}
		}
		
		clearPopulateStickerComposite(patient);
		clearPopulatePatientLabelComposite(patient);
		clearUpdateBalance(patient);
		clearPopulateAppointmentComposite(patient);
		clearPopulateLastEncounterComposite(patient);
		clearPopulateFixedMedicationComposite(patient);
		clearUpdateLastLaboratoryComposite(patient);
		
		layout(true);
		
	}
	
	private void clearUpdateBalance(IPatient patient){
		if (patient != null) {
			INamedQuery<Number> namedQuery = coreModelService.getNamedQuery(Number.class,
				IAccountTransaction.class, true, "balance.patient");
			List<Number> balanceResult =
				namedQuery.executeWithParameters(namedQuery.getParameterMap("patient", patient));
			if (!balanceResult.isEmpty()) {
				int balance = balanceResult.get(0).intValue();
				lblBalance.setText("CHF " + new Money(balance));
				return;
			}
		}
		lblBalance.setText("");
	}
	
	private void clearPopulateLastEncounterComposite(IPatient patient){
		
		if (patient != null) {
			IEncounter lastEncounter = encounterService.getLatestEncounter(patient).orElse(null);
			if (lastEncounter != null) {
				lblLastEncounter
					.setText("Letzte Konsultation " + util.formatDate(lastEncounter.getDate()));
				String encounterText = lastEncounter.getHeadVersionInPlaintext();
				lblLastEncounterText.setText(StringUtils.abbreviate(encounterText, 200));
			} else {
				lblLastEncounter.setText("Letzte Konsultation");
				lblLastEncounterText.setText("-");
			}
		} else {
			lblLastEncounter.setText("");
			lblLastEncounterText.setText("");
		}
		
		encounterComposite.layout();
	}
	
	private void clearPopulateAppointmentComposite(IPatient patient){
		util.clearComposite(appointmentComposite, lblAppointments);
		
		if (patient != null) {
			IQuery<IAppointment> futureAppointmentsQuery =
				coreModelService.getQuery(IAppointment.class);
			futureAppointmentsQuery.and(ModelPackage.Literals.IAPPOINTMENT__SUBJECT_OR_PATIENT,
				COMPARATOR.EQUALS, patient.getId());
			futureAppointmentsQuery.and("tag", COMPARATOR.GREATER, LocalDate.now());
			futureAppointmentsQuery.orderBy("tag", ORDER.ASC);
			futureAppointmentsQuery.limit(2);
			List<IAppointment> futureDates = futureAppointmentsQuery.execute();
			
			IQuery<IAppointment> lastAppointmentQuery =
				coreModelService.getQuery(IAppointment.class);
			lastAppointmentQuery.and(ModelPackage.Literals.IAPPOINTMENT__SUBJECT_OR_PATIENT,
				COMPARATOR.EQUALS, patient.getId());
			lastAppointmentQuery.and("tag", COMPARATOR.LESS_OR_EQUAL, LocalDate.now());
			lastAppointmentQuery.orderBy("tag", ORDER.DESC);
			lastAppointmentQuery.limit(1);
			IAppointment lastAppointment = lastAppointmentQuery.executeSingleResult().orElse(null);
			
			for (IAppointment appointment : futureDates) {
				Label label = new Label(appointmentComposite, SWT.None);
				label.setText(util.getAppointmentLabel(appointment));
			}
			
			if (lastAppointment != null) {
				Label label = new Label(appointmentComposite, SWT.None);
				label.setText(util.getAppointmentLabel(lastAppointment));
			}
		}
		
		appointmentComposite.layout();
	}
	
	private void clearPopulateStickerComposite(IPatient patient){
		util.clearComposite(stickerComposite);
		if (patient != null) {
			List<ISticker> stickers = stickerService.getStickers(patient);
			for (ISticker sticker : stickers) {
				Label label = new Label(stickerComposite, SWT.None);
				label.setForeground(CoreUiUtil.getColorForString(sticker.getForeground()));
				label.setBackground(CoreUiUtil.getColorForString(sticker.getBackground()));
				label.setText(sticker.getLabel());
			}
		}
		stickerComposite.layout();
	}
	
	private void clearPopulatePatientLabelComposite(IPatient patient){
		
		if (patient == null) {
			lblPatientlabel.setText("");
			lblStammarzt.setText("");
			lblInsuranceKVG.setText("");
		} else {
			lblPatientlabel
				.setText(patient.getLabel() + " (" + patient.getAgeInYears() + " Jahre)");
			IContact familyDoctor = patient.getFamilyDoctor();
			if (familyDoctor != null) {
				lblStammarzt.setText("Stammarzt: " + familyDoctor.getLabel());
			} else {
				lblStammarzt.setText("Stammarzt: -");
			}
			IQuery<ICoverage> firstKvgQuery = coreModelService.getQuery(ICoverage.class);
			firstKvgQuery.and(ModelPackage.Literals.ICOVERAGE__PATIENT, COMPARATOR.EQUALS, patient);
			firstKvgQuery.and("gesetz", COMPARATOR.EQUALS, "KVG");
			firstKvgQuery.orderBy(ModelPackage.Literals.IDENTIFIABLE__LASTUPDATE, ORDER.DESC);
			firstKvgQuery.limit(1);
			ICoverage firstKvg = firstKvgQuery.executeSingleResult().orElse(null);
			IContact guarantor = firstKvg != null ? firstKvg.getGuarantor() : null;
			if (guarantor != null) {
				lblInsuranceKVG.setText("Versicherung KVG: " + guarantor.getLabel());
			} else {
				lblInsuranceKVG.setText("Versicherung KVG: -");
			}
		}
		
	}
	
	private void clearPopulateFixedMedicationComposite(IPatient patient){
		util.clearComposite(fixedMedicationComposite, lblFixedMedication);
		
		if (patient != null) {
			List<IPrescription> fixedMedication =
				patient.getMedication(Arrays.asList(EntryType.FIXED_MEDICATION));
			for (IPrescription medication : fixedMedication) {
				Label label = new Label(fixedMedicationComposite, SWT.None);
				label.setText(medication.getLabel());
			}
		}
		fixedMedicationComposite.layout();
	}
	
	private void clearUpdateLastLaboratoryComposite(IPatient patient){
		util.clearComposite(lastLaboratoryComposite, lblLastLaboratory);
		
		if (patient != null) {
			IQuery<ILabResult> labResultQuery = coreModelService.getQuery(ILabResult.class);
			labResultQuery.and(ModelPackage.Literals.ILAB_RESULT__PATIENT, COMPARATOR.EQUALS,
				patient);
			labResultQuery.orderBy(ModelPackage.Literals.ILAB_RESULT__DATE, ORDER.DESC);
			labResultQuery.limit(1);
			ILabResult latestResult = labResultQuery.executeSingleResult().orElse(null);
			if (latestResult != null) {
				Label label = new Label(lastLaboratoryComposite, SWT.NONE);
				label.setText(util.formatDate(latestResult.getDate()) + "");
			}
		}
		lastLaboratoryComposite.layout();
	}
	
	@Override
	public Category appliedForCategory(){
		return Category.PATIENT;
	}
	
}
