package ch.elexis.core.ui.e4.fhir.parts.supplier;

import java.util.List;
import java.util.function.Supplier;

import ch.elexis.core.fhir.model.FhirModelServiceHolder;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IPatient;

public class CoverageSupplierFactory {

	public static Supplier<List<ICoverage>> get(IPatient patient, int limit) {
		if (FhirModelServiceHolder.isAvailable()) {
			return new FhirCoverageSupplier(patient, limit);
		} else {
			return new CoverageSupplier(patient, limit);
		}
	}

}
