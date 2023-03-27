package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.constants.XidConstants;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.INamedQuery;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.utils.OsgiServiceUtil;

public class XidTest {
	private IModelService modelService;

	private IContact contact1;
	private IContact contact2;

	@Before
	public void before() {
		modelService = OsgiServiceUtil
				.getService(IModelService.class, "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)").get();

		contact1 = modelService.create(IContact.class);
		contact1.setDescription1("test contact 1");
		modelService.save(contact1);
		contact2 = modelService.create(IContact.class);
		contact2.setDescription1("test contact 2");
		modelService.save(contact2);
	}

	@After
	public void after() {
		modelService.remove(contact1);
		modelService.remove(contact2);

		OsgiServiceUtil.ungetService(modelService);
		modelService = null;
	}

	@Test
	public void create() {
		IXid xid = modelService.create(IXid.class);
		assertNotNull(xid);
		assertTrue(xid instanceof IXid);

		xid.setDomain("http://www.test.info");
		xid.setDomainId("testId");
		xid.setObject(contact1);
		modelService.save(xid);

		Optional<IXid> loadedXid = modelService.load(xid.getId(), IXid.class);
		assertTrue(loadedXid.isPresent());
		assertFalse(xid == loadedXid.get());
		assertEquals(xid, loadedXid.get());
		assertEquals(contact1, loadedXid.get().getObject(IContact.class));

		modelService.remove(xid);
	}

	@Test
	public void query() {
		IXid xid1 = modelService.create(IXid.class);
		xid1.setDomain("http://www.test.info");
		xid1.setDomainId("testId1");
		xid1.setObject(contact1);
		modelService.save(xid1);

		IXid xid2 = modelService.create(IXid.class);
		xid2.setDomain("http://www.test.info");
		xid2.setDomainId("testId2");
		xid2.setObject(contact2);
		modelService.save(xid2);

		IQuery<IXid> query = modelService.getQuery(IXid.class);
		query.and(ModelPackage.Literals.IXID__DOMAIN, COMPARATOR.EQUALS, "http://www.test.info");
		List<IXid> existing = query.execute();
		assertNotNull(existing);
		assertFalse(existing.isEmpty());
		assertEquals(2, existing.size());
		for (IXid iXid : existing) {
			IContact contact = iXid.getObject(IContact.class);
			assertNotNull(contact);
			if (iXid.getDomainId().endsWith("1")) {
				assertEquals(contact1, contact);
				assertEquals(contact1.getXid("http://www.test.info").getId(), iXid.getId());
			} else if (iXid.getDomainId().endsWith("2")) {
				assertEquals(contact2, contact);
				assertEquals(contact2.getXid("http://www.test.info").getId(), iXid.getId());
			}
		}

		modelService.remove(xid1);
		modelService.remove(xid2);
	}

	/**
	 * AHV numbers may be stored in exact form like <code>756.1234.1234.56</code> or
	 * in loose notation like <code>7561234123456</code>. They are semantically
	 * equivalent, thus a search should be possible that considers this.
	 */
	@Test
	public void queryAhvUnified() {
		IXid xid1 = modelService.create(IXid.class);
		xid1.setDomain(XidConstants.DOMAIN_AHV);
		xid1.setDomainId("756.1234.1234.56");
		xid1.setObject(contact1);
		modelService.save(xid1);

		INamedQuery<IXid> namedQuery = modelService.getNamedQuery(IXid.class, "ahvdomainid");
		List<IXid> xids = namedQuery.executeWithParameters(namedQuery.getParameterMap("ahvdomainid", "7561234123456"));
		assertEquals(contact1.getId(), xids.get(0).getObject(IContact.class).getId());

		xids = namedQuery.executeWithParameters(namedQuery.getParameterMap("ahvdomainid", "756.1234.123456"));
		assertEquals(contact1.getId(), xids.get(0).getObject(IContact.class).getId());
	}
}
