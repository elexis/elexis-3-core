/*******************************************************************************
 * Copyright (c) 2008-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 
 *******************************************************************************/

package ch.elexis.core.ui.exchange.elements;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.model.FallConstants;
import ch.elexis.core.ui.exchange.XChangeExporter;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Kontakt;
import ch.rgw.tools.TimeTool;

public class InsuranceElement extends XChangeElement {
	public static final String XMLNAME = "insurance";
	public static final String ATTR_COMPANYREF = "companyref";
	public static final String ATTR_REASON = "reason";
	public static final String ATTR_DIAGNOSIS = "publicDiagnosis";
	public static final String ATTR_DATEFROM = "dateFrom";
	public static final String ATTR_DATEUNTIL = "dateUntil";

	@Override
	public String getXMLName() {
		return XMLNAME;
	}

	public InsuranceElement asExporter(XChangeExporter p, Konsultation k) {
		asExporter(p);
		Fall fall = k.getFall();
		Kontakt garant = fall.getGarant();
		setAttribute(ATTR_DATEFROM, new TimeTool(fall.getBeginnDatum()).toString(TimeTool.DATE_ISO));
		if (!fall.isOpen()) {
			setAttribute(ATTR_DATEUNTIL, new TimeTool(fall.getEndDatum()).toString(TimeTool.DATE_ISO));
		}
		setAttribute(ATTR_REASON, translateReason(fall.getGrund()));
		ContactElement eGarant = p.addContact(garant);
		setAttribute(ATTR_COMPANYREF, eGarant.getID());
		ContractElement eContract = new ContractElement();
		add(eContract);
		return this;
	}

	public InsuranceElement asExporter(XChangeExporter parent, Fall fall) {
		Kontakt garant = fall.getGarant();
		setAttribute(ATTR_DATEFROM, new TimeTool(fall.getBeginnDatum()).toString(TimeTool.DATE_ISO));
		if (!fall.isOpen()) {
			setAttribute(ATTR_DATEUNTIL, new TimeTool(fall.getEndDatum()).toString(TimeTool.DATE_ISO));
		}
		setAttribute(ATTR_REASON, translateReason(fall.getGrund()));
		ContactElement eGarant = parent.addContact(garant);
		setAttribute(ATTR_COMPANYREF, eGarant.getID());
		ContractElement eContract = new ContractElement().asExporter(parent, fall);
		add(eContract);
		return this;
	}

	public String getReason() {
		String value = getAttr(ATTR_REASON);
		if ("accident".equals(value)) {
			return FallConstants.TYPE_ACCIDENT;
		} else if ("birthdefect".equals(value)) {
			return FallConstants.TYPE_BIRTHDEFECT;
		} else if ("disease".equals(value)) {
			return FallConstants.TYPE_DISEASE;
		} else if ("maternity".equals(value)) {
			return FallConstants.TYPE_MATERNITY;
		} else if ("prevention".equals(value)) {
			return FallConstants.TYPE_PREVENTION;
		}
		return FallConstants.TYPE_OTHER;
	}

	public String translateReason(String grund) {
		if (grund.equals(FallConstants.TYPE_ACCIDENT)) {
			return "accident";
		} else if (grund.equals(FallConstants.TYPE_BIRTHDEFECT)) {
			return "birthdefect";
		} else if (grund.equals(FallConstants.TYPE_DISEASE)) {
			return "disease";
		} else if (grund.equals(FallConstants.TYPE_MATERNITY)) {
			return "maternity";
		} else if (grund.equals(FallConstants.TYPE_PREVENTION)) {
			return "prevention";
		} else {
			return "other";
		}
	}

	public static class ContractElement extends XChangeElement {
		public static final String XMLNAME = "contract";
		public static final String ATTR_COUNTRY = "country";
		public static final String ATTR_NAME = "name";
		public static final String ATTR_CASEID = "caseID";

		public String getXMLName() {
			return XMLNAME;
		}

		public ContractElement asExporter(XChangeExporter p, Fall fall) {
			asExporter(p);
			setAttribute(ATTR_COUNTRY, "CH");
			setAttribute(ATTR_NAME, fall.getAbrechnungsSystem());
			if (StringUtils.isNotEmpty(fall.getRequiredString("Versicherungsnummer"))) {
				setAttribute(ATTR_CASEID, fall.getRequiredString("Versicherungsnummer"));
			} else if (StringUtils.isNotEmpty(fall.getRequiredString("Unfallnummer"))) {
				setAttribute(ATTR_CASEID, fall.getRequiredString("Unfallnummer"));
			} else if (StringUtils.isNotEmpty(fall.getInfoString("Fallnummer"))) {
				setAttribute(ATTR_CASEID, fall.getRequiredString("Fallnummer"));
			}
			return this;
		}
	}

	public ContractElement getContract() {
		return (ContractElement) getChild(ContractElement.XMLNAME, ContractElement.class);
	}
}
