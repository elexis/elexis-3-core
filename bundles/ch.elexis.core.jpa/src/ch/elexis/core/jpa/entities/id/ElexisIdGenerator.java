package ch.elexis.core.jpa.entities.id;

import java.util.UUID;

public class ElexisIdGenerator {

	/**
	 * Generate an Elexis conform Database ID. This ID is limited to 25
	 * characters.
	 * 
	 * @return generated ID string
	 */
	public static String generateId() {
		String randomString = UUID.randomUUID().toString();
		String randomStringWithoutDashes = randomString.replaceAll("-", "");
		if (randomStringWithoutDashes.length() <= 25) {
			return randomStringWithoutDashes;
		}
		return randomStringWithoutDashes.substring(0, 25);
	}
}
