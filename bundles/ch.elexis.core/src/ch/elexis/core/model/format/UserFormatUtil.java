package ch.elexis.core.model.format;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IUser;

public class UserFormatUtil {

	public static String getUserLabel(IUser user) {
		return getUserLabel(user.getAssignedContact());
	}

	public static String getUserLabel(IContact userContact) {
		String ret = userContact.getDescription3();
		if (StringUtils.isBlank(ret)) {
			ret = userContact.getDescription1() + StringUtils.SPACE + userContact.getDescription2();
			if (StringUtils.isBlank(ret)) {
				ret = "unknown";
			}
		}
		return ret;
	}
}
