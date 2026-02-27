package ch.elexis.core.ui.contacts.wizard;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import ch.elexis.core.services.holder.CoreModelServiceHolder;

public class CoverageInsurancesUniqueIdSupplier implements Supplier<Set<String>> {

	private static final String UNIQUE_GUARANTORID = "select distinct f.GarantID from faelle f, kontakt k where f.deleted = '0' and f.GarantID = k.id and k.istOrganisation = '1' and k.deleted = '0'";
	private static final String UNIQUE_COSTBEARERID = "select distinct f.KostentrID from faelle f, kontakt k where f.deleted = '0' and f.KostentrID = k.id and k.istOrganisation = '1' and k.deleted = '0'";

	@SuppressWarnings("unchecked")
	@Override
	public Set<String> get() {
		HashSet<String> uniqueIds = new HashSet<>();
		Stream<String> result = (Stream<String>) CoreModelServiceHolder.get().executeNativeQuery(UNIQUE_GUARANTORID);
		result.forEach(s -> uniqueIds.add(s));
		result = (Stream<String>) CoreModelServiceHolder.get().executeNativeQuery(UNIQUE_COSTBEARERID);
		result.forEach(s -> uniqueIds.add(s));

		return uniqueIds;
	}
}
