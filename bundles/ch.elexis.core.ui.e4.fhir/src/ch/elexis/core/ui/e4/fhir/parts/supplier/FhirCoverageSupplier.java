package ch.elexis.core.ui.e4.fhir.parts.supplier;

import java.util.List;
import java.util.function.Supplier;

import org.hl7.fhir.instance.model.api.IBaseBundle;

import ca.uhn.fhir.rest.gclient.IQuery;
import ch.elexis.core.fhir.model.FhirModelServiceHolder;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IPatient;

public class FhirCoverageSupplier implements Supplier<List<ICoverage>> {

	private IPatient patient;
	private int limit;

	public FhirCoverageSupplier(IPatient patient, int limit) {
		this.patient = patient;
		this.limit = limit;
	}

	@Override
	public List<ICoverage> get() {
		IQuery<IBaseBundle> query = FhirModelServiceHolder.get().getQuery("Coverage?patient=" + patient.getId());
		query.count(limit);
		List<ICoverage> ret = FhirModelServiceHolder.get().getQueryResults(query, ICoverage.class);
		return ret;
	}

}
