package ch.elexis.core.model;

import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.jpa.entities.Behandlung;
import ch.elexis.core.jpa.entities.EntityWithId;
import ch.elexis.core.jpa.entities.Kontakt;
import ch.elexis.core.jpa.entities.Verrechnet;
import ch.elexis.core.jpa.entities.VerrechnetCopy;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;
import ch.elexis.core.model.service.holder.StoreToStringServiceHolder;
import ch.elexis.core.model.util.internal.ModelUtil;
import ch.elexis.core.model.verrechnet.Constants;
import ch.elexis.core.services.IStoreToStringContribution;
import ch.rgw.tools.Money;

public class Billed extends AbstractIdDeleteModelAdapter<Verrechnet> implements IdentifiableWithXid, IBilled {

	private IBillable billable;

	public Billed(Verrechnet entity) {
		super(entity);
	}

	@Override
	public String getLabel() {
		return getText();
	}

	@Override
	public IBillable getBillable() {
		if (billable == null) {
			Optional<String> storeToString = getBillableStoreToString();
			if (storeToString.isPresent()) {
				billable = (IBillable) StoreToStringServiceHolder.get().loadFromString(storeToString.get())
						.orElse(null);
			}
		}
		return billable;
	}

	private Optional<String> getBillableStoreToString() {
		String billableClass = getEntity().getKlasse();
		String billableId = getEntity().getLeistungenCode();
		if (StringUtils.isNotBlank(billableClass) && StringUtils.isNotBlank(billableId)) {
			return Optional.of(billableClass + IStoreToStringContribution.DOUBLECOLON + billableId);
		}
		return Optional.empty();
	}

	@Override
	public void setBillable(IBillable value) {
		String storeToString = StoreToStringServiceHolder.get().storeToString(value)
				.orElseThrow(() -> new IllegalStateException("Could not get store to string for [" + value + "]"));
		String[] split = storeToString.split(IStoreToStringContribution.DOUBLECOLON);
		if (split.length > 1) {
			getEntityMarkDirty().setKlasse(split[0]);
			getEntityMarkDirty().setLeistungenCode(split[1]);
			billable = value;
		}
	}

	@Override
	public IEncounter getEncounter() {
		if (getEntity().getBehandlung() != null) {
			return ModelUtil.getAdapter(getEntity().getBehandlung(), IEncounter.class);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setEncounter(IEncounter value) {
		if (value instanceof AbstractIdModelAdapter) {
			// refresh existing
			if (getEncounter() != null) {
				addRefresh(getEncounter());
			}
			Behandlung valueEntity = ((AbstractIdModelAdapter<Behandlung>) value).getEntity();
			getEntityMarkDirty().setBehandlung(valueEntity);
			addRefresh(value);
		}
	}

	@Override
	public double getAmount() {
		if (getSecondaryScale() == 100) {
			return getEntity().getZahl();
		}
		return getSecondaryScale() / 100d;
	}

	@Override
	public void setAmount(double value) {
		if (value % 1 == 0) {
			// integer
			getEntityMarkDirty().setZahl((int) value);
			setSecondaryScale(100);
		} else {
			// double
			getEntityMarkDirty().setZahl(1);
			int scale2 = (int) Math.round(value * 100);
			setSecondaryScale(scale2);
		}
	}

	@Override
	public Money getPrice() {
		return new Money(getPoints()).multiply(getFactor());
	}

	@Override
	public void setPrice(Money value) {
		setExtInfo(Constants.FLD_EXT_CHANGEDPRICE, "true");
		setPoints(value.getCents());
		setSecondaryScale(100);
	}

	@Override
	public Money getScaledPrice() {
		// do not include secondary as it is either 1 or the amount
		int cents = Math.toIntExact(Math.round(getPoints() * getFactor() * getPrimaryScaleFactor()));
		return new Money(cents);
	}

	@Override
	public Money getNetPrice() {
		return new Money(getEntity().getEk_kosten());
	}

	@Override
	public void setNetPrice(Money value) {
		if (value != null) {
			getEntityMarkDirty().setEk_kosten(value.getCents());
		} else {
			getEntityMarkDirty().setEk_kosten(0);
		}
	}

	@Override
	public String getText() {
		return getEntity().getLeistungenText();
	}

	@Override
	public void setText(String value) {
		getEntityMarkDirty().setLeistungenText(value);

	}

	@Override
	public int getPoints() {
		return getEntity().getVk_tp();
	}

	@Override
	public void setPoints(int value) {
		getEntityMarkDirty().setVk_tp(value);
	}

	@Override
	public double getFactor() {
		String scaleString = getEntity().getVk_scale();
		if (scaleString != null && !scaleString.isEmpty()) {
			try {
				return Double.parseDouble(scaleString);
			} catch (NumberFormatException e) {
				// ignore
			}
		}
		return 1.0;
	}

	@Override
	public void setFactor(double value) {
		getEntityMarkDirty().setVk_scale(Double.toString(value));
	}

	@Override
	public int getPrimaryScale() {
		return getEntity().getScale();
	}

	@Override
	public void setPrimaryScale(int value) {
		getEntityMarkDirty().setScale(value);
	}

	@Override
	public int getSecondaryScale() {
		return getEntity().getScale2();
	}

	@Override
	public void setSecondaryScale(int value) {
		getEntityMarkDirty().setScale2(value);
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
	public String getCode() {
		IBillable billable = getBillable();
		return billable != null ? billable.getCode() : getBillableStoreToString().orElse("?");
	}

	@Override
	public Money getTotal() {
		// do not use getAmount here, as the changed amount is included via secondary
		// scale#
		// get sales for the verrechnet including all scales and quantity
		// replaced with toIntExact and round: new DecimalFormat("#").parse(new
		// DecimalFormat("#").format(value)).doubleValue()
		// special handling for swiss specific AL TL based billed
		int cents = 0;
		if (isALTL()) {
			long roundedAmount = Math.round(getAL() * getFactor() * getEntity().getZahl())
					+ Math.round(getTL() * getFactor() * getEntity().getZahl());
			cents = Math
					.toIntExact(Math.round(roundedAmount * getPrimaryScaleFactor() * getSecondaryScaleFactor()));
		} else {
			cents = Math.toIntExact(Math.round(getPoints() * getFactor() * getPrimaryScaleFactor()
					* getSecondaryScaleFactor() * getEntity().getZahl()));
		}
		return new Money(cents);
	}

	private boolean isALTL() {
		String className = getEntity().getKlasse();
		return className != null && !className.isEmpty()
				&& (className.endsWith("TarmedLeistung") || className.endsWith("TardocLeistung"));
	}

	private double getAL() {
		// if price was changed, use TP as AL
		boolean changedPrice = isChangedPrice();
		if (changedPrice) {
			return getPoints();
		}
		String alString = (String) getExtInfo(Verrechnet.EXT_VERRRECHNET_AL);
		if (alString != null) {
			try {
				return (int) Double.parseDouble(alString);
			} catch (NumberFormatException ne) {
				// ignore
			}
		}
		return 0;
	}

	public double getTL() {
		// if price was changed to 0, use TP as TL
		boolean changedPrice = isChangedPrice();
		if (changedPrice && getPoints() == 0) {
			return getPoints();
		}
		String tlString = (String) getExtInfo(Verrechnet.EXT_VERRRECHNET_TL);
		if (tlString != null) {
			try {
				return (int) Double.parseDouble(tlString);
			} catch (NumberFormatException ne) {
				// ignore
			}
		}
		return 0;
	}

	@Override
	public boolean isChangedPrice() {
		Object changedPrice = getExtInfo(Constants.FLD_EXT_CHANGEDPRICE);
		if (changedPrice instanceof String) {
			return ((String) changedPrice).equalsIgnoreCase("true");
		} else if (changedPrice instanceof Boolean) {
			return (Boolean) changedPrice;
		}
		return false;
	}

	@Override
	public boolean isNonIntegerAmount() {
		if (isChangedPrice()) {
			return false;
		} else {
			return getSecondaryScale() != 100;
		}
	}

	@Override
	public double getPrimaryScaleFactor() {
		if (getPrimaryScale() == 0) {
			return 1.0;
		}
		return (getPrimaryScale()) / 100.0;
	}

	@Override
	public double getSecondaryScaleFactor() {
		if (getSecondaryScale() == 0) {
			return 1.0;
		}
		return (getSecondaryScale()) / 100.0;
	}

	@Override
	public IContact getBiller() {
		return ModelUtil.getAdapter(getEntity().getUser(), IContact.class, true);
	}

	@Override
	public void setBiller(IContact value) {
		if (value instanceof AbstractIdDeleteModelAdapter) {
			getEntityMarkDirty().setUser((Kontakt) ((AbstractIdDeleteModelAdapter<?>) value).getEntity());
		} else if (value == null) {
			getEntityMarkDirty().setUser(null);
		}
	}

	@Override
	public void copy(IBilled to) {
		if (to instanceof AbstractIdDeleteModelAdapter) {
			// IInvoiceBilled do not support set operations, so copy properties of the
			// entities
			EntityWithId toEntity = ((AbstractIdDeleteModelAdapter<?>) to).getEntity();
			if (toEntity instanceof VerrechnetCopy) {
				VerrechnetCopy verrechnetCopy = (VerrechnetCopy) toEntity;
				verrechnetCopy.setKlasse(getEntity().getKlasse());
				verrechnetCopy.setLeistungenCode(getEntity().getLeistungenCode());
				verrechnetCopy.setLeistungenText(getEntity().getLeistungenText());
				verrechnetCopy.setZahl(getEntity().getZahl());
				verrechnetCopy.setEk_kosten(getEntity().getEk_kosten());
				verrechnetCopy.setVk_tp(getEntity().getVk_tp());
				verrechnetCopy.setVk_scale(getEntity().getVk_scale());
				verrechnetCopy.setVk_preis(getEntity().getVk_preis());
				verrechnetCopy.setScale(getEntity().getScale());
				verrechnetCopy.setScale2(getEntity().getScale2());

				verrechnetCopy.setBehandlung(getEntity().getBehandlung());
				verrechnetCopy.setExtInfo(getEntity().getExtInfo());
				verrechnetCopy.setUser(getEntity().getUser());
			} else if (toEntity instanceof Verrechnet) {
				Verrechnet verrechnet = (Verrechnet) toEntity;
				verrechnet.setKlasse(getEntity().getKlasse());
				verrechnet.setLeistungenCode(getEntity().getLeistungenCode());
				verrechnet.setLeistungenText(getEntity().getLeistungenText());
				verrechnet.setZahl(getEntity().getZahl());
				verrechnet.setEk_kosten(getEntity().getEk_kosten());
				verrechnet.setVk_tp(getEntity().getVk_tp());
				verrechnet.setVk_scale(getEntity().getVk_scale());
				verrechnet.setVk_preis(getEntity().getVk_preis());
				verrechnet.setScale(getEntity().getScale());
				verrechnet.setScale2(getEntity().getScale2());

				verrechnet.setBehandlung(getEntity().getBehandlung());
				verrechnet.setExtInfo(getEntity().getExtInfo());
				verrechnet.setUser(getEntity().getUser());
			}
		}
	}

	@Override
	public String toString() {
		return super.toString() + StringUtils.SPACE + getLabel();
	}
}
