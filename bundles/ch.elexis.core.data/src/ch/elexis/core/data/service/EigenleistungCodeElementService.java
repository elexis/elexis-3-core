package ch.elexis.core.data.service;

import java.util.HashMap;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;

import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.services.ICodeElementServiceContribution;
import ch.elexis.data.Eigenleistung;
import ch.elexis.data.Query;

@Component
public class EigenleistungCodeElementService implements ICodeElementServiceContribution {
	
	@Override
	public String getSystem(){
		return Eigenleistung.CODESYSTEM_NAME;
	}
	
	@Override
	public Optional<ICodeElement> createFromCode(String code, HashMap<Object, Object> context){
		Query<Eigenleistung> query = new Query<>(Eigenleistung.class);
		String found = query.findSingle(Eigenleistung.CODE, Query.EQUALS, code);
		if (found != null) {
			return Optional.of((ICodeElement) Eigenleistung.load(found));
		} else {
			query.clear();
			found = query.findSingle(Eigenleistung.FLD_ID, Query.EQUALS, code);
			if (found != null) {
				return Optional.of((ICodeElement) Eigenleistung.load(found));
			}
		}
		return Optional.empty();
	}
}
