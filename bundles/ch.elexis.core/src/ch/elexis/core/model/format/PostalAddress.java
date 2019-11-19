package ch.elexis.core.model.format;

import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.model.IContact;
import ch.elexis.core.types.Country;
import ch.rgw.tools.StringTool;

public class PostalAddress {
	
	private IContact contact;
	
	private String salutation;
	private String address1;
	private String address2;
	
	private String lastName;
	private String firstName;
	
	private String country;
	private String zip;
	private String city;

	public String getSalutation(){
		return salutation;
	}
	
	public String getAddress1(){
		return address1;
	}
	
	public String getAddress2(){
		return address2;
	}
	
	public String getLastName(){
		return lastName;
	}
	
	public String getFirstName(){
		return firstName;
	}
	
	public String getCountry(){
		return country;
	}
	
	public String getZip(){
		return zip;
	}
	
	public String getCity(){
		return city;
	}
	
	public static PostalAddress of(IContact contact){
		PostalAddress ret = new PostalAddress();
		ret.setContact(contact);
		return ret;
	}
	
	public static PostalAddress ofText(String text){
		PostalAddress ret = new PostalAddress();
		ret.initFromText(text);
		return ret;
	}
	
	private void setContact(IContact contact){
		this.contact = contact;
	}
	
	public String getWrittenAddress(boolean withName, boolean multiline){
		String sep = StringTool.lf;
		if (multiline == false) {
			sep = ", "; //$NON-NLS-1$
		}
		StringBuilder ret = new StringBuilder(100);
		if (withName == true) {
			ret.append(contact.getLabel()).append(sep);
		}
		if (StringUtils.isNotEmpty(contact.getStreet())) {
			ret.append(contact.getStreet()).append(sep);
		}
		if (contact.getCountry() != null && contact.getCountry() != Country.NDF) {
			ret.append(contact.getCountry().toString()).append(" - "); //$NON-NLS-1$
		}
		if (StringUtils.isNotEmpty(contact.getZip()) && StringUtils.isNotEmpty(contact.getCity())) {
			ret.append(contact.getZip()).append(StringTool.space).append(contact.getCity());
		}
		if (multiline) {
			// append trailing newline
			ret.append(StringTool.lf);
		}
		return ret.toString();
	}
	
	public String getLabel(){
		return getWrittenAddress(true, false);
	}
	
	private void initFromText(String text){
		// Zeilen lesen
		StringTokenizer tokenizer = new StringTokenizer(text, StringConstants.LF);
		List<String> zeileList = new Vector<String>();
		while (tokenizer.hasMoreElements()) {
			zeileList.add(tokenizer.nextToken());
		}
		// Zeilen interpretieren (so gut es geht)
		String plzOrt = StringTool.leer; //$NON-NLS-1$
		String nameVorname = StringTool.leer; //$NON-NLS-1$
		final int len = zeileList.size();
		switch (len) {
		case 0: // Kann gar nicht sein, aber man weiss ja nie!
			throw new IllegalArgumentException("Could not parse postal address");
		case 1: // Nur Name vorname
			nameVorname = zeileList.get(0);
			break;
		case 2: // NameVorname, Ortsangaben
			nameVorname = zeileList.get(0);
			plzOrt = zeileList.get(1);
			break;
		case 3: // NameVorname, Adr1, Ortsangaben ODER Anrede, NameVorname,
			// Ortsangaben
			if (zeileList.get(0).indexOf(StringTool.space) < 0) { //$NON-NLS-1$
				// Erste Zeile Anrede
				salutation = zeileList.get(0);
				nameVorname = zeileList.get(1);
				plzOrt = zeileList.get(2);
			} else {
				// Erste Zeile NameVorname
				nameVorname = zeileList.get(0);
				address1 = zeileList.get(1);
				plzOrt = zeileList.get(2);
			}
			break;
		case 4: // NameVorname, Adr1, Adr2, Ortsangaben ODER Anrede,
			// NameVorname, Adr1,
			// Ortsangaben
			if (zeileList.get(0).indexOf(StringTool.space) < 0) { //$NON-NLS-1$
				// Erste Zeile Anrede
				salutation = zeileList.get(0);
				nameVorname = zeileList.get(1);
				address1 = zeileList.get(2);
				plzOrt = zeileList.get(3);
			} else {
				// Erste Zeile NameVorname
				nameVorname = zeileList.get(0);
				address1 = zeileList.get(1);
				address2 = zeileList.get(2);
				plzOrt = zeileList.get(3);
			}
			break;
		default:
			if (len > 4) { // Anrede, NameVorname, Adr1, Adr2, Ortsangaben
				salutation = zeileList.get(0);
				nameVorname = zeileList.get(1);
				address1 = zeileList.get(2);
				address2 = zeileList.get(3);
				plzOrt = zeileList.get(4);
			}
			break;
		}
		
		// NameVorname aufteilen. Z.B. Von Allmen Christoph
		if (!StringTool.isNothing(nameVorname)) {
			nameVorname = nameVorname.trim();
			int index = nameVorname.lastIndexOf(StringTool.space); // Z.B. Von Allmen Christoph //$NON-NLS-1$
			if (index > 0) {
				lastName = nameVorname.substring(0, index);
				firstName = nameVorname.substring(index + 1);
			} else {
				lastName = nameVorname;
			}
		}
		
		// plzOrt parsen. Z.B. CH-3600 Lenzburg
		if (plzOrt.length() > 3 && plzOrt.substring(0, 3).indexOf("-") > 0) { //$NON-NLS-1$
			// Land exists
			int index = plzOrt.indexOf("-"); //$NON-NLS-1$
			country = plzOrt.substring(0, index);
			plzOrt = plzOrt.substring(index + 1);
			city = plzOrt;
		}
		if (plzOrt.indexOf(StringTool.space) > 0) { //$NON-NLS-1$
			// Read zip code
			int index = plzOrt.indexOf(StringConstants.SPACE);
			zip = plzOrt.substring(0, index);
			plzOrt = plzOrt.substring(index + 1);
			city = plzOrt;
		}
	}
}
