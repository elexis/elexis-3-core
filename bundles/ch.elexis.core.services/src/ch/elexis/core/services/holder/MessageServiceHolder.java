package ch.elexis.core.services.holder;

import ch.elexis.core.cdi.PortableServiceLoader;
import ch.elexis.core.services.IMessageService;

public class MessageServiceHolder {

	public static IMessageService get() {
		return PortableServiceLoader.get(IMessageService.class);
	}
}
