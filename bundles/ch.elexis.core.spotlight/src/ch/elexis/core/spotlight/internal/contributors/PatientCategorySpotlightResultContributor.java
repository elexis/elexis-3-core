package ch.elexis.core.spotlight.internal.contributors;

import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.spotlight.ISpotlightResult;
import ch.elexis.core.spotlight.ISpotlightResultContributor;
import ch.elexis.core.spotlight.ISpotlightResultEntry.Category;

@Component
public class PatientCategorySpotlightResultContributor implements ISpotlightResultContributor {
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService modelService;
	
	@Override
	public void setSearchTerm(String searchTerm, ISpotlightResult spotlightResult,
		Map<String, String> searchParams){
		
		IQuery<IPatient> query = modelService.getQuery(IPatient.class);
		// TODO native sql only
		query.startGroup();
		query.or(ModelPackage.Literals.IPERSON__FIRST_NAME, COMPARATOR.LIKE, "%" + searchTerm + "%",
			true);
		query.or(ModelPackage.Literals.IPERSON__LAST_NAME, COMPARATOR.LIKE, "%" + searchTerm + "%",
			true);
		
		query.limit(5);
		List<IPatient> patients = query.execute();
		for (IPatient patient : patients) {
			spotlightResult.addEntry(Category.PATIENT, patient.getLabel(), patient.getId(), null);
		}
	}
	
}
