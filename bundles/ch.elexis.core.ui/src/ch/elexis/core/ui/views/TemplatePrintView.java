/*******************************************************************************
 * Copyright (c) 2006-2009, G. Weirich, Daniel Lutz and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Daniel Lutz - initial implementation based on RnPrintView
 *    
 *******************************************************************************/

package ch.elexis.core.ui.views;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.text.ITextPlugin.ICallback;
import ch.elexis.core.ui.text.TextContainer;
import ch.elexis.data.Brief;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Patient;

public class TemplatePrintView extends ViewPart {
	private static final String KEY_TEXT = "text"; //$NON-NLS-1$
	
	private static final String KEY_BRIEF = "brief"; //$NON-NLS-1$
	
	public static final String ID = "ch.elexis.views.TemplatePrintView"; //$NON-NLS-1$
	
	CTabFolder ctab;
	private int existing;
	
	private TextContainer text;
	
	public TemplatePrintView(){}
	
	@Override
	public void createPartControl(Composite parent){
		ctab = new CTabFolder(parent, SWT.BOTTOM);
		ctab.setLayout(new FillLayout());
		
	}
	
	CTabItem addItem(final String template, final String title, final Kontakt adressat){
		CTabItem ret = new CTabItem(ctab, SWT.NONE);
		text = new TextContainer(getViewSite());
		ret.setControl(text.getPlugin().createContainer(ctab, new ICallback() {
			@Override
			public void save(){}
			
			@Override
			public boolean saveAs(){
				return false;
			}
			
		}));
		Brief actBrief = text.createFromTemplateName(Konsultation.getAktuelleKons(), template,
			Brief.UNKNOWN, adressat, title);
		ret.setData(KEY_BRIEF, actBrief);
		ret.setData(KEY_TEXT, text);
		ret.setText(title);
		return ret;
	}
	
	@Override
	public void setFocus(){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void dispose(){
		clearItems();
		super.dispose();
	}
	
	public void clearItems(){
		for (int i = 0; i < ctab.getItems().length; i++) {
			useItem(i, null, null);
		}
	}
	
	public void useItem(int idx, String template, Kontakt adressat){
		CTabItem item = ctab.getItem(idx);
		if (!item.isDisposed()) {
			Brief brief = (Brief) item.getData(KEY_BRIEF);
			TextContainer text = (TextContainer) item.getData(KEY_TEXT);
			text.saveBrief(brief, Brief.UNKNOWN);
			String betreff = brief.getBetreff();
			brief.delete();
			if (template != null) {
				Brief actBrief = text.createFromTemplateName(Konsultation.getAktuelleKons(),
					template, Brief.UNKNOWN, adressat, betreff);
				item.setData(KEY_BRIEF, actBrief);
			}
		}
	}
	
	/**
	 * Show the generated template in the view
	 * 
	 * @param pat
	 * @param templateName
	 */
	public void doShow(Patient pat, String templateName){
		existing = ctab.getItems().length;
		
		if (--existing < 0) {
			addItem(templateName, templateName, pat);
		} else {
			ctab.getItem(0);
			useItem(0, templateName, pat);
		}
	}
	
	/**
	 * Drukt Dokument anhand einer Vorlage
	 * 
	 * @param pat
	 *            der Patient
	 * @param templateName
	 *            Name der Vorlage
	 * @param printer
	 *            Printer
	 * @param tray
	 *            Tray
	 * @param monitor
	 * @return
	 */
	
	public boolean doPrint(Patient pat, String templateName, String printer, String tray,
		IProgressMonitor monitor){
		monitor.subTask(pat.getLabel());
		
		// TODO ?
		// GlobalEvents.getInstance().fireSelectionEvent(rn,getViewSite());
		
		existing = ctab.getItems().length;
		CTabItem ct;
		TextContainer text;
		
		if (--existing < 0) {
			ct = addItem(templateName, templateName, pat);
		} else {
			ct = ctab.getItem(0);
			useItem(0, templateName, pat);
		}
		
		text = (TextContainer) ct.getData(KEY_TEXT);
		
		text.getPlugin().setFont("Serif", SWT.NORMAL, 9); //$NON-NLS-1$
		if (text.getPlugin().print(printer, tray, false) == false) {
			return false;
		}
		monitor.worked(1);
		return true;
	}
	
	@Optional
	@Inject
	public void setFixLayout(MPart part, @Named(Preferences.USR_FIX_LAYOUT)
	boolean currentState){
		CoreUiUtil.updateFixLayout(part, currentState);
	}
}
