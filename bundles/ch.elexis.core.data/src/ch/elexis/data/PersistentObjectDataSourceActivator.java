package ch.elexis.data;

import java.sql.SQLException;
import java.util.Locale;

import javax.sql.DataSource;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.extension.CoreOperationAdvisorHolder;
import ch.elexis.core.data.extension.ICoreOperationAdvisor;
import ch.elexis.core.events.MessageEvent;
import ch.elexis.core.services.IAccessControlService;
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IElexisEntityManager;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.utils.CoreUtil;
import ch.rgw.io.ISettingChangedListener;
import ch.rgw.tools.ExHandler;

/**
 * Connect PersistentObject after NoPo was properly initialized and liquibase
 * was executed (visible due to injection of coreModelService)
 *
 * The unused references assert that we have all pre-conditions met to start
 * PersistentObject
 *
 * If a specific service has to wait for PersistentObjec to become ready, add a
 * reference to this class
 *
 * @since 3.10
 */
@Component(immediate = true, service = PersistentObjectDataSourceActivator.class)
public class PersistentObjectDataSourceActivator {

	@Reference
	private ICoreOperationAdvisor coreOperationAdvisor;

	@Reference(target = "(id=default)")
	private DataSource dataSource;

	@Reference(target = "(id=default)")
	private IElexisEntityManager elexisEntityManager;

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService coreModelService;

	@Reference
	private IContextService contextService;

	@Reference
	private IConfigService configService;

	@Reference
	private IAccessControlService accessControlService;

	private Logger log = LoggerFactory.getLogger(getClass());

	@Activate
	void activate() throws SQLException {
		elexisEntityManager.getEntityManager(true);
		if (!elexisEntityManager.isUpdateSuccess()) {
			coreOperationAdvisor.openInformation("DB Update Fehler",
					"Beim Datenbank Update ist ein Fehler aufgetreten.\n" + "Ihre Datenbank wurde nicht aktualisiert.\n"
							+ "Details dazu finden Sie in der log Datei.");
		}

		log.debug("PersistentObject#connect");
		boolean connect = PersistentObject.connect(dataSource);
		if (!connect) {
			throw new IllegalStateException("Connect to database failed", new Throwable());
		}

		log.debug("PersistentObject#legacyPostInitDB");
		boolean legacyPostInitDB = legacyPostInitDB(coreOperationAdvisor);
		if (!legacyPostInitDB) {
			throw new IllegalStateException("legacyPostInitDB failed", new Throwable());
		}

	}

	/**
	 * Extracted from PersistentObject
	 *
	 * @param coreOperationAdvisor
	 * @return
	 */
	private boolean legacyPostInitDB(ICoreOperationAdvisor coreOperationAdvisor) {
		// globalCfg is null for the firstStart
		// created is null after aborted firstStart

		DBConnection defaultConnection = PersistentObject.getDefaultConnection();

		if (CoreHub.globalCfg == null || CoreHub.globalCfg.get("created", null) == null) {

			log.info("PO data initialization");
			try {
				PersistentObjectUtil.initializeGlobalCfg(defaultConnection);
				Mandant.initializeAdministratorUser(accessControlService, coreModelService);
				Mandant bypassMandator = PersistentObjectUtil
						.autoCreateFirstMandant(defaultConnection.isRunningFromScratch() || CoreUtil.isTestMode());

				if (bypassMandator == null) {
					coreOperationAdvisor.requestInitialMandatorConfiguration();
					MessageEvent.fireInformation("Neue Datenbank", //$NON-NLS-1$
							"Es wurde eine neue Datenbank angelegt."); //$NON-NLS-1$
				} else {
					// When running from Scratch or bypassing the first mandant we
					// do not want to pop up any message dialog before
					// Elexis finished the startup. A log entry is okay
					log.info("Bypassed mandator initialization dialog, auto-created Mandator [{}] {}", //$NON-NLS-1$
							bypassMandator.getId(), bypassMandator.getPersonalia());
				}
				CoreHub.globalCfg.flush();
				CoreHub.localCfg.flush();
			} catch (Throwable ex) {
				ExHandler.handle(ex);
				return false;
			}
		}

		CoreHub.globalCfg.setSettingChangedListener(new ISettingChangedListener() {

			@Override
			public void settingRemoved(String key) {
				Trace.addTraceEntry("W globalCfg key [" + key + "] => removed");
			}

			@Override
			public void settingWritten(String key, String value) {
				Trace.addTraceEntry("W globalCfg key [" + key + "] => value [" + value + "]");
			}

		});

		// verify locale
		Locale locale = Locale.getDefault();
		String dbStoredLocale = CoreHub.globalCfg.get(Preferences.CFG_LOCALE, null);
		if (dbStoredLocale == null) {
			CoreHub.globalCfg.set(Preferences.CFG_LOCALE, locale.toString());
			CoreHub.globalCfg.flush();
		} else {
			if (!locale.toString().equals(dbStoredLocale)) {
				String msg = String.format(
						"Your locale [%1s] does not match the required database locale [%2s] as specified in config table. Ignore?",
						locale.toString(), dbStoredLocale);
				log.error(msg);
				if (!CoreOperationAdvisorHolder.get().openQuestion("Difference in locale setting ", msg)) {
					System.exit(2);
				} else {
					log.error("User continues with difference locale set");
				}
			}
		}

		return true;
	}

}
