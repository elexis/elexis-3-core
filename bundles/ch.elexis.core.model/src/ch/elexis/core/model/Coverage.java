package ch.elexis.core.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.jpa.entities.Fall;
import ch.elexis.core.jpa.entities.Kontakt;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.service.holder.CoreModelServiceHolder;
import ch.elexis.core.model.service.holder.IBillingSystemServiceHolder;
import ch.elexis.core.model.util.internal.ModelUtil;
import ch.elexis.core.time.TimeUtil;

public class Coverage extends AbstractIdDeleteModelAdapter<Fall> implements IdentifiableWithXid, ICoverage {

	public Coverage(Fall entity) {
		super(entity);
	}

	@Override
	public Object getExtInfo(Object key) {
		return extInfoHandler.getExtInfo(key);
	}

	@Override
	public void setExtInfo(Object key, Object value) {
		extInfoHandler.setExtInfo(key, value);
	}

	@Override
	public Map<Object, Object> getMap() {
		return extInfoHandler.getMap();
	}

	@Override
	public IPatient getPatient() {
		return ModelUtil.getAdapter(getEntity().getPatient(), IPatient.class, true);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setPatient(IPatient value) {
		if (getPatient() != null) {
			addRefresh(getPatient());
		}
		if (value != null) {
			getEntityMarkDirty().setPatient(((AbstractIdModelAdapter<Kontakt>) value).getEntity());
			addRefresh(value);
		} else {
			getEntityMarkDirty().setPatient(null);
		}
	}

	@Override
	public String getDescription() {
		return getEntity().getBezeichnung();
	}

	@Override
	public void setDescription(String value) {
		getEntityMarkDirty().setBezeichnung(value);
	}

	@Override
	public String getReason() {
		return getEntity().getGrund();
	}

	@Override
	public void setReason(String value) {
		getEntityMarkDirty().setGrund(value);
	}

	@Override
	public LocalDate getDateFrom() {
		return getEntity().getDatumVon();
	}

	@Override
	public void setDateFrom(LocalDate value) {
		getEntityMarkDirty().setDatumVon(value);
	}

	@Override
	public LocalDate getDateTo() {
		return getEntity().getDatumBis();
	}

	@Override
	public void setDateTo(LocalDate value) {
		getEntityMarkDirty().setDatumBis(value);
	}

	@Override
	public IBillingSystem getBillingSystem() {
		IBillingSystem billingSystem = IBillingSystemServiceHolder.get().getBillingSystem(getEntity().getGesetz())
				.orElse(null);
		if (billingSystem == null) {
			billingSystem = IBillingSystemServiceHolder.get().getDefaultBillingSystem();
		}
		return billingSystem;
	}

	@Override
	public void setBillingSystem(IBillingSystem value) {
		getEntityMarkDirty().setGesetz(value.getName());
	}

	@Override
	public IContact getCostBearer() {
		return ModelUtil.getAdapter(getEntity().getKostentrKontakt(), IContact.class, true);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setCostBearer(IContact value) {
		if (value != null) {
			getEntityMarkDirty().setKostentrKontakt(((AbstractIdModelAdapter<Kontakt>) value).getEntity());
		} else {
			getEntityMarkDirty().setKostentrKontakt(null);
		}
	}

	@Override
	public IContact getGuarantor() {
		if (getEntity().getGarantKontakt() == null || getEntity().getGarantKontakt().isDeleted()) {
			return getPatient();
		}
		IContact ret = ModelUtil.getAdapter(getEntity().getGarantKontakt(), IContact.class, true);
		if (ret != null && ret.isPatient()) {
			ret = ModelUtil.getAdapter(getEntity().getGarantKontakt(), IPatient.class, true);
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setGuarantor(IContact value) {
		if (value != null) {
			getEntityMarkDirty().setGarantKontakt(((AbstractIdModelAdapter<Kontakt>) value).getEntity());
		} else {
			getEntityMarkDirty().setGarantKontakt(null);
		}
	}

	@Override
	public String getInsuranceNumber() {
		String insuranceNumber = getEntity().getVersNummer();
		if (StringUtils.isEmpty(insuranceNumber)) {
			Object extInfoInsuranceNumber = getExtInfo(FallConstants.FLD_EXT_VERSICHERUNGSNUMMER);
			if (extInfoInsuranceNumber instanceof String && StringUtils.isNotBlank((String) extInfoInsuranceNumber)) {
				return (String) extInfoInsuranceNumber;
			}
		}
		return insuranceNumber;
	}

	@Override
	public void setInsuranceNumber(String value) {
		getEntityMarkDirty().setVersNummer(value);
		// compatibility
		setExtInfo(FallConstants.FLD_EXT_VERSICHERUNGSNUMMER, value);
	}

	@Override
	public List<IEncounter> getEncounters() {
		CoreModelServiceHolder.get().refresh(this);
		return getEntity().getConsultations().parallelStream().filter(f -> !f.isDeleted())
				.map(f -> ModelUtil.getAdapter(f, IEncounter.class, true)).collect(Collectors.toList());
	}

	@Override
	public boolean isOpen() {
		return getDateTo() == null;
	}

	@Override
	public String getLabel() {
		StringBuilder ret = new StringBuilder();
		if (!isOpen()) {
			ret.append(Messages.Fall_CLOSED);
		}
		String ges = getBillingSystem().getName();
		ret.append(ges).append(": ").append(getReason()).append(" - "); //$NON-NLS-1$ //$NON-NLS-2$
		ret.append(getDescription()).append("("); //$NON-NLS-1$
		LocalDate dateTo = getDateTo();
		ret.append(TimeUtil.formatSafe(getDateFrom())).append("-") //$NON-NLS-1$
				.append(dateTo == null ? Messages.Fall_Open : TimeUtil.formatSafe(dateTo)).append(")"); //$NON-NLS-1$
		return ret.toString();
	}

	@Override
	public LocalDate getBillingProposalDate() {
		String proposalDateString = getEntity().getBetriebsNummer();
		if (proposalDateString != null && !proposalDateString.isEmpty()) {
			return ModelUtil.toLocalDate(proposalDateString);
		}
		return null;
	}

	@Override
	public void setBillingProposalDate(LocalDate value) {
		if (value != null) {
			getEntityMarkDirty().setBetriebsNummer(ModelUtil.toString(value));
		} else {
			getEntityMarkDirty().setBetriebsNummer(null);
		}
	}
}
