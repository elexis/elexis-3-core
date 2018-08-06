package ch.elexis.core.model.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.jpa.entities.EntityWithId;
import ch.elexis.core.jpa.entities.Userconfig;
import ch.elexis.core.jpa.model.adapter.AbstractModelService;
import ch.elexis.core.model.Config;
import ch.elexis.core.model.DocumentBrief;
import ch.elexis.core.model.IConfig;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.IUserConfig;
import ch.elexis.core.model.IXid;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.model.service.CoreModelAdapterFactory;
import ch.elexis.core.services.IContext;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.INamedQuery;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.rgw.compress.CompEx;
import ch.rgw.tools.MimeTool;

@Component
public class ModelUtil {
	
	private static Logger logger = LoggerFactory.getLogger(ModelUtil.class);
	
	private static IModelService modelService;
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	public void setModelService(IModelService modelService){
		ModelUtil.modelService = modelService;
	}
	
	private static IContextService contextService;
	
	@Reference(cardinality = ReferenceCardinality.OPTIONAL)
	public void setContextService(IContextService contextService){
		ModelUtil.contextService = contextService;
	}
	
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
	public static Optional<File> getExternFile(DocumentBrief documentBrief){
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
					LoggerFactory.getLogger(ModelUtil.class).warn("File [" + ret.getAbsolutePath()
						+ "] not valid e=" + ret.exists() + " f=" + ret.isFile());
				}
			} else {
				LoggerFactory.getLogger(ModelUtil.class)
					.warn("No patient for [" + documentBrief.getId() + "]");
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
	public static Optional<File> createExternFile(DocumentBrief documentBrief){
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
						LoggerFactory.getLogger(ModelUtil.class).error("Error creating file", e);
						return Optional.empty();
					}
				}
				return Optional.of(ret);
			} else {
				LoggerFactory.getLogger(ModelUtil.class)
					.warn("No patient for [" + documentBrief.getId() + "]");
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
					LoggerFactory.getLogger(ModelUtil.class)
						.warn("Configured path [" + path + "] not valid e=" + dir.exists() + " d="
							+ dir.isDirectory() + " w=" + dir.canWrite());
				}
			}
		} else if (log) {
			LoggerFactory.getLogger(ModelUtil.class).warn("No path configured");
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
		Optional<IConfig> loaded = modelService.load(key, IConfig.class);
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
			INamedQuery<IUserConfig> configQuery =
				modelService.getNamedQuery(IUserConfig.class, true, "owner", "param");
			List<IUserConfig> configs = configQuery
				.executeWithParameters(modelService.getParameterMap("owner", owner, "param", key));
			if (configs.isEmpty()) {
				return defaultValue;
			} else {
				IConfig config = configs.get(0);
				if (configs.size() > 1) {
					LoggerFactory.getLogger(ModelUtil.class)
						.warn("Multiple user config entries for [" + key + "] using first.");
				}
				String value = config.getValue();
				return value != null
					&& (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("1"));
			}
		}
		LoggerFactory.getLogger(ModelUtil.class)
			.warn("No user contact for query of key [" + key + "] returning default");
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
		IQuery<IConfig> configQuery = modelService.getQuery(IConfig.class);
		configQuery.and(ModelPackage.Literals.ICONFIG__KEY, COMPARATOR.EQUALS,
			Preferences.P_TEXT_EXTERN_FILE);
		List<IConfig> configs = configQuery.execute();
		if (configs.isEmpty()) {
			return defaultValue;
		} else {
			IConfig config = configs.get(0);
			if (configs.size() > 1) {
				LoggerFactory.getLogger(ModelUtil.class)
					.warn("Multiple config entries for [" + key + "] using first.");
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
		if (contextService != null) {
			Optional<IContact> ret = contextService.getRootContext().getActiveUserContact();
			if (ret.isPresent()) {
				return ret;
			} else {
				Optional<IUser> user = contextService.getRootContext().getActiveUser();
				if (user.isPresent()) {
					return Optional.ofNullable(user.get().getAssignedContact());
				}
			}
		} else {
			LoggerFactory.getLogger(ModelUtil.class)
				.warn("No IContextService available.");
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
		return modelService.getQuery(interfaceClazz);
	}
	
	/**
	 * Add an {@link IXid} to the {@link Identifiable}.
	 * 
	 * @param identifiable
	 * @param domain
	 * @param id
	 * @param updateIfExists
	 * @return
	 */
	public static boolean addXid(Identifiable identifiable, String domain, String id,
		boolean updateIfExists){
		Optional<IXid> existing = getXid(domain, id);
		if (existing.isPresent()) {
			if (updateIfExists) {
				IXid xid = existing.get();
				xid.setDomain(domain);
				xid.setDomainId(id);
				xid.setObject(identifiable);
				return true;
			}
		} else {
			IXid xid = modelService.create(IXid.class);
			xid.setDomain(domain);
			xid.setDomainId(id);
			xid.setObject(identifiable);
			return true;
		}
		return false;
	}
	
	/**
	 * Get an {@link IXid} with matching domain and id.
	 * 
	 * @param domain
	 * @param id
	 * @return
	 */
	public static Optional<IXid> getXid(String domain, String id){
		IQuery<IXid> query = modelService.getQuery(IXid.class);
		query.and(ModelPackage.Literals.IXID__DOMAIN, COMPARATOR.EQUALS, domain);
		query.and(ModelPackage.Literals.IXID__DOMAIN_ID, COMPARATOR.EQUALS, id);
		List<IXid> xids = query.execute();
		if (xids.size() > 0) {
			if (xids.size() > 1) {
				LoggerFactory.getLogger(ModelUtil.class).error(
					"XID [" + domain + "] [" + id + "] on multiple objects, returning first.");
			}
			return Optional.of(xids.get(0));
		}
		return Optional.empty();
	}
	
	/**
	 * Get an {@link IXid} with matching {@link Identifiable} and domain.
	 * 
	 * @param identifiable
	 * @param domain
	 * @return
	 */
	public static Optional<IXid> getXid(Identifiable identifiable, String domain){
		IQuery<IXid> query = modelService.getQuery(IXid.class);
		query.and(ModelPackage.Literals.IXID__DOMAIN, COMPARATOR.EQUALS, domain);
		query.and(ModelPackage.Literals.IXID__OBJECT_ID, COMPARATOR.EQUALS, identifiable.getId());
		List<IXid> xids = query.execute();
		if (xids.size() > 0) {
			if (xids.size() > 1) {
				LoggerFactory.getLogger(ModelUtil.class).error(
					"XID [" + domain + "] [" + identifiable
						+ "] on multiple objects, returning first.");
			}
			return Optional.of(xids.get(0));
		}
		return Optional.empty();
	}
	
	/**
	 * Load the object using the core model service
	 * 
	 * @param objectId
	 * @param clazz
	 * @return
	 */
	public static <T> T load(String objectId, Class<T> clazz){
		Optional<T> ret = modelService.load(objectId, clazz);
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
	@SuppressWarnings("unchecked")
	public static <T> T getAdapter(EntityWithId entity, Class<T> clazz){
		if (entity != null) {
			Optional<Identifiable> adapter =
				CoreModelAdapterFactory.getInstance().getModelAdapter(entity, clazz, true);
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
		return !modelService.load(username, IUser.class).isPresent();
	}
	
	/**
	 * Convert a Hashtable into a compressed byte array.
	 * 
	 * @param hash
	 *            the hashtable to store
	 * @return
	 */
	private static byte[] flatten(final Hashtable<Object, Object> hash){
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream(hash.size() * 30);
			ZipOutputStream zos = new ZipOutputStream(baos);
			zos.putNextEntry(new ZipEntry("hash"));
			ObjectOutputStream oos = new ObjectOutputStream(zos);
			oos.writeObject(hash);
			zos.close();
			baos.close();
			return baos.toByteArray();
		} catch (Exception ex) {
			logger.warn("Exception flattening HashTable, returning null: " + ex.getMessage());
			return null;
		}
	}
	
	/**
	 * Recreate a Hashtable from a byte array as created by flatten()
	 * 
	 * @param flat
	 *            the byte array
	 * @return the original Hashtable or null if no Hashtable could be created from the array
	 */
	@SuppressWarnings("unchecked")
	private static Hashtable<Object, Object> fold(final byte[] flat){
		if (flat.length == 0) {
			return null;
		}
		try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(flat))) {
			ZipEntry entry = zis.getNextEntry();
			if (entry != null) {
				try (ObjectInputStream ois = new ObjectInputStream(zis)) {
					return (Hashtable<Object, Object>) ois.readObject();
				}
			} else {
				return null;
			}
		} catch (IOException | ClassNotFoundException ex) {
			logger.error("Exception folding byte array", ex);
			return null;
		}
	}
	
	/**
	 * Elexis persistence contains BLOBs of serialized {@link Hashtable<Object, Object>}. All types
	 * of serializable data (mostly String) can be stored and loaded from these ExtInfos. This
	 * method serializes a {@link Hashtable} in the Elexis way.
	 * 
	 * @param extInfo
	 * @return
	 */
	public static byte[] extInfoToBytes(Map<Object, Object> extInfo){
		if (extInfo != null && !extInfo.isEmpty()) {
			Hashtable<Object, Object> ov = (Hashtable<Object, Object>) extInfo;
			return flatten(ov);
		}
		return null;
	}
	
	/**
	 * This method loads {@link Hashtable} from the byte array in an Elexis way.
	 * 
	 * @param dataValue
	 * @return
	 */
	public static Map<Object, Object> extInfoFromBytes(byte[] dataValue){
		if (dataValue != null) {
			Hashtable<Object, Object> ret = fold((byte[]) dataValue);
			if (ret == null) {
				return new Hashtable<Object, Object>();
			}
			return ret;
		}
		return Collections.emptyMap();
	}
	
	/**
	 * Expand the compressed bytes using the Elexis {@link CompEx} tool.
	 * 
	 * @param comp
	 * @return
	 */
	public static byte[] getExpanded(byte[] compacted){
		return CompEx.expand(compacted);
	}
	
	/**
	 * Compress the String using the Elexis {@link CompEx} tool.
	 * 
	 * @param comp
	 * @return
	 */
	public static byte[] getCompressed(String value){
		return CompEx.Compress(value, CompEx.ZIP);
	}
	
	public static AbstractModelService getModelService(){
		return (AbstractModelService) modelService;
	}
}
