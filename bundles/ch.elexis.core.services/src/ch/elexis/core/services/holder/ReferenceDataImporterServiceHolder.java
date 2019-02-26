package ch.elexis.core.services.holder;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.services.IReferenceDataImporterService;

@Component
public class ReferenceDataImporterServiceHolder {
	private static IReferenceDataImporterService referenceDataImporterService;
	
	@Reference
	public void setReferenceDataImporterService(
		IReferenceDataImporterService referenceDataImporterService){
		ReferenceDataImporterServiceHolder.referenceDataImporterService =
			referenceDataImporterService;
	}
	
	public static IReferenceDataImporterService get(){
		return referenceDataImporterService;
	}
}
