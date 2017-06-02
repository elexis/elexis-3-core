package ch.elexis.data.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.elexis.core.data.interfaces.IDiagnose;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Rechnung;
import ch.elexis.data.RnStatus;
import ch.elexis.data.Verrechnet;
import ch.rgw.tools.Money;
import ch.rgw.tools.TimeTool;

public class InvoiceCorrectionDTO {
	private final String id;
	private String invoiceNumber;
	
	private String receiver;
	private String phoneInsurance;
	private String causeRejected;
	private String advisor;
	private FallDTO fallDTO;
	
	private List<KonsultationDTO> konsultationDTOs = new ArrayList<>();
	
	public InvoiceCorrectionDTO(){
		this.id = null;
		this.fallDTO = new FallDTO();
	}
	
	public InvoiceCorrectionDTO(Rechnung rechnung){
		this.id = rechnung.getId();
		this.invoiceNumber = rechnung.getNr();
		Fall fall = rechnung.getFall();
		this.fallDTO = new FallDTO(fall);

		this.receiver = fall.getPatient().getLabel();
		this.phoneInsurance = "TODO";
		
		if (rechnung.getStatus() == RnStatus.FEHLERHAFT) {
			
			List<String> rejects = rechnung.getTrace(Rechnung.REJECTED);
			StringBuilder rjj = new StringBuilder();
			for (String r : rejects) {
				rjj.append(r).append("\n------\n"); //$NON-NLS-1$
			}
			this.causeRejected = rjj.toString();
		}
		
		this.advisor = rechnung.getMandant().getLabel();
		
		for (Konsultation konsultation : rechnung.getKonsultationen())
		{
			this.konsultationDTOs.add(new KonsultationDTO(konsultation));
		}

	}
	
	public List<KonsultationDTO> getKonsultationDTOs(){
		return konsultationDTOs;
	}
	
	
	public String[] getInvoiceDetails(){
		return new String[] {
			invoiceNumber, receiver, phoneInsurance, advisor, causeRejected
		};
	}
	
	public String getId(){
		return id;
	}
	
	public FallDTO getFallDTO(){
		return fallDTO;
	}
	
	public class FallDTO
	{
		private final String id;
		private String number;
		private String abrechnungsSystem;
		private Date beginnDate;
		private String costReceiver;
		private String receiver;
		
		public FallDTO(){
			this.id = null;
		}
		
		public FallDTO(Fall fall){
			this.id = fall.getId();
			this.number = "TODO"; //fall.getInfoString("Fallnummer");fall.getFallNummer()
			this.abrechnungsSystem = fall.getAbrechnungsSystem();
			this.beginnDate = new TimeTool(fall.getBeginnDatum()).getTime();
			this.costReceiver = fall.getGarant().getLabel(); //TODO ?
			this.receiver = fall.getGarant().istPatient() ? "PATIENT" : "SUVA"; //TODO
		}
		
		public void setAbrechnungsSystem(String abrechnungsSystem){
			this.abrechnungsSystem = abrechnungsSystem;
		}
		
		public void setBeginnDate(Date beginnDate){
			this.beginnDate = beginnDate;
		}
		
		public void setCostReceiver(String costReceiver){
			this.costReceiver = costReceiver;
		}
		
		public void setNumber(String number){
			this.number = number;
		}
		
		public String getAbrechnungsSystem(){
			return abrechnungsSystem;
		}
		
		public Date getBeginnDate(){
			return beginnDate;
		}
		
		public String getCostReceiver(){
			return costReceiver;
		}
		
		public String getId(){
			return id;
		}
		
		public String getNumber(){
			return number;
		}
		
		public void setReceiver(String receiver){
			this.receiver = receiver;
		}
		
		public String getReceiver(){
			return receiver;
		}
	}
	
	public class KonsultationDTO {
		private final String id;
		private List<LeistungDTO> leistungDTOs = new ArrayList<>();
		private List<DiagnosesDTO> diagnosesDTOs = new ArrayList<>();
		private String date;
		private String stateText;
		
		public KonsultationDTO(Konsultation konsultation){
			this.id = konsultation.getId();
			this.date = konsultation.getDatum();
			this.stateText = konsultation.getStatusText();
			
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
		
		public void setStateText(String stateText){
			this.stateText = stateText;
		}
		
		public String getDate(){
			return date;
		}
		
		public String getStateText(){
			return stateText;
		}
		
		public String getId(){
			return id;
		}
	}
	
	public class LeistungDTO {
		private final String id;
		private String code;
		private String text;
		private Money bruttoPreis;
		private int count;
		
		public LeistungDTO(Verrechnet verrechnet){
			this.id = verrechnet.getId();
			this.code = verrechnet.getCode();
			this.text = verrechnet.getText();
			this.bruttoPreis = verrechnet.getBruttoPreis();
			this.count = verrechnet.getZahl();
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
