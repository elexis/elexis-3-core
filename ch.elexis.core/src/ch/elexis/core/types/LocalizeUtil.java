package ch.elexis.core.types;

import java.util.ResourceBundle;

import org.eclipse.emf.common.util.Enumerator;

import ch.elexis.core.interfaces.ILocalizedEnum;

public class LocalizeUtil {
	/**
	 * Use with EMF generated not instances of {@link ILocalizedEnum}. Get the localized String for
	 * the {@link Enumerator}.
	 * 
	 * @param enumerator
	 * @return
	 */
	public static String getLocaleText(Enumerator enumerator){
		if (enumerator != null) {
			try {
				return ResourceBundle.getBundle("ch.elexis.core.types.messages")
					.getString(enumerator.getClass().getSimpleName() + "." + enumerator.getName());
			} catch (Exception e) {
				return enumerator.getName();
			}
		}
		return "?";
	}
}
