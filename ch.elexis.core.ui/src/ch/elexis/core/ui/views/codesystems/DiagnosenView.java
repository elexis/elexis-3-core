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

package ch.elexis.core.ui.views.codesystems;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.ui.actions.CodeSelectorHandler;
import ch.elexis.core.ui.actions.GlobalActions;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.IActivationListener;
import ch.elexis.core.ui.constants.ExtensionPointConstants;
import ch.elexis.core.ui.views.codesystems.CodeSelectorFactory.cPage;

public class DiagnosenView extends ViewPart implements IActivationListener, ISaveablePart2 {
	public final static String ID = "ch.elexis.DiagnosenView"; //$NON-NLS-1$
	CTabFolder ctab;
	CTabItem selected;
	
	public DiagnosenView(){}
	
	@Override
	public void createPartControl(Composite parent){
		parent.setLayout(new FillLayout());
		ctab = new CTabFolder(parent, SWT.BOTTOM);
		ctab.setSimple(false);
		ctab.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e){
				selected = ctab.getSelection();
				if (selected != null) {
					cPage page = (cPage) selected.getControl();
					if (page == null) {
						page =
							new cPage(ctab, (ICodeElement) selected.getData(),
								(CodeSelectorFactory) selected.getData("csf"));
						selected.setControl(page);
						// parent.redraw();
					}
					page.cv.getConfigurer().getControlFieldProvider().clearValues();
				}
				((cPage) selected.getControl()).refresh();
				setFocus();
			}
			
		});
		
		CodeSelectorFactory.makeTabs(ctab, getViewSite(), ExtensionPointConstants.DIAGNOSECODE);
		
		GlobalEventDispatcher.addActivationListener(this, this);
	}
	
	public void dispose(){
		GlobalEventDispatcher.removeActivationListener(this, this);
		super.dispose();
	}
	
	@Override
	public void setFocus(){
		if (selected == null) {
			if (ctab.getItems().length > 0) {
				selected = ctab.getSelection();
			}
		}
		if (selected != null) {
			cPage page = (cPage) selected.getControl();
			if (page == null) {
				//SWTHelper.alert(CAPTION_ERROR, "cPage=null"); //$NON-NLS-1$
				page =
					new cPage(ctab, (ICodeElement) selected.getData(),
						(CodeSelectorFactory) selected.getData("csf"));
				selected.setControl(page);
				// parent.redraw();
			}
			page.cv.getConfigurer().getControlFieldProvider().setFocus();
		}
	}
	
	public void activation(boolean mode){
		if (mode == false) {
			if (selected != null) {
				cPage page = (cPage) selected.getControl();
				page.cv.getConfigurer().getControlFieldProvider().clearValues();
			}
			
			// remove any ICodeSelectiorTarget, since it's no more needed
			CodeSelectorHandler.getInstance().removeCodeSelectorTarget();
		} else {
			if (selected != null) {
				cPage page = (cPage) selected.getControl();
				page.refresh();
			}
			
		}
		
	}
	
	public void visible(boolean mode){}
	
	/*
	 * Die folgenden 6 Methoden implementieren das Interface ISaveablePart2 Wir benötigen das
	 * Interface nur, um das Schliessen einer View zu verhindern, wenn die Perspektive fixiert ist.
	 * Gibt es da keine einfachere Methode?
	 */
	public int promptToSaveOnClose(){
		return GlobalActions.fixLayoutAction.isChecked() ? ISaveablePart2.CANCEL
				: ISaveablePart2.NO;
	}
	
	public void doSave(IProgressMonitor monitor){ /* leer */
	}
	
	public void doSaveAs(){ /* leer */
	}
	
	public boolean isDirty(){
		return true;
	}
	
	public boolean isSaveAsAllowed(){
		return false;
	}
	
	public boolean isSaveOnCloseNeeded(){
		return true;
	}
}
