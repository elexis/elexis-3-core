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

import java.util.HashMap;
import java.util.List;

import org.jdom.Element;

import ch.elexis.core.model.IDiagnose;
import ch.elexis.core.ui.exchange.XChangeExporter;
import ch.elexis.data.Konsultation;
import ch.elexis.data.PersistentObject;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class AnamnesisElement extends XChangeElement {
	public static final String XMLNAME = "anamnesis";
	
	HashMap<IDiagnose, EpisodeElement> hLink = new HashMap<IDiagnose, EpisodeElement>();
	HashMap<Element, IDiagnose> hBacklink;
	HashMap<String, Element> hElements;
	MedicalElement eMed;
	
	public AnamnesisElement(XChangeExporter sender){
		setWriter(sender);
	}
	
	public String getXMLName(){
		return XMLNAME;
	}
	
	public List<EpisodeElement> getEpisodes(){
		List<EpisodeElement> lep =
			(List<EpisodeElement>) getChildren(EpisodeElement.XMLNAME, EpisodeElement.class);
		return lep;
	}
	
	/**
	 * link a record element to this anamnesis (every episode has a number of treatments related to
	 * that episode) We try to find an episode for each of the diagnoses of the Konsultation given
	 * 
	 * @param k
	 * @param r
	 */
	public void link(Konsultation k, RecordElement r){
		List<IDiagnose> kdl = k.getDiagnosen();
		for (IDiagnose dg : kdl) {
			EpisodeElement episode = hLink.get(dg);
			if (episode == null) {
				episode = new EpisodeElement().asExporter(sender, k, dg);
				hLink.put(dg, episode);
				add(episode);
			}
			EpisodeRefElement episodeRef = new EpisodeRefElement().asExporter(sender, episode);
			r.add(episodeRef);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void doImport(RecordElement r, Konsultation k){
		List<EpisodeElement> eRefs =
			(List<EpisodeElement>) r.getChildren(EpisodeElement.XMLNAME, EpisodeElement.class);
		if (eRefs != null) {
			for (EpisodeElement eRef : eRefs) {
				String id = eRef.getAttr(ATTR_ID);
				Element episode = hElements.get(id);
				if (episode != null) {
					IDiagnose dg = hBacklink.get(episode);
					if (dg != null) {
						k.addDiagnose(dg);
					}
				}
			}
		}
	}
	
	public PersistentObject doImport(PersistentObject context){
		return null;
	}
}
