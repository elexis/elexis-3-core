package ch.elexis.core.ui.contacts.wizard;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import ch.elexis.core.model.IOrganization;
import ch.elexis.core.services.holder.CoreModelServiceHolder;

public class NotAssignedOrganizationsSupplier implements Supplier<List<IOrganization>> {

	@Override
	public List<IOrganization> get() {
		Set<String> managedInsurances = new ManagedInsurancesUniqueIdsSupplier().get();
		if (!managedInsurances.isEmpty()) {
			Set<String> allInsurances = new CoverageInsurancesUniqueIdSupplier().get();
			Set<String> notManagedInsurancesIds = allInsurances.stream().filter(i -> !managedInsurances.contains(i))
					.collect(Collectors.toSet());
			return notManagedInsurancesIds.stream()
					.map(i -> CoreModelServiceHolder.get().load(i, IOrganization.class).get()).toList();
		}
		return Collections.emptyList();
	}

}
