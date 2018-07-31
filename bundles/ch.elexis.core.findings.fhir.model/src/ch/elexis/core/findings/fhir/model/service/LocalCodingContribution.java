package ch.elexis.core.findings.fhir.model.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.ILocalCoding;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.findings.codes.ICodingContribution;
import ch.elexis.core.findings.codes.ILocalCodingContribution;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;

@Component
public class LocalCodingContribution implements ICodingContribution, ILocalCodingContribution {
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.findings.model)")
	private IModelService findingsModelService;
	
	@Override
	public String getCodeSystem(){
		return CodingSystem.ELEXIS_LOCAL_CODESYSTEM.getSystem();
	}
	
	@Override
	public List<ICoding> getCodes(){
		IQuery<ILocalCoding> query = findingsModelService.getQuery(ILocalCoding.class);
		query.and("id", COMPARATOR.NOT_EQUALS, "VERSION");
		return new ArrayList<ICoding>(query.execute());
	}
	
	@Override
	public void addCoding(ICoding coding){
		if(coding.getSystem().equals(getCodeSystem())) {
			Optional<ICoding> exists = getCodingByCode(coding.getCode());
			if (!exists.isPresent()) {
				ILocalCoding localCoding = findingsModelService.create(ILocalCoding.class);
				localCoding.setCode(coding.getCode());
				localCoding.setDisplay(coding.getDisplay());
				findingsModelService.save(localCoding);
			}
		}
	}
	
	private Optional<ICoding> getCodingByCode(String code){
		IQuery<ILocalCoding> query = findingsModelService.getQuery(ILocalCoding.class);
		query.and("id", COMPARATOR.NOT_EQUALS, "VERSION");
		if (code != null && code.isEmpty()) {
			query.startGroup();
			query.or("code", COMPARATOR.EQUALS, code);
			query.or("code", COMPARATOR.EQUALS, null);
			query.andJoinGroups();
		}
		else {
			query.and("code", COMPARATOR.EQUALS, code);
		}
		List<ILocalCoding> existing = query.execute();
		if (!existing.isEmpty()) {
			return Optional.of(existing.get(0));
		}
		return Optional.empty();
	}
	
	@Override
	public void removeCoding(ICoding coding){
		if (coding.getSystem().equals(getCodeSystem())) {
			Optional<ICoding> exists = getCodingByCode(coding.getCode());
			exists.ifPresent(existing -> findingsModelService.delete((ILocalCoding) existing));
		}
	}
	
	@Override
	public Optional<ICoding> getCode(String code){
		return getCodingByCode(code);
	}
}
