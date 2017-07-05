package ch.elexis.data.dto;

import java.util.List;
import java.util.Map;

import ch.elexis.core.data.interfaces.IFall;
import ch.elexis.core.model.IXid;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Patient;
import ch.rgw.tools.TimeTool;

public class FallDTO implements IFall {
	private final String id;
	private final IFall iFall;
	
	public FallDTO(){
		this.id = null;
		this.iFall = null;
	}
	
	public FallDTO(IFall fall){
		this.id = fall.getId();
		iFall = fall;
	}
	

	
	public String getId(){
		return id;
	}
	
	@Override
	public String getAbrechnungsSystem(){
		return iFall.getAbrechnungsSystem();
	}
	
	@Override
	public IXid getXid(){
		return iFall.getXid();
	}
	
	@Override
	public List<IXid> getXids(){
		return iFall.getXids();
	}
	
	@Override
	public long getLastUpdate(){
		return iFall.getLastUpdate();
	}
	
	@Override
	public boolean isValid(){
		return iFall.isValid();
	}
	

	
	@Override
	public int state(){
		return iFall.state();
	}
	
	@Override
	public boolean exists(){
		return iFall.exists();
	}
	
	@Override
	public boolean isAvailable(){
		return iFall.isAvailable();
	}
	
	@Override
	public String getXid(String domain){
		return iFall.getXid(domain);
	}
	
	

	@Override
	public boolean get(String[] fields, String[] values){
		return iFall.get(fields, values);
	}
	
	@Override
	public String get(String field){
		return iFall.get(field);
	}
	
	@Override
	public String getLabel(){
		return iFall.getLabel();
	}
	
	@Override
	public String getInfoString(String kostentraeger){
		return iFall.getInfoString(kostentraeger);
	}
	

	@Override
	public Patient getPatient(){
		return iFall.getPatient();
	}
	

	@Override
	public Konsultation[] getBehandlungen(boolean b){
		return iFall.getBehandlungen(b);
	}
	

	@Override
	public String getBezeichnung(){
		
		return iFall.getBezeichnung();
	}
	
	@Override
	public String getGrund(){
		
		return iFall.getGrund();
	}
	
	@Override
	public String getBeginnDatum(){
		return iFall.getBeginnDatum();
	}
	
	@Override
	public String getEndDatum(){
		return iFall.getEndDatum();
	}
	
	@Override
	public boolean getCopyForPatient(){
		
		return iFall.getCopyForPatient();
	}
	
	@Override
	public Kontakt getGarant(){
		
		return iFall.getGarant();
	}
	
	@Override
	public String getRequirements(){
		return iFall.getRequirements();
	}
	
	@Override
	public String getOptionals(){
		return iFall.getOptionals();
	}
	
	@Override
	public String getUnused(){
		return iFall.getUnused();
	}
	
	@Override
	public Map getMap(String fldExtinfo){
		return iFall.getMap(fldExtinfo);
	}
	
	@Override
	public TimeTool getBillingDate(){
		return iFall.getBillingDate();
	}
	
	@Override
	public String storeToString(){
		return iFall.storeToString();
	}
	
	@Override
	public void setMap(String string, Map<Object, Object> ht){
		
	}
	
	@Override
	public void setGrund(String string){
		
	}
	
	@Override
	public void setBeginnDatum(String string){
		
	}
	
	@Override
	public void setEndDatum(String string){
		
	}
	
	@Override
	public void setBillingDate(TimeTool nDate){
		
	}
	
	@Override
	public void setCopyForPatient(boolean b){
		
	}
	
	@Override
	public void setGarant(Kontakt sel){
		
	}

	@Override
	public boolean set(String field, String value){
		return false;
	}
	
	@Override
	public void setRequiredContact(String kostentraeger, Kontakt k){
		
	}
	
	@Override
	public void setRequiredString(String versicherungsnummer, String vnOld){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void setInfoString(String rechnungsempfaenger, String string){
		
	}
	
	@Override
	public boolean addXid(String domain, String domain_id, boolean updateIfExists){
		return false;
	}
	
	@Override
	public void setAbrechnungsSystem(String item){
		// TODO Auto-generated method stub
		
	}
}

