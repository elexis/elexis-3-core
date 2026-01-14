package ch.elexis.core.fhir.model.interfaces;

import java.util.Optional;

import ch.elexis.core.model.IEncounter;

public interface IFhirCoverage {

	public Optional<IEncounter> getLatestEncounter();

}
