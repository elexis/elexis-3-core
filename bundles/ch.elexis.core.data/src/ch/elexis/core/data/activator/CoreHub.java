/*******************************************************************************
 * Copyright (c) 2015-2016 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.data.activator;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.equinox.internal.app.CommandLineArgs;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.Desk;
import ch.elexis.admin.AbstractAccessControl;
import ch.elexis.admin.RoleBasedAccessControl;
import ch.elexis.core.constants.Elexis;
import ch.elexis.core.constants.ElexisSystemPropertyConstants;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.events.Heartbeat;
import ch.elexis.core.data.events.PatientEventListener;
import ch.elexis.core.data.interfaces.ShutdownJob;
import ch.elexis.core.data.interfaces.events.MessageEvent;
import ch.elexis.core.data.interfaces.scripting.Interpreter;
import ch.elexis.core.data.preferences.CorePreferenceInitializer;
import ch.elexis.core.data.service.ContextServiceHolder;
import ch.elexis.core.data.service.LocalLockServiceHolder;
import ch.elexis.core.jdt.Nullable;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IUser;
import ch.elexis.core.services.IAccessControlService;
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.holder.ElexisServerServiceHolder;
import ch.elexis.data.Anwender;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Mandant;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.PersistentObjectFactory;
import ch.elexis.data.Query;
import ch.rgw.io.Settings;
import ch.rgw.io.SqlSettings;
import ch.rgw.io.SysSettings;
import ch.rgw.tools.Log;
import ch.rgw.tools.StringTool;

/**
 * @since 3.0.0
 */
public class CoreHub implements BundleActivator {
	public static final String PLUGIN_ID = "ch.elexis.core.data";
	/*
	 * This version is needed to compare the DB
	 */
	public static String Version = Elexis.VERSION;
	public static final String APPLICATION_NAME = Elexis.APPLICATION_NAME; // $NON-NLS-1$

	protected static Logger log = LoggerFactory.getLogger(CoreHub.class.getName());

	private static String LocalCfgFile = null;

	private BundleContext context;

	/** Das Singleton-Objekt dieser Klasse */
	public static CoreHub plugin;

	private static List<ShutdownJob> shutdownJobs = new LinkedList<>();

	private static String stationIdentifier;

	/** Factory für interne PersistentObjects */
	public static final PersistentObjectFactory poFactory = new PersistentObjectFactory();

	/** Heartbeat */
	public static Heartbeat heart;

	/**
	 * Beschreibbares Verzeichnis für userspezifische Konfigurationsdaten etc.
	 * Achtung: "User" meint hier: den eingeloggten Betriebssystem-User, nicht den
	 * Elexis-User. In Windows wird userDir meist %USERPROFILE%\elexis sein, in
	 * Linux ~./elexis. Es kann mit getWritableUserDir() geholt werden.
	 */
	private static File userDir;

	/**
	 * Globale Einstellungen (Werden in der Datenbank gespeichert)
	 *
	 * @deprecated use {@link IConfigService}
	 */
	public static Settings globalCfg;

	/**
	 * Lokale Einstellungen (Werden in userhome/localCfg_xxx.xml gespeichert) </br>
	 * <b>WARNING: can not handle more than one / in config name!</b>
	 *
	 * @deprecated use {@link IConfigService}
	 */
	public static Settings localCfg;

	/**
	 * @deprecated please use {@link ElexisEventDispatcher#getSelectedMandator()} to
	 *             retrieve current mandator
	 */
	@Deprecated(forRemoval = true)
	public static Mandant actMandant;

	/** Der Initialisierer für die Voreinstellungen */
	public static final CorePreferenceInitializer pin = new CorePreferenceInitializer();

	/**
	 * Die zentrale Zugriffskontrolle
	 *
	 * @deprecated use {@link IAccessControlService}
	 */
	@Deprecated(forRemoval = true)
	public static final AbstractAccessControl acl = new RoleBasedAccessControl();

	/**
	 * The listener for patient events
	 */
	private final PatientEventListener eeli_pat = new PatientEventListener();

	/**
	 * Returns the actual contact of the logged in User. Use it only for PO
	 * compatibility instead use {@link IContextService#getActiveUser()} directly.
	 *
	 * @return the {@link Anwender} or null if no contact is present
	 * @since 3.8
	 */
	public static @Nullable Anwender getLoggedInContact() {
		Optional<IContact> userContact = ContextServiceHolder.get().getActiveUserContact();
		if (userContact.isPresent()) {
			return Anwender.load(userContact.get().getId());
		}
		return null;
	}

	/**
	 * get the base directory of this currently running elexis application
	 *
	 * @return the topmost directory of this application or null if this information
	 *         could not be retrieved
	 */
	public static String getBasePath() {
		return FrameworkUtil.getBundle(CoreHub.class).getEntry("/").toString();
	}

	/**
	 * Return a directory suitable for temporary files. Most probably this will be a
	 * default tempdir provided by the os. If none such exists, it will be the user
	 * dir.
	 *
	 * @return always a valid and writable directory.
	 */
	public static File getTempDir() {
		File ret = null;
		String temp = System.getProperty("java.io.tmpdir"); //$NON-NLS-1$
		if (!StringTool.isNothing(temp)) {
			ret = new File(temp);
			if (ret.exists() && ret.isDirectory()) {
				return ret;
			} else {
				if (ret.mkdirs()) {
					return ret;
				}
			}
		}
		return getWritableUserDir();
	}

	/**
	 * Initialize the user dir on startup
	 *
	 * @since 3.10 extracted from {@link #getWritableUserDir()}
	 */
	private static void initUserDir() {
		if (CoreHub.userDir == null) {
			String userhome = null;

			if (localCfg != null) {
				userhome = localCfg.get("elexis-userDir", null); //$NON-NLS-1$
			}
			if (userhome == null) {
				userhome = System.getProperty("user.home"); //$NON-NLS-1$
			}
			if (StringTool.isNothing(userhome)) {
				userhome = System.getProperty("java.io.tempdir"); //$NON-NLS-1$
			}
			CoreHub.userDir = new File(userhome, "elexis"); //$NON-NLS-1$
		}
		if (!CoreHub.userDir.exists()) {
			if (!CoreHub.userDir.mkdirs()) {
				System.err.print("fatal: could not create Userdir"); //$NON-NLS-1$
				MessageEvent.fireLoggedError("Panic exit", "could not create userdir " + userDir.getAbsolutePath());
				System.exit(-5);
			}
		}

	}

	/**
	 * return a directory suitable for plugin specific configuration data. If no
	 * such dir exists, it will be created. If it could not be created, the
	 * application will refuse to start.
	 *
	 * @return a directory that exists always and is always writable and readable
	 *         for plugins of the currently running elexis instance. Caution: this
	 *         directory is not necessarily shared among different OS-Users. In
	 *         Windows it is normally %USERPROFILE%\elexis, in Linux ~./elexis
	 */
	public static File getWritableUserDir() {
		return CoreHub.userDir;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		this.context = context;
		log.debug("Starting " + CoreHub.class.getName());
		plugin = this;

		// determine local config
		String[] args = CommandLineArgs.getApplicationArgs();
		String config = "default"; //$NON-NLS-1$
		for (String s : args) {
			if (s.startsWith("--use-config=")) { //$NON-NLS-1$
				String[] c = s.split("="); //$NON-NLS-1$
				config = c[1];
			}
		}
		if (ElexisSystemPropertyConstants.RUN_MODE_FROM_SCRATCH
				.equals(System.getProperty(ElexisSystemPropertyConstants.RUN_MODE))) {
			config = UUID.randomUUID().toString();
		}
		initUserDir();
		loadLocalCfg(config);

		int instanceNo = initializeLock();
		stationIdentifier = CoreHub.localCfg.get(Preferences.STATION_IDENT_ID, "notset_" + System.currentTimeMillis());
		if (instanceNo > 0) {
			stationIdentifier += "$" + instanceNo;
		}

		log.info("Basepath: " + getBasePath());
		pin.initializeDefaultPreferences();

		heart = Heartbeat.getInstance();

		ElexisEventDispatcher.getInstance().addListeners(eeli_pat);

		// add core ClassLoader to default Script Interpreter
		Interpreter.classLoaders.add(CoreHub.class.getClassLoader());

		if (!ElexisSystemPropertyConstants.RUN_MODE_FROM_SCRATCH
				.equals(System.getProperty(ElexisSystemPropertyConstants.RUN_MODE)))
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					SysSettings localCfg = (SysSettings) CoreHub.localCfg;
					localCfg.write_xml(LocalCfgFile);
				}
			});
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		log.debug("Stopping " + CoreHub.class.getName());

		LocalLockServiceHolder.get().releaseAllLocks();
		LocalLockServiceHolder.get().shutdown();

		CoreHub.logoffAnwender();

		PersistentObject.disconnect();
		ElexisEventDispatcher.getInstance().removeListeners(eeli_pat);
		ElexisEventDispatcher.getInstance().dump();

		globalCfg = null;
		heart.stop();

		plugin = null;
		this.context = null;
	}

	/**
	 * Try to get one instance lock of the MAX_LOCKS available. If none is available
	 * anymore, exit.
	 *
	 * @return the instance
	 */
	private int initializeLock() {
		final int MAX_LOCKS = 25;

		FileLock fileLock = null;
		for (int i = 0; i < MAX_LOCKS; i++) {
			// try to get a lock
			String fileName = (i > 0) ? "elexislock." + i : "elexislock";
			File lockFile = new File(getWritableUserDir(), fileName);
			try {
				// do not use try with resources, channel needs to stay open
				@SuppressWarnings("resource")
				FileChannel lockFileChannel = new RandomAccessFile(lockFile, "rw").getChannel();
				fileLock = lockFileChannel.tryLock();
				if (fileLock == null) {
					lockFileChannel.close();
					continue;
				}
				log.debug("Acquired lock on " + fileName);
				return i;
			} catch (IOException ioe) {
				log.error("Can not aquire lock file in " + userDir + "; " + ioe.getMessage()); //$NON-NLS-1$
			}
		}

		log.error("Could not initializeLock()");
		System.exit(-250);
		return -250;
	}

	public static String getId() {
		StringBuilder sb = new StringBuilder();
		sb.append(APPLICATION_NAME).append(" v.").append(Version).append(StringUtils.LF)
				.append(CoreHubHelper.getRevision(true, plugin)).append(StringUtils.LF)
				.append(System.getProperty("os.name")).append(StringConstants.SLASH)
				.append(System.getProperty("os.version")).append(StringConstants.SLASH)
				.append(System.getProperty("os.arch")); //$NON-NLS-1$
		return sb.toString();
	}

	private void loadLocalCfg(String branch) {
		LocalCfgFile = CoreHub.userDir + "/localCfg_" + branch + ".xml";
		String msg = "loadLocalCfg: Loading branch " + branch + " from " + LocalCfgFile;
		System.out.println(msg);
		log.debug(msg);
		SysSettings cfg = SysSettings.getOrCreate(SysSettings.USER_SETTINGS, Desk.class);
		cfg.read_xml(LocalCfgFile);
		CoreHub.localCfg = cfg;
	}

	public static void setMandant(Mandant newMandant) {
		actMandant = newMandant;

		ElexisEventDispatcher.getInstance()
				.fire(new ElexisEvent(newMandant, Mandant.class, ElexisEvent.EVENT_MANDATOR_CHANGED));
	}

	/**
	 *
	 * @param user
	 * @return
	 * @since 3.8 a Kontakt does not have to formally declare IS_USER anymore, any
	 *        Kontakt can have user config entries
	 */
	public static Settings getUserSetting(Kontakt user) {
		Settings settings = new SqlSettings(PersistentObject.getConnection(), "USERCONFIG", "Param", "Value",
				"UserID=" + user.getWrappedId());
		return settings;
	}

	public Bundle getBundle() {
		return context.getBundle();
	}

	/**
	 * get a list of all mandators known to this system
	 *
	 * @since 3.7 does exclude mandators that are marked set as inactive
	 */
	public static List<Mandant> getMandantenList() {
		Query<Mandant> qbe = new Query<>(Mandant.class);
		return qbe.execute().parallelStream().filter(m -> !m.isInactive()).collect(Collectors.toList());
	}

	/**
	 * get a list of all users known to this system
	 */
	public static List<Anwender> getUserList() {
		Query<Anwender> qbe = new Query<>(Anwender.class);
		return qbe.execute();
	}

	/**
	 * Return the name of a config instance, the user chose. This is just the valuie
	 * of the -Dconfig=xx runtime value or "default" if no -Dconfig was set
	 */
	public static String getCfgVariant() {
		String config = System.getProperty("config");
		return config == null ? "default" : config;
	}

	public void setUserDir(File dir) {
		userDir = dir;
		localCfg.set("elexis-userDir", dir.getAbsolutePath()); //$NON-NLS-1$
	}

	/**
	 * Add a ShutdownJob to the list of jobs that has to be done after the Elexis
	 * workbench was shut down.
	 *
	 * @param job
	 */
	public static void addShutdownJob(final ShutdownJob job) {
		if (!shutdownJobs.contains(job)) {
			shutdownJobs.add(job);
		}
	}

	public static int getSystemLogLevel() {
		return localCfg.get(Preferences.ABL_LOGLEVEL, Log.ERRORS);
	}

	/**
	 * @since 3.8
	 */
	public static void reconfigureServices() {
		ElexisServerServiceHolder.get().validateElexisServerConnection();
	}

	/**
	 * Perform the required tasks to log off the current {@link Anwender}
	 *
	 * @since 3.1 moved from {@link Anwender} class
	 */
	public static void logoffAnwender() {
		if (CoreHub.getLoggedInContact() == null)
			return;

		LocalLockServiceHolder.get().releaseAllLocks();

		CoreHub.setMandant(null);
		CoreHub.heart.suspend();
		ContextServiceHolder.get().setActiveUser(null);
		ElexisEventDispatcher.getInstance().fire(new ElexisEvent(null, Anwender.class, ElexisEvent.EVENT_USER_CHANGED));
		ElexisEventDispatcher.getInstance().fire(new ElexisEvent(null, IUser.class, ElexisEvent.EVENT_DESELECTED));
	}

	public static Object getStationIdentifier() {
		return stationIdentifier;
	}

}
