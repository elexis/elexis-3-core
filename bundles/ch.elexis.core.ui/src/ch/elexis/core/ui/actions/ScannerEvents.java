/*******************************************************************************
 * Copyright (c) 2007-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     G. Weirich - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.ui.actions;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PlatformUI;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.util.IScannerListener;

import ch.rgw.tools.StringTool;
public class ScannerEvents implements Listener {
	private static int BUF_LIMIT = 500;
	private static int BUF_MINIMUM = 30;
	private static ScannerEvents input;
	
	private int prefixCode = 0;
	private int postfixCode = 0;
	private int barcodeLength = 13;
	
	private final ArrayList<IScannerListener> listenerList;
	private StringBuffer inputBuffer = new StringBuffer();
	
	private boolean prefixOn = false;
	
	ScannerEvents(){
		listenerList = new ArrayList<IScannerListener>();
		reloadCodes();
	}
	
	public static ScannerEvents getInstance(){
		if (input == null) {
			input = new ScannerEvents();
		}
		return input;
	}
	
	/**
	 * Codes werden neu geladen
	 */
	public void reloadCodes(){
		prefixCode = CoreHub.globalCfg.get(Preferences.SCANNER_PREFIX_CODE, 0);
		postfixCode = CoreHub.globalCfg.get(Preferences.SCANNER_POSTFIX_CODE, 0);
		barcodeLength = CoreHub.globalCfg.get(Preferences.BARCODE_LENGTH, 13);
	}
	
	public static void addListenerToDisplay(Display display){
		display.addFilter(SWT.KeyDown, getInstance());
	}
	
	/**
	 * Barcode aus Buffer extrahieren. Ist sehr heuristische Methode. Es werden die letzten Zeichen
	 * aus dem Buffer gelesen. Alle CR's entfernt und dann die letzten, z.b. 13 bei EAN13,
	 * retourniert.
	 * 
	 * @param strBuf
	 * @return
	 */
	private String getBarcode(StringBuffer strBuf){
		String barcode = strBuf.toString();
		barcode = barcode.replaceAll(new Character(SWT.CR).toString(), StringTool.leer); //$NON-NLS-1$
		barcode = barcode.replaceAll(new Character(SWT.LF).toString(), StringTool.leer); //$NON-NLS-1$
		barcode = barcode.replaceAll(new Character((char) 0).toString(), StringTool.leer); //$NON-NLS-1$
		if (barcode.length() > barcodeLength) {
			return barcode.substring(barcode.length() - barcodeLength);
		}
		return barcode;
	}
	
	/**
	 * Verarbeitet jedes Key-Event und entscheidet danach ob es sich um ein Scanner-Event handelt
	 * oder nicht.
	 */
	public void handleEvent(Event event){
		if (listenerList.size() > 0) {
			if (event.keyCode == prefixCode) {
				prefixOn = true;
				inputBuffer = new StringBuffer();
			} else if (event.keyCode == postfixCode) {
				prefixOn = false;
				fireScannerInput(event.widget, getBarcode(inputBuffer));
				inputBuffer = new StringBuffer();
			} else if (prefixOn && event.character == SWT.CR) {
				prefixOn = false;
				fireScannerInput(event.widget, getBarcode(inputBuffer));
				inputBuffer = new StringBuffer();
			}
			if (inputBuffer.length() > BUF_LIMIT) {
				inputBuffer = inputBuffer.delete(0, inputBuffer.length() - BUF_MINIMUM);
			}
			inputBuffer.append(event.character);
		}
	}
	
	public void addScannerListener(IScannerListener listener){
		listenerList.add(listener);
	}
	
	public void removeScannerListener(IScannerListener listener){
		listenerList.remove(listener);
	}
	
	public void fireScannerInput(Widget widget, String input){
		Event e = new Event();
		e.widget = widget;
		e.text = input;
		for (IScannerListener listener : listenerList) {
			listener.scannerInput(e);
		}
	}
	
	public static void beep(){
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getDisplay().beep();
	}
}
