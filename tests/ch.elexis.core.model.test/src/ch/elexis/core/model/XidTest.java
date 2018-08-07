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

import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.utils.OsgiServiceUtil;

public class XidTest {
	private IModelService modelSerice;
	
	private IContact contact1;
	private IContact contact2;
	
	@Before
	public void before(){
		modelSerice = OsgiServiceUtil.getService(IModelService.class).get();
		
		contact1 = modelSerice.create(IContact.class);
		contact1.setDescription1("test contact 1");
		modelSerice.save(contact1);
		contact2 = modelSerice.create(IContact.class);
		contact2.setDescription1("test contact 2");
		modelSerice.save(contact2);
	}
	
	@After
	public void after(){
		modelSerice.remove(contact1);
		modelSerice.remove(contact2);
		
		OsgiServiceUtil.ungetService(modelSerice);
		modelSerice = null;
	}
	
	@Test
	public void create(){
		IXid xid = modelSerice.create(IXid.class);
		assertNotNull(xid);
		assertTrue(xid instanceof IXid);
		
		xid.setDomain("http://www.test.info");
		xid.setDomainId("testId");
		xid.setObject(contact1);
		assertTrue(modelSerice.save(xid));
		
		Optional<IXid> loadedXid = modelSerice.load(xid.getId(), IXid.class);
		assertTrue(loadedXid.isPresent());
		assertFalse(xid == loadedXid.get());
		assertEquals(xid, loadedXid.get());
		assertEquals(contact1, loadedXid.get().getObject(IContact.class));
		
		modelSerice.remove(xid);
	}
	
	@Test
	public void query(){
		IXid xid1 = modelSerice.create(IXid.class);
		xid1.setDomain("http://www.test.info");
		xid1.setDomainId("testId1");
		xid1.setObject(contact1);
		assertTrue(modelSerice.save(xid1));
		
		IXid xid2 = modelSerice.create(IXid.class);
		xid2.setDomain("http://www.test.info");
		xid2.setDomainId("testId2");
		xid2.setObject(contact2);
		assertTrue(modelSerice.save(xid2));
		
		IQuery<IXid> query = modelSerice.getQuery(IXid.class);
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
		
		modelSerice.remove(xid1);
		modelSerice.remove(xid2);
	}
}
