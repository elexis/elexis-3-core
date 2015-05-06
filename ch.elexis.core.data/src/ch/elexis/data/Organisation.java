/*******************************************************************************
 * Copyright (c) 2005-2010, G. Weirich and Elexis
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

import ch.elexis.core.constants.StringConstants;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.StringTool;

/**
 * Eine Organisation ist eine Kontakt, die ein Kollektiv darstellt. Also eine Firma, eine
 * Versicherung, ein Labor etc.
 * 
 * @author gerry
 * 
 */
public class Organisation extends Kontakt {
	private static final String FLD_NAME = "Name"; //$NON-NLS-1$
	private static final String FLD_ZUSATZ1 = "Zusatz1"; //$NON-NLS-1$
	private static final String FLD_ZUSATZ2 = "Zusatz2"; //$NON-NLS-1$
	private static final String FLD_ZUSATZ3 = "Zusatz3"; //$NON-NLS-1$
	private static final String FLD_CONTACT_PERSON = "Ansprechperson";//$NON-NLS-1$
	private static final String FLD_TEL_DIRECT = "Tel. direkt";
	
	public static final String FLD_XML_NAME = "XML Versicherer Name";
	public static final String FLD_LAW_CODE = "Versicherungsart";
	public static final String FLD_MEDIPORT_SUPPORT = "Mediport Teilnehmer";
	
	public static final String[] DEFAULT_SORT = {
		FLD_NAME, FLD_ZUSATZ1
	};
	
	//@formatter:off
	static {
		addMapping(
			Kontakt.TABLENAME, 
			FLD_NAME +	"="+	Kontakt.FLD_NAME1,
			FLD_ZUSATZ1 + 					"=Bezeichnung2", //$NON-NLS-1$
			FLD_ZUSATZ2 +					"=ExtInfo", //$NON-NLS-1$
			FLD_CONTACT_PERSON +			"=Bezeichnung3",
			FLD_ZUSATZ3 +					"=TITEL", //$NON-NLS-1$
			FLD_TEL_DIRECT + 				"=NatelNr", //$NON-NLS-1$
			FLD_XML_NAME +					"=Allergien",
			FLD_LAW_CODE +					"=TitelSuffix",
			FLD_MEDIPORT_SUPPORT+			"=Gruppe",
			Kontakt.FLD_IS_ORGANIZATION
		);
	}
	//@formatter:on
	
	@Override
	public boolean isValid(){
		return super.isValid();
	}
	
	@Override
	protected String getTableName(){
		return Kontakt.TABLENAME;
	}
	
	Organisation(){/* leer */}
	
	protected Organisation(final String id){
		super(id);
	}
	
	/** Eine Organisation bei gegebener ID aus der Datenbank einlesen */
	public static Organisation load(final String id){
		return new Organisation(id);
	}
	
	/** Eine neue Organisation erstellen */
	public Organisation(final String Name, final String Zusatz1){
		create(null);
		set(new String[] {
			FLD_NAME, FLD_ZUSATZ1
		}, new String[] {
			Name, Zusatz1
		});
	}
	
	@Override
	protected String getConstraint(){
		return new StringBuilder(Kontakt.FLD_IS_ORGANIZATION).append(StringTool.equals)
			.append(JdbcLink.wrap(StringConstants.ONE)).toString();
	}
	
	@Override
	protected void setConstraint(){
		set(Kontakt.FLD_IS_ORGANIZATION, StringConstants.ONE);
	}
	
	public String getXMLName(){
		return get(FLD_XML_NAME);
	}
	
	public String getLawCode(){
		return checkNull(get(FLD_LAW_CODE));
	}
	
	public void setSupportsMediport(boolean mediportSupport){
		if (mediportSupport) {
			set(FLD_MEDIPORT_SUPPORT, StringConstants.ONE);
		} else {
			set(FLD_MEDIPORT_SUPPORT, StringConstants.ZERO);
		}
	}
	
	public boolean supportsMediport(){
		int mediportParticipant = checkZero(get(FLD_MEDIPORT_SUPPORT));
		if (mediportParticipant == 0) {
			return false;
		}
		return true;
	}
	
	public String getInsuranceEAN(){
		return checkNull(getXid(Xid.DOMAIN_EAN));
	}
	
	public void setInsurerEAN(String ean){
		addXid(Xid.DOMAIN_EAN, ean, true);
	}
	
	public String getRecepientEAN(){
		return checkNull(getXid(Xid.DOMAIN_RECIPIENT_EAN));
	}
	
	public void setRecepientEAN(String ean){
		addXid(Xid.DOMAIN_RECIPIENT_EAN, ean, true);
	}
}
