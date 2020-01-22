package ch.elexis.core.coding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.codes.IValueSetContribution;

public class ChErpValueSetsTest {
	
	private static IValueSetContribution contribution;
	
	@BeforeClass
	public static void beforeClass() throws InvalidSyntaxException{
		Bundle bundle = FrameworkUtil.getBundle(ChErpValueSetsTest.class);
		Collection<ServiceReference<IValueSetContribution>> references = Collections.emptyList();
			references = bundle.getBundleContext().getServiceReferences(IValueSetContribution.class, null);
		for (ServiceReference<IValueSetContribution> serviceReference : references) {
			Dictionary<String, Object> props = serviceReference.getProperties();
			if (props.get("component.name")
				.equals("ch.elexis.core.coding.ChEprValueSetsContribution")) {
				contribution = bundle.getBundleContext().getService(serviceReference);
				break;
			}
		}
		assertNotNull(contribution);
	}
	
	@Test
	public void getIds(){
		assertNotNull(contribution.getValueSetIds());
		assertFalse(contribution.getValueSetIds().isEmpty());
		assertTrue(contribution.getValueSetIds().contains("2.16.756.5.30.1.127.3.10.1.1.3"));
	}
	
	@Test
	public void getEprAuthorRoleCodes(){
		List<ICoding> codes = contribution.getValueSet("2.16.756.5.30.1.127.3.10.1.1.3");
		assertNotNull(codes);
		assertFalse(codes.isEmpty());
		ICoding coding = findCoding(codes, "46255001");
		assertNotNull(coding);
		assertEquals("2.16.840.1.113883.6.96", coding.getSystem());
		assertEquals("Apotheker", coding.getDisplay());
	}
	
	private ICoding findCoding(List<ICoding> codes, String string){
		for (ICoding iCoding : codes) {
			if (iCoding.getCode().equals(string)) {
				return iCoding;
			}
		}
		return null;
	}
}
