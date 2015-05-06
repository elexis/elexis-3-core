package ch.elexis.core.ui.importer.div.importers;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.data.Anschrift;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Organisation;
import ch.elexis.data.Query;

public class MediportInsurerImporter {
	private static Logger log = LoggerFactory.getLogger(MediportInsurerImporter.class);
	
	private static final String SEPARATOR = ";";
	private static final String MEDIPORT_CONNECTED = "an MediPort angeschlossen";
	
	private static final int IDX_ORGANISATION = 0;
	private static final int IDX_DEPARTMENT = 1;
	private static final int IDX_ADDR1 = 2;
	private static final int IDX_ADDR2 = 3;
	private static final int IDX_ZIP = 4;
	private static final int IDX_CITY = 5;
	private static final int IDX_PHONE = 6;
	private static final int IDX_WEBSITE = 7;
	private static final int IDX_EMAIL = 8;
	private static final int IDX_INSURANCE_EAN = 9;
	private static final int IDX_RECEPIENT_EAN = 10;
	private static final int IDX_XML_INSURANCE_NAME = 11;
	private static final int IDX_LAW_CODE = 12;
	//	private static final int IDX_TP_ELECTRONIC = 13;
	//	private static final int IDX_CHANGE_BILLTYPE = 14;
	private static final int IDX_MEDIPORT_SUPPORT = 15;
	
	public static List<Organisation> importCSVFromStream(InputStream csvInStream){
		List<Organisation> insurerList = new ArrayList<Organisation>();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(csvInStream));
			String line = "";
			int lineNo = 0;
			
			while ((line = reader.readLine()) != null) {
				lineNo++;
				if (lineNo == 1) {
					continue;
				}
				
				String[] parts = line.split(SEPARATOR, -1);
				String org = parts[IDX_ORGANISATION];
				String dept = parts[IDX_DEPARTMENT];
				String street = parts[IDX_ADDR1];
				String postbox = parts[IDX_ADDR2];
				if (postbox != null && !postbox.isEmpty()) {
					street = street + ", " + postbox;
				}
				String zip = parts[IDX_ZIP];
				String city = parts[IDX_CITY];
				String phone = parts[IDX_PHONE];
				String website = parts[IDX_WEBSITE];
				String email = parts[IDX_EMAIL];
				String insuranceEAN = parts[IDX_INSURANCE_EAN];
				String recepientEAN = parts[IDX_RECEPIENT_EAN];
				String xmlInsuranceName = parts[IDX_XML_INSURANCE_NAME];
				String lawCode = parts[IDX_LAW_CODE];
				String mediport = parts[IDX_MEDIPORT_SUPPORT];
				
				Query<Kontakt> contactQuery = new Query<Kontakt>(Kontakt.class);
				contactQuery.add(Kontakt.FLD_NAME1, Query.EQUALS, org);
				contactQuery.add(Kontakt.FLD_NAME2, Query.EQUALS, dept);
				contactQuery.add(Kontakt.FLD_STREET, Query.EQUALS, street);
				contactQuery.add(Kontakt.FLD_PLACE, Query.EQUALS, city);
				List<Kontakt> contactList = contactQuery.execute();
				if (contactList != null && !contactList.isEmpty()) {
					log.warn("Kontakt [" + org + " " + dept + ", " + street + ", " + city
						+ "] existiert bereits. Wird nicht imporiert.");
					continue;
				}
				
				Organisation insurer = new Organisation(org, dept);
				if (!insurer.set(new String[] {
					Organisation.FLD_STREET, Organisation.FLD_ZIP, Organisation.FLD_PLACE,
					Organisation.FLD_PHONE1, Organisation.FLD_E_MAIL, Organisation.FLD_WEBSITE,
					Organisation.FLD_LAW_CODE, Organisation.FLD_XML_NAME
				}, street, zip, city, phone, email, website, lawCode, xmlInsuranceName)) {
					log.error("Kontaktdaten konnten nicht gespeichert werden.");
				}
				insurer.setAnschrift(new Anschrift(insurer));
				insurer.getXid();
				
				//set EANs
				if (insuranceEAN != null && insuranceEAN.length() == 13) {
					insurer.setInsurerEAN(insuranceEAN);
				}
				if (recepientEAN != null && recepientEAN.length() == 13) {
					insurer.setRecepientEAN(recepientEAN);
				}
				
				//set further information
				insurer.setSupportsMediport(MEDIPORT_CONNECTED.equalsIgnoreCase(mediport));
				
				insurerList.add(insurer);
			}
			reader.close();
		} catch (Exception e) {
			return insurerList;
		}
		return insurerList;
	}
	
}
