package ch.elexis.core.services.holder;

import ch.elexis.core.cdi.PortableServiceLoader;
import ch.elexis.core.services.IVirtualFilesystemService;

public class VirtualFilesystemServiceHolder {

	public static IVirtualFilesystemService get() {
		return PortableServiceLoader.get(IVirtualFilesystemService.class);
	}
}
