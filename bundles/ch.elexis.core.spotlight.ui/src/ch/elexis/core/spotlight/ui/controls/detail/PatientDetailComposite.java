package ch.elexis.core.spotlight.ui.controls.detail;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
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
import ch.elexis.core.spotlight.ui.internal.CustomLinkWithOptionalImage;
import ch.elexis.core.spotlight.ui.internal.SpotlightShell;
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
	private Label lblAppointments;
	private CustomLinkWithOptionalImage lastEncounter;
	private Composite encounterComposite;
	private Label lblLastEncounterText;
	private Label lblPendenzen;
	private CustomLinkWithOptionalImage balance;
	//	private Label lblBalance;
	private Label lblFixedMedication;
	private Composite fixedMedicationComposite;
	private Label lblLastLaboratory;
	private Composite lastLaboratoryComposite;
	
	private ListViewer lvAppointments;
	
	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	public PatientDetailComposite(Composite parent, int style){
		super(parent, style);
		GridLayout gridLayout = new GridLayout(1, true);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.marginTop = 0;
		setLayout(gridLayout);
		
		util = new PatientDetailCompositeUtil();
		coreModelService = CoreModelServiceHolder.get();
		
		lblPatientlabel = new Label(this, SWT.NONE);
		
		Font patientLabelFont;
		//		if (JFaceResources.getFontRegistry().hasValueFor(PATIENT_LABEL_FONT)) {
		//			patientLabelFont = JFaceResources.getFontRegistry().get(PATIENT_LABEL_FONT);
		//		} else {
		FontData[] fontData = lblPatientlabel.getFont().getFontData();
		fontData[0].setHeight(fontData[0].getHeight() + 1);
		JFaceResources.getFontRegistry().put(PATIENT_LABEL_FONT, fontData);
		patientLabelFont = JFaceResources.getFontRegistry().get(PATIENT_LABEL_FONT);
		//		}
		lblPatientlabel.setFont(patientLabelFont);
		lblPatientlabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		lblPatientlabel.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
		lblPatientlabel.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		
		// Patient Stickers
		stickerComposite = new Composite(this, SWT.NONE);
		FillLayout fl_stickerComposite = new FillLayout(SWT.HORIZONTAL);
		fl_stickerComposite.spacing = 5;
		stickerComposite.setLayout(fl_stickerComposite);
		GridData gd_stickerComposite = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_stickerComposite.heightHint = 16;
		stickerComposite.setLayoutData(gd_stickerComposite);
		
		// Stammarzt, Versicherung
		lblStammarzt = new Label(this, SWT.NONE);
		lblStammarzt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblInsuranceKVG = new Label(this, SWT.NONE);
		lblInsuranceKVG.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		// Reminders
		Composite pendenzenComposite = new Composite(this, SWT.NONE);
		pendenzenComposite.setLayout(new FillLayout(SWT.HORIZONTAL));
		pendenzenComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		
		lblPendenzen = new Label(pendenzenComposite, SWT.NONE);
		lblPendenzen.setText("Keine Pendenzen (TODO)");
		
		// Balance
		balance = new CustomLinkWithOptionalImage(this, SWT.None, null);
		//		lblBalance = new Label(this, SWT.NONE);
		//		lblBalance.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
		
		// Appointments
		lblAppointments = new Label(this, SWT.NONE);
		Font boldDefaultFont = JFaceResources.getFontRegistry().getBold("default");
		lblAppointments.setText("Termine");
		lblAppointments.setFont(boldDefaultFont);
		lblAppointments.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		
		lvAppointments = new ListViewer(this, SWT.V_SCROLL);
		lvAppointments.setContentProvider(ArrayContentProvider.getInstance());
		lvAppointments.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element){
				return util.getAppointmentLabel(((IAppointment) element));
			}
		});
		lvAppointments.addSelectionChangedListener(event -> {
			IAppointment firstElement =
				(IAppointment) event.getStructuredSelection().getFirstElement();
			if (firstElement != null) {
				((SpotlightShell) getShell()).setSelectedElement(firstElement);
			} else {
				((SpotlightShell) getShell()).setSelectedElement(null);
			}
			
		});
		org.eclipse.swt.widgets.List listAppointments = lvAppointments.getList();
		listAppointments.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		listAppointments.setBackground(getBackground());
		listAppointments.addListener(SWT.FocusIn, event -> {
			@SuppressWarnings("unchecked")
			List<IAppointment> input = ((List<IAppointment>) lvAppointments.getInput());
			if (input != null && !input.isEmpty()) {
				lvAppointments.setSelection(new StructuredSelection(input.get(0)));
			} else {
				lvAppointments.setSelection(null);
			}
		});
		listAppointments.addListener(SWT.FocusOut, event -> {
			lvAppointments.setSelection(null);
		});
		
		// Encounter
		lastEncounter = new CustomLinkWithOptionalImage(this, SWT.NONE, null);
		lastEncounter.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		lastEncounter.getLink().setText("Letzte Konsultation");
		lastEncounter.getLink().setFont(boldDefaultFont);
		lastEncounter.setFont(boldDefaultFont);
		
		encounterComposite = new Composite(this, SWT.NONE);
		GridLayout gl_encounterComposite = new GridLayout(1, false);
		gl_encounterComposite.marginHeight = 0;
		gl_encounterComposite.marginWidth = 0;
		encounterComposite.setLayout(gl_encounterComposite);
		encounterComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		
		lblLastEncounterText = new Label(encounterComposite, SWT.WRAP | SWT.LEFT);
		GridData gd_lblLastEncounterText = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		gd_lblLastEncounterText.heightHint = 60;
		lblLastEncounterText.setLayoutData(gd_lblLastEncounterText);
		
		fixedMedicationComposite = new Composite(this, SWT.NONE);
		fixedMedicationComposite.setLayout(new FillLayout(SWT.VERTICAL));
		fixedMedicationComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		
		lblFixedMedication = new Label(fixedMedicationComposite, SWT.NONE);
		lblFixedMedication.setText("Fixmedikation");
		lblFixedMedication.setFont(boldDefaultFont);
		
		lastLaboratoryComposite = new Composite(this, SWT.NONE);
		lastLaboratoryComposite.setLayout(new FillLayout(SWT.VERTICAL));
		lastLaboratoryComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		
		lblLastLaboratory = new Label(lastLaboratoryComposite, SWT.NONE);
		lblLastLaboratory.setText("Letztes Labor");
		lblLastLaboratory.setFont(boldDefaultFont);
		
		setTabList(new Control[] {
			balance, listAppointments, lastEncounter
		});
		
		setSpotlightEntry(null);
		
	}
	
	@Override
	public boolean setFocus(){
		return balance.setFocus();
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
		
		clearPopulatePatientLabelComposite(patient);
		clearPopulateStickerComposite(patient);
		clearPopulateReminders(patient);
		clearUpdateBalance(patient);
		updateAppointments(patient);
		updateLastEncounter(patient);
		clearPopulateFixedMedicationComposite(patient);
		clearUpdateLastLaboratoryComposite(patient);
		
		layout(true);
	}
	
	private void clearPopulateReminders(IPatient patient){
		// TODO Auto-generated method stub
	}
	
	private void clearUpdateBalance(IPatient patient){
		balance.setData(null);
		if (patient != null) {
			INamedQuery<Number> namedQuery = coreModelService.getNamedQuery(Number.class,
				IAccountTransaction.class, true, "balance.patient");
			List<Number> balanceResult =
				namedQuery.executeWithParameters(namedQuery.getParameterMap("patient", patient));
			balance.setData("balance::" + patient.getId());
			if (!balanceResult.isEmpty()) {
				int _balance = balanceResult.get(0).intValue();
				balance.getLink().setText("Konto: CHF " + new Money(_balance));
				return;
			}
		}
		balance.getLink().setText("Konto: -");
	}
	
	private void updateLastEncounter(IPatient patient){
		lastEncounter.setData(null);
		if (patient != null) {
			IEncounter _lastEncounter = encounterService.getLatestEncounter(patient).orElse(null);
			if (_lastEncounter != null) {
				lastEncounter.setData(_lastEncounter);
				lastEncounter.getLink()
					.setText("Letzte Konsultation " + util.formatDate(_lastEncounter.getDate()));
				String encounterText = _lastEncounter.getHeadVersionInPlaintext();
				lblLastEncounterText.setText(StringUtils.abbreviate(encounterText, 200));
			} else {
				lastEncounter.getLink().setText("Letzte Konsultation");
				lblLastEncounterText.setText("-");
			}
		} else {
			lastEncounter.getLink().setText("");
			lblLastEncounterText.setText("");
		}
		
		encounterComposite.layout();
	}
	
	private void updateAppointments(IPatient patient){
		if (patient != null) {
			IQuery<IAppointment> futureAppointmentsQuery =
				coreModelService.getQuery(IAppointment.class);
			futureAppointmentsQuery.and(ModelPackage.Literals.IAPPOINTMENT__SUBJECT_OR_PATIENT,
				COMPARATOR.EQUALS, patient.getId());
			futureAppointmentsQuery.and("tag", COMPARATOR.GREATER, LocalDate.now());
			futureAppointmentsQuery.orderBy("tag", ORDER.ASC);
			futureAppointmentsQuery.limit(2);
			List<IAppointment> relevantAppointments = futureAppointmentsQuery.execute();
			
			IQuery<IAppointment> lastAppointmentQuery =
				coreModelService.getQuery(IAppointment.class);
			lastAppointmentQuery.and(ModelPackage.Literals.IAPPOINTMENT__SUBJECT_OR_PATIENT,
				COMPARATOR.EQUALS, patient.getId());
			lastAppointmentQuery.and("tag", COMPARATOR.LESS_OR_EQUAL, LocalDate.now());
			lastAppointmentQuery.orderBy("tag", ORDER.DESC);
			lastAppointmentQuery.limit(1);
			IAppointment lastAppointment = lastAppointmentQuery.executeSingleResult().orElse(null);
			
			if (lastAppointment != null) {
				relevantAppointments.add(lastAppointment);
			}
			
			lvAppointments.setInput(relevantAppointments);
		} else {
			lvAppointments.setInput(null);
		}
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
				String label =
					familyDoctor.getDescription1() + ", " + familyDoctor.getDescription2();
				if (StringUtils.isNotEmpty(familyDoctor.getCode())) {
					label += " (" + familyDoctor.getCode() + ")";
				}
				lblStammarzt.setText("Stammarzt: " + label);
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
