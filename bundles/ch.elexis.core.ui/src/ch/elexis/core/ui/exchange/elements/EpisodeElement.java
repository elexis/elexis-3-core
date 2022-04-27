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

package ch.elexis.core.ui.exchange.elements;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.jdom.Element;

import ch.elexis.core.data.interfaces.IDiagnose;
import ch.elexis.core.ui.exchange.XChangeExporter;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Kontakt;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.XMLTool;

public class EpisodeElement extends XChangeElement {
	public static final String XMLNAME = "episode";
	public static final String ATTR_BEGINDATE = "beginDate";
	public static final String ATTR_ENDDATE = "endDate";
	public static final String ATTR_TITLE = "name";
	public static final String ELEMENT_DIAGNOSIS = "diagnosis";
	public static final String ATTR_CODESYSTEM = "codesystem";
	public static final String ATTR_CODE = "code";

	public String getXMLName() {
		return XMLNAME;
	}

	public EpisodeElement asExporter(XChangeExporter parent, Konsultation k, IDiagnose dg) {
		asExporter(parent);
		setAttribute(ATTR_BEGINDATE, new TimeTool(k.getDatum()).toString(TimeTool.DATE_ISO));
		setAttribute(ATTR_ID, XMLTool.idToXMLID(StringTool.unique("episode")));
		DiagnosisElement eDiag = new DiagnosisElement().asExporter(parent, dg);
		add(eDiag);
		setAttribute(ATTR_TITLE, dg.getLabel());
		InsuranceElement eInsurance = new InsuranceElement().asExporter(parent, k);
		add(eInsurance);
		return this;
	}

	public EpisodeElement asExporter(XChangeExporter parent, Fall fall) {
		asExporter(parent);
		XidElement eXid = new XidElement().asExporter(parent, fall);
		add(eXid);
		setAttribute(ATTR_BEGINDATE, new TimeTool(fall.getBeginnDatum()).toString(TimeTool.DATE_ISO));
		if (StringUtils.isNotBlank(fall.getEndDatum())) {
			setAttribute(ATTR_ENDDATE, new TimeTool(fall.getEndDatum()).toString(TimeTool.DATE_ISO));

		}
		setAttribute(ATTR_ID, fall.getId());
		Konsultation[] behandlungen = fall.getBehandlungen(false);
		if (behandlungen != null && behandlungen.length > 0) {
			Optional<IDiagnose> firstDiagnose = Arrays.asList(behandlungen).stream()
					.flatMap(k -> k.getDiagnosen().stream()).findFirst();
			if (firstDiagnose.isPresent()) {
				DiagnosisElement eDiag = new DiagnosisElement().asExporter(parent, firstDiagnose.get());
				add(eDiag);
			}
		}

		setAttribute(ATTR_TITLE, fall.getBezeichnung());
		InsuranceElement eInsurance = new InsuranceElement().asExporter(parent, fall);
		add(eInsurance);

		Kontakt costBearer = fall.getCostBearer();
		if (costBearer != null) {
			parent.addContact(costBearer);
			addMeta("costbearer", costBearer.getId());
		}
		if (StringUtils.isNotEmpty((String) fall.getExtInfoStoredObjectByKey("VEKANr"))) {
			addMeta("vekanr", (String) fall.getExtInfoStoredObjectByKey("VEKANr"));
		}
		if (StringUtils.isNotEmpty((String) fall.getExtInfoStoredObjectByKey("Unfalldatum"))) {
			TimeTool timeTool = new TimeTool((String) fall.getExtInfoStoredObjectByKey("Unfalldatum"));
			addMeta("accidentdate", timeTool.toString(TimeTool.DATE_ISO));
		}
		return this;
	}

	// public EpisodeElement(XChangeContainer parent, )
	public String getBeginDate() {
		return getAttr(ATTR_BEGINDATE);
	}

	public String getEndDate() {
		return getAttr(ATTR_ENDDATE);
	}

	public String getTitle() {
		return getAttr(ATTR_TITLE);
	}

	public String getText() {
		Element text = getElement().getChild("text", getContainer().getNamespace());
		if (text != null) {
			return text.getText();
		}
		return StringUtils.EMPTY;
	}

	public String getDiagnosis() {
		DiagnosisElement dia = (DiagnosisElement) getChild(ELEMENT_DIAGNOSIS, DiagnosisElement.class);
		if (dia != null) {
			DiagnosisElement de = new DiagnosisElement();
			String ret = de.getCode() + " (" + de.getCodeSystem() + ")";
			return ret;
		}
		return StringUtils.EMPTY;
	}

	public void addMeta(String name, String value) {
		MetaElement meta = new MetaElement().asExporter(sender, name, value);
		add(meta);
	}

	public MetaElement getMeta(String name) {
		@SuppressWarnings("unchecked")
		List<MetaElement> meta = (List<MetaElement>) getChildren(MetaElement.XMLNAME, MetaElement.class);
		if (meta != null && !meta.isEmpty()) {
			for (MetaElement metaElement : meta) {
				if (name.equals(metaElement.getAttr(MetaElement.ATTR_NAME))) {
					return metaElement;
				}
			}
		}
		return null;
	}

	static class DiagnosisElement extends XChangeElement {
		public String getXMLName() {
			return ELEMENT_DIAGNOSIS;
		}

		public DiagnosisElement asExporter(XChangeExporter parent, IDiagnose dg) {
			asExporter(parent);
			setAttribute(ATTR_CODESYSTEM, dg.getCodeSystemName());
			setAttribute(ATTR_CODE, dg.getCode());
			return this;
		}

		public String getCodeSystem() {
			return getAttr(ATTR_CODESYSTEM);
		}

		public String getCode() {
			return getAttr(ATTR_CODE);
		}
	}
}
