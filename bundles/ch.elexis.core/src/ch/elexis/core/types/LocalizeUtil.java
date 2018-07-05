package ch.elexis.core.types;

import java.util.Locale;
import java.util.ResourceBundle;

import org.eclipse.emf.common.util.Enumerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.interfaces.ILocalizedEnum;
import ch.elexis.core.l10n.Messages;

public class LocalizeUtil {
	 private static Logger log = LoggerFactory.getLogger(LocalizeUtil.class);
	 
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
				return ResourceBundle.getBundle(ch.elexis.core.l10n.Messages.BUNDLE_NAME)
					.getString(enumerator.getClass().getSimpleName() + "_" + enumerator.getName());
			} catch (Exception e) {
				log.warn("No translation for {}_{} locale {} in {} ",
					enumerator.getClass().getSimpleName(), enumerator.getName(),
					Locale.getDefault(),
					ch.elexis.core.l10n.Messages.class.getName());
				return enumerator.getName();
			}
		}
		return "?";
	}
	/**
	 * Next procedure is never use, but avoids warnings when starting Elexis because of unused messages
	 * */
	@SuppressWarnings("unused")
	private static String ensureUseOAddressTypefMessages() {
		return Messages.AddressType_SECONDARY_RESIDENCE + Messages.AddressType_HOLIDAY_HOME
			+ Messages.AddressType_PRINCIPAL_RESIDENCE + Messages.AddressType_PLACE_OF_RESIDENCE
			+ Messages.AddressType_EMPLOYER + Messages.AddressType_FAMILY_FRIENDS
			+ Messages.AddressType_ATTACHMENT_FIGURE + Messages.AddressType_PRISON
			+ Messages.AddressType_NURSING_HOME;
	}
	/**
	 * Next procedure is never use, but avoids warnings when starting Elexis because of unused messages
	 * */
	@SuppressWarnings("unused")
	private static String ensureUseOfRelationShipMessages() {
		return Messages.RelationshipType_A_GENERIC +
		Messages.RelationshipType_BUSINESS_EMPLOYER  +
		Messages.RelationshipType_BUSINESS_EMPLOYEE  +
		Messages.RelationshipType_FAMILY_PARENT +
		Messages.RelationshipType_FAMILY_CHILD  +
		Messages.RelationshipType_FAMILY_GUARDIAN  +
		Messages.RelationshipType_FAMILY_ICE  +
		Messages.RelationshipType_FAMILY_ALTERNATIVE  +
		Messages.RelationshipType_WELFARE_GENERAL_PRACTITIONER  +
		Messages.RelationshipType_WELFARE_PATIENT  +
		Messages.RelationshipType_WELFARE_CONSULTANT  +
		Messages.RelationshipType_WELFARE_INSURER  +
		Messages.RelationshipType_WELFARE_INSUREE;
	}
		
}
