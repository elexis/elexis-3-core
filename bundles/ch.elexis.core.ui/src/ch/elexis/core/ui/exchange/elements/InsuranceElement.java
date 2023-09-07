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
	public static final String XMLNAME = "insurance"; //$NON-NLS-1$
	public static final String ATTR_COMPANYREF = "companyref"; //$NON-NLS-1$
	public static final String ATTR_REASON = "reason"; //$NON-NLS-1$
	public static final String ATTR_DIAGNOSIS = "publicDiagnosis"; //$NON-NLS-1$
	public static final String ATTR_DATEFROM = "dateFrom"; //$NON-NLS-1$
	public static final String ATTR_DATEUNTIL = "dateUntil"; //$NON-NLS-1$

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
		if ("accident".equals(value)) { //$NON-NLS-1$
			return FallConstants.TYPE_ACCIDENT;
		} else if ("birthdefect".equals(value)) { //$NON-NLS-1$
			return FallConstants.TYPE_BIRTHDEFECT;
		} else if ("disease".equals(value)) { //$NON-NLS-1$
			return FallConstants.TYPE_DISEASE;
		} else if ("maternity".equals(value)) { //$NON-NLS-1$
			return FallConstants.TYPE_MATERNITY;
		} else if ("prevention".equals(value)) { //$NON-NLS-1$
			return FallConstants.TYPE_PREVENTION;
		}
		return FallConstants.TYPE_OTHER;
	}

	public String translateReason(String grund) {
		if (grund.equals(FallConstants.TYPE_ACCIDENT)) {
			return "accident"; //$NON-NLS-1$
		} else if (grund.equals(FallConstants.TYPE_BIRTHDEFECT)) {
			return "birthdefect"; //$NON-NLS-1$
		} else if (grund.equals(FallConstants.TYPE_DISEASE)) {
			return "disease"; //$NON-NLS-1$
		} else if (grund.equals(FallConstants.TYPE_MATERNITY)) {
			return "maternity"; //$NON-NLS-1$
		} else if (grund.equals(FallConstants.TYPE_PREVENTION)) {
			return "prevention"; //$NON-NLS-1$
		} else {
			return "other"; //$NON-NLS-1$
		}
	}

	public static class ContractElement extends XChangeElement {
		public static final String XMLNAME = "contract"; //$NON-NLS-1$
		public static final String ATTR_COUNTRY = "country"; //$NON-NLS-1$
		public static final String ATTR_NAME = "name"; //$NON-NLS-1$
		public static final String ATTR_CASEID = "caseID"; //$NON-NLS-1$

		@Override
		public String getXMLName() {
			return XMLNAME;
		}

		public ContractElement asExporter(XChangeExporter p, Fall fall) {
			asExporter(p);
			setAttribute(ATTR_COUNTRY, "CH"); //$NON-NLS-1$
			setAttribute(ATTR_NAME, fall.getAbrechnungsSystem());
			if (StringUtils.isNotEmpty(fall.getRequiredString("Versicherungsnummer"))) { //$NON-NLS-1$
				setAttribute(ATTR_CASEID, fall.getRequiredString("Versicherungsnummer")); //$NON-NLS-1$
			} else if (StringUtils.isNotEmpty(fall.getRequiredString("Unfallnummer"))) { //$NON-NLS-1$
				setAttribute(ATTR_CASEID, fall.getRequiredString("Unfallnummer")); //$NON-NLS-1$
			} else if (StringUtils.isNotEmpty(fall.getInfoString("Fallnummer"))) { //$NON-NLS-1$
				setAttribute(ATTR_CASEID, fall.getRequiredString("Fallnummer")); //$NON-NLS-1$
			}
			return this;
		}
	}

	public ContractElement getContract() {
		return (ContractElement) getChild(ContractElement.XMLNAME, ContractElement.class);
	}
}
