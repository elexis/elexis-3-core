package ch.elexis.core.serial;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import ch.elexis.core.utils.CoreUtil;

public class Connection implements SerialPortDataListener {

	public static final int STX = 0x02;
	public static final int ETX = 0x03;

	private static Logger logger = LoggerFactory.getLogger(Connection.class);

	public interface ComPortListener {
		/**
		 * Called with chunk as {@link String} with default charset. Overwrite if
		 * {@link String} access to data is enough.
		 *
		 * @param conn
		 * @param chunk
		 */
		public default void gotChunk(Connection conn, String chunk) {
		};

		/**
		 * Called with chunk as byte array. Overwrite if binary access to data is needed
		 * or charset control is needed.
		 *
		 * @param conn
		 * @param data
		 */
		public default void gotData(Connection conn, byte[] data) {
		}

		/**
		 * Called after {@link Connection#close()} or {@link Connection#close(int)} is
		 * executed.
		 */
		public void closed();
	}

	private ComPortListener listener;
	private String myPort;
	private String[] mySettings;
	private String name;

	private SerialPort serialPort;

	private byte[] startOfChunk;
	private List<byte[]> endOfChunk;
	private ByteArrayOutputStream buffer;
	private boolean excludeDelimiters = false;

	public Connection(final String portName, final String port, final String settings, final ComPortListener l) {
		listener = l;
		myPort = port;
		mySettings = settings.split(","); //$NON-NLS-1$
		name = portName;
		logger.info("SerialPort config: [" + portName + "][" + port + "][" + settings + "]");
	}

	public boolean connect() {
		try {
			openConnection();
			return serialPort != null && serialPort.isOpen();
		} catch (Exception ex) {
			logger.error("Exception on connect", ex);
			return false;
		}

	}

	/**
	 * Data is collected in buffer until one of the provided end of chunk bytes are
	 * received. {@link ComPortListener#gotChunk(Connection, String)} is only called
	 * with data including end of chunk.
	 *
	 * @param endOfChunk
	 * @return
	 */
	public Connection withEndOfChunk(byte[]... endOfChunk) {
		if (endOfChunk != null) {
			this.endOfChunk = Arrays.asList(endOfChunk);
		} else {
			this.endOfChunk = null;
		}
		return this;
	}

	/**
	 * Data is collected in buffer until end of chunk bytes (see
	 * {@link #withEndOfChunk(byte[])}) are received.
	 * {@link ComPortListener#gotChunk(Connection, String)} is called with data
	 * starting from start of chunk, including end of chunk.
	 *
	 * @param endOfChunk
	 * @return
	 */
	public Connection withStartOfChunk(byte[] startOfChunk) {
		this.startOfChunk = startOfChunk;
		return this;
	}

	/**
	 * Attempts to open a serial connection and streams using the settings.
	 *
	 */
	public void openConnection() {
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
		serialPort.setComPortParameters(Integer.parseInt(mySettings[0]), Integer.parseInt(mySettings[1]),
				Integer.parseInt(mySettings[3]), getParity(mySettings[2]));

		int flowControl = -1;
		if (mySettings.length >= 5 && mySettings[4] != null && mySettings.length >= 6 && mySettings[5] != null) {
			flowControl = getFlowControl(mySettings[4]);
		}
		if (mySettings.length >= 6 && mySettings[5] != null) {
			flowControl = flowControl | getFlowControl(mySettings[5]);
		}
		if (flowControl > -1) {
			serialPort.setFlowControl(flowControl);
		}
	}

	/**
	 * Get the flow control value for the provided {@link String}. Values matching
	 * an rxtx config value are translated to jserial config values.<br/>
	 * <br/>
	 * RXTX flow control constants:<br/>
	 * <li>FLOWCONTROL_NONE = 0</li>
	 * <li>FLOWCONTROL_RTSCTS_IN = 1</li>
	 * <li>FLOWCONTROL_RTSCTS_OUT = 2</li>
	 * <li>FLOWCONTROL_XONXOFF_IN = 4</li>
	 * <li>FLOWCONTROL_XONXOFF_OUT = 8</li> <br/>
	 * <br/>
	 * JSERIAL flow control constants:<br/>
	 * <li>FLOW_CONTROL_DISABLED = 0</li>
	 * <li>FLOW_CONTROL_RTS_ENABLED = 1</li>
	 * <li>FLOW_CONTROL_CTS_ENABLED = 16</li>
	 * <li>FLOW_CONTROL_DSR_ENABLED = 256</li>
	 * <li>FLOW_CONTROL_DTR_ENABLED = 4096</li>
	 * <li>FLOW_CONTROL_XONXOFF_IN_ENABLED = 65536</li>
	 * <li>FLOW_CONTROL_XONXOFF_OUT_ENABLED = 1048576</li>
	 *
	 * @param string
	 * @return
	 */
	private int getFlowControl(String string) {
		try {
			int value = Integer.parseInt(string);
			if (value == 2) {
				value = SerialPort.FLOW_CONTROL_CTS_ENABLED;
			} else if (value == 4) {
				value = SerialPort.FLOW_CONTROL_XONXOFF_IN_ENABLED;
			} else if (value == 8) {
				value = SerialPort.FLOW_CONTROL_XONXOFF_OUT_ENABLED;
			}
			return value;
		} catch (NumberFormatException e) {
			logger.warn("Non numeric flow control [" + string + "]");
		}
		return -1;
	}

	private int getParity(String parity) {
		if (parity.equalsIgnoreCase("Even")) { //$NON-NLS-1$
			return SerialPort.EVEN_PARITY;
		}
		if (parity.equalsIgnoreCase("Odd")) { //$NON-NLS-1$
			return SerialPort.ODD_PARITY;
		}
		return SerialPort.NO_PARITY;
	}

	@Override
	public void serialEvent(final SerialPortEvent e) {
		if (e.getEventType() == SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
			byte[] newData = new byte[serialPort.bytesAvailable()];
			int numRead = serialPort.readBytes(newData, newData.length);
			if (numRead != newData.length) {
				logger.warn("Failed to read [" + newData.length + "] bytes, got [" + numRead + "]");
			}
			setData(newData);
		}
	}

	/**
	 * If {@link Connection#endOfChunk} is set the data is buffered, else the data
	 * is passed to the {@link ComPortListener}. This method is also used as entry
	 * point to test the data buffer handling.
	 *
	 * @param newData
	 */
	protected void setData(byte[] newData) {
		if (endOfChunk != null) {
			try {
				if (buffer == null) {
					buffer = new ByteArrayOutputStream();
				}
				buffer.write(newData);
				while (hasChunk(buffer)) {
					byte[] bytes = buffer.toByteArray();
					for (byte[] bs : endOfChunk) {
						int endIndex = indexOf(bytes, bs);
						if (endIndex != -1) {
							fireData(getChunk(bytes, bs, endIndex));
							// start new buffer
							buffer = new ByteArrayOutputStream();
							// if any remaining bytes add to new buffer
							if (bytes.length > endIndex + bs.length) {
								buffer.write(Arrays.copyOfRange(bytes, endIndex + bs.length, bytes.length));
							}
						}
					}
				}
				writeDebugBuffer();
			} catch (Exception ex) {
				logger.error("Exception buffering chunk", ex);
			}
		} else {
			fireData(newData);
		}
	}

	private void writeDebugBuffer() {
		File userDir = CoreUtil.getWritableUserDir();
		File bufferOutput = new File(userDir, "serailbuffer_debug.bin");
		try (FileOutputStream fout = new FileOutputStream(bufferOutput)) {
			fout.write(buffer.toByteArray());
		} catch (IOException e) {
			logger.error("Could not write serailbuffer_debug.bin", e);
		}
		logger.info("Wrote [" + bufferOutput.getAbsolutePath() + "] with size [" + bufferOutput.length() + "]");
	}

	protected void fireData(byte[] data) {
		if (data != null && data.length > 0) {
			listener.gotData(this, data);
			fireChunk(new String(data));
		}
	}

	protected void fireChunk(String chunk) {
		if (StringUtils.isNotBlank(chunk)) {
			logger.info("Serial chunk [" + chunk + "]");
			listener.gotChunk(this, chunk);
		}
	}

	private boolean hasChunk(ByteArrayOutputStream buffer) {
		for (byte[] bs : endOfChunk) {
			if (indexOf(buffer.toByteArray(), bs) != -1) {
				return true;
			}
		}
		return false;
	}

	private byte[] getChunk(byte[] buffer, byte[] matchingEndOfChunk, int endIndex) {
		if (startOfChunk != null) {
			int startIndex = indexOf(buffer, startOfChunk);
			if (startIndex == -1) {
				startIndex = 0;
			}
			if (excludeDelimiters) {
				byte[] chunkBytes = Arrays.copyOfRange(buffer, startIndex + startOfChunk.length, endIndex);
				int startOffset = getStartOffset(chunkBytes);
				return Arrays.copyOfRange(chunkBytes, startOffset, chunkBytes.length);
			} else {
				return Arrays.copyOfRange(buffer, startIndex, endIndex + matchingEndOfChunk.length);
			}
		} else {
			if (excludeDelimiters) {
				return Arrays.copyOfRange(buffer, 0, endIndex);
			} else {
				return Arrays.copyOfRange(buffer, 0, endIndex + matchingEndOfChunk.length);
			}
		}
	}

	private int getStartOffset(byte[] buffer) {
		int ret = 0;
		int offset = indexOf(buffer, startOfChunk);
		while (offset != -1) {
			ret = ret + offset + startOfChunk.length;
			buffer = Arrays.copyOfRange(buffer, offset + startOfChunk.length, buffer.length);
			offset = indexOf(buffer, startOfChunk);
		}
		return ret;
	}

	private int indexOf(byte[] array, byte[] target) {
		if (target.length == 0) {
			return 0;
		}

		outer: for (int i = 0; i < array.length - target.length + 1; i++) {
			for (int j = 0; j < target.length; j++) {
				if (array[i + j] != target[j]) {
					continue outer;
				}
			}
			return i;
		}
		return -1;
	}

	public void close() {
		close(1000);
	}

	public void close(int sleepTime) {
		// avoid rxtx-deadlock when called from an EventListener
		new Thread(new Runnable() {

			public void run() {
				try {
					Thread.sleep(sleepTime);
					serialPort.closePort();
				} catch (Exception ex) {

				}
			}
		}).start();
		listener.closed();
	}

	/**
	 * Reports the open status of the port.
	 *
	 * @return true if port is open, false if port is closed.
	 */
	public boolean isOpen() {
		return serialPort != null && serialPort.isOpen();
	}

	public boolean send(byte[] bytes) {
		return serialPort.writeBytes(bytes, bytes.length) == bytes.length;
	}

	public boolean send(final String data) {
		try {
			return send(data.getBytes());
		} catch (Exception ex) {
			logger.error("Exception sending data [" + data + "]");
			return false;
		}
	}

	/**
	 * Send a one second break signal.
	 */
	public void sendBreak() {
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

	public static String[] getComPorts() {
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

	public String getName() {
		return name;
	}

	public ComPortListener getListener() {
		return listener;
	}

	public String getMyPort() {
		return myPort;
	}

	@Override
	public int getListeningEvents() {
		return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
	}

	/**
	 * If set to true the configured delimiters (see
	 * {@link Connection#withEndOfChunk(byte[])} and
	 * {@link Connection#withStartOfChunk(byte[])}) the delimiters are not included
	 * in the chunk.
	 *
	 * @param excludeDelimiters
	 * @return
	 */
	public Connection excludeDelimiters(boolean excludeDelimiters) {
		this.excludeDelimiters = excludeDelimiters;
		return this;
	}
}
