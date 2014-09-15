package ch.elexis.core.logging;

// See https://gist.github.com/mmdemirbas/5411395
// http://stackoverflow.com/questions/7967165/how-to-configure-logback-with-java-code-to-set-log-level

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.MessageFormat;

/**
 * Contains methods to access and manipulate logback framework dynamically at run-time. Here
 * 'dynamically' means without referencing the logback JAR, but using it if found in the classpath.
 * 
 * @author Muhammed Demirbas
 * @since 19 Mar 2013
 */
public final class LogbackUtils {
	public static final String LOGBACK_CLASSIC = "ch.qos.logback.classic";
	public static final String LOGBACK_CLASSIC_LOGGER = "ch.qos.logback.classic.Logger";
	public static final String LOGBACK_CLASSIC_LEVEL = "ch.qos.logback.classic.Level";
	private static final Logger logger = LoggerFactory.getLogger(LogbackUtils.class);
	
	private LogbackUtils(){
		// Prevent instance creation
	}
	
	/**
	 * Dynamically sets the logback log level for the given class to the specified level.
	 * 
	 * @param loggerName
	 *            Name of the logger to set its log level. If blank, root logger will be used.
	 * @param logLevel
	 *            One of the supported log levels: TRACE, DEBUG, INFO, WARN, ERROR, FATAL, OFF.
	 *            {@code null} value is considered as 'OFF'.
	 */
	public static boolean setLogLevel(String loggerName, String logLevel){
		String logLevelUpper = (logLevel == null) ? "OFF" : logLevel.toUpperCase();
		
		try {
			Package logbackPackage = Package.getPackage(LOGBACK_CLASSIC);
			if (logbackPackage == null) {
				// logger.info("Logback is not in the classpath!");
				// in eclipse logback is not in the classpath!
				// return false;
			}
			
			// Use ROOT logger if given logger name is blank.
			if ((loggerName == null) || loggerName.trim().isEmpty()) {
				loggerName = (String) getFieldVaulue(LOGBACK_CLASSIC_LOGGER, "ROOT_LOGGER_NAME");
			}
			
			// Obtain logger by the name
			Logger loggerObtained = LoggerFactory.getLogger(loggerName);
			if (loggerObtained == null) {
				// I don't know if this case occurs
				logger.warn("No logger for the name: {}", loggerName);
				return false;
			}
			
			Object logLevelObj = getFieldVaulue(LOGBACK_CLASSIC_LEVEL, logLevelUpper);
			if (logLevelObj == null) {
				logger.warn("No such log level: {}", logLevelUpper);
				return false;
			}
			
			Class<?>[] paramTypes = {
				logLevelObj.getClass()
			};
			Object[] params = {
				logLevelObj
			};
			
			Class<?> clz = Class.forName(LOGBACK_CLASSIC_LOGGER);
			Method method = clz.getMethod("setLevel", paramTypes);
			method.invoke(loggerObtained, params);
			
			logger.debug("Log level set to {} for the logger '{}'", logLevelUpper, loggerName);
			return true;
		} catch (Exception e) {
			logger.warn("Couldn't set log level to {} for the logger '{}'", logLevelUpper,
				loggerName, e);
			return false;
		}
	}
	
	private static Object getFieldVaulue(String fullClassName, String fieldName){
		try {
			Class<?> clazz = Class.forName(fullClassName);
			Field field = clazz.getField(fieldName);
			return field.get(null);
		} catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException
				| NoSuchFieldException | SecurityException ignored) {
			return null;
		}
	}
}
