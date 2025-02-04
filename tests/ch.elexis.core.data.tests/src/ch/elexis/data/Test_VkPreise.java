package ch.elexis.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.JdbcLinkException;
import ch.rgw.tools.TimeTool;

public class Test_VkPreise extends AbstractPersistentObjectTest {

	@Test
	public void testVKMultiplikator() throws SQLException {
		Eigenleistung leistung = new Eigenleistung("TD999", "Leistung.xy999", "99", "10");
		TimeTool now = new TimeTool(System.currentTimeMillis());
		leistung.setVKMultiplikator(now, null, 98.12345, "typ");
		Assert.assertEquals(98.12345, leistung.getVKMultiplikator(now, "typ"), 0.00001);

		ResultSet res = getLink().getStatement().query("SELECT ID FROM VK_PREISE WHERE TYP='typ'");
		if (res.next()) {
			// checks if id is generated successfully after insert
			Assert.assertNotNull(res.getString("ID"));
		} else {
			Assert.fail("no result for vk_preise");
		}

	}

	@Test
	public void testStoringVkPreiseWithSameId() {
		try {
			String id = String.valueOf(UUID.randomUUID()).substring(0, 20);
			Assert.assertEquals(1, getLink().exec("INSERT INTO VK_PREISE (ID) VALUES (" + JdbcLink.wrap(id) + ")"));
			getLink().exec("INSERT INTO VK_PREISE (ID) VALUES (" + JdbcLink.wrap(id) + ")");
			Assert.fail("should not happen");
		} catch (JdbcLinkException e) {
			Assert.assertTrue(true);
		}
	}

	// workaround use a trim as in Test_StockService.testCreateEditAndDeleteStock
	// and Test_StockService.testStoreUnstoreFindPreferredArticleInStock
	@Test
	@Ignore("not working for postgre - char 8 field returns exactly 8 bytes only for postgre")
	public void testStoringVkPreiseDatumIdChar8() throws SQLException {
		String id = String.valueOf(UUID.randomUUID()).substring(0, 20);
		String datumVon = "2010121";
		getLink().exec("INSERT INTO VK_PREISE (ID, DATUM_VON) VALUES (" + JdbcLink.wrap(id) + ", "
				+ JdbcLink.wrap(datumVon) + ")");
		ResultSet res = getLink().getStatement().query("SELECT DATUM_VON FROM VK_PREISE WHERE ID=" + JdbcLink.wrap(id));
		if (res.next()) {
			Assert.assertEquals(datumVon.length(), res.getString("DATUM_VON").length());
		} else {
			Assert.fail("no result for vk_preise");
		}
	}

}
