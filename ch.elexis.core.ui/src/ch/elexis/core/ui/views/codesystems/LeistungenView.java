/*******************************************************************************
 * Copyright (c) 2006-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    M. Descher - extracted Eigenartikel to ch.elexis.eigenartikel
 * 
 *******************************************************************************/

package ch.elexis.core.ui.views.codesystems;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.CodeSelectorHandler;
import ch.elexis.core.ui.actions.GlobalActions;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.IActivationListener;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.views.codesystems.CodeSelectorFactory.cPage;
import ch.elexis.data.Leistungsblock;
import ch.rgw.tools.StringTool;

public class LeistungenView extends ViewPart implements IActivationListener, ISaveablePart2 {
	
	private static final String CAPTION_ERROR = Messages.LeistungenView_error; //$NON-NLS-1$
	public final static String ID = "ch.elexis.LeistungenView"; //$NON-NLS-1$
	public CTabFolder ctab;
	CTabItem selected;
	private String defaultRGB;
	
	public LeistungenView(){
		defaultRGB = UiDesk.createColor(new RGB(255, 255, 255));
	}
	
	@Override
	public void createPartControl(final Composite parent){
		
		parent.setLayout(new GridLayout());
		ctab = new CTabFolder(parent, SWT.BOTTOM);
		ctab.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		ctab.setSimple(false);
		ctab.setMRUVisible(true);
		ctab.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e){
				selected = ctab.getSelection();
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
					page.cv.getConfigurer().getControlFieldProvider().clearValues();
				}
				((cPage) selected.getControl()).refresh();
				setFocus();
			}
			
		});
		
		// menu to select & define color
		Menu tabFolderMenu = new Menu(ctab);
		MenuItem miColor = new MenuItem(tabFolderMenu, SWT.POP_UP);
		miColor.setText(Messages.LeistungenView_defineColor);
		miColor.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				CTabItem item = ctab.getSelection();
				ColorDialog cd = new ColorDialog(UiDesk.getTopShell());
				RGB selected = cd.open();
				if (selected != null) {
					CoreHub.globalCfg.set(Preferences.LEISTUNGSCODES_COLOR + item.getText(),
						UiDesk.createColor(selected));
					setCTabItemColor(ctab.getSelection().getText());
				}
			}
		});
		ctab.setMenu(tabFolderMenu);
		
		BlockSelector cs = new BlockSelector();
		CTabItem ct = new CTabItem(ctab, SWT.NONE);
		ICodeElement ics = (ICodeElement) CoreHub.poFactory.createTemplate(Leistungsblock.class);
		if (ics == null) {
			SWTHelper.alert(CAPTION_ERROR, "ICodeElement=null"); //$NON-NLS-1$
			throw new NullPointerException("ICodeElement; LeistungenView"); //$NON-NLS-1$
		}
		if (StringTool.isNothing(ics.getCodeSystemName())) {
			SWTHelper.alert(CAPTION_ERROR, "CodeSystemname=null"); //$NON-NLS-1$
		}
		ct.setText(ics.getCodeSystemName());
		ct.setData(ics);
		// cPage page=new cPage(ctab, getViewSite(),ics,cs);
		BlockSelector.bsPage bspage = new BlockSelector.bsPage(ctab, cs);
		ct.setControl(bspage);
		
		getSite().registerContextMenu(cs.getMgr(), cs.getCv().getViewerWidget());
		getSite().setSelectionProvider(cs.getCv().getViewerWidget());
		
		CodeSelectorFactory.makeTabs(ctab, getViewSite(),
			ExtensionPointConstantsUi.VERRECHNUNGSCODE); //$NON-NLS-1$
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
		setCTabItemColor(selected.getText());
	}
	
	private void setCTabItemColor(String id){
		String rgbColor = CoreHub.globalCfg.get(Preferences.LEISTUNGSCODES_COLOR + id, defaultRGB);
		Color color = UiDesk.getColorFromRGB(rgbColor);
		ctab.setSelectionBackground(new Color[] {
			UiDesk.getDisplay().getSystemColor(SWT.COLOR_WHITE), color
		}, new int[] {
			100
		}, true);
	}
	
	void swapTabs(int iLeft, int iRight){
		CTabItem ctLeft = ctab.getItem(iLeft);
		CTabItem ctRight = ctab.getItem(iRight);
		String t = ctLeft.getText();
		Control c = ctLeft.getControl();
		ctLeft.setText(ctRight.getText());
		ctLeft.setControl(ctRight.getControl());
		ctRight.setText(t);
		ctRight.setControl(c);
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
	
	public void setSelected(CTabItem ctab){
		selected = ctab;
	}
}
