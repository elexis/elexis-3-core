package ch.elexis.core.findings.fhir.po.codes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;

import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.findings.codes.ICodingContribution;
import ch.elexis.core.findings.codes.ILocalCodingContribution;
import ch.elexis.data.Query;

@Component
public class LocalCodingContribution implements ICodingContribution, ILocalCodingContribution {
	
	@Override
	public String getCodeSystem(){
		return CodingSystem.ELEXIS_LOCAL_CODESYSTEM.getSystem();
	}
	
	@Override
	public List<ICoding> getCodes(){
		Query<LocalCoding> query = new Query<>(LocalCoding.class);
		query.add(LocalCoding.FLD_ID, Query.NOT_EQUAL, "VERSION");
		return new ArrayList<ICoding>(query.execute());
	}
	
	@Override
	public void addCoding(ICoding coding){
		if(coding.getSystem().equals(getCodeSystem())) {
			Optional<ICoding> exists = getCodingByCode(coding.getCode());
			if (!exists.isPresent()) {
				new LocalCoding(coding);
			}
		}
	}
	
	private Optional<ICoding> getCodingByCode(String code){
		Query<LocalCoding> query = new Query<>(LocalCoding.class);
		query.add(LocalCoding.FLD_ID, Query.NOT_EQUAL, LocalCoding.VERSION);
		query.add(LocalCoding.FLD_CODE, Query.EQUALS, code);
		List<LocalCoding> existing = query.execute();
		if (!existing.isEmpty()) {
			return Optional.of(existing.get(0));
		}
		return Optional.empty();
	}
	
	@Override
	public void removeCoding(ICoding coding){
		if (coding.getSystem().equals(getCodeSystem())) {
			Optional<ICoding> exists = getCodingByCode(coding.getCode());
			exists.ifPresent(existing -> ((LocalCoding) existing).delete());
		}
	}
	
	@Override
	public Optional<ICoding> getCode(String code){
		return getCodingByCode(code);
	}
}
