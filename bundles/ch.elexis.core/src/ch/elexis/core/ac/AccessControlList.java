package ch.elexis.core.ac;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AccessControlList {

	@JsonProperty("roles-represented")
	private Set<String> rolesRepresented;
	@JsonProperty("system-configuration")
	private Map<String, Object> systemConfiguration;
	@JsonProperty("system-command")
	private Map<String, ACEAccessBitMap> systemCommand;
	@JsonProperty("object")
	private Map<String, ACEAccessBitMap> object;

	public AccessControlList() {
		rolesRepresented = new HashSet<>();
		systemCommand = new HashMap<>();
		systemConfiguration = new HashMap<>();
		object = new HashMap<>();
	}

	/**
	 * copy constructor
	 * 
	 * @param acl
	 */
	public AccessControlList(AccessControlList acl) {
		this();

		if (acl.rolesRepresented != null) {
			rolesRepresented.addAll(acl.rolesRepresented);
		}

		if (acl.systemCommand != null) {
			systemCommand.putAll(acl.systemCommand);
		}

		if (acl.systemConfiguration != null) {
			systemConfiguration.putAll(acl.systemConfiguration);
		}

		if (acl.object != null) {
			object.putAll(acl.object);
		}
	}

	public Set<String> getRolesRepresented() {
		return rolesRepresented;
	}

	public Map<String, ACEAccessBitMap> getObject() {
		return object;
	}

	public Map<String, ACEAccessBitMap> getSystemCommand() {
		return systemCommand;
	}

}
