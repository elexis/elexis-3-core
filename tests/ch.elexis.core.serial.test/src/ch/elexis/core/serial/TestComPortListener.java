package ch.elexis.core.serial;

import java.util.ArrayList;
import java.util.List;

import ch.elexis.core.serial.Connection.ComPortListener;

public class TestComPortListener implements ComPortListener {
	
	private List<String> chunks;
	
	public TestComPortListener(){
		chunks = new ArrayList<>();
	}
	
	@Override
	public void gotChunk(Connection conn, String chunk){
		chunks.add(chunk);
	}
	
	public List<String> getChunks(){
		return chunks;
	}
}
