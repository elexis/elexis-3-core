/*******************************************************************************
 * Copyright (c) 2013 MEDEVIT <office@medevit.at>.
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
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.Desk;
import ch.elexis.admin.AccessControl;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.constants.ElexisSystemPropertyConstants;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.events.Heartbeat;
import ch.elexis.core.data.events.Heartbeat.HeartListener;
import ch.elexis.core.data.events.PatientEventListener;
import ch.elexis.core.data.extension.CoreOperationExtensionPoint;
import ch.elexis.core.data.interfaces.ShutdownJob;
import ch.elexis.core.data.interfaces.events.MessageEvent;
import ch.elexis.core.data.interfaces.scripting.Interpreter;
import ch.elexis.core.data.preferences.CorePreferenceInitializer;
import ch.elexis.core.data.util.PlatformHelper;
import ch.elexis.data.Anwender;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Mandant;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.PersistentObjectFactory;
import ch.elexis.data.Query;
import ch.rgw.io.LockFile;
import ch.rgw.io.Settings;
import ch.rgw.io.SqlSettings;
import ch.rgw.io.SysSettings;
import ch.rgw.tools.Log;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.VersionInfo;

/**
 * @since 3.0.0
 */
public class CoreHub implements BundleActivator {
	public static final String PLUGIN_ID = "ch.elexis.core.data";
	/*
	 * This version is needed to compare the DB
	 */
	public static String Version = "3.0.0.qualifier"; //$NON-NLS-1$
	public static final String APPLICATION_NAME = "Elexis Core"; //$NON-NLS-1$
	static final String neededJRE = "1.7.0"; //$NON-NLS-1$
	public static final String DBVersion = "3.0.0"; //$NON-NLS-1$
	
	protected static Logger log = LoggerFactory.getLogger(CoreHub.class.getName());
	
	private static String LocalCfgFile = null;
	
	private BundleContext context;
	
	/** Das Singleton-Objekt dieser Klasse */
	public static CoreHub plugin;
	
	private static List<ShutdownJob> shutdownJobs = new LinkedList<ShutdownJob>();
	
	/** Factory für interne PersistentObjects */
	public static final PersistentObjectFactory poFactory = new PersistentObjectFactory();
	
	/** Heartbeat */
	public static Heartbeat heart;
	
	/**
	 * Beschreibbares Verzeichnis für userspezifische Konfigurationsdaten etc. Achtung: "User" meint
	 * hier: den eingeloggten Betriebssystem-User, nicht den Elexis-User. In Windows wird userDir
	 * meist %USERPROFILE%\elexis sein, in Linux ~./elexis. Es kann mit getWritableUserDir() geholt
	 * werden.
	 * */
	static File userDir;
	
	/** Globale Einstellungen (Werden in der Datenbank gespeichert) */
	public static Settings globalCfg;
	
	/** Lokale Einstellungen (Werden in der Registry bzw. ~/.java gespeichert) */
	public static Settings localCfg;
	
	/** Anwenderspezifische Einstellungen (Werden in der Datenbank gespeichert) */
	public static Settings userCfg;
	
	/** Mandantspezifische EInstellungen (Werden in der Datenbank gespeichert) */
	public static Settings mandantCfg;
	
	public static Anwender actUser; // TODO set
	/**
	 * @deprecated please use {@link ElexisEventDispatcher#getSelected(Mandant.class)} to retrieve
	 *             current mandator
	 */
	public static Mandant actMandant;
	
	/** Der Initialisierer für die Voreinstellungen */
	public static final CorePreferenceInitializer pin = new CorePreferenceInitializer();
	
	/** Die zentrale Zugriffskontrolle */
	public static final AccessControl acl = new AccessControl();
	
	/**
	 * The listener for patient events
	 */
	private final PatientEventListener eeli_pat = new PatientEventListener();
	
	/**
	 * get the base directory of this currently running elexis application
	 * 
	 * @return the topmost directory of this application or null if this information could not be
	 *         retrieved
	 */
	public static String getBasePath(){
		return PlatformHelper.getBasePath(PLUGIN_ID);
	}
	
	/**
	 * Return a directory suitable for temporary files. Most probably this will be a default tempdir
	 * provided by the os. If none such exists, it will be the user dir.
	 * 
	 * @return always a valid and writable directory.
	 */
	public static File getTempDir(){
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
	 * return a directory suitable for plugin specific configuration data. If no such dir exists, it
	 * will be created. If it could not be created, the application will refuse to start.
	 * 
	 * @return a directory that exists always and is always writable and readable for plugins of the
	 *         currently running elexis instance. Caution: this directory is not necessarily shared
	 *         among different OS-Users. In Windows it is normally %USERPROFILE%\elexis, in Linux
	 *         ~./elexis
	 */
	public static File getWritableUserDir(){
		if (userDir == null) {
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
			userDir = new File(userhome, "elexis"); //$NON-NLS-1$
		}
		if (!userDir.exists()) {
			if (!userDir.mkdirs()) {
				System.err.print("fatal: could not create Userdir"); //$NON-NLS-1$
				MessageEvent.fireLoggedError("Panic exit",
					"could not create userdir " + userDir.getAbsolutePath());
				System.exit(-5);
			}
		}
		return userDir;
	}
	
	@Override
	public void start(BundleContext context) throws Exception{
		this.context = context;
		log.debug("Starting " + CoreHub.class.getName());
		plugin = this;
		
		// Check if we "are complete" - throws Error if not
		CoreOperationExtensionPoint.getCoreOperationAdvisor();
		
		startUpBundle();
		setUserDir(userDir);
		heart = Heartbeat.getInstance();
		
		ElexisEventDispatcher.getInstance().addListeners(eeli_pat);
		
		// add core ClassLoader to default Script Interpreter
		Interpreter.classLoaders.add(CoreHub.class.getClassLoader());
		
		if (!ElexisSystemPropertyConstants.RUN_MODE_FROM_SCRATCH.equals(System
			.getProperty(ElexisSystemPropertyConstants.RUN_MODE)))
			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run(){
					SysSettings localCfg = (SysSettings) CoreHub.localCfg;
					localCfg.write_xml(LocalCfgFile);
				}
			});
	}
	
	/*
	 * We use maven resources filtering replace in rsc/version.properties the property
	 * ${pom.version} by the current build version and place it into the file /version.properties of
	 * each jar file.
	 * 
	 * See http://maven.apache.org /plugins/maven-resources-plugin/examples/filter.html
	 */
	public static String readElexisBuildVersion(){
		Properties prop = new Properties();
		String elexis_version = "Developer";
		String url_name = "platform:/plugin/ch.elexis.core.data/version.properties";
		try {
			URL url;
			url = new URL(url_name);
			InputStream inputStream = url.openConnection().getInputStream();
			if (inputStream != null) {
				prop.load(inputStream);
				elexis_version = prop.getProperty("elexis.version");
			}
		} catch (IOException e) {
			log.warn("Error reading build version information from " + url_name);
		}
		return elexis_version.replace("-SNAPSHOT", "");
	}
	
	@Override
	public void stop(BundleContext context) throws Exception{
		log.debug("Stopping " + CoreHub.class.getName());
		if (CoreHub.actUser != null) {
			Anwender.logoff();
		}
		
		PersistentObject.disconnect();
		ElexisEventDispatcher.getInstance().removeListeners(eeli_pat);
		ElexisEventDispatcher.getInstance().dump();
		
		globalCfg = null;
		heart.stop();
		
		plugin = null;
		this.context = null;
	}
	
	private void startUpBundle(){
		String[] args = Platform.getApplicationArgs();
		String config = "default"; //$NON-NLS-1$
		for (String s : args) {
			if (s.startsWith("--use-config=")) { //$NON-NLS-1$
				String[] c = s.split("="); //$NON-NLS-1$
				config = c[1];
			}
		}
		if (ElexisSystemPropertyConstants.RUN_MODE_FROM_SCRATCH.equals(System
			.getProperty(ElexisSystemPropertyConstants.RUN_MODE))) {
			config = UUID.randomUUID().toString();
		}
		loadLocalCfg(config);
		
		// Damit Anfragen auf userCfg und mandantCfg bei nicht eingeloggtem User
		// keine NPE werfen
		userCfg = localCfg;
		mandantCfg = localCfg;
		
		// Java Version prüfen
		VersionInfo vI = new VersionInfo(System.getProperty("java.version", "0.0.0")); //$NON-NLS-1$ //$NON-NLS-2$
		log.info(getId() + "; Java: " + vI.version() + "\nencoding: "
			+ System.getProperty("file.encoding"));
		
		if (vI.isOlder(neededJRE)) {
			MessageEvent.fireLoggedError("Invalid Java version", "Your Java version is older than "
				+ neededJRE + ", please update.");
		}
		log.info("Basepath: " + getBasePath());
		pin.initializeDefaultPreferences();
		
		heart = Heartbeat.getInstance();
		initializeLock();
	}
	
	private static void initializeLock(){
		final int timeoutSeconds = 600;
		try {
			final LockFile lockfile = new LockFile(userDir, "elexislock", 4, timeoutSeconds); //$NON-NLS-1$
			final int n = lockfile.lock();
			if (n == 0) {
				MessageEvent.fireLoggedError("Too many instances",
					"Too many concurrent instances of Elexis running. Will exit.");
				log.error("Too many concurent instances. Check elexis.lock files in " + userDir);
				System.exit(2);
			} else {
				HeartListener lockListener = new HeartListener() {
					long timeSet;
					
					public void heartbeat(){
						long now = System.currentTimeMillis();
						if ((now - timeSet) > timeoutSeconds) {
							lockfile.updateLock(n);
							timeSet = now;
						}
					}
				};
				heart.addListener(lockListener, Heartbeat.FREQUENCY_LOW);
			}
		} catch (IOException ex) {
			log.error("Can not aquire lock file in " + userDir + "; " + ex.getMessage()); //$NON-NLS-1$
		}
	}
	
	public static String getId(){
		StringBuilder sb = new StringBuilder();
		sb.append(APPLICATION_NAME).append(" v.").append(Version).append("\n")
			.append(CoreHubHelper.getRevision(true, plugin)).append("\n")
			.append(System.getProperty("os.name")).append(StringConstants.SLASH)
			.append(System.getProperty("os.version")).append(StringConstants.SLASH)
			.append(System.getProperty("os.arch")); //$NON-NLS-1$
		return sb.toString();
	}
	
	private void loadLocalCfg(String branch){
		LocalCfgFile = CoreHubHelper.getWritableUserDir() + "/localCfg_" + branch + ".xml";
		String msg = "loadLocalCfg: Loading branch " + branch + " from " + LocalCfgFile;
		System.out.println(msg);
		log.debug(msg);
		SysSettings cfg = new SysSettings(SysSettings.USER_SETTINGS, Desk.class);
		cfg.read_xml(LocalCfgFile);
		CoreHub.localCfg = cfg;
	}
	
	public static void setMandant(Mandant newMandant){
		if (actMandant != null && mandantCfg != null) {
			mandantCfg.flush();
		}
		if (newMandant == null) {
			mandantCfg = userCfg;
		} else {
			mandantCfg = getUserSetting(newMandant);
		}
		
		actMandant = newMandant;
		
		ElexisEventDispatcher.getInstance().fire(
			new ElexisEvent(newMandant, Mandant.class, ElexisEvent.EVENT_MANDATOR_CHANGED));
	}
	
	public static Settings getUserSetting(Kontakt user){
		if (StringConstants.ONE.equals(user.get(Kontakt.FLD_IS_USER))) {
			Settings settings =
				new SqlSettings(PersistentObject.getConnection(), "USERCONFIG", "Param", "Value",
					"UserID=" + user.getWrappedId());
			return settings;
		}
		return null;
	}
	
	public Bundle getBundle(){
		return context.getBundle();
	}
	
	/**
	 * get a list of all mandators known to this system
	 */
	public static List<Mandant> getMandantenList(){
		Query<Mandant> qbe = new Query<Mandant>(Mandant.class);
		return qbe.execute();
	}
	
	/**
	 * get a list of all users known to this system
	 */
	public static List<Anwender> getUserList(){
		Query<Anwender> qbe = new Query<Anwender>(Anwender.class);
		return qbe.execute();
	}
	
	/**
	 * Return the name of a config instance, the user chose. This is just the valuie of the
	 * -Dconfig=xx runtime value or "default" if no -Dconfig was set
	 */
	public static String getCfgVariant(){
		String config = System.getProperty("config");
		return config == null ? "default" : config;
	}
	
	public void setUserDir(File dir){
		userDir = dir;
		localCfg.set("elexis-userDir", dir.getAbsolutePath()); //$NON-NLS-1$
	}
	
	/**
	 * Add a ShutdownJob to the list of jobs that has to be done after the Elexis workbench was shut
	 * down.
	 * 
	 * @param job
	 */
	public static void addShutdownJob(final ShutdownJob job){
		if (!shutdownJobs.contains(job)) {
			shutdownJobs.add(job);
		}
	}
	
	public static int getSystemLogLevel(){
		return localCfg.get(Preferences.ABL_LOGLEVEL, Log.ERRORS);
	}
}
