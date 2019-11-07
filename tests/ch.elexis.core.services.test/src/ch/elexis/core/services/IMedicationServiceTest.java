package ch.elexis.core.services;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPrescription;
import ch.elexis.core.model.builder.IArticleBuilder;
import ch.elexis.core.model.builder.IContactBuilder;
import ch.elexis.core.model.builder.IPrescriptionBuilder;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.types.ArticleTyp;
import ch.elexis.core.types.Gender;
import ch.elexis.core.utils.OsgiServiceUtil;

public class IMedicationServiceTest extends AbstractServiceTest {
	
	private IMedicationService medicationService =
		OsgiServiceUtil.getService(IMedicationService.class).get();
	
	private IPatient patient;
	
	private List<IPrescription> createdPrescriptions;
	
	private IArticle localArticle;
	
	@Before
	public void before(){
		patient = new IContactBuilder.PatientBuilder(CoreModelServiceHolder.get(), "test",
			"patient", LocalDate.of(2000, 1, 1), Gender.FEMALE).buildAndSave();
		
		localArticle = new IArticleBuilder(coreModelService, "test medication article", "1234567",
			ArticleTyp.EIGENARTIKEL).build();
		localArticle.setGtin("1111111000000");
		localArticle.setPackageSize(2);
		localArticle.setSellingSize(1);
		coreModelService.save(localArticle);
		
		createdPrescriptions = new ArrayList<>();
		createdPrescriptions
			.add(new IPrescriptionBuilder(coreModelService, localArticle, patient, "0-1-1-0")
				.buildAndSave());
		createdPrescriptions
			.add(new IPrescriptionBuilder(coreModelService, localArticle, patient, "1-0-0-1")
				.buildAndSave());
		createdPrescriptions
			.add(new IPrescriptionBuilder(coreModelService, localArticle, patient, "1-0-0-0")
				.buildAndSave());
		createdPrescriptions
			.add(new IPrescriptionBuilder(coreModelService, localArticle, patient, "0-0-0-1")
				.buildAndSave());
	}
	
	@After
	public void after(){
		coreModelService.remove(createdPrescriptions);
		coreModelService.remove(localArticle);
		coreModelService.remove(patient);
	}
	
	@Test
	public void stopPatientPrescriptions(){
		List<IPrescription> prescriptions = getPrescriptions(patient, "all");
		LocalDateTime now = LocalDateTime.now();
		prescriptions.forEach(pr -> medicationService.stopPrescription(pr, now));
		// changed but not saved
		prescriptions.forEach(pr -> assertNotNull(pr.getDateTo()));
		createdPrescriptions.forEach(pr -> assertNull(pr.getDateTo()));
		
		coreModelService.save(prescriptions);
		// changed and saved
		prescriptions.forEach(pr -> assertNotNull(pr.getDateTo()));
		createdPrescriptions.forEach(pr -> assertNotNull(pr.getDateTo()));
	}
	
	private List<IPrescription> getPrescriptions(IPatient patient, String medicationType){
		if ("all".equals(medicationType)) {
			return patient.getMedication(Arrays.asList(EntryType.FIXED_MEDICATION,
				EntryType.RESERVE_MEDICATION, EntryType.SYMPTOMATIC_MEDICATION));
		} else if ("fix".equals(medicationType)) {
			return patient.getMedication(Arrays.asList(EntryType.FIXED_MEDICATION));
		} else if ("reserve".equals(medicationType)) {
			return patient.getMedication(Arrays.asList(EntryType.RESERVE_MEDICATION));
		} else if ("symptomatic".equals(medicationType)) {
			return patient.getMedication(Arrays.asList(EntryType.SYMPTOMATIC_MEDICATION));
		}
		return Collections.emptyList();
	}
}
