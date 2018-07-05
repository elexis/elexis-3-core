package ch.elexis.core.model;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.jpa.entities.AbstractDBObject;
import ch.elexis.core.jpa.entitymanager.ElexisEntityManger;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.rgw.tools.MimeTool;

@Component
public class ModelUtil {
	
	private static IModelService modelService;
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	public void setModelService(IModelService modelService){
		ModelUtil.modelService = modelService;
	}
	
	private static ElexisEntityManger entityManager;
	
	@Reference(cardinality = ReferenceCardinality.MANDATORY)
	public void setEntityManger(ElexisEntityManger entityManager){
		ModelUtil.entityManager = entityManager;
	}
	
	/**
	 * Save an entity.
	 * 
	 * @param entity
	 */
	protected static void saveEntity(AbstractDBObject entity){
		if (entity != null) {
			EntityManager em = entityManager.getEntityManager();
			try {
				em.getTransaction().begin();
				em.merge(entity);
				em.getTransaction().commit();
			} finally {
				em.close();
			}
		}
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
		IQuery<IConfig> configQuery = modelService.getQuery(IConfig.class);
		configQuery.add(ModelPackage.Literals.ICONFIG__KEY, COMPARATOR.EQUALS,
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
			String value = config.getValue();
			return value != null && (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("1"));
		}
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
		configQuery.add(ModelPackage.Literals.ICONFIG__KEY, COMPARATOR.EQUALS,
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
}
