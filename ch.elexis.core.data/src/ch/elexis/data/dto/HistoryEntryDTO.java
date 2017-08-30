package ch.elexis.data.dto;

import java.util.Date;

import ch.elexis.data.Mandant;
import ch.elexis.data.dto.InvoiceCorrectionDTO.KonsultationDTO;
import ch.elexis.data.dto.InvoiceCorrectionDTO.LeistungDTO;
import ch.rgw.tools.TimeTool;

public class HistoryEntryDTO {
	Object base;
	Object item;
	Object ref;
	OperationType operationType;
	Date timestamp;
	
	/**
	 * 
	 * @param base
	 * @param ref
	 * @param operationType
	 * @param item
	 */
	public HistoryEntryDTO(Object base, Object ref, OperationType operationType, Object item){
		super();
		this.timestamp = new Date();
		this.base = base;
		this.ref = ref;
		this.item = item;
		this.operationType = operationType;
	}
	
	public Object getItem(){
		return this;
	}
	
	public Object getRef(){
		return ref;
	}
	
	public OperationType getOperationType(){
		return operationType;
	}
	
	public Date getTimestamp(){
		return timestamp;
	}

	public enum OperationType {
			ADD, DELETE, UPDATE
	}
	
	public String getText(){
		StringBuilder builder = new StringBuilder();
		if (base instanceof KonsultationDTO) {
			builder.append(new TimeTool(timestamp).toString(TimeTool.TIME_FULL));
			builder.append(": ");
			builder.append("Konsultation ");
			builder.append(((KonsultationDTO) base).getDate());
			
			builder.append(" - ");

			if (ref instanceof LeistungDTO) {
				builder.append("Leistung ");
				builder.append(((LeistungDTO) ref).getCode());
				
				// leistung changes
				if (item instanceof Integer) {
					builder.append(" - ");
					builder.append("Anzahl auf ");
					builder.append(item);
				} else if (item instanceof Double) {
					builder.append(" - ");
					builder.append("Preis auf ");
					builder.append(item);
				}
				
			} else {
				// konsultation changes
				if (item instanceof Date) {
					builder.append("Datum auf ");
					builder.append(new TimeTool((Date) item).toString(TimeTool.DATE_GER));
				} else if (item instanceof Mandant) {
					builder.append("Mandant auf ");
					builder.append(((Mandant) item).getLabel());
				}
			}
			
		}
		else if (base instanceof FallDTO) {
			builder.append("Fall - ");
			builder.append(((FallDTO) base).getLabel());
		}
		if (OperationType.ADD.equals(operationType)) {
			builder.append(" hinzugefügt.");
		}
		else if (OperationType.UPDATE.equals(operationType)) {
			builder.append(" geändert.");
		}
		if (OperationType.DELETE.equals(operationType)) {
			builder.append(" entfernt.");
		}
		return builder.toString();
	}
}
