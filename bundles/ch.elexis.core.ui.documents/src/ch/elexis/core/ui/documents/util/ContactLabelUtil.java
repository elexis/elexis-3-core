package ch.elexis.core.ui.documents.util;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.ui.dialogs.KontaktSelektor;

public class ContactLabelUtil {

	public static String[] getContactHints(String lbl) {
		String[] hints = new String[KontaktSelektor.HINTSIZE];
		Arrays.fill(hints, StringUtils.EMPTY);
		String[] splits = lbl.split(","); //$NON-NLS-1$
		if (splits.length > 0) {
			String[] fullNames = splits[0].split(StringUtils.SPACE);
			if (fullNames.length > 0) {
				// name firstname(zusatz)
				hints[KontaktSelektor.HINT_NAME] = fullNames[0].trim();
			}
			if (fullNames.length > 1) {
				String[] nameWithZusatz = fullNames[1].trim().split("\\(|\\)"); //$NON-NLS-1$
				if (nameWithZusatz.length > 0) {
					hints[KontaktSelektor.HINT_FIRSTNAME] = nameWithZusatz[0].trim();
				}
				if (nameWithZusatz.length > 1) {
					hints[KontaktSelektor.HINT_ADD] = nameWithZusatz[1].trim();
				}
			}
		}
		if (splits.length > 1) {
			hints[KontaktSelektor.HINT_STREET] = splits[1].trim();
		}
		if (splits.length > 2) {
			String[] plzWithCity = splits[2].trim().split(StringUtils.SPACE);
			hints[KontaktSelektor.HINT_ZIP] = plzWithCity[0].trim();
			if (plzWithCity.length > 1) {
				hints[KontaktSelektor.HINT_PLACE] = plzWithCity[1].trim();
			}
		}

		return hints;
	}
}
