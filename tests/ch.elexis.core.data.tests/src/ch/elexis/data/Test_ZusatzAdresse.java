package ch.elexis.data;

import java.util.List;
import java.util.Locale;

import org.junit.Test;

import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.types.AddressType;
import ch.elexis.data.dto.ZusatzAdresseDTO;
import ch.rgw.tools.JdbcLink;
import junit.framework.Assert;

public class Test_ZusatzAdresse extends AbstractPersistentObjectTest {
	
	public Test_ZusatzAdresse(JdbcLink link){
		super(link);
	}

	@Test(expected = ElexisException.class)
	public void TestZusatzAdresseWithoutKontakt() throws ElexisException{
		ZusatzAdresseDTO zusatzAdresseDTO = new ZusatzAdresseDTO();
		zusatzAdresseDTO.setAddressType(AddressType.ATTACHMENT_FIGURE);
		zusatzAdresseDTO.setCountry("A");
		zusatzAdresseDTO.setStreet1("Teststreet 1");
		zusatzAdresseDTO.setKontaktId("1");
		zusatzAdresseDTO.setZip("1010");
		zusatzAdresseDTO.setPlace("Vienna");
		zusatzAdresseDTO.setStreet2("Teststreet 2");
		zusatzAdresseDTO.setKontaktId(null);
		
		ZusatzAdresse zusatzAdresse = ZusatzAdresse.load(null);
		zusatzAdresse.persistDTO(zusatzAdresseDTO);
	}
	
	@Test
	public void TestZusatzAdresseWithKontakt() throws ElexisException{
		Patient patient = new Patient("Mustermann", "Max", "1.1.2000", "m");
		ZusatzAdresseDTO zusatzAdresseDTO = new ZusatzAdresseDTO();
		zusatzAdresseDTO.setAddressType(AddressType.PRISON);
		zusatzAdresseDTO.setCountry("A");
		zusatzAdresseDTO.setStreet1("Teststreet 1");
		zusatzAdresseDTO.setKontaktId("1");
		zusatzAdresseDTO.setZip("1010");
		zusatzAdresseDTO.setPlace("Vienna");
		zusatzAdresseDTO.setStreet2("Teststreet 2");
		zusatzAdresseDTO.setKontaktId(patient.getId());
		ZusatzAdresse zusatzAdresse = ZusatzAdresse.load(null);
		zusatzAdresse.persistDTO(zusatzAdresseDTO);
		
		List<ZusatzAdresse> zusatzAdressen = patient.getZusatzAdressen();
		Assert.assertTrue(zusatzAdressen.size() == 1);
		ZusatzAdresse savedZusatzAdresse = zusatzAdressen.get(0);
		Assert.assertEquals(AddressType.PRISON.getLiteral(), savedZusatzAdresse.getDTO().getAddressType().getLiteral());
		if (Locale.getDefault().toString().equals("de_CH")) {
			// System.out.println("found de_CH: " + savedZusatzAdresse.getLabel());
			Assert.assertTrue(savedZusatzAdresse.getLabel().startsWith("Justizanstalt"));
		} else {
			System.out.println(Locale.getDefault().toString() );
		}
		if (Locale.getDefault().toString().equals("en_US")) {
			// System.out.println("found en_US: " + savedZusatzAdresse.getLabel());
			Assert.assertTrue(savedZusatzAdresse.getLabel().startsWith("prison"));
		}
		Assert.assertTrue(savedZusatzAdresse.getLabel().startsWith(ch.elexis.core.l10n.Messages.AddressType_PRISON));
		Assert.assertNotNull(savedZusatzAdresse.getId());
		Assert.assertEquals("Teststreet 1", savedZusatzAdresse.getDTO().getStreet1());
		Assert.assertEquals("Teststreet 1", savedZusatzAdresse.get(ZusatzAdresse.STREET1));
		Assert.assertEquals(savedZusatzAdresse.getId(), savedZusatzAdresse.getDTO().getId());
		Assert.assertEquals(savedZusatzAdresse.get(ZusatzAdresse.KONTAKT_ID),
			savedZusatzAdresse.getDTO().getKontaktId());
	}
	
	@Test
	public void TestZusatzAdresseWithoutDTOPersisting() throws ElexisException{
		Patient patient = new Patient("Mustermann", "Max", "1.1.2000", "m");
		ZusatzAdresse zusatzAdresse = new ZusatzAdresse(patient);
		zusatzAdresse.set(new String[] {
			ZusatzAdresse.STREET1, ZusatzAdresse.TYPE
		}, new String[] {
			"Teststreet 2", String.valueOf(AddressType.FAMILY_FRIENDS.getValue())
		});
		
		List<ZusatzAdresse> zusatzAdressen = patient.getZusatzAdressen();
		Assert.assertTrue(zusatzAdressen.size() == 1);
		
		ZusatzAdresse savedZusatzAdresse = zusatzAdressen.get(0);
		
		Assert.assertNotNull(savedZusatzAdresse.getId());
		Assert.assertEquals("Teststreet 2", savedZusatzAdresse.getDTO().getStreet1());
		Assert.assertEquals("Teststreet 2", savedZusatzAdresse.get(ZusatzAdresse.STREET1));
		Assert.assertEquals(String.valueOf(AddressType.FAMILY_FRIENDS.getValue()),
			savedZusatzAdresse.get(ZusatzAdresse.TYPE));
		Assert.assertEquals(savedZusatzAdresse.getId(), savedZusatzAdresse.getDTO().getId());
		Assert.assertEquals(savedZusatzAdresse.get(ZusatzAdresse.KONTAKT_ID),
			savedZusatzAdresse.getDTO().getKontaktId());
	}
}
