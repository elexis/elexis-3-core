package ch.elexis.core.model;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.model.util.internal.ModelUtil;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.types.LabItemTyp;

public class LabItem extends AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.LabItem>
		implements IdentifiableWithXid, ILabItem {

	public LabItem(ch.elexis.core.jpa.entities.LabItem entity) {
		super(entity);
	}

	@Override
	public LabItemTyp getTyp() {
		return getEntity().getTyp();
	}

	@Override
	public void setTyp(LabItemTyp value) {
		getEntityMarkDirty().setTyp(value);
	}

	@Override
	public String getReferenceMale() {
		return StringUtils.defaultString(getEntity().getReferenceMale());
	}

	@Override
	public void setReferenceMale(String value) {
		getEntityMarkDirty().setReferenceMale(value);
	}

	@Override
	public String getReferenceFemale() {
		return StringUtils.defaultString(getEntity().getReferenceFemale()).split("##")[0];
	}

	@Override
	public void setReferenceFemale(String value) {
		getEntityMarkDirty().setReferenceFemale(value);
	}

	@Override
	public String getGroup() {
		return getEntity().getGroup();
	}

	@Override
	public void setGroup(String value) {
		getEntityMarkDirty().setGroup(value);
	}

	@Override
	public String getPriority() {
		return getEntity().getPriority();
	}

	@Override
	public void setPriority(String value) {
		getEntityMarkDirty().setPriority(value);
	}

	@Override
	public String getCode() {
		return getEntity().getCode();
	}

	@Override
	public void setCode(String value) {
		getEntityMarkDirty().setCode(value);
	}

	@Override
	public String getUnit() {
		return getEntity().getUnit();
	}

	@Override
	public void setUnit(String value) {
		getEntityMarkDirty().setUnit(value);
	}

	@Override
	public String getName() {
		return getEntity().getName();
	}

	@Override
	public void setName(String value) {
		getEntityMarkDirty().setName(value);
	}

	@Override
	public int getDigits() {
		return getEntity().getDigits();
	}

	@Override
	public void setDigits(int value) {
		getEntityMarkDirty().setDigits(value);
	}

	@Override
	public boolean isVisible() {
		return getEntity().isVisible();
	}

	@Override
	public void setVisible(boolean value) {
		getEntityMarkDirty().setVisible(value);
	}

	@Override
	public String getFormula() {
		String formula = getEntity().getFormula();

		if (formula == null || formula.isEmpty()) {
			String[] refWEntry = StringUtils.defaultString(getEntity().getReferenceFemale()).split("##");
			formula = refWEntry.length > 1 ? refWEntry[1] : StringUtils.EMPTY;

			if (formula != null && !formula.isEmpty()) {
				setFormula(formula);
			}
		}
		return formula;
	}

	@Override
	public void setFormula(String value) {
		getEntityMarkDirty().setFormula(value);
	}

	@Override
	public String getLoincCode() {
		return getEntity().getLoinccode();
	}

	@Override
	public void setLoincCode(String value) {
		getEntityMarkDirty().setLoinccode(value);
	}

	@Override
	public String getBillingCode() {
		return getEntity().getBillingCode();
	}

	@Override
	public void setBillingCode(String value) {
		getEntityMarkDirty().setBillingCode(value);
	}

	@Override
	public String getExport() {
		return getEntity().getExport();
	}

	@Override
	public void setExport(String value) {
		getEntityMarkDirty().setExport(value);
	}

	@Override
	public boolean isNoReferenceValueItem() {
		return LabItemConstants.REFVAL_INCONCLUSIVE.equals(getReferenceMale())
				&& LabItemConstants.REFVAL_INCONCLUSIVE.equals(getReferenceFemale());
	}

	@Override
	public List<ILabMapping> getMappings() {
		CoreModelServiceHolder.get().refresh(this);
		return getEntity().getMappings().parallelStream().filter(f -> !f.isDeleted())
				.map(f -> ModelUtil.getAdapter(f, ILabMapping.class)).collect(Collectors.toList());
	}

	@Override
	public String getVariableName() {
		String group = getGroup();
		if (group != null && group.contains(StringUtils.SPACE)) {
			String[] group_tokens = group.split(StringUtils.SPACE, 2);
			String prio = getPriority();
			String num = (prio != null) ? prio.trim() : "9999";
			return group_tokens[0] + "_" + num;
		}

		return "ERROR";
	}

	@Override
	public String getLabel() {
		StringBuilder sb = new StringBuilder();
		sb.append(getCode() + ", " + getName());
		if (LabItemTyp.NUMERIC == getTyp()) {
			sb.append(" (" + getReferenceMale() + "/" + getReferenceFemale() + StringUtils.SPACE + getUnit() + ")");
		} else {
			sb.append(" (" + getReferenceFemale() + ")");
		}
		sb.append("[" + getGroup() + ", " + getPriority() + "]");
		return sb.toString();
	}

	@Override
	public String toString() {
		return super.toString() + StringUtils.SPACE + getLabel();
	}
}
