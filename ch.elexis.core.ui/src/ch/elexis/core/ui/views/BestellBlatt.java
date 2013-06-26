/*******************************************************************************
 * Copyright (c) 2006-2009, G. Weirich and Elexis
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

import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.data.Bestellung.Item;
import ch.elexis.core.data.Brief;
import ch.elexis.core.data.Kontakt;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.icons.Images;
import ch.elexis.core.ui.text.ITextPlugin;
import ch.elexis.core.ui.text.TextContainer;
import ch.elexis.core.ui.text.ITextPlugin.ICallback;
import ch.elexis.core.ui.util.SWTHelper;
import ch.rgw.tools.Money;
import ch.rgw.tools.StringTool;

public class BestellBlatt extends ViewPart implements ICallback {
	public final static String ID = "ch.elexis.BestellBlatt"; //$NON-NLS-1$
	TextContainer text;
	Brief actBest;
	private final static String TEMPLATENAME = Messages.getString("BestellBlatt.TemplateName"); //$NON-NLS-1$
	private static final String ERRMSG_CAPTION = Messages
		.getString("BestellBlatt.CouldNotCreateOrder"); //$NON-NLS-1$
	private static final String ERRMSG_BODY = Messages
		.getString("BestellBlatt.CouldNotCreateOrderBody"); //$NON-NLS-1$
	
	@Override
	public void createPartControl(final Composite parent){
		setTitleImage(Images.IMG_PRINTER.getImage());
		text = new TextContainer(getViewSite());
		text.getPlugin().createContainer(parent, this);
	}
	
	public void createOrder(final Kontakt adressat, final List<Item> items){
		String[][] tbl = new String[items.size() + 2][];
		int i = 1;
		Money sum = new Money();
		tbl[0] =
			new String[] {
				Messages.getString("BestellBlatt.Number"), Messages.getString("BestellBlatt.Pharmacode"), Messages.getString("BestellBlatt.Name"), Messages.getString("BestellBlatt.UnitPrice"), Messages.getString("BestellBlatt.LinePrice") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			};
		// DecimalFormat df=new DecimalFormat("\u00a4\u00a4  #.00");
		for (Item it : items) {
			String[] row = new String[5];
			row[0] = Integer.toString(it.num);
			row[1] = it.art.getPharmaCode();
			row[2] = it.art.getName();
			row[3] = it.art.getEKPreis().getAmountAsString(); // Integer.toString(it.art.getEKPreis());
			// int amount=it.num*it.art.getEKPreis();
			Money amount = it.art.getEKPreis().multiply(it.num);
			row[4] = amount.getAmountAsString();
			sum.addMoney(amount);
			tbl[i++] = row;
		}
		tbl[i] =
			new String[] {
				Messages.getString("BestellBlatt.Sum"), StringTool.leer, StringTool.leer, StringTool.leer, sum.getAmountAsString() //$NON-NLS-1$
			};
		actBest = text.createFromTemplateName(null, TEMPLATENAME, Brief.BESTELLUNG, adressat, null);
		if (actBest == null) {
			SWTHelper.showError(ERRMSG_CAPTION, ERRMSG_BODY + "'" + TEMPLATENAME + "'"); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			actBest.setPatient(CoreHub.actUser);
			text.getPlugin().insertTable("[" + TEMPLATENAME + "]", //$NON-NLS-1$ //$NON-NLS-2$
				ITextPlugin.FIRST_ROW_IS_HEADER | ITextPlugin.GRID_VISIBLE, tbl, null);
			if (text.getPlugin().isDirectOutput()) {
				text.getPlugin().print(null, null, true);
				getSite().getPage().hideView(this);
			}
		}
	}
	
	@Override
	public void setFocus(){
		// TODO Automatisch erstellter Methoden-Stub
		
	}
	
	public void save(){
		if (actBest != null) {
			actBest.save(text.getPlugin().storeToByteArray(), text.getPlugin().getMimeType());
		}
	}
	
	public boolean saveAs(){
		// TODO Automatisch erstellter Methoden-Stub
		return false;
	}
}
