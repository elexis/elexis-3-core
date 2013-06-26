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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.data.AUF;
import ch.elexis.core.data.Brief;
import ch.elexis.core.data.Konsultation;
import ch.elexis.core.icons.Images;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.GlobalEventDispatcher.IActivationListener;
import ch.elexis.core.ui.text.TextContainer;
import ch.elexis.core.ui.text.ITextPlugin.ICallback;

public class AUFZeugnis extends ViewPart implements ICallback, IActivationListener {
	public static final String ID = "ch.elexis.AUFView"; //$NON-NLS-1$
	TextContainer text;
	Brief actBrief;
	
	public AUFZeugnis(){}
	
	@Override
	public void dispose(){
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
	
	public void createAUZ(final AUF auf){
		actBrief =
			text.createFromTemplateName(Konsultation.getAktuelleKons(), "AUF-Zeugnis", Brief.AUZ, //$NON-NLS-1$
				null, null);
		// text.getPlugin().setFormat(PageFormat.A5);
		if (text.getPlugin().isDirectOutput()) {
			text.getPlugin().print(null, null, true);
			getSite().getPage().hideView(this);
		}
	}
	
	public TextContainer getTextContainer(){
		return text;
	}
	
	public void save(){
		if (actBrief != null) {
			actBrief.save(text.getPlugin().storeToByteArray(), text.getPlugin().getMimeType());
		}
	}
	
	public boolean saveAs(){
		return true;
	}
	
	public void activation(boolean mode){
		if (mode == false) {
			save();
		}
	}
	
	public void visible(boolean mode){}
	
}
