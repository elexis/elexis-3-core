package ch.elexis.core.jpa.entities.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Hashtable;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.jpa.entities.EntityWithExtInfo;

public class ExtInfoUtil {

	private static Logger log = LoggerFactory.getLogger(ExtInfoUtil.class);

	public static Object getExtInfo(EntityWithExtInfo entity, Object key) {
		Map<Object, Object> extInfo = loadExtInfo(entity);
		return extInfo.get(key);
	}

	public static void setExtInfo(EntityWithExtInfo entity, Object key, Object value) {
		Map<Object, Object> extInfo = loadExtInfo(entity);

		if (value == null) {
			extInfo.remove(key);
		} else {
			extInfo.put(key, value);
		}
		entity.setExtInfo(extInfoToBytes(extInfo));
	}

	public static Map<Object, Object> loadExtInfo(EntityWithExtInfo entity) {
		byte[] bytes = entity.getExtInfo();
		if (bytes != null) {
			return extInfoFromBytes(bytes);
		} else {
			return new Hashtable<>();
		}
	}

	/**
	 * This method loads {@link Hashtable} from the byte array in an Elexis way.
	 *
	 * @param dataValue
	 * @return
	 */
	public static Map<Object, Object> extInfoFromBytes(byte[] dataValue) {
		if (dataValue != null) {
			Hashtable<Object, Object> ret = fold((byte[]) dataValue);
			if (ret == null) {
				return new Hashtable<Object, Object>();
			}
			return ret;
		}
		return new Hashtable<Object, Object>();
	}

	/**
	 * Elexis persistence contains BLOBs of serialized {@link Hashtable<Object,
	 * Object>}. All types of serializable data (mostly String) can be stored and
	 * loaded from these ExtInfos. This method serializes a {@link Hashtable} in the
	 * Elexis way.
	 *
	 * @param extInfo
	 * @return
	 */
	public static byte[] extInfoToBytes(Map<Object, Object> extInfo) {
		if (extInfo != null && !extInfo.isEmpty()) {
			Hashtable<Object, Object> ov = (Hashtable<Object, Object>) extInfo;
			return flatten(ov);
		}
		return null;
	}

	/**
	 * Convert a Hashtable into a compressed byte array.
	 *
	 * @param hash the hashtable to store
	 * @return
	 */
	private static byte[] flatten(final Hashtable<Object, Object> hash) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream(hash.size() * 30);
			ZipOutputStream zos = new ZipOutputStream(baos);
			zos.putNextEntry(new ZipEntry("hash"));
			ObjectOutputStream oos = new ObjectOutputStream(zos);
			oos.writeObject(hash);
			zos.close();
			baos.close();
			return baos.toByteArray();
		} catch (Exception ex) {
			log.warn("Exception flattening HashTable, returning null: " + ex.getMessage());
			return null;
		}
	}

	/**
	 * Recreate a Hashtable from a byte array as created by flatten()
	 *
	 * @param flat the byte array
	 * @return the original Hashtable or null if no Hashtable could be created from
	 *         the array
	 */
	@SuppressWarnings("unchecked")
	private static Hashtable<Object, Object> fold(final byte[] flat) {
		if (flat.length == 0) {
			return null;
		}
		try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(flat))) {
			ZipEntry entry = zis.getNextEntry();
			if (entry != null) {
				try (ObjectInputStream ois = new ObjectInputStream(zis)) {
					Hashtable<Object, Object> readObject = (Hashtable<Object, Object>) ois.readObject();
					return readObject;
				}
			} else {
				return null;
			}
		} catch (IOException | ClassNotFoundException ex) {
			log.error("Exception folding byte array", ex);
			return null;
		}
	}
}