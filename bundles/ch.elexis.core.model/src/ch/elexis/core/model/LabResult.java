package ch.elexis.core.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.jpa.entities.Kontakt;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.adapter.mixin.ExtInfoHandler;
import ch.elexis.core.model.util.internal.LabPathologicEvaluator;
import ch.elexis.core.model.util.internal.ModelUtil;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.types.PathologicDescription;

public class LabResult extends AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.LabResult>
		implements IdentifiableWithXid, ILabResult {
	
	private LabPathologicEvaluator labPathologicEvaluator;
	private ExtInfoHandler extInfoHandler;

	public LabResult(ch.elexis.core.jpa.entities.LabResult entity){
		super(entity);
		extInfoHandler = new ExtInfoHandler(this);
	}
	
	@Override
	public String getUnit(){
		return getEntity().getUnit();
	}
	
	@Override
	public void setUnit(String value){
		getEntityMarkDirty().setUnit(value);
	}
	
	@Override
	public LocalDateTime getAnalyseTime(){
		return getEntity().getAnalysetime();
	}
	
	@Override
	public void setAnalyseTime(LocalDateTime value){
		getEntityMarkDirty().setAnalysetime(value);
	}
	
	@Override
	public LocalDateTime getObservationTime(){
		return getEntity().getObservationtime();
	}
	
	@Override
	public void setObservationTime(LocalDateTime value){
		getEntityMarkDirty().setObservationtime(value);
	}
	
	@Override
	public LocalDateTime getTransmissionTime(){
		return getEntity().getTransmissiontime();
	}
	
	@Override
	public void setTransmissionTime(LocalDateTime value){
		getEntityMarkDirty().setTransmissiontime(value);
	}
	
	@Override
	public boolean isPathologic(){
		return getEntity().isFlagged(LabResultConstants.PATHOLOGIC);
	}
	
	@Override
	public void setPathologic(boolean value){
		int oldFlags = getEntity().getFlags();
		if (value) {
			getEntityMarkDirty().setFlags(oldFlags | LabResultConstants.PATHOLOGIC);
		} else {
			getEntityMarkDirty().setFlags(oldFlags & ~LabResultConstants.PATHOLOGIC);
		}
	}
	
	@Override
	public String getResult(){
		return getEntity().getResult();
	}
	
	@Override
	public void setResult(String value){
		getEntityMarkDirty().setResult(value);
		setPathologic(getLabPathologicEvaluator().isPathologic(this));
	}
	
	@Override
	public String getComment(){
		return getEntity().getComment();
	}
	
	@Override
	public void setComment(String value){
		getEntityMarkDirty().setComment(value);
	}
	
	@Override
	public String getReferenceMale(){
		return resolvePreferredRefValue(getItem().getReferenceMale(), getEntity().getRefMale());
	}
	
	@Override
	public void setReferenceMale(String value){
		getEntityMarkDirty().setRefMale(value);
		setPathologic(getLabPathologicEvaluator().isPathologic(this));
	}
	
	@Override
	public String getReferenceFemale(){
		return resolvePreferredRefValue(getItem().getReferenceFemale(), getEntity().getRefFemale());
	}
	
	@Override
	public void setReferenceFemale(String value){
		getEntityMarkDirty().setRefFemale(value);
		setPathologic(getLabPathologicEvaluator().isPathologic(this));
	}
	
	@Override
	public LocalDate getDate(){
		return getEntity().getDate();
	}
	
	@Override
	public void setDate(LocalDate value){
		getEntityMarkDirty().setDate(value);
	}
	
	@Override
	public ILabItem getItem(){
		return ModelUtil.getAdapter(getEntity().getItem(), ILabItem.class);
	}
	
	@Override
	public void setItem(ILabItem value){
		if (value instanceof AbstractIdDeleteModelAdapter) {
			getEntityMarkDirty().setItem(
				(ch.elexis.core.jpa.entities.LabItem) ((AbstractIdDeleteModelAdapter<?>) value)
					.getEntity());
		} else if (value == null) {
			getEntityMarkDirty().setItem(null);
		}
	}
	
	@Override
	public PathologicDescription getPathologicDescription(){
		return getEntity().getPathologicDescription();
	}
	
	@Override
	public void setPathologicDescription(PathologicDescription value){
		getEntityMarkDirty().setPathologicDescription(value);
	}
	
	@Override
	public IContact getOrigin(){
		return ModelUtil.getAdapter(getEntity().getOrigin(), IContact.class);
	}
	
	@Override
	public void setOrigin(IContact value){
		if (value instanceof AbstractIdDeleteModelAdapter) {
			getEntityMarkDirty()
				.setOrigin((Kontakt) ((AbstractIdDeleteModelAdapter<?>) value).getEntity());
		} else if (value == null) {
			getEntityMarkDirty().setOrigin(null);
		}
	}
	
	@Override
	public IPatient getPatient(){
		return ModelUtil.getAdapter(getEntity().getPatient(), IPatient.class);
	}
	
	@Override
	public void setPatient(IPatient value){
		if (value instanceof AbstractIdDeleteModelAdapter) {
			getEntityMarkDirty()
				.setPatient((Kontakt) ((AbstractIdDeleteModelAdapter<?>) value).getEntity());
		} else if (value == null) {
			getEntityMarkDirty().setPatient(null);
		}
	}
	
	@Override
	public ILabOrder getLabOrder(){
		IQuery<ILabOrder> query = ModelUtil.getQuery(ILabOrder.class);
		query.and(ModelPackage.Literals.ILAB_ORDER__RESULT, COMPARATOR.EQUALS, this);
		List<ILabOrder> results = query.execute();
		if (results.size() > 0) {
			if (results.size() > 1) {
				LoggerFactory.getLogger(getClass())
					.warn("Multiple LabOrders for LabResult [{}] found, please check", getId());
			}
			return results.get(0);
		}
		return null;
	}
	
	/**
	 * Get reference value based on user settings (either from local system (LabItem) or device sent
	 * (LabResult)).
	 * 
	 * @param itemRef
	 *            {@link LabItem} reference
	 * @param resultRef
	 *            {@link LabResult} reference
	 * @return Preferred refValue. Per default reference of {@link LabItem} is returned
	 */
	private String resolvePreferredRefValue(String itemRef, String resultRef){
		boolean useLocalRefs = ModelUtil.isUserConfig(ModelUtil.getActiveUserContact().orElse(null),
			Preferences.LABSETTINGS_CFG_LOCAL_REFVALUES, true);
		if (useLocalRefs && itemRef != null && !itemRef.isEmpty()) {
			return itemRef;
		} else {
			String ref = StringUtils.defaultString(resultRef);
			if (ref.isEmpty()) {
				return itemRef;
			}
			return ref;
		}
	}
	
	private synchronized LabPathologicEvaluator getLabPathologicEvaluator(){
		if (labPathologicEvaluator == null) {
			labPathologicEvaluator = new LabPathologicEvaluator();
		}
		return labPathologicEvaluator;
	}
	
	@Override
	public Object getExtInfo(Object key){
		return extInfoHandler.getExtInfo(key);
	}
	
	@Override
	public void setExtInfo(Object key, Object value){
		extInfoHandler.setExtInfo(key, value);
	}
	
	@Override
	public String getLabel(){
		return getItem().getLabel() + ", " + getDate() + ": " + getResult();
	}
}
