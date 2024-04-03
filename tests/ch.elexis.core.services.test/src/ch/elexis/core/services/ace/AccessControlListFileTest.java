package ch.elexis.core.services.ace;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;

import ch.elexis.core.ac.ACEAccessBitMap;
import ch.elexis.core.ac.AccessControlList;
import ch.elexis.core.ac.AccessControlListUtil;
import ch.elexis.core.model.IOrganization;
import ch.elexis.core.utils.OsgiServiceUtil;

public class AccessControlListFileTest {

	private static Gson gson;

	@BeforeClass
	public static void beforeClass() {
		gson = OsgiServiceUtil.getService(Gson.class).get();
	}

	@Test
	public void deserializeJsonFile() throws IOException {
		AccessControlList aclUser;
		try (InputStream roleAccessDefaultUserFile = AccessControlList.class.getClassLoader()
				.getResourceAsStream("/rsc/acl/user.json")) {
			aclUser = gson.fromJson(new InputStreamReader(roleAccessDefaultUserFile), AccessControlList.class);
		}

		assertEquals("user", aclUser.getRolesRepresented().iterator().next());
		byte[] bs = aclUser.getObject().get(IOrganization.class.getName()).getAccessRightMap();
		// c, r, u, d, x, v, e, i, z
		// 0 no right
		// 4 for all elements
		assertArrayEquals(new byte[] { 0, 4, 0, 0, 0, 4, 0, 0, 0 }, bs);

		ACEAccessBitMap aceAccessBitMap = aclUser.getSystemCommand().get("ch.elexis.core.ui.login");
		assertArrayEquals(new byte[] { 0, 0, 0, 0, 4, 0, 0, 0, 0 }, aceAccessBitMap.getAccessRightMap());

	}

	@Test
	public void deserializeCombineUserAssistant() throws IOException {
		AccessControlList aclUser;
		try (InputStream roleAccessDefaultUserFile = AccessControlList.class.getClassLoader()
				.getResourceAsStream("/rsc/acl/user.json")) {
			aclUser = gson.fromJson(new InputStreamReader(roleAccessDefaultUserFile), AccessControlList.class);
		}
		byte[] bs = aclUser.getObject().get("ch.elexis.core.model.IArticle").getAccessRightMap();
		assertArrayEquals(Arrays.toString(bs), new byte[] { 0, 4, 0, 0, 0, 4, 0, 0, 0 }, bs);

		bs = aclUser.getObject().get("ch.elexis.core.model.IContact").getAccessRightMap();
		assertArrayEquals(Arrays.toString(bs), new byte[] { 0, 4, 0, 0, 0, 4, 0, 0, 0 }, bs);

		AccessControlList aclMedicalUser;
		try (InputStream roleAccessDefaultAssistantFile = AccessControlList.class.getClassLoader()
				.getResourceAsStream("/rsc/acl/medical-user.json")) {
			aclMedicalUser = gson.fromJson(new InputStreamReader(roleAccessDefaultAssistantFile),
					AccessControlList.class);
		}
		bs = aclMedicalUser.getObject().get("ch.elexis.core.model.IArticle").getAccessRightMap();
		assertArrayEquals(Arrays.toString(bs), new byte[] { 4, 4, 4, 4, 4, 4, 4, 4, 0 }, bs);

		AccessControlList merge = AccessControlListUtil.merge(aclUser, aclMedicalUser);
		AccessControlList revMerge = AccessControlListUtil.merge(aclMedicalUser, aclUser);

		assertTrue(merge.getRolesRepresented().contains("user"));
		assertTrue(merge.getRolesRepresented().contains("medical-user"));
		bs = merge.getObject().get("ch.elexis.core.model.IArticle").getAccessRightMap();
		assertArrayEquals(Arrays.toString(bs), new byte[] { 4, 4, 4, 4, 4, 4, 4, 4, 0 }, bs);
		bs = revMerge.getObject().get("ch.elexis.core.model.IArticle").getAccessRightMap();
		assertArrayEquals(Arrays.toString(bs), new byte[] { 4, 4, 4, 4, 4, 4, 4, 4, 0 }, bs);

		bs = merge.getObject().get("ch.elexis.core.model.IInvoice").getAccessRightMap();
		assertArrayEquals(Arrays.toString(bs), new byte[] { 1, 1, 1, 1, 1, 1, 0, 0, 0 }, bs);
		bs = revMerge.getObject().get("ch.elexis.core.model.IInvoice").getAccessRightMap();
		assertArrayEquals(Arrays.toString(bs), new byte[] { 1, 1, 1, 1, 1, 1, 0, 0, 0 }, bs);

		bs = revMerge.getObject().get("ch.elexis.core.model.IContact").getAccessRightMap();
		assertArrayEquals(Arrays.toString(bs), new byte[] { 4, 4, 4, 4, 4, 4, 4, 4, 0 }, bs);
	}

}
