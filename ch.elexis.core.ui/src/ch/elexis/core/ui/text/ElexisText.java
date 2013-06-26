/*******************************************************************************
 * Copyright (c) 2006-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 
 *******************************************************************************/
package ch.elexis.core.ui.text;

import java.util.ArrayList;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

import ch.elexis.core.ui.actions.ScannerEvents;
import ch.elexis.core.ui.util.IScannerListener;

/**
 * An org.eclipse.swt.widgets.Text decorator, implementing IScannerListener
 * 
 * @author gerry
 * 
 */
public class ElexisText implements IScannerListener, DisposeListener {
	final ArrayList<IScannerListener> scannerListener;
	final Text text;
	
	public ElexisText(Text text){
		this.text = text;
		this.scannerListener = new ArrayList<IScannerListener>();
		this.text.addDisposeListener(this);
	}
	
	public Widget getWidget(){
		return this.text;
	}
	
	public void widgetDisposed(DisposeEvent e){
		ScannerEvents.getInstance().removeScannerListener(this);
	}
	
	public ElexisText(Composite parent, int style){
		this(new Text(parent, style));
	}
	
	public void addKeyListener(KeyListener listener){
		text.addKeyListener(listener);
	}
	
	public void addModifyListener(ModifyListener listener){
		text.addModifyListener(listener);
	}
	
	public void addSelectionListener(SelectionListener listener){
		text.addSelectionListener(listener);
	}
	
	public void addVerifyListener(VerifyListener listener){
		text.addVerifyListener(listener);
	}
	
	public void addScannerListener(IScannerListener listener){
		if (scannerListener.size() == 0) {
			ScannerEvents.getInstance().addScannerListener(this);
		}
		scannerListener.add(listener);
	}
	
	public void removeScannerListener(IScannerListener listener){
		scannerListener.remove(listener);
	}
	
	public void scannerInput(Event e){
		if (e.widget == text) {
			fireScannerEvent(e);
		}
	}
	
	public void fireScannerEvent(Event e){
		for (IScannerListener listener : scannerListener) {
			listener.scannerInput(e);
		}
	}
	
	public void append(String string){
		text.append(string);
	}
	
	public void clearSelection(){
		text.clearSelection();
	}
	
	public Point computeSize(int hint, int hint2, boolean changed){
		return text.computeSize(hint, hint2, changed);
	}
	
	public Rectangle computeTrim(int x, int y, int width, int height){
		return text.computeTrim(x, y, width, height);
	}
	
	public void copy(){
		text.copy();
	}
	
	public void cut(){
		text.cut();
	}
	
	public int getBorderWidth(){
		return text.getBorderWidth();
	}
	
	public int getCaretLineNumber(){
		return text.getCaretLineNumber();
	}
	
	public Point getCaretLocation(){
		return text.getCaretLocation();
	}
	
	public int getCaretPosition(){
		return text.getCaretPosition();
	}
	
	public int getCharCount(){
		return text.getCharCount();
	}
	
	public boolean getDoubleClickEnabled(){
		return text.getDoubleClickEnabled();
	}
	
	public char getEchoChar(){
		return text.getEchoChar();
	}
	
	public boolean getEditable(){
		return text.getEditable();
	}
	
	public int getLineCount(){
		return text.getLineCount();
	}
	
	public String getLineDelimiter(){
		return text.getLineDelimiter();
	}
	
	public int getLineHeight(){
		return text.getLineHeight();
	}
	
	public String getMessage(){
		return text.getMessage();
	}
	
	public int getOrientation(){
		return text.getOrientation();
	}
	
	public Point getSelection(){
		return text.getSelection();
	}
	
	public int getSelectionCount(){
		return text.getSelectionCount();
	}
	
	public String getSelectionText(){
		return text.getSelectionText();
	}
	
	public int getTabs(){
		return text.getTabs();
	}
	
	public String getText(){
		return text.getText();
	}
	
	public String getText(int start, int end){
		return text.getText(start, end);
	}
	
	public int getTextLimit(){
		return text.getTextLimit();
	}
	
	public int getTopIndex(){
		return text.getTopIndex();
	}
	
	public int getTopPixel(){
		return text.getTopPixel();
	}
	
	public void insert(String string){
		text.insert(string);
	}
	
	public void paste(){
		text.paste();
	}
	
	public void removeModifyListener(ModifyListener listener){
		text.removeModifyListener(listener);
	}
	
	public void removeSelectionListener(SelectionListener listener){
		text.removeSelectionListener(listener);
	}
	
	public void removeVerifyListener(VerifyListener listener){
		text.removeVerifyListener(listener);
	}
	
	public void removeKeyListener(KeyListener listener){
		text.removeKeyListener(listener);
	}
	
	public void selectAll(){
		text.selectAll();
	}
	
	public void setDoubleClickEnabled(boolean doubleClick){
		text.setDoubleClickEnabled(doubleClick);
	}
	
	public void setEchoChar(char echo){
		text.setEchoChar(echo);
	}
	
	public void setEditable(boolean editable){
		text.setEditable(editable);
	}
	
	public void setFont(Font font){
		text.setFont(font);
	}
	
	public void setMessage(String message){
		text.setMessage(message);
	}
	
	public void setOrientation(int orientation){
		text.setOrientation(orientation);
	}
	
	public void setRedraw(boolean redraw){
		text.setRedraw(redraw);
	}
	
	public void setSelection(int start, int end){
		text.setSelection(start, end);
	}
	
	public void setSelection(int start){
		text.setSelection(start);
	}
	
	public void setSelection(Point selection){
		text.setSelection(selection);
	}
	
	public void setTabs(int tabs){
		text.setTabs(tabs);
	}
	
	public void setText(String string){
		text.setText(string);
	}
	
	public void setTextLimit(int limit){
		text.setTextLimit(limit);
	}
	
	public void setTopIndex(int index){
		text.setTopIndex(index);
	}
	
	public void showSelection(){
		text.showSelection();
	}
	
	public boolean setFocus(){
		return text.setFocus();
	}
	
	public void setToolTipText(String string){
		text.setToolTipText(string);
	}
	
	public void setLayoutData(Object layoutData){
		text.setLayoutData(layoutData);
	}
}
