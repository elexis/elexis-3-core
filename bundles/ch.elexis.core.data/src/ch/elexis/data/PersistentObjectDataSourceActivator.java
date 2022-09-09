package ch.elexis.data;

import javax.sql.DataSource;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.extension.ICoreOperationAdvisor;
import ch.elexis.core.services.IElexisEntityManager;

/**
 * Connect PersistentObject after NoPo was properly initialized and liquibase
 * was executed (visible due to injection of coreModelService)
 */
@Component(immediate = true, service = {})
public class PersistentObjectDataSourceActivator {

	@Reference(target = "(id=default)")
	private IElexisEntityManager elexisEntityManager;

	@Reference(target = "(id=default)")
	private DataSource dataSource;

	@Reference
	private ICoreOperationAdvisor coreOperationAdvisor;

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Activate
	public void activate() {
		elexisEntityManager.getEntityManager(true);
		if (!elexisEntityManager.isUpdateSuccess()) {
			coreOperationAdvisor.openInformation("DB Update Fehler",
					"Beim Datenbank Update ist ein Fehler aufgetreten.\n" + "Ihre Datenbank wurde nicht aktualisiert.\n"
							+ "Details dazu finden Sie in der log Datei.");
		}

		boolean connect = PersistentObject.connect(dataSource);
		logger.debug("PO#connect " + connect);
		boolean legacyPostInitDB = PersistentObject.legacyPostInitDB(coreOperationAdvisor);
		logger.debug("PO#legacyPostInitDB " + legacyPostInitDB);
	}

}
