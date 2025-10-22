package ch.elexis.core.hl7.v2x;

import ch.elexis.core.importer.div.importers.ILabContactResolver;
import ch.elexis.core.model.ILaboratory;

/**
 * Dummy-Implementierung für Tests ohne echte Labordaten.
 */
public class DummyLabContactResolver implements ILabContactResolver {

	@Override
	public ILaboratory getLabContact(String identifier, String sendingFacility) {
		// Für Tests keine echte Laborzuordnung nötig
		return null;
	}
}