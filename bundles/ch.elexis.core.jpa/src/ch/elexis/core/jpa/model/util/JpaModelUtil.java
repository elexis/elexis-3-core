package ch.elexis.core.jpa.model.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.jpa.entities.EntityWithId;
import ch.elexis.core.jpa.entities.Xid;
import ch.elexis.core.model.IXid;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IElexisEntityManager;
import ch.rgw.compress.CompEx;

/**
 * Utility class with methods JPA Identifiable specific methods
 * 
 * @author thomas
 *
 */
@Component
public class JpaModelUtil {
	
	private static Logger logger = LoggerFactory.getLogger(JpaModelUtil.class);
	
	private static IElexisEntityManager entityManager;
	
	@Reference(cardinality = ReferenceCardinality.MANDATORY)
	public void setEntityManager(IElexisEntityManager entityManager){
		JpaModelUtil.entityManager = entityManager;
	}
	
	/**
	 * Add an {@link IXid} to the {@link Identifiable}. Performs save operation.
	 * 
	 * @param identifiable
	 * @param domain
	 * @param id
	 * @param updateIfExists
	 * @return
	 */
	public static boolean addXid(Identifiable identifiable, String domain, String id,
		boolean updateIfExists){
		Optional<Xid> existing = getXid(domain, id);
		if (existing.isPresent()) {
			if (updateIfExists) {
				Xid xid = existing.get();
				xid.setDomain(domain);
				xid.setDomainId(id);
				xid.setObject(identifiable.getId());
				saveEntity(xid);
				return true;
			}
		} else {
			Xid xid = new Xid();
			xid.setDomain(domain);
			xid.setDomainId(id);
			xid.setObject(identifiable.getId());
			saveEntity(xid);
			return true;
		}
		return false;
	}
	
	/**
	 * Get an {@link IXid} with matching domain and id.
	 * 
	 * @param domain
	 * @param id
	 * @return
	 */
	public static Optional<Xid> getXid(String domain, String id){
		EntityManager em = (EntityManager) entityManager.getEntityManager();
		TypedQuery<Xid> query = em.createNamedQuery("Xid.domain.domainid", Xid.class);
		query.setParameter("domain", domain);
		query.setParameter("domainid", id);
		List<Xid> xids = query.getResultList();
		if (xids.size() > 0) {
			if (xids.size() > 1) {
				logger.error(
					"XID [" + domain + "] [" + id + "] on multiple objects, returning first.");
			}
			return Optional.of(xids.get(0));
		}
		return Optional.empty();
	}
	
	/**
	 * Get an {@link IXid} with matching {@link Identifiable} and domain.
	 * 
	 * @param identifiable
	 * @param domain
	 * @return
	 */
	public static Optional<Xid> getXid(Identifiable identifiable, String domain){
		EntityManager em = (EntityManager) entityManager.getEntityManager();
		TypedQuery<Xid> query = em.createNamedQuery("Xid.domain.objectid", Xid.class);
		query.setParameter("domain", domain);
		query.setParameter("objectid", identifiable.getId());
		List<Xid> xids = query.getResultList();
		if (xids.size() > 0) {
			if (xids.size() > 1) {
				logger.error(
					"XID [" + domain + "] [" + identifiable
						+ "] on multiple objects, returning first.");
			}
			return Optional.of(xids.get(0));
		}
		return Optional.empty();
	}
	
	private static void saveEntity(EntityWithId entity){
		EntityManager em = (EntityManager) entityManager.getEntityManager(false);
		try {
			em.getTransaction().begin();
			em.merge(entity);
			em.getTransaction().commit();
		} finally {
			entityManager.closeEntityManager(em);
		}
	}
	
	/**
	 * Convert a Hashtable into a compressed byte array.
	 * 
	 * @param hash
	 *            the hashtable to store
	 * @return
	 */
	private static byte[] flatten(final Hashtable<Object, Object> hash){
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
			logger.warn("Exception flattening HashTable, returning null: " + ex.getMessage());
			return null;
		}
	}
	
	/**
	 * Recreate a Hashtable from a byte array as created by flatten()
	 * 
	 * @param flat
	 *            the byte array
	 * @return the original Hashtable or null if no Hashtable could be created from the array
	 */
	@SuppressWarnings("unchecked")
	private static Hashtable<Object, Object> fold(final byte[] flat){
		if (flat.length == 0) {
			return null;
		}
		try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(flat))) {
			ZipEntry entry = zis.getNextEntry();
			if (entry != null) {
				try (ObjectInputStream ois = new ObjectInputStream(zis)) {
					return (Hashtable<Object, Object>) ois.readObject();
				}
			} else {
				return null;
			}
		} catch (IOException | ClassNotFoundException ex) {
			logger.error("Exception folding byte array", ex);
			return null;
		}
	}
	
	/**
	 * Elexis persistence contains BLOBs of serialized {@link Hashtable<Object, Object>}. All types
	 * of serializable data (mostly String) can be stored and loaded from these ExtInfos. This
	 * method serializes a {@link Hashtable} in the Elexis way.
	 * 
	 * @param extInfo
	 * @return
	 */
	public static byte[] extInfoToBytes(Map<Object, Object> extInfo){
		if (extInfo != null && !extInfo.isEmpty()) {
			Hashtable<Object, Object> ov = (Hashtable<Object, Object>) extInfo;
			return flatten(ov);
		}
		return null;
	}
	
	/**
	 * This method loads {@link Hashtable} from the byte array in an Elexis way.
	 * 
	 * @param dataValue
	 * @return
	 */
	public static Map<Object, Object> extInfoFromBytes(byte[] dataValue){
		if (dataValue != null) {
			Hashtable<Object, Object> ret = fold((byte[]) dataValue);
			if (ret == null) {
				return new Hashtable<Object, Object>();
			}
			return ret;
		}
		return Collections.emptyMap();
	}
	
	/**
	 * Expand the compressed bytes using the Elexis {@link CompEx} tool.
	 * 
	 * @param comp
	 * @return
	 */
	public static byte[] getExpanded(byte[] compacted){
		return CompEx.expand(compacted);
	}
	
	/**
	 * Compress the String using the Elexis {@link CompEx} tool.
	 * 
	 * @param comp
	 * @return
	 */
	public static byte[] getCompressed(String value){
		return CompEx.Compress(value, CompEx.ZIP);
	}
}
