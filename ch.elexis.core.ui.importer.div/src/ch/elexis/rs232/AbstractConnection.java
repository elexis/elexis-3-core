/*******************************************************************************
 * Copyright (c) 2007-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 
 *******************************************************************************/
package ch.elexis.rs232;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.UnsupportedCommOperationException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.TooManyListenersException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.ui.UiDesk;
import ch.rgw.io.FileTool;
import ch.rgw.tools.ExHandler;

public abstract class AbstractConnection implements PortEventListener {
	
	private static final String simulate = null; // "c:/abx.txt";
	
	protected final StringBuilder sbFrame = new StringBuilder();
	protected final StringBuilder sbLine = new StringBuilder();
	protected int frameStart, frameEnd, overhang, checksumBytes;
	protected final ComPortListener listener;
	protected long endTime;
	protected int timeToWait;
	private int timeout;
	private boolean adjustEndTime;
	private boolean closed = false;
	
	private static byte lineSeparator;
	
	private CommPortIdentifier portId;
	private SerialPort sPort;
	private boolean bOpen;
	protected OutputStream os;
	private InputStream is;
	private final String myPort;
	private final String[] mySettings;
	private final String name;
	
	private int state;
	
	private Thread watchdogThread;
	
	public interface ComPortListener {
		
		public void gotData(AbstractConnection conn, byte[] bytes);
		
		public void gotBreak(AbstractConnection conn);
		
		public void timeout();
		
		public void cancelled();
		
		public void closed();
	}
	
	public AbstractConnection(final String portName, final String port, final String settings,
		final ComPortListener l){
		listener = l;
		myPort = port;
		mySettings = settings.split(","); //$NON-NLS-1$
		name = portName;
	}
	
	public String connect(){
		SerialParameters sp = new SerialParameters();
		sp.setPortName(myPort);
		sp.setBaudRate(mySettings[0]);
		sp.setDatabits(mySettings[1]);
		sp.setParity(mySettings[2]);
		sp.setStopbits(mySettings[3]);
		if (mySettings.length >= 5 && mySettings[4] != null)
			sp.setFlowControlIn(mySettings[4]);
		if (mySettings.length >= 6 && mySettings[5] != null)
			sp.setFlowControlOut(mySettings[5]);
		try {
			if (simulate != null) {
				final AbstractConnection mine = this;
				new Thread(new Runnable() {
					
					public void run(){
						try {
							Thread.sleep(1000);
							final String in =
								FileTool.readTextFile(new File(simulate))
									.replaceAll("\\r\\n", "\r"); //$NON-NLS-1$ //$NON-NLS-2$
							listener.gotData(mine, in.getBytes());
						} catch (Exception ex) {
							
						}
						
					}
				}).start();
			} else {
				sbLine.setLength(0);
				openConnection(sp);
			}
			return null;
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return ex.getMessage();
		}
		
	}
	
	/**
	 * Attempts to open a serial connection and streams using the parameters in the SerialParameters
	 * object. If it is unsuccesfull at any step it returns the port to a closed state, throws a
	 * <code>SerialConnectionException</code>, and returns.
	 * 
	 * Gives a timeout of 30 seconds on the portOpen to allow other applications to reliquish the
	 * port if have it open and no longer need it.
	 */
	private void openConnection(final SerialParameters parameters) throws SerialConnectionException{
		
		// Obtain a CommPortIdentifier object for the port you want to open.
		try {
			portId = CommPortIdentifier.getPortIdentifier(parameters.getPortName());
		} catch (NoSuchPortException e) {
			String msg = e.getMessage();
			if (msg == null || msg.length() == 0) {
				msg = e.getClass().getSimpleName();
			}
			throw new SerialConnectionException(msg);
		}
		
		// Open the port represented by the CommPortIdentifier object. Give
		// the open call a timeout of 1 seconds
		try {
			sPort = (SerialPort) portId.open(name, 1000);
		} catch (PortInUseException e) {
			throw new SerialConnectionException(Messages.AbstractConnection_ComPortInUse);
		}
		
		// Set the parameters of the connection. If they won't set, close the
		// port before throwing an exception.
		try {
			setConnectionParameters(parameters);
		} catch (SerialConnectionException e) {
			sPort.close();
			throw e;
		}
		
		// Open the input and output streams for the connection. If they won't
		// open, close the port before throwing an exception.
		try {
			os = sPort.getOutputStream();
			is = sPort.getInputStream();
		} catch (IOException e) {
			sPort.close();
			throw new SerialConnectionException("Error opening i/o streams"); //$NON-NLS-1$
		}
		
		// Add this object as an event listener for the serial port.
		try {
			sPort.addEventListener(this);
		} catch (TooManyListenersException e) {
			sPort.close();
			throw new SerialConnectionException("too many listeners added"); //$NON-NLS-1$
		}
		
		// Set notifyOnDataAvailable to true to allow event driven input.
		sPort.notifyOnDataAvailable(true);
		
		// Set notifyOnBreakInterrup to allow event driven break handling.
		sPort.notifyOnBreakInterrupt(true);
		
		// Set receive timeout to allow breaking out of polling loop during
		// input handling.
		try {
			sPort.enableReceiveTimeout(30);
		} catch (UnsupportedCommOperationException e) {
			throw new SerialConnectionException("Unsupported Com operation"); //$NON-NLS-1$
		}
		bOpen = true;
	}
	
	/**
	 * Sets the connection parameters to the setting in the parameters object. If set fails return
	 * the parameters object to origional settings and throw exception.
	 */
	public void setConnectionParameters(final SerialParameters parameters)
		throws SerialConnectionException{
		
		// Save state of parameters before trying a set.
		int oldBaudRate = sPort.getBaudRate();
		int oldDatabits = sPort.getDataBits();
		int oldStopbits = sPort.getStopBits();
		int oldParity = sPort.getParity();
		
		// Set connection parameters, if set fails return parameters object
		// to original state.
		try {
			sPort.setSerialPortParams(parameters.getBaudRate(), parameters.getDatabits(),
				parameters.getStopbits(), parameters.getParity());
		} catch (UnsupportedCommOperationException e) {
			parameters.setBaudRate(oldBaudRate);
			parameters.setDatabits(oldDatabits);
			parameters.setStopbits(oldStopbits);
			parameters.setParity(oldParity);
			throw new SerialConnectionException("Unsupported parameter"); //$NON-NLS-1$
		}
		
		// Set flow control.
		try {
			sPort
				.setFlowControlMode(parameters.getFlowControlIn() | parameters.getFlowControlOut());
		} catch (UnsupportedCommOperationException e) {
			throw new SerialConnectionException("Unsupported flow control"); //$NON-NLS-1$
		}
	}
	
	/**
	 * Wait for a frame of the device to be sent. Ignores all input until a start byte is found.
	 * collects all bytes from that point until an end byte was received or the timeout happened.
	 * 
	 * @param start
	 *            character defining the start of a frame
	 * @param end
	 *            character singalling end of frame
	 * @param following
	 *            number of bytes after end to wait for (e.g. checksum)
	 * @param timeout
	 *            number of seconds to wait for a frame to complete before givng up
	 */
	public synchronized void awaitFrame(final Shell shell, final String text, final int start,
		final int end, final int following, final int timeout, final boolean background,
		final boolean adjustEndTime){
		frameStart = start;
		frameEnd = end;
		overhang = following;
		this.timeout = timeout;
		this.adjustEndTime = adjustEndTime;
		endTime = System.currentTimeMillis() + (timeout * 1000);
		if (background) {
			watchdogThread = new Thread(new BackgroundWatchdog());
		} else {
			watchdogThread = new Thread(new MonitoredWatchdog(shell, text));
		}
		timeToWait = timeout;
		checksumBytes = overhang;
		watchdogThread.start();
	}
	
	/**
	 * Handles SerialPortEvents. The two types of SerialPortEvents that this program is registered
	 * to listen for are DATA_AVAILABLE and BI. During DATA_AVAILABLE the port buffer is read until
	 * it is drained, when no more data is available and 30ms has passed the method returns. When a
	 * BI event occurs the words BREAK RECEIVED are written to the messageAreaIn.
	 */
	
	public void serialEvent(final SerialPortEvent e){
		if (adjustEndTime) {
			endTime = System.currentTimeMillis() + (timeout * 1000);
		}
		if (e.getEventType() == SerialPortEvent.BI) {
			breakInterrupt(state);
		} else {
			try {
				serialEvent(this.state, is, e);
			} catch (Exception ex) {
				ExHandler.handle(ex);
			}
		}
	}
	
	public abstract void serialEvent(final int state, final InputStream inputStream,
		final SerialPortEvent e) throws IOException;
	
	public void breakInterrupt(final int state){
		this.watchdogThread.interrupt();
		listener.gotBreak(this);
	}
	
	public void close(){
		closed = true;
		if ((watchdogThread != null) && watchdogThread.isAlive()) {
			watchdogThread.interrupt();
		}
		// avoid rxtx-deadlock when called from an EventListener
		new Thread(new Runnable() {
			
			public void run(){
				try {
					Thread.sleep(500);
					sPort.close();
					bOpen = false;
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
		return bOpen;
	}
	
	/**
	 * Send a one second break signal.
	 */
	public void sendBreak(){
		if (sPort != null) {
			sPort.sendBreak(1000);
		} else {
			ExHandler.handle(new Throwable("sPort is null"));
		}
	}
	
	public boolean send(final String data){
		return send(data.getBytes());
	}
	
	public boolean send(final byte[] bytes){
		try {
			os.write(bytes);
			os.flush();
			return true;
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return false;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static String[] getComPorts(){
		Enumeration<CommPortIdentifier> ports = CommPortIdentifier.getPortIdentifiers();
		ArrayList<String> p = new ArrayList<String>();
		while (ports.hasMoreElements()) {
			CommPortIdentifier port = ports.nextElement();
			p.add(port.getName());
		}
		return p.toArray(new String[0]);
	}
	
	class BackgroundWatchdog implements Runnable {
		
		public void run(){
			while (System.currentTimeMillis() < endTime && !closed) {
				try {
					Thread.sleep(1000); // 1s.
				} catch (InterruptedException ex) {
					return;
				}
			}
			if (closed) {
				listener.closed();
			} else {
				listener.timeout();
			}
		}
	}
	
	class MonitoredWatchdog implements Runnable {
		final Shell shell;
		final String text;
		
		public MonitoredWatchdog(Shell shell, String text){
			super();
			this.shell = shell;
			this.text = text;
		}
		
		public void run(){
			final IRunnableWithProgress runnableWithProgress = new IRunnableWithProgress() {
				private int count = 0;
				
				public void run(IProgressMonitor monitor) throws InvocationTargetException,
					InterruptedException{
					monitor.setTaskName(Messages.AbstractConnection_PleaseWait);
					while (!monitor.isCanceled() && System.currentTimeMillis() < endTime && !closed) {
						if (count == 160) {
							monitor.beginTask(text, 100);
							count = 0;
						}
						
						if (monitor.isCanceled()) {
							monitor.done();
							return;
						}
						
						monitor.worked(1);
						count++;
						
						Thread.sleep(10); // 0.001s.
					}
					if (monitor.isCanceled()) {
						listener.cancelled();
					} else if (closed) {
						listener.closed();
					} else {
						listener.timeout();
					}
					monitor.done();
				}
			};
			
			Thread monitorDialogThread = new Thread() {
				public void run(){
					ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
					try {
						dialog.run(true, true, runnableWithProgress);
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			};
			
			UiDesk.getDisplay().asyncExec(monitorDialogThread);
		}
	}
	
	protected void interruptWatchdog(){
		this.watchdogThread.interrupt();
	}
	
	public byte getLineSeparator(){
		return lineSeparator;
	}
	
	public void setState(int state){
		this.state = state;
	}
	
	public int getState(){
		return this.state;
	}
}
