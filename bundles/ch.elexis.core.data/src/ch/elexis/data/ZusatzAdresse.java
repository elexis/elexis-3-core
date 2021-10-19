package ch.elexis.data;

import org.apache.commons.lang3.math.NumberUtils;

import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.interfaces.ITransferable;
import ch.elexis.core.types.AddressType;
import ch.elexis.core.types.LocalizeUtil;
import ch.elexis.data.dto.ZusatzAdresseDTO;
import ch.rgw.tools.StringTool;

public class ZusatzAdresse extends PersistentObject implements ITransferable<ZusatzAdresseDTO> {
	
	protected static final String KONTAKT_ID = "Kontakt_Id";
	private static final String COUNTRY = "Land";
	private static final String PLACE = "Ort";
	private static final String ZIP = "Plz";
	public static final String STREET1 = "Strasse1";
	private static final String STREET2 = "Strasse2";
	public static final String TYPE = "Typ";
	public static final String POSTAL_ADDRESS = "Anschrift";
	
	public static final String TABLENAME = "ZusatzAdresse";
	
	private static final String[] FIELDS = new String[] {
		KONTAKT_ID, STREET1, STREET2, COUNTRY, PLACE, ZIP, TYPE, POSTAL_ADDRESS
	};
	
	static {
		addMapping(TABLENAME, KONTAKT_ID, STREET1, STREET2, ZIP, PLACE, COUNTRY, TYPE,
			POSTAL_ADDRESS);
	}
	
	protected ZusatzAdresse(){}
	
	protected ZusatzAdresse(String id){
		super(id);
	}
	
	public ZusatzAdresse(Kontakt kontakt){
		create(null, new String[] {
			KONTAKT_ID
		}, new String[] {
			kontakt.getId()
		});
	}
	
	public static ZusatzAdresse load(String id){
		return new ZusatzAdresse(id);
	}

	/**
	 * Eine Etikette der Anschrift liefern
	 * 
	 * @param withName
	 *            TODO
	 * @param multiline
	 *            Wenn true wird die Etikette mehrzeilig, sonst einzeilig
	 */
	public String getEtikette(boolean withName, boolean multiline){
		return getEtikette(withName, multiline, get(KONTAKT_ID), get(STREET1), get(COUNTRY),
			get(ZIP), get(PLACE));
	}
	
	public String getEtikette(boolean withName, boolean multiline, String kontaktId, String street1, String country, String zip, String place)
	{
		String sep = StringTool.lf;
		if (multiline == false) {
			sep = ", ";
		}
		StringBuilder ret = new StringBuilder(100);
		if (withName == true) {
			ret.append(Kontakt.load(kontaktId).getLabel(false)).append(sep);
		}
		if (street1 != null) {
			ret.append(street1).append(sep);
		}
		if (!StringTool.isNothing(country)) {
			ret.append(country).append(" - ");
		}
		if ((zip != null) && (place != null)) {
			ret.append(zip).append(StringTool.space).append(place);
		}
		if (multiline) {
			// append trailing newline
			ret.append(StringTool.lf);
		}
		return ret.toString();
	}
	
	@Override
	public String getLabel(){
		String etikette = getEtikette(false, false);
		if (get(TYPE) != null) {
			return LocalizeUtil.getLocaleText(AddressType.get(NumberUtils.toInt(get(TYPE)))) + ": "
				+ etikette;
		}
		return etikette;
	}

	@Override
	protected String getTableName(){
		return TABLENAME;
	}
	
	@Override
	public ZusatzAdresseDTO getDTO(){
		if (getId() == null) {
			return new ZusatzAdresseDTO();
		} else {
			String[] values = new String[FIELDS.length];
			get(FIELDS, values);
			
			ZusatzAdresseDTO zusatzAddresseData = new ZusatzAdresseDTO();
			zusatzAddresseData.setId(getId());
			zusatzAddresseData.setKontaktId(values[0]);
			zusatzAddresseData.setStreet1(values[1]);
			zusatzAddresseData.setStreet2(values[2]);
			zusatzAddresseData.setCountry(values[3]);
			zusatzAddresseData.setPlace(values[4]);
			zusatzAddresseData.setZip(values[5]);
			zusatzAddresseData.setAddressType(AddressType.get(NumberUtils.toInt(values[6])));
			zusatzAddresseData.setPostalAddress(values[7]);
			return zusatzAddresseData;
		}
	}
	
	@Override
	public void persistDTO(ZusatzAdresseDTO zusatzAdresseDTO) throws ElexisException{
		// validations
		if (zusatzAdresseDTO.getKontaktId() == null) {
			throw new ElexisException(ZusatzAdresse.class, "Kontakt not definied",
				ElexisException.EE_NOT_SUPPORTED, true);
		}
		
		// persist or update
		if (getId() == null) {
			create(StringTool.unique("prso"), new String[] {
				KONTAKT_ID
			}, new String[] {
				zusatzAdresseDTO.getKontaktId()
			});
		}
		
		set(FIELDS, zusatzAdresseDTO.getKontaktId(), zusatzAdresseDTO.getStreet1(),
			zusatzAdresseDTO.getStreet2(),
			zusatzAdresseDTO.getCountry(), zusatzAdresseDTO.getPlace(), zusatzAdresseDTO.getZip(),
			String.valueOf(zusatzAdresseDTO.getAddressType().getValue()),
			zusatzAdresseDTO.getPostalAddress());
	}
}
