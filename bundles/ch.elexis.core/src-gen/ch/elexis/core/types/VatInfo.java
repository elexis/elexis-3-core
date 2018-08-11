package ch.elexis.core.types;

import java.util.EnumSet;

import ch.elexis.core.model.IBillable;

/**
 * Definition of VAT related information for {@link IBillable}.
 * <p>
 * Schweizer MWSt (at.medevit.medelexis.vat_ch):
 * <li>VAT_DEFAULT ... Standard MWST Satz laut Einstellungsseite</li>
 * <li>VAT_NONE ... Keine MWST</li>
 * <li>VAT_CH_ISMEDICAMENT ... Artikel ist als Medikament anerkannt</li>
 * <li>VAT_CH_NOTMEDICAMENT ... Artikel ist nicht als Medikament anerkannt</li>
 * <li>VAT_CH_ISTREATMENT ... Leistung ist als Heilbehandlung anerkannt</li>
 * <li>VAT_CH_NOTTREATMENT ... Leistung ist nicht als Heilbehandlung anerkannt</li>
 * </p>
 */
public enum VatInfo {
		VAT_DEFAULT, VAT_NONE, VAT_CH_ISMEDICAMENT, VAT_CH_NOTMEDICAMENT, VAT_CH_ISTREATMENT,
		VAT_CH_NOTTREATMENT;
	
	/**
	 * Get a String representation of a set of {@link VatInfo} elements for persisting the
	 * information.
	 * 
	 * @param set
	 * @return
	 */
	public static String encodeAsString(EnumSet<VatInfo> set){
		StringBuilder sb = new StringBuilder();
	
		for (VatInfo info : set) {
			if (sb.length() == 0)
				sb.append(info.name());
			else
				sb.append("," + info.name());
		}
		return sb.toString();
	}
	
	/**
	 * Get an EnumSet of {@link VatInfo} from a String representation produced with
	 * {@link VatInfo#encodeAsString(EnumSet)}.
	 * 
	 * @param code
	 * @return
	 */
	public static EnumSet<VatInfo> decodeFromString(String code){
		String[] names = code.split(",");
		EnumSet<VatInfo> ret = EnumSet.noneOf(VatInfo.class);
		
		for (int i = 0; i < names.length; i++) {
			ret.add(VatInfo.valueOf(names[i]));
		}
		return ret;
	}
}
