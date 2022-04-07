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

package ch.elexis.core.ui.views;

import static ch.elexis.core.ui.text.TextTemplateRequirement.TT_AUF_CERT;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.service.LocalLockServiceHolder;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.IActivationListener;
import ch.elexis.core.ui.dialogs.DocumentSelectDialog;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.locks.LockResponseHelper;
import ch.elexis.core.ui.text.EditLocalDocumentUtil;
import ch.elexis.core.ui.text.ITextPlugin.ICallback;
import ch.elexis.core.ui.text.ITextPlugin.Parameter;
import ch.elexis.core.ui.text.TextContainer;
import ch.elexis.data.Brief;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Kontakt;

public class AUFZeugnis extends ViewPart implements ICallback, IActivationListener {
	public static final String ID = "ch.elexis.AUFView"; //$NON-NLS-1$
	TextContainer text;
	Brief actBrief;
	
	public AUFZeugnis(){}
	
	@Override
	public void dispose(){
		if (actBrief != null) {
			LocalLockServiceHolder.get().releaseLock(actBrief);
		}
		GlobalEventDispatcher.removeActivationListener(this, this);
		super.dispose();
	}
	
	@Override
	public void createPartControl(Composite parent){
		setTitleImage(Images.IMG_PRINTER.getImage());
		text = new TextContainer(getViewSite());
		text.getPlugin().createContainer(parent, this);
		GlobalEventDispatcher.addActivationListener(this, this);
	}
	
	@Override
	public void setFocus(){
		text.setFocus();
	}
	
	public void createAUZ(){
		Kontakt adressat = null;
		if (DocumentSelectDialog.getDontAskForAddresseeForThisTemplateName(TT_AUF_CERT)) {
			// trick: just supply a dummy address for creating the doc
			adressat = Kontakt.load("-1"); //$NON-NLS-1$
		}
		actBrief =
			text.createFromTemplateName(Konsultation.getAktuelleKons(), TT_AUF_CERT, Brief.AUZ, //$NON-NLS-1$
				adressat, null);
		updateTextLock();
		// text.getPlugin().setFormat(PageFormat.A5);
		if (text.getPlugin().isDirectOutput()) {
			text.getPlugin().print(null, null, true);
			getSite().getPage().hideView(this);
		} else {
			EditLocalDocumentUtil.startEditLocalDocument(this, actBrief);
		}
	}
	
	private void updateTextLock(){
		// test lock and set read only before opening the Brief
		LockResponse result = LocalLockServiceHolder.get().acquireLock(actBrief);
		if (result.isOk()) {
			text.getPlugin().setParameter(null);
		} else {
			LockResponseHelper.showInfo(result, actBrief, null);
			text.getPlugin().setParameter(Parameter.READ_ONLY);
		}
	}
	
	public TextContainer getTextContainer(){
		return text;
	}
	
	@Override
	public void save(){
		if (actBrief != null) {
			actBrief.save(text.getPlugin().storeToByteArray(), text.getPlugin().getMimeType());
		}
	}
	
	@Override
	public boolean saveAs(){
		return true;
	}
	
	@Override
	public void activation(boolean mode){
		if (mode == false) {
			save();
		}
	}
	
	@Override
	public void visible(boolean mode){}
	
	@Optional
	@Inject
	public void setFixLayout(MPart part, @Named(Preferences.USR_FIX_LAYOUT)
	boolean currentState){
		CoreUiUtil.updateFixLayout(part, currentState);
	}
	
}
