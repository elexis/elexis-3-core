package ch.elexis.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ch.elexis.core.data.activator.CoreHub;
import ch.rgw.io.InMemorySettings;
import ch.rgw.io.Settings;
import ch.rgw.tools.JdbcLink;

public class Test_UserConfig extends AbstractPersistentObjectTest {

	public Test_UserConfig(JdbcLink link) {
		super(link);
	}

	@Test
	public void testUserConfigBlob() {
		// save content to blob
		CoreHub.userCfg.set("test/userconfig", true);
		CoreHub.userCfg.set("test/user", CoreHub.getLoggedInContact().getLabel());
		InMemorySettings ims = new InMemorySettings();
		ims.overlay(CoreHub.userCfg, Settings.OVL_REPLACE);
		assertTrue(ims.get("test/userconfig", false));
		assertEquals(ims.get("test/user", ""), CoreHub.getLoggedInContact().getLabel());
		NamedBlob blob = NamedBlob.load("UserCfg:test"); //$NON-NLS-1$
		blob.put(ims.getNode());
		// clear
		CoreHub.userCfg.clear();
		assertFalse(CoreHub.userCfg.get("test/userconfig", false));
		// reload from blob
		ims = new InMemorySettings(blob.getHashtable());
		CoreHub.userCfg.overlay(ims, Settings.OVL_REPLACE);
		assertTrue(CoreHub.userCfg.get("test/userconfig", false));
		assertEquals(CoreHub.userCfg.get("test/user", ""), CoreHub.getLoggedInContact().getLabel());
	}

}
