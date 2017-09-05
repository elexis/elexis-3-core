package ch.elexis.data.dto;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import ch.elexis.core.model.InvoiceState;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Rechnung;
import ch.elexis.data.RnStatus;
import ch.elexis.data.dto.InvoiceHistoryEntryDTO.OperationType;

public class InvoiceCorrectionDTO {
	private final String id;
	private String invoiceNumber;
	private String bemerkung;
	private String receiver;
	private String phoneInsurance;
	private String causeRejected;
	private String advisor;
	private String invoiceStateText;
	private FallDTO fallDTO;
	private String outputText;
	
	private List<KonsultationDTO> konsultationDTOs = new ArrayList<>();
	
	private List<InvoiceHistoryEntryDTO> correctionHistory = new ArrayList<>();
	
	List<InvoiceHistoryEntryDTO> cache = new ArrayList<>();
	
	public InvoiceCorrectionDTO(){
		this.id = null;
		this.fallDTO = null;
		this.outputText = null;
		cache.clear();
		correctionHistory.clear();
	}
	
	public InvoiceCorrectionDTO(Rechnung rechnung){
		cache.clear();
		correctionHistory.clear();
		this.id = rechnung.getId();
		this.invoiceNumber = rechnung.getNr();
		this.bemerkung = rechnung.getBemerkung();
		Fall fall = rechnung.getFall();
		this.fallDTO = fall.getDTO();
		this.outputText = null;
		this.receiver = fall.getPatient().getLabel();
		this.phoneInsurance = "";
		
		if (StringUtils.isNotEmpty(rechnung.getNr())) {
			InvoiceState invoiceState = rechnung.getInvoiceState();
			if (invoiceState != null) {
				invoiceStateText = invoiceState.getLocaleText();
			}
			
			if (rechnung.getStatus() == RnStatus.FEHLERHAFT) {
				
				List<String> rejects = rechnung.getTrace(Rechnung.REJECTED);
				StringBuilder rjj = new StringBuilder();
				for (String r : rejects) {
					rjj.append(r).append("\n------\n"); //$NON-NLS-1$
				}
				this.causeRejected = rjj.toString();
			}
		}
		this.advisor = rechnung.getMandant().getLabel();
		
		for (Konsultation konsultation : rechnung.getKonsultationen())
		{
			this.konsultationDTOs.add(new KonsultationDTO(konsultation));
		}

	}
	
	public String getInvoiceNumber(){
		return invoiceNumber;
	}
	
	public List<KonsultationDTO> getKonsultationDTOs(){
		return konsultationDTOs;
	}
	
	public String[] getInvoiceDetails(){
		return new String[] {
			invoiceNumber, invoiceStateText, receiver, phoneInsurance, advisor,
			causeRejected, bemerkung
		};
	}
	
	public String getId(){
		return id;
	}
	
	public FallDTO getFallDTO(){
		return fallDTO;
	}
	
	public void setOutputText(String outputText){
		this.outputText = outputText;
	}
	
	public String getOutputText(){
		return outputText;
	}
	
	public void addToCache(InvoiceHistoryEntryDTO historyEntryDTO)
	{
		if (!historyEntryDTO.getOperationType().isMultiAllowed()) {
			cache.remove(historyEntryDTO);
		}
		cache.add(historyEntryDTO);
	}
	
	public void updateHistory(){
		outputText = null;
		correctionHistory.clear();
		if (fallDTO != null && fallDTO.isChanged()) {
			correctionHistory.add(new InvoiceHistoryEntryDTO(OperationType.FALL_COPY, fallDTO, null));
			correctionHistory.add(new InvoiceHistoryEntryDTO(OperationType.FALL_CHANGE, fallDTO, null));
			correctionHistory.add(
				new InvoiceHistoryEntryDTO(OperationType.FALL_KONSULTATION_TRANSER, fallDTO, null));
		}
		correctionHistory.addAll(cache);
		
		if (!correctionHistory.isEmpty()) {
			correctionHistory.add(0,
				new InvoiceHistoryEntryDTO(OperationType.RECHNUNG_STORNO, this, null));
			correctionHistory.add(new InvoiceHistoryEntryDTO(OperationType.RECHNUNG_NEW, this, null));
		}
	}
	
	public List<InvoiceHistoryEntryDTO> getHistory(){
		return correctionHistory;
	}
	
	public boolean isCorrectionSuccess(){
		for (InvoiceHistoryEntryDTO historyEntryDTO : correctionHistory) {
			if (!historyEntryDTO.isSuccess()) {
				return false;
			}
		}
		return true;
	}
}
