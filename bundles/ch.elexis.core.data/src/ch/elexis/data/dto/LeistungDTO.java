package ch.elexis.data.dto;

import java.util.Optional;
import java.util.function.Consumer;

import org.slf4j.LoggerFactory;

import ch.elexis.core.data.interfaces.IFall;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.model.IBillable;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.IBillingSystemFactor;
import ch.elexis.core.model.IInvoice;
import ch.elexis.core.model.IInvoiceBillRecordInfo;
import ch.elexis.core.services.holder.BillingServiceHolder;
import ch.elexis.core.services.holder.InvoiceServiceHolder;
import ch.rgw.tools.Money;
import ch.rgw.tools.Result;

public class LeistungDTO {
	private final String id;
	private String code;
	private final String text;
	private double count;
	private IBillable iVerrechenbar;
	private long lastUpdate;
	private IBilled billed;

	private Optional<IInvoiceBillRecordInfo> recordInfo;

	private int tp = 0;
	private double tpw = 1.0;
	private double scale1 = 1.0;

	public LeistungDTO(IBilled verrechnet) throws ElexisException {
		try {
			if (verrechnet.getLastupdate() < 0) {
				throw new Exception();
			}
		} catch (Exception e) {
			throw new ElexisException(
					"Die verrechnete Leistung wird ignoriert - Datum der letzten Aktualisierung ist fehlerhaft [ID: "
							+ verrechnet.getId() + "].",
					e);
		}
		this.billed = verrechnet;
		this.lastUpdate = verrechnet.getLastupdate();
		this.id = verrechnet.getId();
		this.code = this.billed.getCode();
		this.text = verrechnet.getText();
		this.tp = this.billed.getPoints();
		this.tpw = this.billed.getFactor();
		this.count = this.billed.getAmount();
		this.iVerrechenbar = this.billed.getBillable();
	}

	public LeistungDTO(IBillable iVerrechenbar, IFall fall) {
		this.lastUpdate = System.currentTimeMillis();
		this.id = iVerrechenbar.getId();
		this.code = iVerrechenbar.getCode();
		this.text = iVerrechenbar.getText();
		this.tp = -1;
		this.tpw = 1.0;
		this.scale1 = 1.0;
		this.count = 1;
		this.iVerrechenbar = iVerrechenbar;
	}

	public boolean calcPrice(KonsultationDTO konsultationDTO, FallDTO fallDTO, Consumer<Result<IBilled>> showResult) {
		if (billed == null) {
			@SuppressWarnings("unchecked")
			Result<IBilled> result = iVerrechenbar.getOptifier().add(iVerrechenbar,
					konsultationDTO.getTransientCopyWithoutBillable(iVerrechenbar), 1.0, false);
			if (result.isOK()) {
				tp = result.get().getPoints();
				tpw = result.get().getFactor();
				scale1 = result.get().getPrimaryScaleFactor();
			} else {
				LoggerFactory.getLogger(getClass()).warn("Adding billable failed [" + result.getMessages() + "]");
				showResult.accept(result);
				return false;
			}
		} else {
			tpw = getFactor();
			scale1 = billed.getPrimaryScaleFactor();
		}
		return true;
	}

	private double getFactor() {
		if (iVerrechenbar != null) {
			Optional<IBillingSystemFactor> billingFactor = BillingServiceHolder.get()
					.getBillingSystemFactor(iVerrechenbar.getCodeSystemName(), billed.getEncounter().getDate());
			if (billingFactor.isPresent()) {
				return billingFactor.get().getFactor();
			}
		}
		return 1.0;
	}

	public void setTp(int tp) {
		this.tp = tp;
	}

	public IBilled getVerrechnet() {
		return billed;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public String getText() {
		return text;
	}

	public void setVerrechnet(IBilled verrechnet) {
		this.billed = verrechnet;
	}

	public String getId() {
		return id;
	}

	public Money getPrice() {
		return new Money((int) (Math.round(tp * tpw) * scale1 * count));
	}

	public void setCount(double count) {
		this.count = count;
	}

	public double getCount() {
		return count;
	}

	public void setiVerrechenbar(IBillable iVerrechenbar) {
		this.iVerrechenbar = iVerrechenbar;
	}

	public IBillable getIVerrechenbar() {
		return iVerrechenbar;
	}

	public long getLastUpdate() {
		return lastUpdate;
	}

	public int getTp() {
		return tp;
	}

	public double getTpw() {
		return tpw;
	}

	public IBilled getBilled() {
		return billed;
	}

	public Optional<IInvoiceBillRecordInfo> getInvoiceBillRecordInfo(IInvoice invoice) {
		if (recordInfo == null) {
			recordInfo = InvoiceServiceHolder.get().getInvoiceInvoiceBillRecordInfo(invoice, billed);
		}
		return recordInfo;
	}
}
