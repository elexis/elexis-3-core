package ch.elexis.data.dto;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import ch.elexis.core.data.interfaces.IFall;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.model.InvoiceState;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Rechnung;
import ch.elexis.data.dto.InvoiceHistoryEntryDTO.OperationType;

public class InvoiceCorrectionDTO {
	private final String id;
	private String invoiceNumber;
	private String newInvoiceNumber;
	private String bemerkung;
	private String receiver;
	private String invoiceStateText;
	private FallDTO fallDTO;
	private String outputText;
	private String betrag;
	
	private List<KonsultationDTO> konsultationDTOs = new ArrayList<>();
	private List<InvoiceHistoryEntryDTO> correctionHistory = new ArrayList<>();
	private List<InvoiceHistoryEntryDTO> cache = new ArrayList<>();
	private List<ElexisException> errors = new ArrayList<>();
	private List<IInvoiceCorrectionChanged> invoiceCorrectionChanges = new ArrayList<>();
	
	private boolean openNewInvoice = false;
	
	public InvoiceCorrectionDTO(){
		this.id = null;
		this.fallDTO = null;
		this.outputText = null;
		this.invoiceNumber = null;
		this.newInvoiceNumber = null;
		this.openNewInvoice = false;
		cache.clear();
		correctionHistory.clear();
		invoiceCorrectionChanges.clear();
		errors.clear();
	}
	
	public InvoiceCorrectionDTO(Rechnung rechnung){
		errors.clear();
		invoiceCorrectionChanges.clear();
		cache.clear();
		correctionHistory.clear();
		this.id = rechnung.getId();
		this.invoiceNumber = rechnung.getNr();
		this.bemerkung = rechnung.getBemerkung();
		Fall fall = rechnung.getFall();
		this.fallDTO = fall.getDTO();
		this.outputText = null;
		this.receiver = fall.getPatient().getLabel();
		this.betrag = rechnung.getBetrag().getAmountAsString();
		this.newInvoiceNumber = rechnung.getExtInfo(Rechnung.INVOICE_CORRECTION);
		this.openNewInvoice = false;
		if (StringUtils.isNotEmpty(rechnung.getNr())) {
			InvoiceState invoiceState = rechnung.getInvoiceState();
			if (invoiceState != null) {
				invoiceStateText = invoiceState.getLocaleText();
			}
		}
		
		for (Konsultation konsultation : rechnung.getKonsultationen())
		{
			KonsultationDTO konsultationDTO = new KonsultationDTO(konsultation);
			this.konsultationDTOs.add(konsultationDTO);
			errors.addAll(konsultationDTO.getErrors());
		}
	}
	
	public List<ElexisException> getErrors(){
		return errors;
	}
	
	public void setNewInvoiceNumber(String newInvoiceNumber){
		this.newInvoiceNumber = newInvoiceNumber;
	}
	
	public String getNewInvoiceNumber(){
		return newInvoiceNumber;
	}
	
	public String getInvoiceNumber(){
		return invoiceNumber;
	}
	
	public List<KonsultationDTO> getKonsultationDTOs(){
		return konsultationDTOs;
	}
	
	public String[] getInvoiceDetails(){
		return new String[] {
			invoiceNumber, invoiceStateText, receiver, betrag
		};
	}
	
	public String getBemerkung(){
		return bemerkung;
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
	
	public boolean hasChanges(){
		return !cache.isEmpty();
	}
	
	public InvoiceHistoryEntryDTO getHistoryEntryForLeistungTransferFromCache(IFall fall){
		if (fall != null && fall.getId() != null) {
			for (InvoiceHistoryEntryDTO invoiceHistoryEntryDTO : cache) {
				if (invoiceHistoryEntryDTO.getOperationType()
					.equals(OperationType.LEISTUNG_TRANSFER_TO_FALL_KONS)) {
					if (invoiceHistoryEntryDTO.getAdditional() instanceof IFall
						&& ((IFall) invoiceHistoryEntryDTO.getAdditional()).getId()
							.equals(fall.getId())) {
						return invoiceHistoryEntryDTO;
					}
				}
			}
		}
		return null;
	}
	
	public void addToCache(InvoiceHistoryEntryDTO historyEntryDTO)
	{
		if (!historyEntryDTO.getOperationType().isMultiAllowed()) {
			cache.remove(historyEntryDTO);
		}
		informChanged();
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
			if (!Boolean.TRUE.equals(historyEntryDTO.isSuccess())) {
				return false;
			}
		}
		return true;
	}
	
	public void register(IInvoiceCorrectionChanged invoiceCorrectionChanged){
		invoiceCorrectionChanges.add(invoiceCorrectionChanged);
	}
	
	private void informChanged(){
		for (IInvoiceCorrectionChanged invoiceCorrectionChanged : invoiceCorrectionChanges) {
			invoiceCorrectionChanged.changed(this);
		}
	}
	
	public interface IInvoiceCorrectionChanged {
		public void changed(InvoiceCorrectionDTO invoiceCorrectionDTO);
	}
	
	public void setOpenNewInvoice(boolean openNewInvoice){
		this.openNewInvoice = openNewInvoice;
	}
	
	public boolean isOpenNewInvoice(){
		return openNewInvoice;
	}
}
