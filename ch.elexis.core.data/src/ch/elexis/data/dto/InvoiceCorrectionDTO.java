package ch.elexis.data.dto;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import ch.elexis.core.data.interfaces.IDiagnose;
import ch.elexis.core.data.interfaces.IVerrechenbar;
import ch.elexis.core.model.InvoiceState;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Mandant;
import ch.elexis.data.Rechnung;
import ch.elexis.data.RnStatus;
import ch.elexis.data.Verrechnet;
import ch.elexis.data.dto.HistoryEntryDTO.OperationType;
import ch.rgw.tools.Money;
import ch.rgw.tools.TimeTool;

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
	
	private List<HistoryEntryDTO> correctionHistory = new ArrayList<>();
	
	List<HistoryEntryDTO> cache = new ArrayList<>();
	
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
	
	public void addToCache(HistoryEntryDTO historyEntryDTO)
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
			correctionHistory.add(new HistoryEntryDTO(OperationType.FALL_COPY, fallDTO, null));
			correctionHistory.add(new HistoryEntryDTO(OperationType.FALL_CHANGE, fallDTO, null));
			correctionHistory.add(
				new HistoryEntryDTO(OperationType.FALL_KONSULTATION_TRANSER, fallDTO, null));
		}
		correctionHistory.addAll(cache);
		
		if (!correctionHistory.isEmpty()) {
			correctionHistory.add(0,
				new HistoryEntryDTO(OperationType.RECHNUNG_STORNO, this, null));
			correctionHistory.add(new HistoryEntryDTO(OperationType.RECHNUNG_NEW, this, null));
		}
	}
	
	public List<HistoryEntryDTO> getHistory(){
		return correctionHistory;
	}
	
	public boolean isCorrectionSuccess(){
		for (HistoryEntryDTO historyEntryDTO : correctionHistory) {
			if (!historyEntryDTO.isSuccess()) {
				return false;
			}
		}
		return true;
	}

	public class KonsultationDTO {
		private final String id;
		private List<LeistungDTO> leistungDTOs = new ArrayList<>();
		private List<DiagnosesDTO> diagnosesDTOs = new ArrayList<>();
		private String date;
		private String srcDate;
		private Mandant mandant;
		
		public KonsultationDTO(Konsultation konsultation){
			this.id = konsultation.getId();
			this.date = konsultation.getDatum();
			this.srcDate = new String(konsultation.getDatum());
			this.mandant = konsultation.getMandant();
			for (Verrechnet verrechnet : konsultation.getLeistungen()) {
				leistungDTOs.add(new LeistungDTO(verrechnet));
			}
			
			for (IDiagnose iDiagnose : konsultation.getDiagnosen()) {
				diagnosesDTOs.add(new DiagnosesDTO(iDiagnose));
			}
		}
		public void setLeistungDTOs(List<LeistungDTO> leistungDTOs){
			this.leistungDTOs = leistungDTOs;
		}
		
		public List<LeistungDTO> getLeistungDTOs(){
			return leistungDTOs;
		}
		
		public void setDiagnosesDTOs(List<DiagnosesDTO> diagnosesDTOs){
			this.diagnosesDTOs = diagnosesDTOs;
		}
		
		public List<DiagnosesDTO> getDiagnosesDTOs(){
			return diagnosesDTOs;
		}
		
		public void setDate(String date){
			this.date = date;
		}
		
		public String getDate(){
			return date;
		}
		
		public String getSrcDate(){
			return srcDate;
		}
		
		public String getId(){
			return id;
		}
		
		public void setMandant(Mandant mandant){
			this.mandant = mandant;
		}
		
		public Mandant getMandant(){
			return mandant;
		}
	}
	
	public class LeistungDTO {
		private final String id;
		private String code;
		private String text;
		private Money bruttoPreis;
		private int count;
		private IVerrechenbar iVerrechenbar;
		
		public LeistungDTO(Verrechnet verrechnet){
			this.id = verrechnet.getId();
			this.code = verrechnet.getCode();
			this.text = verrechnet.getText();
			this.bruttoPreis = verrechnet.getBruttoPreis();
			this.count = verrechnet.getZahl();
		}
		
		public LeistungDTO(IVerrechenbar iVerrechenbar){
			this.id = iVerrechenbar.getId();
			this.code = iVerrechenbar.getCode();
			this.text = iVerrechenbar.getText();
			this.bruttoPreis = iVerrechenbar.getKosten(new TimeTool());//TODO PREIS ???
			this.count = 1;
			this.iVerrechenbar = iVerrechenbar;
		}
		
		public void setCode(String code){
			this.code = code;
		}
		
		public void setText(String text){
			this.text = text;
		}
		
		public String getCode(){
			return code;
		}
		
		public String getText(){
			return text;
		}
		
		public String getId(){
			return id;
		}
		
		public Money getBruttoPreis(){
			return bruttoPreis;
		}
		
		public void setBruttoPreis(Money bruttoPreis){
			this.bruttoPreis = bruttoPreis;
		}
		
		public void setCount(int count){
			this.count = count;
		}
		
		public int getCount(){
			return count;
		}
		
		public void setiVerrechenbar(IVerrechenbar iVerrechenbar){
			this.iVerrechenbar = iVerrechenbar;
		}
		
		public IVerrechenbar getIVerrechenbar(){
			return iVerrechenbar;
		}
	}
	
	public class DiagnosesDTO {
		private final String id;
		private String label;
		public DiagnosesDTO(IDiagnose iDiagnose){
			this.id = iDiagnose.getId();
			this.label = iDiagnose.getLabel();
		}
		
		public String getId(){
			return id;
		}
		
		public void setLabel(String label){
			this.label = label;
		}
		
		public String getLabel(){
			return label;
		}
	}
}
