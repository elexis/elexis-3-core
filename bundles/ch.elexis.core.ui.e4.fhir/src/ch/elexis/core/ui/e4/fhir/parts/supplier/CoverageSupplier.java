package ch.elexis.core.ui.e4.fhir.parts.supplier;

import java.util.List;
import java.util.function.Supplier;

import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.CoreModelServiceHolder;

public class CoverageSupplier implements Supplier<List<ICoverage>> {

	private IPatient patient;
	private int limit;

	public CoverageSupplier(IPatient patient, int limit) {
		this.patient = patient;
		this.limit = limit;
	}

	@Override
	public List<ICoverage> get() {
		IQuery<ICoverage> query = CoreModelServiceHolder.get().getQuery(ICoverage.class);
		query.and(ModelPackage.Literals.ICOVERAGE__PATIENT, COMPARATOR.EQUALS, patient);
		query.limit(limit);
		return query.execute();
	}

}
