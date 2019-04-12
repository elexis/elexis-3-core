package ch.elexis.core.importer.div.service.holder;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import ch.elexis.core.services.IVirtualFilesystemService;

@Component
public class IVirtualFilesystemServiceHolder {
	private static IVirtualFilesystemService vfsService;
	
	@Reference(cardinality = ReferenceCardinality.MANDATORY)
	public void setModelService(IVirtualFilesystemService vfsService){
		IVirtualFilesystemServiceHolder.vfsService = vfsService;
	}
	
	public static IVirtualFilesystemService get(){
		if (vfsService == null) {
			throw new IllegalStateException("No vfsService available");
		}
		return vfsService;
	}
}
