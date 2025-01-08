package ch.elexis.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import ch.elexis.core.model.IBlob;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.builder.IContactBuilder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.types.Gender;
import ch.elexis.core.utils.OsgiServiceUtil;

public class IConfigServiceTest extends AbstractServiceTest {

	private IConfigService configService = OsgiServiceUtil.getService(IConfigService.class).get();

	@SuppressWarnings("unchecked")
	@Test
	public void testUserConfigBlob() {
		IPerson person = new IContactBuilder.PersonBuilder(coreModelService, "TestPerson", "TestPerson",
				LocalDate.now(), Gender.FEMALE).mandator().buildAndSave();
		// save content to blob
		configService.set(person, "test/userconfig", true);
		configService.set(person, "test/user", person.getLabel());

		Map<Object, Object> configMap = configService.getAsMap(person);
		assertEquals("1", ((Map<Object, Object>) configMap.get("test")).get("userconfig"));
		assertEquals(person.getLabel(), ((Map<Object, Object>) configMap.get("test")).get("user"));
		IBlob blob = CoreModelServiceHolder.get().create(IBlob.class);
		blob.setId("UserCfg:test"); //$NON-NLS-1$
		blob.setMapContent(configMap);
		CoreModelServiceHolder.get().save(blob);
		// clear
		configService.set(person, "test/userconfig", null);
		configService.set(person, "test/user", null);
		assertFalse(configService.get(person, "test/userconfig", false));
		// reload from blob
		Map<Object, Object> map = blob.getMapContent();
		configService.setFromMap(person, map);
		assertTrue(configService.get(person, "test/userconfig", false));
		assertEquals(person.getLabel(), configService.get(person, "test/user", ""));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testUserConfigBlobCompatibility() throws IOException {
		IPerson person = new IContactBuilder.PersonBuilder(coreModelService, "TestPerson", "TestPerson",
				LocalDate.now(), Gender.FEMALE).mandator().buildAndSave();
		// save old content to blob
		IBlob blob = CoreModelServiceHolder.get().create(IBlob.class);
		blob.setId("UserCfg:test"); //$NON-NLS-1$
		try (InputStream in = IConfigServiceTest.class.getResourceAsStream("/rsc/usrcfg.blob")) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			IOUtils.copy(in, out);
			blob.setContent(out.toByteArray());
		}
		assertNotEquals("1", configService.get(person, "ch.elexis.omnivore//savesortdirection", ""));
		// reload from blob
		Map<Object, Object> blobMap = blob.getMapContent();
		configService.setFromMap(person, blobMap);
		assertEquals("1", configService.get(person, "ch.elexis.omnivore//savesortdirection", ""));
		assertEquals("Allgemein", configService.get(person, "fall/std_label", ""));
		// load as map and compare with map from blob
		Map<Object, Object> configMap = configService.getAsMap(person);
		assertTrue(mapsContentEquals(blobMap, configMap));
		assertEquals("1", ((Map<Object, Object>) ((Map<Object, Object>) configMap.get("ch.elexis.omnivore")).get(""))
				.get("savesortdirection"));

	}

	@SuppressWarnings("unchecked")
	private boolean mapsContentEquals(Map<Object, Object> left, Map<Object, Object> right) {
		for (Object key : left.keySet()) {
			if (left.get(key) instanceof Map) {
				if (!mapsContentEquals((Map<Object, Object>) left.get(key), (Map<Object, Object>) right.get(key))) {
					return false;
				}
			} else {
				if (!left.get(key).equals(right.get(key))) {
					System.out.println("key [" + key + "] left [" + left.get(key) + "] right [" + right.get(key) + "]");
					return false;
				}
			}
		}
		return true;
	}

	@Test
	public void getSetUserconfig() {
		IPerson person = new IContactBuilder.PersonBuilder(coreModelService, "TestPerson", "TestPerson",
				LocalDate.now(), Gender.FEMALE).mandator().buildAndSave();
		IPerson person2 = new IContactBuilder.PersonBuilder(coreModelService, "TestPerson2", "TestPerson2",
				LocalDate.now(), Gender.FEMALE).mandator().buildAndSave();

		assertTrue(configService.set(person, "key", "value"));
		assertTrue(configService.set(person2, "key", "value2"));

		assertEquals("value", configService.get(person, "key", null));
		assertEquals("value2", configService.get(person2, "key", null));

		assertTrue(configService.set(person, "key", null));
		assertNull(configService.get(person, "key", null));
		assertFalse(configService.set(person, "key", null));
	}

	@Test
	public void getSetConfig() {
		assertTrue(configService.set("key", "value"));
		assertEquals("value", configService.get("key", null));
		// remove value
		assertTrue(configService.set("key", null));
		assertFalse(configService.set("key", null));
	}

	@Test
	public void getOrInsert() {
		assertNull(configService.get("getOrInsert", null));
		assertEquals("1234567890abcde", configService.getOrInsert(null, "getOrInsert", () -> "1234567890abcde"));
		assertEquals("1234567890abcde", configService.getOrInsert(null, "getOrInsert", () -> "fghijklmnopqrst"));
	}

	@Test
	public void getOrInsertContact() {
		IPerson person = new IContactBuilder.PersonBuilder(coreModelService, "TestPersonGetOrInsert",
				"TestPersonGetOrInsert", LocalDate.now(), Gender.FEMALE).mandator().buildAndSave();
		assertNull(configService.get(person, "getOrInsert", null));
		assertEquals("1234567890abcde", configService.getOrInsert(person, "getOrInsert", () -> "1234567890abcde"));
		assertEquals("1234567890abcde", configService.getOrInsert(person, "getOrInsert", () -> "fghijklmnopqrst"));
	}

	@Test
	public void getSetAsList() {
		String TEST_KEY_SET = "TestKeySet";
		List<String> values = Arrays.asList(new String[] { "TestValue", "TestValue2", "TestValue3" });
		configService.set(TEST_KEY_SET, null);
		configService.setFromList(TEST_KEY_SET, values);
		List<String> asSet = configService.getAsList(TEST_KEY_SET, Collections.emptyList());
		assertEquals(3, asSet.size());
		assertTrue(asSet.contains("TestValue"));
		assertTrue(asSet.contains("TestValue2"));
		assertTrue(asSet.contains("TestValue3"));
	}

	@Test
	public void getSetBoolean() {
		configService.set("keyBoolA", "1");
		configService.set("keyBoolB", "true");
		configService.set("keyBoolC", "bla");
		configService.set("keyBoolD", "0");
		configService.set("keyBoolE", "false");
		assertTrue(configService.get("keyBoolA", false));
		assertTrue(configService.get("keyBoolB", false));
		assertFalse(configService.get("keyBoolC", true));
		assertFalse(configService.get("keyBoolD", true));
		assertFalse(configService.get("keyBoolE", true));
	}

	@Test
	public void setWithWithoutTrace() throws InterruptedException {
		assertTrue(configService.set("asdfkeyWoTrace", "valueNoTrace", false));
		assertTrue(configService.set("asdfkeyWTrace", "valueTrace", true));
		// trace gets added async

		String insertStatement = "SELECT action FROM TRACES WHERE action LIKE '%asdfkeyW%'";
		for (int i = 0; i < 10; i++) {
			List<String> traces = coreModelService.executeNativeQuery(insertStatement).map(o -> o.toString())
					.collect(Collectors.toList());
			if (traces.size() > 0) {
				assertEquals(1, traces.size());
				assertTrue(traces.get(0).startsWith("W globalCfg key [asdfkeyWTrace"));
				return;
			}
			Thread.sleep(100);
		}
		fail("could not find trace");
	}

	@Test
	public void getSetLocal() {
		configService.setLocal("localKey", null);
		assertEquals("foo", configService.getLocal("localKey", "foo"));
		configService.setLocal("localKey", "localValue");
		assertEquals("localValue", configService.getLocal("localKey", "foo"));
		configService.setLocal("localKey", null);
		assertEquals("foo", configService.getLocal("localKey", "foo"));
	}

}
