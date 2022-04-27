package ch.elexis.core.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import ch.elexis.core.jpa.entities.Behandlung;
import ch.elexis.core.jpa.entities.Diagnosis;
import ch.elexis.core.jpa.entities.Kontakt;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;
import ch.elexis.core.model.service.holder.ContextServiceHolder;
import ch.elexis.core.model.service.holder.CoreModelServiceHolder;
import ch.elexis.core.model.util.internal.ModelUtil;
import ch.elexis.core.services.holder.CodeElementServiceHolder;
import ch.elexis.core.text.model.Samdas;
import ch.elexis.core.time.TimeUtil;
import ch.rgw.tools.VersionedResource;

public class Encounter extends AbstractIdDeleteModelAdapter<Behandlung> implements IdentifiableWithXid, IEncounter {

	private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmmss");

	public Encounter(Behandlung entity) {
		super(entity);
	}

	@Override
	public LocalDateTime getTimeStamp() {
		String time = getEntity().getTime();
		if (StringUtils.isNotBlank(time)) {
			return LocalDateTime.of(getEntity().getDatum(), LocalTime.parse(time, timeFormatter));
		} else {
			return getDate().atStartOfDay();
		}
	}

	@Override
	public void setTimeStamp(LocalDateTime value) {
		Behandlung entity = getEntityMarkDirty();
		entity.setDatum(value.toLocalDate());
		entity.setTime(timeFormatter.format(value));
	}

	@Override
	public IPatient getPatient() {
		if (getEntity().getFall() != null) {
			return ModelUtil.getAdapter(getEntity().getFall().getPatient(), IPatient.class);
		}
		return null;
	}

	@Override
	public ICoverage getCoverage() {
		return ModelUtil.getAdapter(getEntity().getFall(), ICoverage.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setCoverage(ICoverage value) {
		if (getCoverage() != null) {
			addRefresh(getCoverage());
		}
		if (value instanceof AbstractIdModelAdapter) {
			getEntityMarkDirty()
					.setFall(((AbstractIdModelAdapter<ch.elexis.core.jpa.entities.Fall>) value).getEntity());
			addRefresh(value);
		} else if (value == null) {
			getEntityMarkDirty().setFall(null);
		}
	}

	@Override
	public IMandator getMandator() {
		return ModelUtil.getAdapter(getEntity().getMandant(), IMandator.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setMandator(IMandator value) {
		if (value != null) {
			getEntityMarkDirty().setMandant(((AbstractIdModelAdapter<Kontakt>) value).getEntity());
		} else {
			getEntityMarkDirty().setMandant(null);
		}
	}

	@Override
	public List<IBilled> getBilled() {
		CoreModelServiceHolder.get().refresh(this);
		return getEntity().getBilled().parallelStream().filter(b -> !b.isDeleted())
				.map(b -> ModelUtil.getAdapter(b, IBilled.class, true)).collect(Collectors.toList());
	}

	@Override
	public void removeBilled(IBilled billed) {
		CoreModelServiceHolder.get().delete(billed);
	}

	@Override
	public LocalDate getDate() {
		return getEntity().getDatum();
	}

	@Override
	public void setDate(LocalDate value) {
		Behandlung entity = getEntityMarkDirty();
		entity.setDatum(value);
		entity.setTime("000000");
	}

	@Override
	public VersionedResource getVersionedEntry() {
		return getEntity().getEintrag();
	}

	@Override
	public void setVersionedEntry(VersionedResource value) {
		getEntityMarkDirty().setEintrag(value);
	}

	@Override
	public List<IDiagnosisReference> getDiagnoses() {
		CoreModelServiceHolder.get().refresh(this);
		return getEntity().getDiagnoses().parallelStream().filter(d -> !d.isDeleted())
				.map(d -> ModelUtil.getAdapter(d, IDiagnosisReference.class, true)).collect(Collectors.toList());
	}

	@Override
	public void addDiagnosis(IDiagnosis diagnosis) {
		IDiagnosisReference diagnosisRef = null;
		if (diagnosis instanceof IDiagnosisReference) {
			diagnosisRef = (IDiagnosisReference) diagnosis;
		} else {
			diagnosisRef = ModelUtil.getOrCreateDiagnosisReference(diagnosis);
		}
		@SuppressWarnings("unchecked")
		Diagnosis diag = ((AbstractIdModelAdapter<Diagnosis>) diagnosisRef).getEntity();
		if (!getEntity().getDiagnoses().contains(diag)) {
			getEntityMarkDirty().getDiagnoses().add(diag);
		}
		CodeElementServiceHolder.updateStatistics(diagnosis,
				ContextServiceHolder.get().getActiveUserContact().orElse(null));
		CodeElementServiceHolder.updateStatistics(diagnosis, getPatient());
	}

	@Override
	public void removeDiagnosis(IDiagnosis diagnosis) {
		if (!(diagnosis instanceof IDiagnosisReference)) {
			LoggerFactory.getLogger(getClass()).warn("Can only remove IDiagnosisReference");
			return;
		}
		@SuppressWarnings("unchecked")
		Diagnosis diag = ((AbstractIdModelAdapter<Diagnosis>) diagnosis).getEntity();
		getEntityMarkDirty().getDiagnoses().remove(diag);
	}

	@Override
	public IInvoice getInvoice() {
		return ModelUtil.getAdapter(getEntity().getInvoice(), IInvoice.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setInvoice(IInvoice value) {
		if (getInvoice() != null) {
			addRefresh(getInvoice());
		}
		if (value != null) {
			getEntityMarkDirty()
					.setInvoice(((AbstractIdModelAdapter<ch.elexis.core.jpa.entities.Invoice>) value).getEntity());
			addRefresh(value);
		} else {
			getEntityMarkDirty().setInvoice(null);
		}
	}

	@Override
	public boolean isBillable() {
		return getEntity().getBillable();
	}

	@Override
	public void setBillable(boolean value) {
		getEntityMarkDirty().setBillable(value);
	}

	@Override
	public InvoiceState getInvoiceState() {
		IInvoice invoice = getInvoice();
		if (invoice != null) {
			return invoice.getState();
		}
		IMandator mandator = getMandator();
		IMandator activeMandator = ContextServiceHolder.get().getActiveMandator().orElse(null);
		if ((mandator != null && activeMandator != null) && (mandator.equals(activeMandator))) {
			if (getDate().isEqual(LocalDate.now())) {
				return InvoiceState.FROM_TODAY;
			} else {
				return InvoiceState.NOT_FROM_TODAY;
			}
		} else {
			return InvoiceState.NOT_FROM_YOU;
		}
	}

	@Override
	public String getLabel() {
		StringBuffer ret = new StringBuffer();
		IMandator m = getMandator();
		ret.append(TimeUtil.formatSafe(getTimeStamp()));
		ret.append(" (" + getInvoiceStateText() + ") - ");
		ret.append((m == null) ? "?" : m.getLabel());
		return ret.toString();
	}

	private String getInvoiceStateText() {
		String statusText = StringUtils.EMPTY;

		IInvoice rechnung = getInvoice();
		if (rechnung != null) {
			statusText += "RG " + rechnung.getNumber() + ": ";
		}
		statusText += getInvoiceState().getLocaleText();
		return statusText;
	}

	@Override
	public String getHeadVersionInPlaintext() {
		String head = getVersionedEntry().getHead();
		if (head != null) {
			if (head.startsWith("<")) {
				Samdas samdas = new Samdas(head);
				String recordText = samdas.getRecordText();
				recordText = maskHTML(recordText);
				return recordText;
			}
			return head.trim();
		}
		return StringUtils.EMPTY;
	}

	/**
	 * From ch.elexis.core.ui.actions.HistoryLoader
	 *
	 * @param input
	 * @return
	 */
	private String maskHTML(String input) {
		String s = input.replaceAll("<", "&lt;"); //$NON-NLS-1$ //$NON-NLS-2$
		s = s.replaceAll(">", "&gt;"); //$NON-NLS-1$ //$NON-NLS-2$
		s = s.replaceAll("&", "&amp;"); //$NON-NLS-1$ //$NON-NLS-2$
		return s;
	}
}
