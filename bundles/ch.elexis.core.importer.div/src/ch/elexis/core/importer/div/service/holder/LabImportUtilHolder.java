package ch.elexis.core.importer.div.service.holder;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import ch.elexis.core.importer.div.importers.ILabImportUtil;
import jakarta.inject.Inject;

@Component
public class LabImportUtilHolder {

	private static ILabImportUtil labImportUtil;

	@Inject
	@Reference(cardinality = ReferenceCardinality.MANDATORY)
	public void setModelService(ILabImportUtil labImportUtil) {
		LabImportUtilHolder.labImportUtil = labImportUtil;
	}

	public static ILabImportUtil get() {
		if (labImportUtil == null) {
			throw new IllegalStateException("No ILabImportUtil available");
		}
		return labImportUtil;
	}
}
