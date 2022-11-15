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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.ui.exchange.XChangeContainer;
import ch.elexis.core.ui.exchange.XChangeExporter;
import ch.elexis.data.Brief;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.LabResult;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Prescription;
import ch.elexis.data.Query;
import ch.rgw.tools.StringTool;

/**
 * THis represents the medical History of a given patient
 *
 * @author gerry
 *
 */

public class MedicalElement extends XChangeElement {
	public static final String XMLNAME = "medical"; //$NON-NLS-1$
	private DocumentsElement eDocuments;
	private RisksElement eRisks;
	private AnamnesisElement elAnamnesis;
	private MedicationsElement eMedications;
	private RecordsElement eRecords;
	private AnalysesElement eAnalyses;
	private EpisodesElement eEpisodes;

	public String getXMLName() {
		return XMLNAME;
	}

	public MedicalElement asExporter(XChangeExporter parent, Patient p) {
		asExporter(parent);
		parent.getContainer().addMapping(this, p);
		Fall[] faelle = p.getFaelle();
		for (Fall fall : faelle) {
			EpisodeElement episode = new EpisodeElement().asExporter(parent, fall);
			addEpisode(episode);

			Konsultation[] kons = fall.getBehandlungen(false);
			for (Konsultation k : kons) {
				RecordElement record = new RecordElement().asExporter(parent, k);
				record.addEpisodeRef(episode);
				addRecord(record);
			}
		}

		Query<LabResult> qbe = new Query<LabResult>(LabResult.class);
		qbe.add(LabResult.PATIENT_ID, Query.EQUALS, p.getId());
		List<LabResult> labs = qbe.execute();
		if (labs != null) {
			for (LabResult lr : labs) {
				ResultElement.addResult(this, lr);
			}
		}

		Query<Brief> qb = new Query<Brief>(Brief.class);
		qb.add(LabResult.PATIENT_ID, Query.EQUALS, p.getId());
		List<Brief> lBriefe = qb.execute();
		if ((lBriefe != null) && (lBriefe.size()) > 0) {
			for (Brief b : lBriefe) {
				addDocument(new DocumentElement().asExporter(sender, b, DocumentElement.VAL_DOCUMENT_TYPE_LETTER));
			}
		}

		List<Prescription> relevantMedis = new ArrayList<Prescription>();
		relevantMedis.addAll(p.getMedication(EntryType.FIXED_MEDICATION));
		relevantMedis.addAll(p.getMedication(EntryType.RESERVE_MEDICATION));
		for (Prescription medi : relevantMedis) {
			add(new MedicationElement().asExporter(parent, medi));
		}
		String risks = p.get(Patient.FLD_RISKS); // $NON-NLS-1$
		if (!StringTool.isNothing(risks)) {
			for (String r : risks.split("[\\n\\r]+")) { //$NON-NLS-1$
				add(new RiskElement().asExporter(sender, r));
			}
		}
		risks = p.get(Patient.FLD_ALLERGIES); // $NON-NLS-1$
		if (!StringTool.isNothing(risks)) {
			for (String r : risks.split("[\\n\\r]+")) { //$NON-NLS-1$
				add(new RiskElement().asExporter(sender, r, "allergy")); //$NON-NLS-1$
			}
		}
		return this;
	}

	public void add(AnamnesisElement ae) {
		elAnamnesis = ae;
		super.add(ae);
	}

	public void add(RiskElement re) {
		if (eRisks == null) {
			eRisks = new RisksElement();
			add(eRisks);
			getContainer().addChoice(eRisks, "Risiken"); //$NON-NLS-1$
		}
		eRisks.add(re);
	}

	public void add(MedicationElement med) {
		if (eMedications == null) {
			eMedications = new MedicationsElement();
			getContainer().addChoice(eMedications, Messages.MedicalElement_Medcaments); // $NON-NLS-1$
			add(eMedications);
		}
		eMedications.add(med);
	}

	/**
	 * Return or create the anamnesis-Element
	 *
	 * @return the newly created or existing anamnesis element
	 */
	public AnamnesisElement getAnamnesis() {
		if (elAnamnesis == null) {
			elAnamnesis = (AnamnesisElement) getChild(AnamnesisElement.XMLNAME, AnamnesisElement.class);
			if (elAnamnesis == null) {
				elAnamnesis = new AnamnesisElement(getSender());
				elAnamnesis.setReader(getReader());

			}
		}
		return elAnamnesis;
	}

	/**
	 * Add a medical record. This will create the records-parent element if
	 * neccessary
	 *
	 * @param rc the RecordElement to add
	 */
	public void addRecord(RecordElement rc) {
		if (eRecords == null) {
			eRecords = (RecordsElement) getChild(XChangeContainer.ENCLOSE_RECORDS, RecordsElement.class);
		}
		if (eRecords == null) {
			eRecords = new RecordsElement();
			eRecords.setReader(getReader());
			eRecords.setWriter(getSender());
			add(eRecords);
			getContainer().addChoice(eRecords, Messages.MedicalElement_EMREntries, eRecords); // $NON-NLS-1$
		}
		eRecords.add(rc);
	}

	public void addEpisode(EpisodeElement episode) {
		if (eEpisodes == null) {
			eEpisodes = (EpisodesElement) getChild(XChangeContainer.ENCLOSE_EPISODES, EpisodesElement.class);
		}
		if (eEpisodes == null) {
			eEpisodes = new EpisodesElement();
			eEpisodes.setReader(getReader());
			eEpisodes.setWriter(getSender());
			add(eEpisodes);
			getContainer().addChoice(eEpisodes, "Faelle", eEpisodes); //$NON-NLS-1$
		}
		eEpisodes.add(episode);
	}

	/**
	 * Add a result
	 *
	 * @param le
	 */
	public void addAnalyse(ResultElement le) {
		if (eAnalyses == null) {
			eAnalyses = (AnalysesElement) getChild(FindingElement.ENCLOSING, AnalysesElement.class);
		}
		if (eAnalyses == null) {
			eAnalyses = new AnalysesElement();
			add(eAnalyses);
			getContainer().addChoice(eAnalyses, Messages.MedicalElement_Findings, eAnalyses); // $NON-NLS-1$
		}
		eAnalyses.add(le);
	}

	public void addFindingItem(FindingElement fe) {
		if (eAnalyses == null) {
			eAnalyses = (AnalysesElement) getChild(FindingElement.ENCLOSING, AnalysesElement.class);
		}
		if (eAnalyses == null) {
			eAnalyses = new AnalysesElement();
			add(eAnalyses);
			getContainer().addChoice(eAnalyses, Messages.MedicalElement_Findings, eAnalyses); // $NON-NLS-1$
		}
		eAnalyses.add(fe);
	}

	@SuppressWarnings("unchecked")
	public void addDocument(DocumentElement de) {
		if (eDocuments == null) {
			getContainer();
			eDocuments = (DocumentsElement) getChild(XChangeContainer.ENCLOSE_DOCUMENTS, DocumentsElement.class);
		}
		if (eDocuments == null) {
			eDocuments = new DocumentsElement();
			add(eDocuments);
			getContainer().addChoice(eDocuments, Messages.AccessControlDefaults_documents, eDocuments); // $NON-NLS-1$
		}
		List<DocumentElement> lEx = (List<DocumentElement>) eDocuments.getChildren(DocumentElement.XMLNAME,
				DocumentElement.class);
		if (!lEx.contains(de)) {
			eDocuments.add(de);
		}

	}

	/*************************
	 * Load methods
	 *******************************************/
	@SuppressWarnings("unchecked")
	public List<RecordElement> getRecords() {
		if (eRecords == null) {
			eRecords = (RecordsElement) getChild(XChangeContainer.ENCLOSE_RECORDS, RecordsElement.class);
		}
		if (eRecords != null) {
			List<RecordElement> records = (List<RecordElement>) eRecords.getChildren(RecordElement.XMLNAME,
					RecordElement.class);
			return records;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public List<EpisodeElement> getEpisodes() {
		if (eEpisodes == null) {
			eEpisodes = (EpisodesElement) getChild(XChangeContainer.ENCLOSE_EPISODES, EpisodesElement.class);
		}
		if (eEpisodes != null) {
			List<EpisodeElement> episodes = (List<EpisodeElement>) eEpisodes.getChildren(EpisodeElement.XMLNAME,
					EpisodeElement.class);
			return episodes;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public List<FindingElement> getAnalyses() {
		if (eAnalyses == null) {
			eAnalyses = (AnalysesElement) getChild(XChangeContainer.ENCLOSE_FINDINGS, AnalysesElement.class);
		}
		if (eAnalyses != null) {
			List<FindingElement> analyses = (List<FindingElement>) eAnalyses.getChildren(FindingElement.XMLNAME,
					FindingElement.class);
			return analyses;
		}
		return new LinkedList<FindingElement>();
	}

	@SuppressWarnings("unchecked")
	public List<DocumentElement> getDocuments() {
		if (eDocuments == null) {
			eDocuments = (DocumentsElement) getChild(XChangeContainer.ENCLOSE_DOCUMENTS, DocumentsElement.class);
		}
		if (eDocuments != null) {
			List<DocumentElement> documents = (List<DocumentElement>) eDocuments.getChildren(DocumentElement.XMLNAME,
					DocumentElement.class);
			return documents;

		}
		return new LinkedList<DocumentElement>();
	}

	public String toString() {
		StringBuilder ret = new StringBuilder();
		List<RecordElement> records = getRecords();
		for (RecordElement record : records) {
			ret.append("\n......\n").append(record.toString()); //$NON-NLS-1$
		}

		return ret.toString();
	}

	/**
	 * Load medical data from xchange-file into patient
	 *
	 * @param context the Patient
	 * @return the patient
	 */
	public PersistentObject doImport(PersistentObject context) {
		Patient pat = Patient.load(context.getId());
		List<RecordElement> records = getRecords();

		return pat;
	}
}
