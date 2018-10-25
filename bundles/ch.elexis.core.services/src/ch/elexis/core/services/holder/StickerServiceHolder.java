package ch.elexis.core.services.holder;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.services.IStickerService;

@Component
public class StickerServiceHolder {
	
	private static IStickerService stickerService;
	
	@Reference
	public void setStockService(IStickerService stickerService){
		StickerServiceHolder.stickerService = stickerService;
	}
	
	public static IStickerService get(){
		if (stickerService == null) {
			throw new IllegalStateException("No IStickerService available");
		}
		return stickerService;
	}
}
