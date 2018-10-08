package ch.elexis.core.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.adapter.mixin.IdentifiableWithXid;
import ch.elexis.core.model.util.ModelUtil;
import ch.elexis.core.types.LabItemTyp;

public class LabItem extends AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.LabItem>
		implements IdentifiableWithXid, ILabItem {
	
	public LabItem(ch.elexis.core.jpa.entities.LabItem entity){
		super(entity);
	}
	
	@Override
	public LabItemTyp getTyp(){
		return getEntity().getTyp();
	}
	
	@Override
	public void setTyp(LabItemTyp value){
		getEntity().setTyp(value);
	}
	
	@Override
	public String getReferenceMale(){
		return getEntity().getReferenceMale();
	}
	
	@Override
	public void setReferenceMale(String value){
		getEntity().setReferenceMale(value);
	}
	
	@Override
	public String getReferenceFemale(){
		return StringUtils.defaultString(getEntity().getReferenceFemale()).split("##")[0];
	}
	
	@Override
	public void setReferenceFemale(String value){
		getEntity().setReferenceFemale(value);
	}
	
	@Override
	public String getGroup(){
		return getEntity().getGroup();
	}
	
	@Override
	public void setGroup(String value){
		getEntity().setGroup(value);
	}
	
	@Override
	public String getPriority(){
		return getEntity().getPriority();
	}
	
	@Override
	public void setPriority(String value){
		getEntity().setPriority(value);
	}
	
	@Override
	public String getCode(){
		return getEntity().getCode();
	}
	
	@Override
	public void setCode(String value){
		getEntity().setCode(value);
	}
	
	@Override
	public String getUnit(){
		return getEntity().getUnit();
	}
	
	@Override
	public void setUnit(String value){
		getEntity().setUnit(value);
	}
	
	@Override
	public String getName(){
		return getEntity().getName();
	}
	
	@Override
	public void setName(String value){
		getEntity().setName(value);
	}
	
	@Override
	public int getDigits(){
		return getEntity().getDigits();
	}
	
	@Override
	public void setDigits(int value){
		getEntity().setDigits(value);
	}
	
	@Override
	public boolean isVisible(){
		return getEntity().isVisible();
	}
	
	@Override
	public void setVisible(boolean value){
		getEntity().setVisible(value);
	}
	
	@Override
	public String getFormula(){
		String formula = getEntity().getFormula();
		
		if (formula == null || formula.isEmpty()) {
			String[] refWEntry =
				StringUtils.defaultString(getEntity().getReferenceFemale()).split("##");
			formula = refWEntry.length > 1 ? refWEntry[1] : "";
			
			if (formula != null && !formula.isEmpty()) {
				setFormula(formula);
			}
		}
		return formula;
	}
	
	@Override
	public void setFormula(String value){
		getEntity().setFormula(value);
	}
	
	@Override
	public String getLoincCode(){
		return getEntity().getLoinccode();
	}
	
	@Override
	public void setLoincCode(String value){
		getEntity().setLoinccode(value);
	}
	
	@Override
	public String getBillingCode(){
		return getEntity().getBillingCode();
	}
	
	@Override
	public void setBillingCode(String value){
		getEntity().setBillingCode(value);
	}
	
	@Override
	public String getExport(){
		return getEntity().getExport();
	}
	
	@Override
	public void setExport(String value){
		getEntity().setExport(value);
	}
	
	@Override
	public List<ILabMapping> getMappings(){
		return getEntity().getMappings().parallelStream().filter(f -> !f.isDeleted())
			.map(f -> ModelUtil.getAdapter(f, ILabMapping.class)).collect(Collectors.toList());
	}
	
	@Override
	public ILabMapping addMapping(ILabMapping mapping){
		if (mapping instanceof AbstractIdDeleteModelAdapter) {
			mapping.setItem(this);
			Set<ch.elexis.core.jpa.entities.LabMapping> mappings =
				new HashSet<>(getEntity().getMappings());
			mappings.add(
				(ch.elexis.core.jpa.entities.LabMapping) ((AbstractIdDeleteModelAdapter<?>) mapping)
					.getEntity());
			getEntity().setMappings(mappings);
		}
		return mapping;
	}
	
	@Override
	public void removeMapping(ILabMapping mapping){
		if (mapping instanceof AbstractIdDeleteModelAdapter) {
			Set<ch.elexis.core.jpa.entities.LabMapping> mappings =
				new HashSet<>(getEntity().getMappings());
			mappings.remove(
				(ch.elexis.core.jpa.entities.LabMapping) ((AbstractIdDeleteModelAdapter<?>) mapping)
					.getEntity());
			getEntity().setMappings(mappings);
		}
		
	}

	@Override
	public String getVariableName(){
		String group = getGroup();
		if (group != null && group.contains(" ")) {
			String[] group_tokens = group.split(" ", 2);
			String prio = getPriority();
			String num = (prio != null) ? prio.trim() : "9999";
			return group_tokens[0] + "_" + num;
		}

		return "ERROR";
	}
}
