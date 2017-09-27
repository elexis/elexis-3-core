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

package ch.elexis.data;

import java.util.Comparator;
import java.util.List;

import ch.elexis.core.data.interfaces.IOptifier;
import ch.elexis.core.data.interfaces.IVerrechenbar;
import ch.elexis.core.data.util.MultiplikatorList;
import ch.elexis.data.VerrechenbarFavorites.Favorite;
import ch.rgw.tools.IFilter;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.JdbcLink.Stm;
import ch.rgw.tools.Money;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public abstract class VerrechenbarAdapter extends PersistentObject implements IVerrechenbar {
	
	@Override
	public String getLabel(){
		return getText();
	}
	
	@Override
	protected abstract String getTableName();
	
	public String getCode(){
		return null;
	}
	
	public String getCodeSystemName(){
		return null;
	}
	
	public String getText(){
		return null;
	}
	
	public IOptifier getOptifier(){
		return optifier;
	}
	
	public Comparator<IVerrechenbar> getComparator(){
		return comparator;
	}
	
	public IFilter getFilter(final Mandant m){
		return ifilter;
	}
	
	public void setVKMultiplikator(final TimeTool von, TimeTool bis, final double factor,
		final String typ){
		StringBuilder sql = new StringBuilder();
		String eoue = new TimeTool(TimeTool.END_OF_UNIX_EPOCH).toString(TimeTool.DATE_COMPACT);
		if (bis == null) {
			bis = new TimeTool(TimeTool.END_OF_UNIX_EPOCH);
		}
		String from = von.toString(TimeTool.DATE_COMPACT);
		Stm stm = getConnection().getStatement();
		sql.append("UPDATE VK_PREISE SET DATUM_BIS=").append(JdbcLink.wrap(from))
			.append(" WHERE (DATUM_BIS=").append(JdbcLink.wrap(eoue))
			.append(" OR DATUM_BIS='99991231') AND TYP=").append(JdbcLink.wrap(typ));
		stm.exec(sql.toString());
		sql.setLength(0);
		sql.append("INSERT INTO VK_PREISE (ID,DATUM_VON,DATUM_BIS,MULTIPLIKATOR,TYP) VALUES (")
			.append(JdbcLink.wrap(StringTool.unique("rtsu"))).append(",")
			.append(JdbcLink.wrap(von.toString(TimeTool.DATE_COMPACT))).append(",")
			.append(JdbcLink.wrap(bis.toString(TimeTool.DATE_COMPACT))).append(",")
			.append(JdbcLink.wrap(Double.toString(factor))).append(",").append(JdbcLink.wrap(typ))
			.append(");");
		stm.exec(sql.toString());
		getConnection().releaseStatement(stm);
	}
	
	public double getVKMultiplikator(final TimeTool date, final String typ){
		return getMultiplikator(date, "VK_PREISE", typ);
	}
	
	public double getVKMultiplikator(final TimeTool date, final Fall fall){
		return getMultiplikator(date, "VK_PREISE", fall.getAbrechnungsSystem());
	}
	
	public double getEKMultiplikator(final TimeTool date, final Fall fall){
		return getMultiplikator(date, "EK_PREISE", fall.getAbrechnungsSystem());
	}
	
	private double getMultiplikator(final TimeTool date, final String table, final String typ){
		MultiplikatorList multis = new MultiplikatorList(table, typ);
		return multis.getMultiplikator(date);
	}
	
	public Money getKosten(final TimeTool dat){
		return new Money(0);
	}
	
	public int getMinutes(){
		return 0;
	}
	
	protected VerrechenbarAdapter(final String id){
		super(id);
	}
	
	public String getCodeSystemCode(){
		return "999";
	}
	
	protected VerrechenbarAdapter(){}
	
	@Override
	public VatInfo getVatInfo(){
		return VatInfo.VAT_DEFAULT;
	}
	
}
