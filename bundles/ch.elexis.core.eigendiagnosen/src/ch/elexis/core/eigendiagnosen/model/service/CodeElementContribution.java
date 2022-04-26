package ch.elexis.core.eigendiagnosen.model.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.model.IDiagnosisTree;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.ICodeElementService.CodeElementTyp;
import ch.elexis.core.services.ICodeElementServiceContribution;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;

@Component
public class CodeElementContribution implements ICodeElementServiceContribution {

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.eigendiagnosen.model)")
	private IModelService modelService;

	@Override
	public String getSystem() {
		return "ED";
	}

	@Override
	public CodeElementTyp getTyp() {
		return CodeElementTyp.DIAGNOSE;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Optional<ICodeElement> loadFromCode(String code, Map<Object, Object> context) {
		return (Optional<ICodeElement>) (Optional<?>) ModelUtil.loadDiagnosisWithCode(code);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ICodeElement> getElements(Map<Object, Object> context) {
		IQuery<IDiagnosisTree> query = modelService.getQuery(IDiagnosisTree.class);
		query.and(ModelPackage.Literals.IDIAGNOSIS_TREE__PARENT, COMPARATOR.NOT_EQUALS, null);
		return (List<ICodeElement>) (List<?>) query.execute();
	}

}
