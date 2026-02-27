package ch.elexis.core.ui.contacts.wizard;

import java.util.function.Supplier;
import java.util.stream.Stream;

import ch.elexis.core.model.IOrganization;
import ch.elexis.core.services.holder.CoreModelServiceHolder;

public class CountCoverageSupplier implements Supplier<Long> {

	private String organizationId;

	private static final String COUNT_COVERAGES = "select count(*) from faelle f where f.deleted = '0' and f.GarantID = '[ORGANIZATIONID]' or f.KostentrID = '[ORGANIZATIONID]'";

	public CountCoverageSupplier(IOrganization element) {
		this.organizationId = element.getId();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Long get() {
		Stream<Long> result = (Stream<Long>) CoreModelServiceHolder.get()
				.executeNativeQuery(COUNT_COVERAGES.replace("[ORGANIZATIONID]", organizationId));
		return result.findFirst().get();
	}

}
