package ch.elexis.core.model.util.internal;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.jpa.entities.EntityWithId;
import ch.elexis.core.jpa.entities.Userconfig;
import ch.elexis.core.jpa.model.adapter.AbstractModelService;
import ch.elexis.core.model.Config;
import ch.elexis.core.model.DocumentLetter;
import ch.elexis.core.model.IConfig;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IDiagnosis;
import ch.elexis.core.model.IDiagnosisReference;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.IUserConfig;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.model.service.CoreModelAdapterFactory;
import ch.elexis.core.model.service.holder.ContextServiceHolder;
import ch.elexis.core.model.service.holder.CoreModelServiceHolder;
import ch.elexis.core.model.service.holder.StoreToStringServiceHolder;
import ch.elexis.core.services.IContext;
import ch.elexis.core.services.INamedQuery;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.IStoreToStringContribution;
import ch.rgw.tools.MimeTool;

/**
 * Utility class with core model specific methods
 * 
 * @author thomas
 *
 */
public class ModelUtil {
	
	private static Logger logger = LoggerFactory.getLogger(ModelUtil.class);
	
	/**
	 * Get the file extension part of the input String.
	 * 
	 * @param input
	 * @return
	 */
	public static String evaluateFileExtension(String input){
		String ext = MimeTool.getExtension(input);
		if (StringUtils.isEmpty(ext)) {
			ext = FilenameUtils.getExtension(input);
			if (StringUtils.isEmpty(ext)) {
				ext = input;
			}
		}
		return ext;
	}
	
	/**
	 * Test if configuration of {@value Preferences#P_TEXT_EXTERN_FILE} is set and pointing to a
	 * valid directory.
	 * 
	 * @return
	 */
	public static boolean isExternFile(){
		if (isConfig(Preferences.P_TEXT_EXTERN_FILE, false)) {
			String path = getConfig(Preferences.P_TEXT_EXTERN_FILE_PATH, null);
			if (path != null) {
				return pathExistsAndCanWrite(path, true);
			}
		}
		return false;
	}
	
	/**
	 * Read the configured {@value Preferences#P_TEXT_EXTERN_FILE_PATH} and return a {@link File}
	 * representation of the document.
	 * 
	 * @param documentBrief
	 * @return
	 */
	public static Optional<File> getExternFile(DocumentLetter documentBrief){
		String path = getConfig(Preferences.P_TEXT_EXTERN_FILE_PATH, null);
		if (pathExistsAndCanWrite(path, true)) {
			File dir = new File(path);
			StringBuilder sb = new StringBuilder();
			IPatient patient = documentBrief.getPatient();
			if (patient != null) {
				sb.append(patient.getPatientNr()).append(File.separator)
					.append(documentBrief.getId())
					.append("." + evaluateFileExtension(documentBrief.getMimeType()));
				File ret = new File(dir, sb.toString());
				if (ret.exists() && ret.isFile()) {
					return Optional.of(ret);
				} else {
					logger.warn("File [" + ret.getAbsolutePath() + "] not valid e=" + ret.exists()
						+ " f=" + ret.isFile());
				}
			} else {
				logger.warn("No patient for [" + documentBrief.getId() + "]");
			}
		}
		return Optional.empty();
	}
	
	/**
	 * Create a external file at the directory configured with
	 * {@value Preferences#P_TEXT_EXTERN_FILE_PATH} and the patient number.
	 * 
	 * @param documentBrief
	 * @return
	 */
	public static Optional<File> createExternFile(DocumentLetter documentBrief){
		String path = getConfig(Preferences.P_TEXT_EXTERN_FILE_PATH, null);
		if (pathExistsAndCanWrite(path, true)) {
			File dir = new File(path);
			IPatient patient = documentBrief.getPatient();
			if (patient != null) {
				File patPath = new File(dir, patient.getPatientNr());
				if (!patPath.exists()) {
					patPath.mkdirs();
				}
				File ret = new File(patPath, documentBrief.getId() + "."
					+ evaluateFileExtension(documentBrief.getMimeType()));
				if (!ret.exists()) {
					try {
						ret.createNewFile();
					} catch (IOException e) {
						logger.error("Error creating file", e);
						return Optional.empty();
					}
				}
				return Optional.of(ret);
			} else {
				logger.warn("No patient for [" + documentBrief.getId() + "]");
			}
		}
		return Optional.empty();
	}
	
	/**
	 * Test if the path exists is directory and application has write permissions.
	 * 
	 * @param path
	 * @param log
	 * @return
	 */
	public static boolean pathExistsAndCanWrite(String path, boolean log){
		if (path != null) {
			File dir = new File(path);
			if (dir.exists() && dir.isDirectory() && dir.canWrite()) {
				return true;
			} else {
				if (log) {
					logger.warn("Configured path [" + path + "] not valid e=" + dir.exists() + " d="
						+ dir.isDirectory() + " w=" + dir.canWrite());
				}
			}
		} else if (log) {
			logger.warn("No path configured");
		}
		return false;
	}
	
	/**
	 * Test if there is a matching {@link Config} entry with a value that can be interpreted as
	 * true. If no {@link Config} is present defaultValue is returned.
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static boolean isConfig(String key, boolean defaultValue){
		Optional<IConfig> loaded = CoreModelServiceHolder.get().load(key, IConfig.class);
		if (loaded.isPresent()) {
			String value = loaded.get().getValue();
			return value != null && (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("1"));
		} else {
			return defaultValue;
		}
	}
	
	/**
	 * Test if there is a matching {@link Userconfig} entry for the owner, with a value that can be
	 * interpreted as true. If no {@link Userconfig} entry is present defaultValue is returned.
	 * 
	 * @param owner
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static boolean isUserConfig(IContact owner, String key, boolean defaultValue){
		if (owner != null) {
			INamedQuery<IUserConfig> configQuery = CoreModelServiceHolder.get()
				.getNamedQuery(IUserConfig.class, true, "ownerid", "param");
			List<IUserConfig> configs = configQuery.executeWithParameters(CoreModelServiceHolder
				.get().getParameterMap("ownerid", owner.getId(), "param", key));
			if (configs.isEmpty()) {
				return defaultValue;
			} else {
				IConfig config = configs.get(0);
				if (configs.size() > 1) {
					logger.warn("Multiple user config entries for [" + key + "] using first.");
				}
				String value = config.getValue();
				return value != null
					&& (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("1"));
			}
		}
		logger.warn("No user contact for query of key [" + key + "] returning default");
		return defaultValue;
	}
	
	/**
	 * Get a matching {@link Config} entry and return its value. If no {@link Config} is present
	 * defaultValue is returned.
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static String getConfig(String key, String defaultValue){
		IQuery<IConfig> configQuery = CoreModelServiceHolder.get().getQuery(IConfig.class);
		configQuery.and(ModelPackage.Literals.ICONFIG__KEY, COMPARATOR.EQUALS,
			Preferences.P_TEXT_EXTERN_FILE);
		List<IConfig> configs = configQuery.execute();
		if (configs.isEmpty()) {
			return defaultValue;
		} else {
			IConfig config = configs.get(0);
			if (configs.size() > 1) {
				logger.warn("Multiple config entries for [" + key + "] using first.");
			}
			return config.getValue();
		}
	}
	
	/**
	 * Get the active {@link IContact} of the active {@link IUser} from the root {@link IContext}.
	 * 
	 * @return
	 */
	public static Optional<IContact> getActiveUserContact(){
		if (ContextServiceHolder.isPresent()) {
			Optional<IContact> ret =
				ContextServiceHolder.get().getRootContext().getActiveUserContact();
			if (ret.isPresent()) {
				return ret;
			} else {
				Optional<IUser> user =
					ContextServiceHolder.get().getRootContext().getActiveUser();
				if (user.isPresent()) {
					return Optional.ofNullable(user.get().getAssignedContact());
				}
			}
		} else {
			logger.warn("No IContextService available.");
		}
		return Optional.empty();
	}
	
	/**
	 * Get a {@link IQuery} instance for the provided interfaceClazz.
	 * 
	 * @param interfaceClazz
	 * @return
	 */
	public static <T> IQuery<T> getQuery(Class<T> interfaceClazz){
		return CoreModelServiceHolder.get().getQuery(interfaceClazz);
	}
	
	/**
	 * Load the object using the core model service
	 * 
	 * @param objectId
	 * @param clazz
	 * @return
	 */
	public static <T> T load(String objectId, Class<T> clazz){
		Optional<T> ret = CoreModelServiceHolder.get().load(objectId, clazz);
		return ret.orElse(null);
	}
	
	/**
	 * Wrap the entity in a new ModelAdapter matching the provided type clazz. If entity is null,
	 * null is returned.
	 * 
	 * @param entity
	 * @param clazz
	 * @return
	 */
	public static <T> T getAdapter(EntityWithId entity, Class<T> clazz){
		return getAdapter(entity, clazz, false);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getAdapter(EntityWithId entity, Class<T> clazz,
		boolean registerEntityChangeEvent){
		if (entity != null) {
			Optional<Identifiable> adapter =
				CoreModelAdapterFactory.getInstance().getModelAdapter(entity, clazz, true,
					registerEntityChangeEvent);
			return (T) adapter.orElse(null);
		}
		return null;
	}
	
	/**
	 * verify whether the proposed username is not already in use
	 * 
	 * @param username
	 * @return <code>true</code> if the given username may be used
	 */
	public static boolean verifyUsernameNotTaken(String username){
		return !CoreModelServiceHolder.get().load(username, IUser.class).isPresent();
	}
	
	public static AbstractModelService getModelService(){
		return (AbstractModelService) CoreModelServiceHolder.get();
	}
	
	public static Optional<Identifiable> getFromStoreToString(String storeToString){
		return StoreToStringServiceHolder.get().loadFromString(storeToString);
	}
	
	public static Optional<String> getStoreToString(Identifiable identifiable){
		return StoreToStringServiceHolder.get().storeToString(identifiable);
	}
	
	public static IDiagnosis getOrCreateDiagnosisReference(IDiagnosis diagnosis){
		Optional<String> storeToString = StoreToStringServiceHolder.get().storeToString(diagnosis);
		if (storeToString.isPresent()) {
			String[] parts = storeToString.get().split(IStoreToStringContribution.DOUBLECOLON);
			INamedQuery<IDiagnosisReference> query = CoreModelServiceHolder.get()
				.getNamedQuery(IDiagnosisReference.class, "code", "diagnosisClass");
			List<IDiagnosisReference> existing = query.executeWithParameters(CoreModelServiceHolder
				.get().getParameterMap("code", diagnosis.getCode(), "diagnosisClass", parts[0]));
			if (!existing.isEmpty()) {
				return existing.get(0);
			} else {
				IDiagnosisReference reference =
					CoreModelServiceHolder.get().create(IDiagnosisReference.class);
				reference.setCode(diagnosis.getCode());
				reference.setReferredClass(parts[0]);
				reference.setText(diagnosis.getText());
			}
		}
		return null;
	}
}
