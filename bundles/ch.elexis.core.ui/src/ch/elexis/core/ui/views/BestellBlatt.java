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

import static ch.elexis.core.ui.text.TextTemplateRequirement.TT_ORDER;

import java.text.ParseException;
import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.text.ITextPlugin;
import ch.elexis.core.ui.text.ITextPlugin.ICallback;
import ch.elexis.core.ui.text.TextContainer;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Brief;
import ch.elexis.data.Kontakt;
import ch.rgw.tools.Money;
import ch.rgw.tools.StringTool;

public class BestellBlatt extends ViewPart implements ICallback {
	public final static String ID = "ch.elexis.BestellBlatt"; //$NON-NLS-1$
	TextContainer text;
	Brief actBest;
	private static final String ERRMSG_CAPTION = Messages.BestellBlatt_CouldNotCreateOrder; //$NON-NLS-1$
	private static final String ERRMSG_BODY = Messages.BestellBlatt_CouldNotCreateOrderBody; //$NON-NLS-1$
	
	@Override
	public void createPartControl(final Composite parent){
		setTitleImage(Images.IMG_PRINTER.getImage());
		text = new TextContainer(getViewSite());
		text.getPlugin().createContainer(parent, this);
	}
	
	public void createOrder(final IContact receiver, final List<IOrderEntry> toOrder){
		String[][] tbl = new String[toOrder.size() + 2][];
		int i = 1;
		Money sum = new Money();
		tbl[0] = new String[] {
			Messages.BestellBlatt_Number, Messages.BestellBlatt_Pharmacode,
			Messages.BestellBlatt_Name, Messages.BestellBlatt_UnitPrice,
			Messages.BestellBlatt_LinePrice
		};
		for (IOrderEntry orderEntry : toOrder) {
			String[] row = new String[5];
			row[0] = Integer.toString(orderEntry.getAmount());
			row[1] = orderEntry.getArticle().getCode();
			row[2] = orderEntry.getArticle().getName();
			Money purchasePrice;
			try {
				purchasePrice = new Money(orderEntry.getArticle().getPurchasePrice());
			} catch (ParseException e) {
				purchasePrice = new Money();
			}
			row[3] = purchasePrice.getAmountAsString();
			Money amount = purchasePrice.multiply(orderEntry.getAmount());
			row[4] = amount.getAmountAsString();
			sum.addMoney(amount);
			tbl[i++] = row;
		}
		tbl[i] = new String[] {
			Messages.BestellBlatt_Sum, StringTool.leer, StringTool.leer, StringTool.leer,
			sum.getAmountAsString()
				//$NON-NLS-1$
		};
		actBest = text.createFromTemplateName(null, TT_ORDER, Brief.BESTELLUNG,
			Kontakt.load(receiver.getId()), null);
		if (actBest == null) {
			SWTHelper.showError(ERRMSG_CAPTION, ERRMSG_BODY + "'" + TT_ORDER + "'"); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			actBest.setPatient(CoreHub.actUser);
			text.getPlugin().insertTable("[" + TT_ORDER + "]", //$NON-NLS-1$ //$NON-NLS-2$
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
