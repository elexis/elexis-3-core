package ch.elexis.data.dto;

import java.util.Optional;

import ch.elexis.core.data.interfaces.IFall;
import ch.elexis.core.data.util.NoPoUtil;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.model.IBillable;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.IBillingSystemFactor;
import ch.elexis.core.services.holder.BillingServiceHolder;
import ch.elexis.data.Verrechnet;
import ch.rgw.tools.Money;
import ch.rgw.tools.Result;

public class LeistungDTO {
	private final String id;
	private String code;
	private final String text;
	private int count;
	private IBillable iVerrechenbar;
	private long lastUpdate;
	private IBilled verrechnet;

	private int tp = 0;
	private double tpw = 1.0;
	private double scale1 = 1.0;
	private double scale2 = 1.0;

	public LeistungDTO(Verrechnet verrechnet) throws ElexisException {

		if (!verrechnet.exists()) {
			throw new ElexisException(
					"Verrechnete Leistung wird ignoriert - Keine Leistung vorhanden [ID: " + verrechnet.getId() + "].",
					new Exception());
		}
		try {
			if (verrechnet.getLastUpdate() < 0) {
				throw new Exception();
			}
		} catch (Exception e) {
			throw new ElexisException(
					"Die verrechnete Leistung wird ignoriert - Datum der letzten Aktualisierung ist fehlerhaft [ID: "
							+ verrechnet.getId() + "].",
					e);
		}
		this.verrechnet = NoPoUtil.loadAsIdentifiable(verrechnet, IBilled.class).get();
		this.lastUpdate = verrechnet.getLastUpdate();
		this.id = verrechnet.getId();
		this.code = this.verrechnet.getCode();
		this.text = verrechnet.getText();
		this.tp = this.verrechnet.getPoints();
		this.tpw = this.verrechnet.getFactor();
		this.count = verrechnet.getZahl();
		this.iVerrechenbar = this.verrechnet.getBillable();
	}

	public LeistungDTO(IBillable iVerrechenbar, IFall fall) {
		this.lastUpdate = System.currentTimeMillis();
		this.id = iVerrechenbar.getId();
		this.code = iVerrechenbar.getCode();
		this.text = iVerrechenbar.getText();
		this.tp = -1;
		this.tpw = 1.0;
		this.scale1 = 1.0;
		this.scale2 = 1.0;
		this.count = 1;
		this.iVerrechenbar = iVerrechenbar;
	}

	public void calcPrice(KonsultationDTO konsultationDTO, FallDTO fallDTO) {
		if (verrechnet == null) {
			@SuppressWarnings("unchecked")
			Result<IBilled> result = iVerrechenbar.getOptifier().add(iVerrechenbar, konsultationDTO.getTransientCopy(),
					1.0, false);
			if (result.isOK()) {
				tp = result.get().getPoints();
				tpw = result.get().getFactor();
				scale1 = result.get().getPrimaryScaleFactor();
				scale2 = result.get().getSecondaryScaleFactor();
			}
		} else {
			tpw = getFactor();
			scale1 = verrechnet.getPrimaryScaleFactor();
			scale2 = verrechnet.getSecondaryScaleFactor();
		}
	}

	private double getFactor() {
		if (iVerrechenbar != null) {
			Optional<IBillingSystemFactor> billingFactor = BillingServiceHolder.get()
					.getBillingSystemFactor(iVerrechenbar.getCodeSystemName(), verrechnet.getEncounter().getDate());
			if (billingFactor.isPresent()) {
				return billingFactor.get().getFactor();
			}
		}
		return 1.0;
	}

	public void setTp(int tp) {
		this.tp = tp;
	}

	public void setScale2(double scale2) {
		this.scale2 = scale2;
	}

	public double getScale2() {
		return scale2;
	}

	public IBilled getVerrechnet() {
		return verrechnet;
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
		this.verrechnet = verrechnet;
	}

	public String getId() {
		return id;
	}

	public Money getPrice() {
		return new Money((int) (Math.round(tp * tpw) * scale1 * scale2 * count));
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getCount() {
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
}
