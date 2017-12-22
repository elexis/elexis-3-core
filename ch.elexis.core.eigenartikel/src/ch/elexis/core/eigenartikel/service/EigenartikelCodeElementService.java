package ch.elexis.core.eigenartikel.service;

import java.util.HashMap;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;

import ch.elexis.core.eigenartikel.Eigenartikel;
import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.services.ICodeElementServiceContribution;
import ch.elexis.data.Artikel;
import ch.elexis.data.Query;

@Component
public class EigenartikelCodeElementService implements ICodeElementServiceContribution {
	
	@Override
	public String getSystem(){
		return Eigenartikel.TYPNAME;
	}
	
	@Override
	public Optional<ICodeElement> createFromCode(String code, HashMap<Object, Object> context){
		Query<Eigenartikel> query = new Query<>(Eigenartikel.class);
		String found = query.findSingle(Artikel.FLD_SUB_ID, Query.EQUALS, code);
		if (found != null) {
			return Optional.of(Eigenartikel.load(found));
		} else {
			query.clear();
			found = query.findSingle(Eigenartikel.FLD_ID, Query.EQUALS, code);
			if (found != null) {
				return Optional.of((ICodeElement) Eigenartikel.load(found));
			}
		}
		return Optional.empty();
	}
	
}
