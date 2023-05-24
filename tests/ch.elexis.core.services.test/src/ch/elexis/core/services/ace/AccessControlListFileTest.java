package ch.elexis.core.services.ace;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.elexis.core.ac.ACEAccessBitMap;
import ch.elexis.core.ac.AccessControlList;
import ch.elexis.core.ac.AccessControlListUtil;
import ch.elexis.core.model.IOrganization;

public class AccessControlListFileTest {

	@Test
	public void deserializeJsonFile() throws StreamReadException, DatabindException, IOException {

		InputStream roleAccessDefaultUserFile = AccessControlList.class.getClassLoader()
				.getResourceAsStream("/rsc/acl/user.json");
		AccessControlList aclUser = new ObjectMapper().configure(Feature.ALLOW_COMMENTS, true)
				.readValue(roleAccessDefaultUserFile, AccessControlList.class);

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
	public void deserializeCombineUserAssistant() throws StreamReadException, DatabindException, IOException {
		InputStream roleAccessDefaultUserFile = AccessControlList.class.getClassLoader()
				.getResourceAsStream("/rsc/acl/user.json");
		AccessControlList aclUser = new ObjectMapper().configure(Feature.ALLOW_COMMENTS, true)
				.readValue(roleAccessDefaultUserFile, AccessControlList.class);
		byte[] bs = aclUser.getObject().get("ch.elexis.core.model.IArticle").getAccessRightMap();
		assertArrayEquals(Arrays.toString(bs), new byte[] { 0, 4, 0, 0, 0, 4, 0, 0, 0 }, bs);

		InputStream roleAccessDefaultAssistantFile = AccessControlList.class.getClassLoader()
				.getResourceAsStream("/rsc/acl/assistant.json");
		AccessControlList aclAssistant = new ObjectMapper().configure(Feature.ALLOW_COMMENTS, true)
				.readValue(roleAccessDefaultAssistantFile, AccessControlList.class);
		bs = aclAssistant.getObject().get("ch.elexis.core.model.IArticle").getAccessRightMap();
		assertArrayEquals(Arrays.toString(bs), new byte[] { 4, 4, 4, 4, 0, 4, 0, 0, 0 }, bs);

		AccessControlList merge = AccessControlListUtil.merge(aclUser, aclAssistant);
		AccessControlList revMerge = AccessControlListUtil.merge(aclAssistant, aclUser);

		assertTrue(merge.getRolesRepresented().contains("user"));
		assertTrue(merge.getRolesRepresented().contains("assistant"));
		bs = merge.getObject().get("ch.elexis.core.model.IArticle").getAccessRightMap();
		assertArrayEquals(Arrays.toString(bs), new byte[] { 4, 4, 4, 4, 0, 4, 0, 0, 0 }, bs);
		bs = revMerge.getObject().get("ch.elexis.core.model.IArticle").getAccessRightMap();
		assertArrayEquals(Arrays.toString(bs), new byte[] { 4, 4, 4, 4, 0, 4, 0, 0, 0 }, bs);
		
		bs = merge.getObject().get("ch.elexis.core.model.IInvoice").getAccessRightMap();
		assertArrayEquals(Arrays.toString(bs), new byte[] { 2, 2, 0, 2, 0, 2, 0, 0, 0 }, bs);
		bs = revMerge.getObject().get("ch.elexis.core.model.IInvoice").getAccessRightMap();
		assertArrayEquals(Arrays.toString(bs), new byte[] { 2, 2, 0, 2, 0, 2, 0, 0, 0 }, bs);

//		new ObjectMapper().configure(SerializationFeature.INDENT_OUTPUT, true).writeValue(System.out, merge);
	}

}
