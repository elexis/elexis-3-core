package ch.elexis.data.dto;

import java.util.Date;

import ch.rgw.tools.TimeTool;

public class InvoiceHistoryEntryDTO {
	Object base;
	Object item;
	OperationType operationType;
	Date timestamp;
	Boolean success;
	
	/**
	 * 
	 * @param base
	 * @param ref
	 * @param operationType
	 * @param item
	 */
	public InvoiceHistoryEntryDTO(OperationType operationType, Object base, Object item){
		super();
		this.timestamp = new Date();
		this.base = base;
		this.item = item;
		this.operationType = operationType;
		this.success = null;
	}
	
	public void setSuccess(Boolean success){
		this.success = success;
	}
	
	public Boolean isSuccess(){
		return success;
	}
	
	public Object getItem(){
		return item;
	}

	public OperationType getOperationType(){
		return operationType;
	}
	
	public Date getTimestamp(){
		return timestamp;
	}
	
	public Object getBase(){
		return base;
	}

	public enum OperationType {
			LEISTUNG_ADD(true), LEISTUNG_REMOVE(true), LEISTUNG_CHANGE_COUNT, LEISTUNG_CHANGE_PRICE,
			LEISTUNG_TRANSFER_TO_NEW_FALL_KONS(true), DIAGNOSE_ADD(true), DIAGNOSE_REMOVE(true),
			KONSULTATION_CHANGE_DATE, KONSULTATION_CHANGE_MANDANT, FALL_COPY, FALL_CHANGE,
			FALL_KONSULTATION_TRANSER, RECHNUNG_STORNO,
			RECHNUNG_NEW;
		
		final boolean multiAllowed;
		
		private OperationType(){
			this.multiAllowed = false;
		}
		
		private OperationType(boolean multiAllowed){
			this.multiAllowed = multiAllowed;
		}
		
		public boolean isMultiAllowed(){
			return multiAllowed;
		}
	}
	
	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + ((base == null) ? 0 : base.hashCode());
		result = prime * result + ((operationType == null) ? 0 : operationType.hashCode());
		result = prime * result + ((item == null) ? 0 : item.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj){
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		InvoiceHistoryEntryDTO other = (InvoiceHistoryEntryDTO) obj;
		if (base == null) {
			if (other.base != null)
				return false;
		} else if (!base.equals(other.base))
			return false;
		if (operationType != other.operationType)
			return false;
		if (item == null) {
			if (other.item != null)
				return false;
		} else if (!item.equals(other.item))
			return false;
		return true;
	}
	
	public String getText(){
		StringBuilder builder = new StringBuilder();
		
		if (base instanceof KonsultationDTO) {
			builder.append(new TimeTool(timestamp).toString(TimeTool.TIME_FULL));
			builder.append(": ");
			builder.append("Konsultation ");
			builder.append(((KonsultationDTO) base).getSrcDate());
			builder.append(" - ");
		} else if (base instanceof FallDTO) {
			builder.append("Fall - ");
			builder.append(((FallDTO) base).getLabel());
		}
		
		switch (operationType) {
		case RECHNUNG_STORNO:
			builder.append("Rechnung ");
			builder.append(((InvoiceCorrectionDTO) base).getInvoiceNumber());
			builder.append(" - ");
			builder.append("stornieren.");
			break;
		case RECHNUNG_NEW:
			builder.append("Neue Rechnung erstellen.");
			break;
		case FALL_COPY:
			builder.append(" kopieren.");
			break;
		case FALL_CHANGE:
			builder.append(" Änderungen übernehmen.");
			break;
		case FALL_KONSULTATION_TRANSER:
			builder.append(" freigegebene und offene Konsultationen transferieren.");
			break;
		case KONSULTATION_CHANGE_DATE:
			builder.append("Datum auf ");
			builder.append(((KonsultationDTO) base).getDate());
			builder.append(" verändern.");
			break;
		case KONSULTATION_CHANGE_MANDANT:
			builder.append("Mandant auf ");
			builder.append(((KonsultationDTO) base).getMandant().getLabel());
			builder.append(" verändern.");
			break;
		case LEISTUNG_ADD:
			builder.append("Leistung ");
			builder.append(((LeistungDTO) item).getText());
			builder.append(" hinzufügen.");
			break;
		case LEISTUNG_CHANGE_COUNT:
			builder.append("Leistung ");
			builder.append(((LeistungDTO) item).getText());
			builder.append(" - ");
			builder.append("Anzahl auf ");
			builder.append(((LeistungDTO) item).getCount());
			builder.append(" verändern.");
			break;
		case LEISTUNG_CHANGE_PRICE:
			builder.append("Leistung ");
			builder.append(((LeistungDTO) item).getText());
			builder.append(" - ");
			builder.append("Preis auf ");
			builder.append(((LeistungDTO) item).getPrice().getAmountAsString());
			builder.append(" verändern.");
			break;
		case LEISTUNG_REMOVE:
			builder.append("Leistung ");
			builder.append(((LeistungDTO) item).getText());
			builder.append(" entfernen.");
			break;
		case LEISTUNG_TRANSFER_TO_NEW_FALL_KONS:
			builder.append("Leistung ");
			builder.append(((LeistungDTO) item).getText());
			builder.append(" auf einen neuen Fall/Konsultation transferieren.");
			break;
		case DIAGNOSE_ADD:
			builder.append("Diagnose ");
			builder.append(((DiagnosesDTO) item).getLabel());
			builder.append(" hinzufügen.");
			break;
		case DIAGNOSE_REMOVE:
			builder.append("Diagnose ");
			builder.append(((DiagnosesDTO) item).getLabel());
			builder.append(" entfernen.");
			break;
		default:
			break;
		}
		return builder.toString();
	}
}
