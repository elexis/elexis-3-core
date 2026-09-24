package ch.elexis.core.fhir.model.service;

import java.util.HashMap;
import java.util.function.Supplier;

import org.slf4j.LoggerFactory;

import ch.elexis.core.fhir.model.dto.ICoverageDto;
import ch.elexis.core.fhir.model.dto.IMandatorDto;
import ch.elexis.core.fhir.model.dto.IPatientDto;
import ch.elexis.core.fhir.model.dto.IPersonDto;
import ch.elexis.core.fhir.model.dto.IdentifiableDeletableDto;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.Identifiable;

public class FhirDtoProvider {

	private static HashMap<Class<? extends Identifiable>, Supplier<? extends Identifiable>> supplierMap;

	static {
		supplierMap = new HashMap<>();
		supplierMap.put(ICoverage.class, ICoverageDto::new);
		supplierMap.put(IPerson.class, IPersonDto::new);
		supplierMap.put(IPatient.class, IPatientDto::new);
		supplierMap.put(IMandator.class, IMandatorDto::new);
	}

	@SuppressWarnings("unchecked")
	public static <T extends Identifiable> T createDto(Class<? extends Identifiable> modelType, String id,
			Long lastUpdate) {
		Supplier<? extends Identifiable> supplier = getSupplier(modelType);
		T result = null;
		if (supplier != null) {
			result = (T) supplier.get(); // Unchecked cast, but safer with the corrected map type
		}
		if (result != null) {
			((IdentifiableDeletableDto) result).setId(id);
			((IdentifiableDeletableDto) result).setLastupdate(lastUpdate);
			return result;
		}

		LoggerFactory.getLogger(FhirDtoProvider.class).error("No supplier found for " + modelType);
		throw new UnsupportedOperationException("No supplier found for " + modelType);
	}

	private static Supplier<? extends Identifiable> getSupplier(Class<? extends Identifiable> modelType) {
		return supplierMap.get(modelType);
	}
}
