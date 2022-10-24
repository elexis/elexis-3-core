package ch.elexis.core.model.util;

import org.apache.commons.lang3.StringUtils;
import java.util.UUID;

public class ElexisIdGenerator {

	/**
	 * Generate an Elexis conform Database ID. This ID is limited to 25 characters.
	 *
	 * @return generated ID string
	 */
	public static String generateId() {
		String randomString = UUID.randomUUID().toString();
		String randomStringWithoutDashes = randomString.replaceAll("-", StringUtils.EMPTY);
		if (randomStringWithoutDashes.length() <= 25) {
			return randomStringWithoutDashes;
		}
		return randomStringWithoutDashes.substring(0, 25);
	}
}
