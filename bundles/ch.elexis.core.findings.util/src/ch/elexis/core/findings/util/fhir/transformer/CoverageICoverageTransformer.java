package ch.elexis.core.findings.util.fhir.transformer;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.Period;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.transformer.helper.AbstractHelper;
import ch.elexis.core.findings.util.fhir.transformer.helper.ICoverageHelper;
import ch.elexis.core.model.FallConstants;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.builder.ICoverageBuilder;
import ch.elexis.core.services.IModelService;

@Component
public class CoverageICoverageTransformer implements IFhirTransformer<Coverage, ICoverage> {
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService modelService;
	
	private ICoverageHelper coverageHelper;
	
	@Activate
	public void activate(){
		coverageHelper = new ICoverageHelper();
	}
	
	@Override
	public Optional<Coverage> getFhirObject(ICoverage localObject, SummaryEnum summaryEnum,
		Set<Include> includes){
		Coverage coverage = new Coverage();
		
		coverage.setId(new IdDt("Coverage", localObject.getId()));
		coverage.addIdentifier(getElexisObjectIdentifier(localObject));
		
		coverage.setDependent(coverageHelper.getDependent(localObject));
		coverage.setBeneficiary(coverageHelper.getBeneficiaryReference(localObject));
		coverage
			.setPayor(Collections.singletonList(coverageHelper.getIssuerReference(localObject)));
		coverage.setPeriod(coverageHelper.getPeriod(localObject));
		
		coverageHelper.getType(localObject).ifPresent(coding -> {
			coverage.setType(coding);
		});
		
		coverageHelper.setText(coverage, coverageHelper.getFallText(localObject));
		
		return Optional.of(coverage);
	}
	
	@Override
	public Optional<ICoverage> getLocalObject(Coverage fhirObject){
		if (fhirObject != null && fhirObject.getId() != null) {
			Optional<ICoverage> existing = modelService.load(fhirObject.getId(), ICoverage.class);
			if (existing.isPresent()) {
				return Optional.of(existing.get());
			}
		}
		return Optional.empty();
	}
	
	@Override
	public Optional<ICoverage> updateLocalObject(Coverage fhirObject, ICoverage localObject){
		// TODO Auto-generated method stub
		return Optional.empty();
	}
	
	@Override
	public Optional<ICoverage> createLocalObject(Coverage fhirObject){
		if (fhirObject.hasBeneficiary()) {
			Optional<IPatient> patient = modelService.load(
				fhirObject.getBeneficiary().getReferenceElement().getIdPart(), IPatient.class);
			Optional<String> type = coverageHelper.getType(fhirObject);
			if (patient.isPresent() && type.isPresent()) {
				ICoverage created = new ICoverageBuilder(modelService, patient.get(),
					"online created", FallConstants.TYPE_DISEASE, type.get()).buildAndSave();
				String dependent = fhirObject.getDependent();
				if (dependent != null) {
					coverageHelper.setBin(created, dependent);
				}
				Period period = fhirObject.getPeriod();
				if (period != null && period.getStart() != null) {
					coverageHelper.setPeriod(created, fhirObject.getPeriod());
				} else {
					created.setDateFrom(LocalDate.now());
				}
				modelService.save(created);
				AbstractHelper.acquireAndReleaseLock(created);
				return Optional.of(created);
			} else {
				LoggerFactory.getLogger(CoverageICoverageTransformer.class).warn(
					"Could not create fall for patinet [" + patient + "] type [" + type + "]");
			}
		}
		return Optional.empty();
	}
	
	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz){
		return Coverage.class.equals(fhirClazz) && ICoverage.class.equals(localClazz);
	}
	
}
