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
package ch.elexis.core.ui.util;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.program.Program;

import ch.elexis.core.ui.text.EnhancedTextField;
import ch.elexis.core.ui.text.IRichTextDisplay;
import ch.rgw.tools.GenericRange;
import ch.rgw.tools.StringTool;

public class WikipediaSearchAction extends Action implements IKonsExtension, IHandler {
	
	public static final String ID = "ch.elexis.util.WikipediaSearchAction"; //$NON-NLS-1$
	private static IRichTextDisplay textField;
	private static WikipediaSearchAction instance;
	
	public String connect(IRichTextDisplay tf){
		WikipediaSearchAction.textField = (EnhancedTextField) tf;
		return "ch.elexis.util.WikipediaSearchAction"; //$NON-NLS-1$
	}
	
	public WikipediaSearchAction(){
		super(Messages.getString("WikipediaSearchAction.DisplayName")); //$NON-NLS-1$
	}
	
	public boolean doLayout(StyleRange n, String provider, String id){
		return false;
	}
	
	public boolean doXRef(String refProvider, String refID){
		return false;
	}
	
	@Override
	public void run(){
		String search = ""; //$NON-NLS-1$
		if (textField != null) {
			String text = textField.getContentsPlaintext();
			GenericRange gr = textField.getSelectedRange();
			if (gr.getLength() == 0) {
				search = StringTool.getWordAtIndex(text, gr.getPos());
			} else {
				search = text.substring(gr.getPos(), gr.getPos() + gr.getLength());
			}
			search = search.trim().replace("\r\n", " "); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		String url = "http://de.wikipedia.org/wiki/" + search; //$NON-NLS-1$
		Program.launch(url);
	}
	
	public IAction[] getActions(){
		return new IAction[] {
			this
		};
	}
	
	public void insert(Object o, int pos){
		// TODO Auto-generated method stub
		
	}
	
	public void removeXRef(String refProvider, String refID){
		// TODO Auto-generated method stub
		
	}
	
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
		throws CoreException{
		// TODO Auto-generated method stub
		
	}
	
	public void addHandlerListener(IHandlerListener handlerListener){
		// TODO Auto-generated method stub
		
	}
	
	public void dispose(){
		// TODO Auto-generated method stub
		
	}
	
	public Object execute(ExecutionEvent event) throws ExecutionException{
		if (instance != null)
			instance.run();
		return null;
	}
	
	public void removeHandlerListener(IHandlerListener handlerListener){
		// TODO Auto-generated method stub
	}
}
