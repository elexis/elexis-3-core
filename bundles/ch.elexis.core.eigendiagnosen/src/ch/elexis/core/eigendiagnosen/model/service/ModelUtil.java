package ch.elexis.core.eigendiagnosen.model.service;

import java.util.List;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.model.IDiagnosisTree;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.INamedQuery;

@Component
public class ModelUtil {
	private static IModelService diagnosisModelService;
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME
		+ "=ch.elexis.core.eigendiagnosen.model)")
	public void setModelService(IModelService modelService){
		ModelUtil.diagnosisModelService = modelService;
	}
	
	public static <T> Optional<T> loadDiagnosis(String id, Class<T> clazz){
		if (id != null) {
			return diagnosisModelService.load(id, clazz);
		}
		return Optional.empty();
	}
	
	public static Optional<IDiagnosisTree> loadDiagnosisWithCode(String code){
		INamedQuery<IDiagnosisTree> query =
				diagnosisModelService.getNamedQuery(IDiagnosisTree.class, "code");
		List<IDiagnosisTree> found =
			query.executeWithParameters(query.getParameterMap("code", code));
		if (!found.isEmpty()) {
			return Optional.of(found.get(0));
		}
		return Optional.empty();
	}
	
	public static List<IDiagnosisTree> loadDiagnosisWithParent(String parentCode){
		INamedQuery<IDiagnosisTree> query =
			diagnosisModelService.getNamedQuery(IDiagnosisTree.class, "parent");
		return query
			.executeWithParameters(query.getParameterMap("parent", parentCode));
	}
}
