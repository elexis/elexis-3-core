package ch.elexis.data.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import ch.elexis.core.data.interfaces.IFall;
import ch.elexis.core.model.IXid;
import ch.elexis.data.Fall;
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
	private String bezeichnung;
	
	private List<IFallChanged> fallChanges = new ArrayList<>();
	
	private boolean changed;
	
	public FallDTO(IFall fall){
		changed = false;
		iFall = fall;
		fallChanges.clear();
		abrechnungsSystem = iFall.getAbrechnungsSystem();
		grund = iFall.getGrund();
		beginnDatum = iFall.getBeginnDatum();
		endDatum = iFall.getEndDatum();
		billingDate = iFall.getBillingDate();
		garant = iFall.getGarant();
		copyForPatient = iFall.getCopyForPatient();
		extInfo = iFall.getMap(PersistentObject.FLD_EXTINFO);
		bezeichnung = iFall.getBezeichnung();
	}
	
	/// editable fields
	@Override
	public void setAbrechnungsSystem(String abrechnungsSystem){
		if (!StringUtils.equals(this.abrechnungsSystem, abrechnungsSystem)) {
			this.abrechnungsSystem = abrechnungsSystem;
			informChanged(true);
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
			informChanged(false);
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
			informChanged(false);
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
			informChanged(false);
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
		informChanged(false);
	}
	
	@Override
	public void setGarant(Kontakt garant){
		this.garant = garant;
		informChanged(false);
	}
	
	@Override
	public Kontakt getGarant(){
		return garant;
	}
	
	@Override
	public void setMap(String string, Map<Object, Object> ht){
		if (PersistentObject.FLD_EXTINFO.equals(string)) {
			extInfo = ht;
			informChanged(false);
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
		informChanged(false);
	}
	
	@Override
	public void setCopyForPatient(boolean copyForPatient){
		this.copyForPatient = copyForPatient;
		informChanged(false);
	}
	
	public void setBezeichnung(String bezeichnung){
		if (!StringUtils.equals(this.bezeichnung, bezeichnung)) {
			informChanged(false);
		}
		this.bezeichnung = bezeichnung;
	}
	
	@Override
	public String getBezeichnung(){
		return bezeichnung;
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
	public String getOptionals(){
		return iFall.getOptionals();
	}
	
	@Override
	public String getUnused(){
		return iFall.getUnused();
	}
	
	@Override
	public void setCostBearer(Kontakt costBearer){
		iFall.setCostBearer(costBearer);
	}

	@Override
	public Kontakt getCostBearer(){
		return iFall.getCostBearer();
	}
	
	
	@Override
	public String storeToString(){
		return iFall.storeToString();
	}
	
	@Override
	public boolean set(String field, String value){
		// the view fallDetailBlatt sets this field with the set method
		if (StringUtils.equals(field, Fall.FLD_BEZEICHNUNG)) {
			setBezeichnung(value);
		}
		informChanged(false);
		return false;
	}
	
	@Override
	public void setRequiredContact(String kostentraeger, Kontakt k){
		informChanged(false);
	}
	
	@Override
	public void setRequiredString(String versicherungsnummer, String vnOld){
		informChanged(false);
		
	}
	
	@Override
	public boolean addXid(String domain, String domain_id, boolean updateIfExists){
		informChanged(false);
		return false;
	}
	
	public boolean isChanged(){
		return changed;
	}
	
	public void register(IFallChanged fallChanged){
		fallChanges.add(fallChanged);
	}
	
	private void informChanged(boolean triggersRecalc){
		for (IFallChanged iFallChanged : fallChanges) {
			iFallChanged.changed(this, triggersRecalc);
			
		}
		changed = true;
	}
	
	public interface IFallChanged {
		public void changed(FallDTO fallDTO, boolean triggersRecalc);
	}

}

