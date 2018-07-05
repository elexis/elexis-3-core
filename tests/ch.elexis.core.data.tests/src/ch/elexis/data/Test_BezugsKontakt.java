package ch.elexis.data;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Test;

import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.types.RelationshipType;
import ch.rgw.tools.JdbcLink;
import junit.framework.Assert;

public class Test_BezugsKontakt extends AbstractPersistentObjectTest {
	
	public Test_BezugsKontakt(JdbcLink link){
		super(link);
	}


	@Test
	public void TestBezugsKontakt() throws ElexisException{
		Patient patient = new Patient("Mustermann", "Max", "1.1.2000", "m");
		Patient kontakt = new Patient("Musterfrau", "Erika", "1.1.1970", "f");
		Map<String, BezugsKontaktRelation> mapBezugKonktatRelation = new HashMap<>();
		BezugsKontaktRelation selectedBezugKontaktRelation = new BezugsKontaktRelation();
		selectedBezugKontaktRelation.setDestRelationType(RelationshipType.FAMILY_PARENT);
		selectedBezugKontaktRelation.setSrcRelationType(RelationshipType.FAMILY_CHILD);
		BezugsKontakt bezugsKontakt = new BezugsKontakt(kontakt, patient, selectedBezugKontaktRelation);
		
		List<BezugsKontakt> bezugsKontakte = kontakt.getBezugsKontakte();
		Assert.assertEquals(1, bezugsKontakte.size());
		
		BezugsKontakt bezogenerKontakt = bezugsKontakte.get(0);
		if (Locale.getDefault().toString().equals("de_CH")) {
			// System.out.println("found de_CH: " + savedZusatzAdresse.getLabel());
			Assert.assertTrue(bezogenerKontakt.getLabel().startsWith("Kind"));
		}
		if (Locale.getDefault().toString().equals("en_US")) {
			// System.out.println("found en_US: " + savedZusatzAdresse.getLabel());
			Assert.assertTrue(bezogenerKontakt.getLabel().startsWith("child"));
		}
		Assert.assertTrue(bezogenerKontakt.getLabel().startsWith(ch.elexis.core.l10n.Messages.RelationshipType_FAMILY_CHILD));
		System.out.println(RelationshipType.FAMILY_CHILD.getName());
		Assert.assertNotNull(bezogenerKontakt.getId());
	}	
}
