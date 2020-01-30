package ch.elexis.core.model.tasks;

import java.io.Serializable;
import java.util.Map;

public class SerializableBoolean {
	
	/**
	 * Interpret a deserialized {@link Serializable} boolean value
	 * 
	 * @param runContext
	 * @param key
	 * @return
	 * @throws IllegalArgumentException
	 *             if String not <code>true</code> or <code>false</code>, or any type other than
	 *             {@link Boolean} or {@link String}
	 */
	public static boolean valueOf(Map<String, Serializable> runContext, String key){
		Serializable value = runContext.get(key);
		
		if (value instanceof Boolean) {
			return (boolean) value;
		} else if (value instanceof String) {
			if (Boolean.TRUE.toString().equals(value)) {
				return true;
			} else if (Boolean.FALSE.toString().equals(value)) {
				return false;
			}
			throw new IllegalArgumentException("Could not interpret value [" + value + "]");
		}
		throw new IllegalArgumentException("Can not handle type " + value.getClass().getName());
	}
	
}
