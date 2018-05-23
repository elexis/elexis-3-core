package ch.elexis.core.importer.div.importers;

import java.util.Map;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ILabItem;
import ch.elexis.core.model.ILabOrder;
import ch.elexis.core.model.ILabResult;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.types.Gender;
import ch.elexis.core.types.LabItemTyp;
import ch.elexis.core.types.PathologicDescription;
import ch.elexis.core.types.PathologicDescription.Description;
import ch.elexis.hl7.model.OrcMessage;
import ch.rgw.tools.TimeTool;

public class TransientLabResult {
	private IPatient patient;
	private ILabItem labItem;
	private IContact origin;
	
	private String result;
	private String comment;
	private String refMale;
	private String refFemale;
	private String unit;
	private String subId;
	private Integer flags;
	private String rawAbnormalFlags;
	
	private TimeTool date;
	private TimeTool analyseTime;
	private TimeTool observationTime;
	private TimeTool transmissionTime;
	
	private Map<String, String> setProperties;
	private ILabImportUtil labImportUtil;
	
	private OrcMessage orcMessage;
	
	private TransientLabResult(Builder builder, ILabImportUtil labImportUtil){
		this.patient = builder.patient;
		this.labItem = builder.labItem;
		this.origin = builder.origin;
		
		this.result = builder.result;
		this.comment = builder.comment;
		this.refMale = builder.refMale;
		this.refFemale = builder.refFemale;
		this.unit = builder.unit;
		this.flags = builder.flags;
		this.rawAbnormalFlags = builder.rawAbnormalFlags;
		
		this.date = builder.date;
		this.analyseTime = builder.analyseTime;
		this.observationTime = builder.observationTime;
		this.transmissionTime = builder.transmissionTime;
		
		this.setProperties = builder.setProperties;
		
		this.labImportUtil = labImportUtil;
		this.orcMessage = builder.orcMessage;
		this.subId = builder.subId;
	}
	
	/**
	 * Checks if result, ref (if set) and unit are the same in the LabResult.
	 * 
	 * @param labResult
	 * @return
	 */
	public boolean isSameResult(ILabResult labResult){
		if (refMale != null) {
			if (!labResult.getRefMale().equals(refMale)) {
				return false;
			}
		}
		if (refFemale != null) {
			if (!labResult.getRefFemale().equals(refFemale)) {
				return false;
			}
		}
		if (unit != null) {
			if (!labResult.getUnit().equals(unit)) {
				return false;
			}
		}
		String matchResult = labResult.getResult();
		if (!matchResult.equals(result)) {
			return false;
		}
		
		return true;
	}
	
	public void overwriteExisting(ILabResult labResult){
		labResult.setComment(comment);
		labResult.setResult(result);
		
		setFieldsAndInterpret(labResult);
		
		// pathologic check takes place in labResult if it is numeric
		if (labItem.getTyp() == LabItemTyp.NUMERIC) {
			flags = labResult.getFlags();
		} else {
			if (flags != null) {
				labResult.setPathologicDescription(
					new PathologicDescription(Description.PATHO_IMPORT, rawAbnormalFlags));
			} else {
				labResult.setPathologicDescription(
					new PathologicDescription(Description.PATHO_IMPORT_NO_INFO, rawAbnormalFlags));
			}
		}
		
	}
	
	public ILabResult persist(ILabOrder labOrder, String orderId,
		String mandantId, TimeTool time, String groupName){
		// determine gender, set refVal
		String refVal;
		if (Gender.MALE == patient.getGender()) {
			refVal = refMale;
		} else {
			refVal = refFemale;
		}
		
		ILabResult labResult =
			labImportUtil.createLabResult(patient, date, labItem, result, comment, refVal, origin,
				subId, labOrder, orderId, mandantId, time, groupName);
		
		setFieldsAndInterpret(labResult);
		
		if (flags != null) {
			// if the pathologic flag is already set during import
			// keep it
			labResult.setFlags(flags);
			labResult.setPathologicDescription(
				new PathologicDescription(Description.PATHO_IMPORT, rawAbnormalFlags));
		} else {
			// if not, at last for numeric values keep the evaluation done in setFieldsAndInterpret 
			if (!(LabItemTyp.NUMERIC == labItem.getTyp())) {
				labResult.setPathologicDescription(
					new PathologicDescription(Description.PATHO_IMPORT_NO_INFO, rawAbnormalFlags));
			}
		}
		
		return labResult;
	}
	
	public String getLabel(){
		StringBuilder sb = new StringBuilder();
		sb.append(labItem.getLabel()).append(", ").append(getDate().toString(TimeTool.DATE_GER))
			.append(": ").append(getResult());
		return sb.toString();
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(labItem.getLabel()).append(", date ")
			.append(getDate().toString(TimeTool.TIMESTAMP));
		
		if (refMale != null) {
			sb.append(" refm ").append(refMale);
		}
		if (refFemale != null) {
			sb.append(" reff ").append(refFemale);
		}
		if (unit != null) {
			sb.append(" unit ").append(unit);
		}
		if (analyseTime != null) {
			sb.append(" aTime ").append(analyseTime.toString(TimeTool.TIMESTAMP));
		}
		if (observationTime != null) {
			sb.append(" oTime ").append(observationTime.toString(TimeTool.TIMESTAMP));
		}
		if (transmissionTime != null) {
			sb.append(" tTime ").append(transmissionTime.toString(TimeTool.TIMESTAMP));
		}
		
		sb.append(" res ").append(getResult());
		return sb.toString();
	}
	
	private void setFieldsAndInterpret(ILabResult labResult){
		if (refMale != null) {
			labResult.setRefMale(refMale);
			ILabItem item = labResult.getItem();
			if (item != null) {
				String itemRefMale = item.getReferenceMale();
				if (itemRefMale == null || itemRefMale.isEmpty()) {
					item.setReferenceMale(refMale);
				}
			}
		}
		if (refFemale != null) {
			labResult.setRefFemale(refFemale);
			ILabItem item = labResult.getItem();
			if (item != null) {
				String itemRefFemale = item.getReferenceFemale();
				if (itemRefFemale == null || itemRefFemale.isEmpty()) {
					item.setReferenceFemale(refFemale);
				}
			}
		}
		if (unit != null) {
			labResult.setUnit(unit);
		}
		if (analyseTime != null) {
			labResult.setAnalyseTime(analyseTime);
		}
		if (observationTime != null) {
			labResult.setObservationTime(observationTime);
		}
		if (transmissionTime != null) {
			labResult.setTransmissionTime(transmissionTime);
		}

		labImportUtil.updateLabResult(labResult, this);
	}
	
	public IPatient getPatient(){
		return patient;
	}
	
	public ILabItem getLabItem(){
		return labItem;
	}
	
	public String getResult(){
		return result;
	}
	
	public String getComment(){
		return comment;
	}
	
	public String getRefMale(){
		return refMale;
	}
	
	public String getRefFemale(){
		return refFemale;
	}
	
	public String getUnit(){
		return unit;
	}
	
	public int getFlags(){
		return flags;
	}
	
	public TimeTool getDate(){
		return date;
	}
	
	public TimeTool getAnalyseTime(){
		return analyseTime;
	}
	
	public OrcMessage getOrcMessage(){
		return orcMessage;
	}
	
	public String getSubId(){
		return subId;
	}
	
	/**
	 * Check if analyse time is valid, by comparing it with the transmission time.
	 * 
	 * @return
	 */
	public boolean isAnalyseTime(){
		if (analyseTime == null) {
			return false;
		}
		if (transmissionTime != null) {
			return !analyseTime.isEqual(transmissionTime);
		} else {
			return true;
		}
	}
	
	public TimeTool getObservationTime(){
		return observationTime;
	}
	
	/**
	 * Check if observation time is valid, by comparing it with the transmission time.
	 * 
	 * @return
	 */
	public boolean isObservationTime(){
		if (observationTime == null) {
			return false;
		}
		if (transmissionTime != null) {
			return !observationTime.isEqual(transmissionTime);
		} else {
			return true;
		}
	}
	
	public TimeTool getTransmissionTime(){
		return transmissionTime;
	}
	
	public static class Builder {
		// required parameters
		private IPatient patient;
		private ILabItem labItem;
		private IContact origin;
		private String result;
		private String subId;
		
		// optional parameters
		private String comment;
		private String refMale;
		private String refFemale;
		private String unit;
		private Integer flags;
		private String rawAbnormalFlags;
		
		private TimeTool date;
		private TimeTool analyseTime;
		private TimeTool observationTime;
		private TimeTool transmissionTime;
		
		private Map<String, String> setProperties;
		private OrcMessage orcMessage;
		
		public Builder(IPatient patient, IContact origin, ILabItem labItem, String result){
			this.patient = patient;
			this.labItem = labItem;
			this.result = result;
			this.origin = origin;
		}
		
		public Builder comment(String comment){
			this.comment = comment;
			return this;
		}
		
		public Builder refMale(String refMale){
			this.refMale = refMale;
			return this;
		}
		
		public Builder refFemale(String refFemale){
			this.refFemale = refFemale;
			return this;
		}
		
		public Builder ref(String ref){
			if (patient.getGender() == Gender.MALE) {
				refMale(ref);
			} else {
				refFemale(ref);
			}
			return this;
		}
		
		public Builder unit(String unit){
			this.unit = unit;
			return this;
		}
		
		public Builder flags(Integer flags){
			this.flags = flags;
			return this;
		}
		
		public Builder rawAbnormalFlags(String rawAbnormalFlags){
			this.rawAbnormalFlags = rawAbnormalFlags;
			return this;
		}
		
		public Builder date(TimeTool date){
			this.date = date;
			return this;
		}
		
		public Builder analyseTime(TimeTool analyseTime){
			this.analyseTime = analyseTime;
			return this;
		}
		
		public Builder observationTime(TimeTool observationTime){
			this.observationTime = observationTime;
			return this;
		}
		
		public Builder transmissionTime(TimeTool transmissionTime){
			this.transmissionTime = transmissionTime;
			return this;
		}
		
		public TransientLabResult build(ILabImportUtil labImportUtil){
			return new TransientLabResult(this, labImportUtil);
		}
		
		public Builder orcMessage(OrcMessage orcMessage){
			this.orcMessage = orcMessage;
			return this;
		}
		
		public Builder subId(String subId){
			this.subId = subId;
			return this;
		}
		
		//		public Builder setProperty(String property, String value){
		//			if (setProperties == null) {
		//				setProperties = new HashMap<String, String>();
		//			}
		//			setProperties.put(property, value);
		//			return this;
		//		}
	}
}
