package ch.elexis.core.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class DocumentStatusMapperTest {
	
	@Test
	public void test(){
		Set<DocumentStatus> map = new HashSet<DocumentStatus>(DocumentStatusMapper.map(3));
		assertTrue(map.contains(DocumentStatus.INDEXED));
		assertTrue(map.contains(DocumentStatus.PREPROCESSED));
		assertFalse(map.contains(DocumentStatus.NEW));
		assertFalse(map.contains(DocumentStatus.SENT));
		
		int value = DocumentStatusMapper.map(new HashSet<>(map));
		assertEquals(3, value);
		
		value = DocumentStatusMapper.map(Collections.singletonList(DocumentStatus.SENT));
		assertEquals(DocumentStatus.SENT_VALUE, value);
	}
	
}
