package ch.elexis.core.ui.contacts.extension;

import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.types.Country;

import ch.elexis.core.ui.contacts.interfaces.IContactGenoameService;

public class ContactGeonamesExtensionPoint {
	private static final String EXT_POINT = "ch.elexis.core.ui.contacts.geonames"; //$NON-NLS-1$

	private static Logger log = LoggerFactory.getLogger(ContactGeonamesExtensionPoint.class);

	private static HashMap<Country, IContactGenoameService> services = new HashMap<>();

	public static void init() {
		IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(EXT_POINT);
		for (IConfigurationElement ice : config) {
			try {
				final Object o = ice.createExecutableExtension("geonames"); //$NON-NLS-1$
				if (o instanceof IContactGenoameService) {
					IContactGenoameService cgs = (IContactGenoameService) o;
					log.debug("IContactGenoameService found @ " + ice.getContributor().getName() + ": " //$NON-NLS-1$ //$NON-NLS-2$
							+ o.getClass().getName());
					services.put(cgs.getProvidesInformationForCountry(), cgs);
				}
				return;
			} catch (CoreException ex) {
				log.error("Error at IContactGenoameService extension initialization", ex); //$NON-NLS-1$
			}
		}

		// TODO shall we initialize them?
	}

	public static IContactGenoameService getGeonameServiceForCountry(Country country) {
		return services.get(country);
	}

}
