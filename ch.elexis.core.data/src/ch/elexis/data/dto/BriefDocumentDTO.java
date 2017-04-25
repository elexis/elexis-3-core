package ch.elexis.data.dto;

import java.util.Date;

import org.apache.commons.lang.StringUtils;

import ch.elexis.core.types.DocumentStatus;
import ch.elexis.data.Brief;
import ch.elexis.data.Kontakt;
import ch.rgw.tools.TimeTool;

public class BriefDocumentDTO extends AbstractDocumentDTO {
	
	public BriefDocumentDTO(){
	}
	
	public BriefDocumentDTO(Brief brief, String storeId){
		String[] fetch = new String[]
		{
			Brief.FLD_PATIENT_ID, Brief.FLD_SENDER_ID, Brief.FLD_NOTE, Brief.FLD_SUBJECT,
			Brief.FLD_MIME_TYPE, Brief.FLD_DESTINATION_ID, Brief.FLD_TYPE
		};
		String[] data = new String[fetch.length];
		brief.get(fetch, data);
		
		setId(brief.getId());
		setLabel(brief.getLabel());
		
		setPatientId(data[0]);
		setAuthorId(data[1]);
		setDescription(data[2]);
		setTitle(data[3]);
		setMimeType(data[4]);
		setLastchanged(new Date(Long.valueOf(brief.get(Brief.FLD_LASTUPDATE))));
		setCreated(new TimeTool(brief.get(Brief.FLD_DATE)).getTime());
		
		if (StringUtils.isNotEmpty(data[5])) {
			setStatus(DocumentStatus.SENT);
			Kontakt kontakt = Kontakt.load(data[5]);
			if (kontakt != null) {
				getHistory().add(new HistoryDocumentDTO(getLastchanged(),
					DocumentStatus.SENT, kontakt.getLabel()));
			}
		}
		if (StringUtils.isNotEmpty(data[6])) {
			setCategory(new CategoryDocumentDTO(data[6]));
		}
		setStoreId(storeId);
	}
}
