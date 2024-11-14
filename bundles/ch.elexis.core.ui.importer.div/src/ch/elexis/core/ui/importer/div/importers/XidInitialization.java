package ch.elexis.core.ui.importer.div.importers;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.services.IXidService;
import ch.elexis.data.Xid;

@Component
public class XidInitialization {

	@Reference
	private IXidService xidService;

	@Activate
	private void activate() {
		xidService.localRegisterXIDDomainIfNotExists(Presets.KONTAKTID, Messages.Presets_PreviousID,
					Xid.ASSIGNMENT_LOCAL);
	}
}
