package ch.elexis.core.data.service.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.stock.ICommissioningSystemDriverFactory;

@Component(service = {})
public class StockCommissioningSystemDriverFactories {

	private Logger log = LoggerFactory.getLogger(StockCommissioningSystemDriverFactories.class);

	private static Map<UUID, ICommissioningSystemDriverFactory> driverFactories = new ConcurrentHashMap<UUID, ICommissioningSystemDriverFactory>();

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	public void bind(ICommissioningSystemDriverFactory driverFactory) {
		log.debug("Binding " + driverFactory.getClass().getName());
		driverFactories.put(driverFactory.getIdentification(), driverFactory);
	}

	public void unbind(ICommissioningSystemDriverFactory driverFactory) {
		log.debug("Unbdinding " + driverFactory.getClass().getName());
		driverFactories.remove(driverFactory.getIdentification());
	}

	public static ICommissioningSystemDriverFactory getDriverFactory(UUID driver) {
		return driverFactories.get(driver);
	}

	public static List<UUID> getAllDriverUuids() {
		return new ArrayList<UUID>(driverFactories.keySet());
	}

	public static String getInfoStringForDriver(UUID driverUuid, boolean extended) {
		ICommissioningSystemDriverFactory icsdf = driverFactories.get(driverUuid);
		if (icsdf != null) {
			if (extended) {
				return icsdf.getName() + " (" + icsdf.getDescription() + ")";
			} else {
				return icsdf.getName();
			}
		}
		return "["+driverUuid.toString()+"] info string found";
	}
}
