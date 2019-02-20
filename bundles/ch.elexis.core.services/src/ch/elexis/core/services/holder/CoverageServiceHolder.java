package ch.elexis.core.services.holder;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.services.ICoverageService;

@Component
public class CoverageServiceHolder {
	private static ICoverageService coverageService;
	
	@Reference
	public void setCoverageService(ICoverageService coverageService){
		CoverageServiceHolder.coverageService = coverageService;
	}
	
	public static ICoverageService get(){
		return coverageService;
	}
}
