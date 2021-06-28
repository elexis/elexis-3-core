package ch.elexis.core.serial;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

public class Connection implements SerialPortDataListener {
	
	private static Logger logger = LoggerFactory.getLogger(Connection.class);
	
	public interface ComPortListener {
		public void gotChunk(Connection conn, String chunk);
	}
	
	private ComPortListener listener;
	private String myPort;
	private String[] mySettings;
	private String name;
	
	private SerialPort serialPort;
	
	private byte[] endOfChunk;
	private ByteArrayOutputStream buffer;
	
	public Connection(final String portName, final String port, final String settings,
		final ComPortListener l){
		listener = l;
		myPort = port;
		mySettings = settings.split(","); //$NON-NLS-1$
		name = portName;
	}
	
	public boolean connect(){
		try {
			openConnection();
			return serialPort != null && serialPort.isOpen();
		} catch (Exception ex) {
			logger.error("Exception on connect", ex);
			return false;
		}
		
	}
	
	/**
	 * Data is collected in buffer until end of chunk bytes are received. ComPortListener#gotChunk
	 * is only called with data including end of chunk.
	 * 
	 * @param endOfChunk
	 * @return
	 */
	public Connection withEndOfChunk(byte[] endOfChunk){
		this.endOfChunk = endOfChunk;
		return this;
	}
	
	/**
	 * Attempts to open a serial connection and streams using the settings.
	 * 
	 */
	public void openConnection(){
		// lookup a SerialPort matching myPort
		serialPort = SerialPort.getCommPort(myPort);
		if (serialPort != null) {
			setConnectionParameters();
			if (serialPort.openPort()) {
				serialPort.addDataListener(this);
			}
		} else {
			logger.warn("No serial port [" + myPort + "] found.");
		}
	}
	
	/**
	 * Sets the connection parameters to the settings.
	 */
	public void setConnectionParameters() {
		
		//		sp.setPortName(myPort);
		serialPort.setComPortParameters(Integer.parseInt(mySettings[0]),
			Integer.parseInt(mySettings[1]), Integer.parseInt(mySettings[3]),
			getParity(mySettings[2]));
		//		sp.setBaudRate(mySettings[0]);
		//		sp.setDatabits(mySettings[1]);
		//		sp.setParity(mySettings[2]);
		//		sp.setStopbits(mySettings[3]);
		// TODO serialPort.setFlowControl(getListeningEvents())
		//		serialPort.setFlowControl(
		//			SerialPort.FLOW_CONTROL_RTS_ENABLED | SerialPort.FLOW_CONTROL_CTS_ENABLED);
		//		if (mySettings.length >= 5 && mySettings[4] != null) {
		//			sp.setFlowControlIn(Integer.parseInt(mySettings[4]));
		//		}
		//		if (mySettings.length >= 6 && mySettings[5] != null) {
		//			sp.setFlowControlOut(Integer.parseInt(mySettings[5]));
		//		}
	}
	

	
	private int getParity(String parity){
		if (parity.equalsIgnoreCase("Even")) { //$NON-NLS-1$
			return SerialPort.EVEN_PARITY;
		}
		if (parity.equalsIgnoreCase("Odd")) { //$NON-NLS-1$
			return SerialPort.ODD_PARITY;
		}
		return SerialPort.NO_PARITY;
	}
	
	@Override
	public void serialEvent(final SerialPortEvent e){
		if (e.getEventType() == SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
			byte[] newData = new byte[serialPort.bytesAvailable()];
			int numRead = serialPort.readBytes(newData, newData.length);
			if (numRead != newData.length) {
				logger.warn("Failed to read [" + newData.length + "] bytes, got [" + numRead + "]");
			}
			if (endOfChunk != null) {
				try {
					if (buffer == null) {
						buffer = new ByteArrayOutputStream();
					}
					buffer.write(newData);
					if (find(buffer.toByteArray(), endOfChunk)) {
						listener.gotChunk(this, new String(buffer.toByteArray()));
						// start new buffer
						buffer = new ByteArrayOutputStream();
					}
				} catch (Exception ex) {
					logger.error("Exception buffering chunk", ex);
				}
			} else {
				listener.gotChunk(this, new String(newData));
			}
		}
	}
	
	private boolean find(byte[] buffer, byte[] key){
		for (int i = 0; i <= buffer.length - key.length; i++) {
			int j = 0;
			while (j < key.length && buffer[i + j] == key[j]) {
				j++;
			}
			if (j == key.length) {
				return true;
			}
		}
		return false;
	}
	
	public void close(){
		close(5000);
	}
	
	public void close(int sleepTime){
		// avoid rxtx-deadlock when called from an EventListener
		new Thread(new Runnable() {
			
			public void run(){
				try {
					Thread.sleep(sleepTime);
					serialPort.closePort();
				} catch (Exception ex) {
					
				}
			}
		}).start();
	}
	
	/**
	 * Reports the open status of the port.
	 * 
	 * @return true if port is open, false if port is closed.
	 */
	public boolean isOpen(){
		return serialPort != null && serialPort.isOpen();
	}
	
	public boolean send(final String data){
		try {
			byte[] bytes = data.getBytes();
			return serialPort.writeBytes(bytes, bytes.length) == bytes.length;
		} catch (Exception ex) {
			logger.error("Exception sending data [" + data + "]");
			return false;
		}
	}
	
	/**
	 * Send a one second break signal.
	 */
	public void sendBreak(){
		if (serialPort != null) {
			serialPort.setBreak();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// ignore
			}
			serialPort.clearBreak();
		}
	}
	
	public static String[] getComPorts(){
		ArrayList<String> p = new ArrayList<String>();
		try {
			for (SerialPort serialPort : SerialPort.getCommPorts()) {
				p.add(serialPort.getSystemPortName());
			}
		} catch (Exception ex) {
			logger.error("Exception getting comm ports ", ex);
		}
		return p.toArray(new String[p.size()]);
	}
	
	public String getName(){
		return name;
	}
	
	public String getMyPort(){
		return myPort;
	}
	
	@Override
	public int getListeningEvents(){
		return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
	}
}
