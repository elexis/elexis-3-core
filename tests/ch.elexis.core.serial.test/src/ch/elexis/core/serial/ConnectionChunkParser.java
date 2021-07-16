package ch.elexis.core.serial;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import ch.elexis.core.serial.test.AllTests;

public class ConnectionChunkParser {
	
	@Ignore
	@Test
	public void testEndOfChunk() throws IOException{
		TestComPortListener portListener = new TestComPortListener();
		Connection conn = new Connection("ConnectionChunkParser", "", "", portListener)
			.withEndOfChunk(new byte[] {
				Connection.ETX
			}).excludeDelimiters(true);
		byte[] bytes = AllTests.readBytes("/rsc/test_endofchunk");
		// set bytes sequential
		for (byte b : bytes) {
			conn.setData(new byte[] {
				b
			});
		}
		assertTrue(portListener.getChunks().size() == 1);
		assertEquals("test", portListener.getChunks().get(0));
	}
	
	@Ignore
	@Test
	public void testEndOfChunkMulti() throws IOException{
		TestComPortListener portListener = new TestComPortListener();
		Connection conn = new Connection("ConnectionChunkParser", "", "", portListener)
			.withEndOfChunk(new byte[] {
				Connection.ETX
			}).excludeDelimiters(true);
		byte[] bytes = AllTests.readBytes("/rsc/test_endofchunk_multi");
		// set bytes sequential
		for (byte b : bytes) {
			conn.setData(new byte[] {
				b
			});
		}
		assertTrue(portListener.getChunks().size() == 3);
		assertEquals("test", portListener.getChunks().get(0));
		assertEquals("test1", portListener.getChunks().get(1));
		assertEquals("test2", portListener.getChunks().get(2));
	}
	
	@Test
	public void testStartEndOfChunkMulti() throws IOException{
		TestComPortListener portListener = new TestComPortListener();
		Connection conn = new Connection("ConnectionChunkParser", "", "", portListener)
			.withEndOfChunk(new byte[] {
				Connection.ETX
			}).withStartOfChunk(new byte[] {
				Connection.STX
			}).excludeDelimiters(true);
		byte[] bytes = AllTests.readBytes("/rsc/test_startendofchunk_multi");
		// set bytes sequential
		for (byte b : bytes) {
			conn.setData(new byte[] {
				b
			});
		}
		assertTrue(portListener.getChunks().size() == 4);
		assertEquals("test", portListener.getChunks().get(0));
		assertEquals("test1", portListener.getChunks().get(1));
		assertEquals("test2", portListener.getChunks().get(2));
		assertEquals("test3", portListener.getChunks().get(3));
	}
	
	@Test
	public void testStartVaraibleEndOfChunkMulti() throws IOException{
		TestComPortListener portListener = new TestComPortListener();
		Connection conn = new Connection("ConnectionChunkParser", "", "", portListener)
			.withEndOfChunk("END OF CHUNK".getBytes(), "....".getBytes())
			.withStartOfChunk("START OF CHUNK".getBytes()).excludeDelimiters(true);
		byte[] bytes = AllTests.readBytes("/rsc/test_startvariableendofchunk");
		// set bytes sequential
		for (byte b : bytes) {
			conn.setData(new byte[] {
				b
			});
		}
		assertTrue(portListener.getChunks().size() == 2);
		assertTrue(portListener.getChunks().get(0).contains("LINE 1"));
		assertTrue(portListener.getChunks().get(1).contains("LINE 1\nLINE 2"));
	}
}
