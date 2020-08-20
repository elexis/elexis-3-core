package ch.elexis.data.dto;

import java.util.ArrayList;
import java.util.List;

import ch.elexis.core.data.util.NoPoUtil;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.model.IDiagnosisReference;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Mandant;
import ch.elexis.data.Verrechnet;
import ch.rgw.tools.TimeTool;

public class KonsultationDTO {
	private final String id;
	private List<LeistungDTO> leistungDTOs = new ArrayList<>();
	private List<DiagnosesDTO> diagnosesDTOs = new ArrayList<>();
	private String date;
	private String srcDate;
	private Mandant mandant;
	
	private List<ElexisException> errors = new ArrayList<>();
	
	public KonsultationDTO(Konsultation konsultation){
		this.errors.clear();
		this.id = konsultation.getId();
		this.date = konsultation.getDatum();
		this.srcDate = new String(konsultation.getDatum());
		this.mandant = konsultation.getMandant();
		for (Verrechnet verrechnet : konsultation.getLeistungen()) {
			try {
				leistungDTOs.add(new LeistungDTO(verrechnet));
			} catch (ElexisException e) {
				errors.add(e);
			}
		}
		
		for (IDiagnosisReference iDiagnose : NoPoUtil
			.loadAsIdentifiable(konsultation, IEncounter.class).get()
			.getDiagnoses()) {
			diagnosesDTOs.add(new DiagnosesDTO(iDiagnose));
		}
	}
	
	public IEncounter getTransientCopy(){
		IEncounter copy = CoreModelServiceHolder.get().create(IEncounter.class);
		copy.setDate(new TimeTool(date).toLocalDate());
		copy.setMandator(NoPoUtil.loadAsIdentifiable(mandant, IMandator.class).get());
		
		return copy;
	}
	
	public List<ElexisException> getErrors(){
		return errors;
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