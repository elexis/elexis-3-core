package ch.elexis.core.ui.importer.div.services;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.importer.div.importers.ILabImportUtil;

@Component
public class LabImportUtilHolder {
	private static ILabImportUtil labImportUtil;
	
	@Reference
	public void setModelService(ILabImportUtil labImportUtil){
		LabImportUtilHolder.labImportUtil = labImportUtil;
	}
	
	public static ILabImportUtil get(){
		if (labImportUtil == null) {
			throw new IllegalStateException("No ILabImportUtil available");
		}
		return labImportUtil;
	}
}
