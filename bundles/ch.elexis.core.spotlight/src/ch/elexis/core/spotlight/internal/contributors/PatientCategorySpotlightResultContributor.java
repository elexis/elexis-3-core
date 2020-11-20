package ch.elexis.core.spotlight.internal.contributors;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.IQuery.ORDER;
import ch.elexis.core.spotlight.ISpotlightResult;
import ch.elexis.core.spotlight.ISpotlightResultContributor;
import ch.elexis.core.spotlight.ISpotlightResultEntry.Category;

@Component
public class PatientCategorySpotlightResultContributor implements ISpotlightResultContributor {
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService modelService;
	
	@Override
	public void computeResult(List<String> stringTerms, List<LocalDate> dateTerms,
		List<Number> numericTerms, ISpotlightResult spotlightResult,
		Map<String, String> searchParams){
		
		IQuery<IPatient> query = modelService.getQuery(IPatient.class);
		
		if (stringTerms.size() == 1) {
			// only single name - don't know if part of firstname or lastname
			query.startGroup();
			query.or(ModelPackage.Literals.IPERSON__FIRST_NAME, COMPARATOR.LIKE,
				"%" + stringTerms.get(0) + "%", true);
			query.or(ModelPackage.Literals.IPERSON__LAST_NAME, COMPARATOR.LIKE,
				"%" + stringTerms.get(0) + "%", true);
			
		} else if (stringTerms.size() > 1) {
			// two names, could either be parts of "firstname lastname" or "lastname firstname"
			query.startGroup();
			query.and(ModelPackage.Literals.IPERSON__FIRST_NAME, COMPARATOR.LIKE,
				"%" + stringTerms.get(0) + "%", true);
			query.and(ModelPackage.Literals.IPERSON__LAST_NAME, COMPARATOR.LIKE,
				"%" + stringTerms.get(1) + "%", true);
			
			query.startGroup();
			query.and(ModelPackage.Literals.IPERSON__FIRST_NAME, COMPARATOR.LIKE,
				"%" + stringTerms.get(1) + "%", true);
			query.and(ModelPackage.Literals.IPERSON__LAST_NAME, COMPARATOR.LIKE,
				"%" + stringTerms.get(0) + "%", true);
			query.orJoinGroups();
		}
		
		if (!dateTerms.isEmpty()) {
			if (!stringTerms.isEmpty()) {
				query.andJoinGroups();
			}
			query.and(ModelPackage.Literals.IPERSON__DATE_OF_BIRTH, COMPARATOR.EQUALS,
				dateTerms.get(0));
		}
		
		if (!numericTerms.isEmpty()) {
			if (!stringTerms.isEmpty() || !dateTerms.isEmpty()) {
				query.andJoinGroups();
			}
			query.and(ModelPackage.Literals.ICONTACT__CODE, COMPARATOR.LIKE,
				numericTerms.get(0).intValue() + "%");
		}
		
		query.orderBy(ModelPackage.Literals.IPERSON__LAST_NAME, ORDER.ASC);
		query.limit(5);
		
		List<IPatient> patients = query.execute();
		for (IPatient patient : patients) {
			spotlightResult.addEntry(Category.PATIENT, patient.getLabel(), patient.getId(), null);
		}
	}
	
}
