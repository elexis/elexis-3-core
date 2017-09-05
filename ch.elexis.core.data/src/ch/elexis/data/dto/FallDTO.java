package ch.elexis.data.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import ch.elexis.core.data.interfaces.IFall;
import ch.elexis.core.model.IXid;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.rgw.tools.TimeTool;

public class FallDTO implements IFall {
	private final IFall iFall; // source
	
	private String abrechnungsSystem;
	private String grund;
	private String beginnDatum;
	private String endDatum;
	private TimeTool billingDate;
	private Kontakt garant;
	private Map extInfo = new HashMap<>();
	private boolean copyForPatient;
	
	private boolean changed;
	
	private List<IFallChanged> fallChanges = new ArrayList<>();
	
	public void register(IFallChanged fallChanged){
		fallChanges.add(fallChanged);
	}
	
	public FallDTO(IFall fall){
		iFall = fall;
		abrechnungsSystem = iFall.getAbrechnungsSystem();
		grund = iFall.getGrund();
		beginnDatum = iFall.getBeginnDatum();
		endDatum = iFall.getEndDatum();
		billingDate = iFall.getBillingDate();
		garant = iFall.getGarant();
		copyForPatient = iFall.getCopyForPatient();
		
		extInfo = iFall.getMap(PersistentObject.FLD_EXTINFO);
		
		changed = false;
	}
	
	/// editable fields
	@Override
	public void setAbrechnungsSystem(String abrechnungsSystem){
		if (!StringUtils.equals(this.abrechnungsSystem, abrechnungsSystem)) {
			this.abrechnungsSystem = abrechnungsSystem;
			changed = true;
			informChanged();
		}
	}
	
	@Override
	public String getAbrechnungsSystem(){
		return abrechnungsSystem;
	}
	
	@Override
	public String getGrund(){
		return grund;
	}
	
	@Override
	public void setGrund(String grund){
		if (!StringUtils.equals(this.grund, grund)) {
			changed = true;
		}
		this.grund = grund;
	}
	
	@Override
	public String getBeginnDatum(){
		return beginnDatum;
	}
	
	@Override
	public void setBeginnDatum(String beginnDatum){
		if (!StringUtils.equals(this.beginnDatum, beginnDatum)) {
			changed = true;
		}
		this.beginnDatum = beginnDatum;
	}
	
	@Override
	public String getEndDatum(){
		return endDatum;
	}
	
	@Override
	public void setEndDatum(String endDatum){
		if (!StringUtils.equals(this.endDatum, endDatum)) {
			changed = true;
		}
		this.endDatum = endDatum;
	}
	
	@Override
	public TimeTool getBillingDate(){
		return billingDate;
	}
	
	@Override
	public void setBillingDate(TimeTool billingDate){
		this.billingDate = billingDate;
		changed = true;
	}
	
	@Override
	public void setGarant(Kontakt garant){
		this.garant = garant;
		changed = true;
	}
	
	@Override
	public Kontakt getGarant(){
		return garant;
	}
	
	@Override
	public void setMap(String string, Map<Object, Object> ht){
		if (PersistentObject.FLD_EXTINFO.equals(string)) {
			extInfo = ht;
			changed = true;
		}
	}
	
	@Override
	public Map getMap(String fldExtinfo){
		return extInfo;
	}
	
	@Override
	public String getInfoString(String key){
		Object value = extInfo.get(key);
		if (value instanceof String) {
			return (String) value;
		}
		return "";
	}
	
	@Override
	public void setInfoString(String key, String value){
		extInfo.put(key, value);
		changed = true;
	}
	
	@Override
	public void setCopyForPatient(boolean copyForPatient){
		this.copyForPatient = copyForPatient;
		changed = true;
	}
	
	@Override
	public boolean getCopyForPatient(){
		return copyForPatient;
	}

	/// readonly fields	
	public String getId(){
		return iFall.getId();
	}
	
	@Override
	public IXid getXid(){
		return iFall.getXid();
	}
	
	@Override
	public String getBezeichnung(){
		return iFall.getBezeichnung();
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
	public Patient getPatient(){
		return iFall.getPatient();
	}
	

	@Override
	public Konsultation[] getBehandlungen(boolean b){
		return iFall.getBehandlungen(b);
	}
	
	@Override
	public String getRequirementsBySystem(String abrechnungsSystem){
		return iFall.getRequirementsBySystem(abrechnungsSystem);
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
	public String storeToString(){
		return iFall.storeToString();
	}
	
	@Override
	public boolean set(String field, String value){
		changed = true;
		return false;
	}
	
	@Override
	public void setRequiredContact(String kostentraeger, Kontakt k){
		changed = true;
	}
	
	@Override
	public void setRequiredString(String versicherungsnummer, String vnOld){
		changed = true;
		
	}
	
	@Override
	public boolean addXid(String domain, String domain_id, boolean updateIfExists){
		changed = true;
		return false;
	}
	
	public boolean isChanged(){
		return changed;
	}
	
	private void informChanged(){
		for (IFallChanged iFallChanged : fallChanges) {
			iFallChanged.changed(this);
			
		}
	}
	
	public interface IFallChanged {
		public void changed(FallDTO fallDTO);
	}

}

