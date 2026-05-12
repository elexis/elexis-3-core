package ch.elexis.core.model;

import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;
import ch.elexis.core.model.util.internal.ModelUtil;

public class InvoiceBillRecordInfo
		extends AbstractIdModelAdapter<ch.elexis.core.jpa.entities.InvoiceBillRecordInfo>
		implements IdentifiableWithXid, IInvoiceBillRecordInfo {

	public InvoiceBillRecordInfo(ch.elexis.core.jpa.entities.InvoiceBillRecordInfo entity) {
		super(entity);
	}

	@Override
	public IInvoice getInvoice() {
		return ModelUtil.getAdapter(getEntity().getInvoice(), IInvoice.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setInvoice(IInvoice value) {
		if (value != null) {
			getEntityMarkDirty()
					.setInvoice(((AbstractIdModelAdapter<ch.elexis.core.jpa.entities.Invoice>) value).getEntity());
		} else {
			getEntityMarkDirty().setInvoice(null);
		}
	}

	@Override
	public IBilled getBilled() {
		return ModelUtil.getAdapter(getEntity().getBilled(), IBilled.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setBilled(IBilled value) {
		if (value != null) {
			getEntityMarkDirty()
					.setBilled(((AbstractIdModelAdapter<ch.elexis.core.jpa.entities.Verrechnet>) value).getEntity());
		} else {
			getEntityMarkDirty().setBilled(null);
		}
	}

	@Override
	public String getBillid() {
		return getEntity().getBillid();
	}

	@Override
	public void setBillid(String value) {
		getEntityMarkDirty().setBillid(value);
	}

	@Override
	public String getBillrecordid() {
		return getEntity().getBillrecordid();
	}

	@Override
	public void setBillrecordid(String value) {
		getEntityMarkDirty().setBillrecordid(value);
	}

	@Override
	public String getInfo() {
		return getEntity().getInfo();
	}

	@Override
	public void setInfo(String value) {
		getEntityMarkDirty().setInfo(value);
	}

	@Override
	public String getInfocode() {
		return getEntity().getInfocode();
	}

	@Override
	public void setInfocode(String value) {
		getEntityMarkDirty().setInfocode(value);
	}
}
