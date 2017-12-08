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
package ch.elexis.core.ui.text;

import java.io.InputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.core.data.interfaces.text.ReplaceCallback;
import ch.elexis.core.ui.util.IKonsExtension;
import ch.rgw.compress.CompEx;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;

/**
 * A TextPlugin based on an EnhancedTextField
 * 
 * @author gerry
 * 
 */
public class ETFTextPlugin implements ITextPlugin {
	private static final String CHARSET = "UTF-8"; //$NON-NLS-1$
	EnhancedTextField etf;
	ICallback handler;
	boolean bSaveOnFocusLost = false;
	IKonsExtension ike;
	
	public boolean clear(){
		etf.setText(StringTool.leer);
		return true;
	}
	
	public void setSaveOnFocusLost(boolean mode){
		bSaveOnFocusLost = mode;
	}
	
	public Composite createContainer(Composite parent, ICallback h){
		handler = h;
		etf = new EnhancedTextField(parent);
		etf.text.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e){
				if (bSaveOnFocusLost) {
					if (handler != null) {
						handler.save();
					}
				}
			}
			
		});
		ike = new ExternalLink();
		ike.connect(etf);
		etf.setText(StringTool.leer);
		return etf;
	}
	
	public boolean createEmptyDocument(){
		etf.setText(StringTool.leer);
		return true;
	}
	
	public void dispose(){
		etf.dispose();
	}
	
	public boolean findOrReplace(String pattern, ReplaceCallback cb){
		// TODO Auto-generated method stub
		return false;
	}
	
	public PageFormat getFormat(){
		return PageFormat.USER;
	}
	
	public String getMimeType(){
		return "text/xml"; //$NON-NLS-1$
	}
	
	public boolean insertTable(String place, int properties, String[][] contents, int[] columnSizes){
		// TODO Auto-generated method stub
		return false;
	}
	
	public Object insertText(String marke, String text, int adjust){
		int pos = 0;
		if (StringTool.isNothing(marke)) {
			etf.text.setSelection(0);
		} else {
			String tx = etf.text.getText();
			pos = tx.indexOf(marke);
			etf.text.setSelection(pos, pos + marke.length());
		}
		etf.text.insert(text);
		return new Integer(pos + text.length());
	}
	
	public Object insertText(Object pos, String text, int adjust){
		if (!(pos instanceof Integer)) {
			return null;
		}
		Integer px = (Integer) pos;
		etf.text.setSelection(px);
		etf.text.insert(text);
		return new Integer(px + text.length());
	}
	
	public Object insertTextAt(int x, int y, int w, int h, String text, int adjust){
		// TODO Auto-generated method stub
		return null;
	}
	
	public boolean loadFromByteArray(byte[] bs, boolean asTemplate){
		try {
			byte[] exp = CompEx.expand(bs);
			String cnt = StringTool.leer;
			if (exp != null) {
				cnt = new String(exp, CHARSET);
			}
			etf.setText(cnt);
			return true;
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return false;
		}
	}
	
	public byte[] storeToByteArray(){
		try {
			String cnt = etf.getContentsAsXML();
			byte[] exp = cnt.getBytes(CHARSET);
			return CompEx.Compress(exp, CompEx.ZIP);
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return null;
		}
		
	}
	
	public boolean loadFromStream(InputStream is, boolean asTemplate){
		// TODO Auto-generated method stub
		return false;
	}
	
	public boolean print(String toPrinter, String toTray, boolean waitUntilFinished){
		// TODO Auto-generated method stub
		return false;
	}
	
	public void setFocus(){
		etf.setFocus();
	}
	
	public boolean setFont(String name, int style, float size){
		// Font font=new Font(Desk.theDisplay,name,Math.round(size),style);
		return true;
	}
	
	public boolean setStyle(final int style){
		return false;
	}
	
	public void setFormat(PageFormat f){
		// TODO Auto-generated method stub
		
	}
	
	public void showMenu(boolean b){
		// TODO Auto-generated method stub
		
	}
	
	public void showToolbar(boolean b){
		// TODO Auto-generated method stub
		
	}
	
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
		throws CoreException{
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean isDirectOutput(){
		return false;
	}
	
	@Override
	public void setParameter(Parameter parameter){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void initTemplatePrintSettings(String template){
		// TODO Auto-generated method stub
		
	}
	
}
