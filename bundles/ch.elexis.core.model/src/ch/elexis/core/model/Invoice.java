package ch.elexis.core.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import ch.elexis.core.jpa.entities.Fall;
import ch.elexis.core.jpa.entities.Kontakt;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;
import ch.elexis.core.jpa.model.adapter.mixin.ExtInfoHandler;
import ch.elexis.core.model.InvoiceState.REJECTCODE;
import ch.elexis.core.model.service.holder.CoreModelServiceHolder;
import ch.elexis.core.model.util.internal.ModelUtil;
import ch.elexis.core.services.INamedQuery;
import ch.rgw.tools.Money;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class Invoice extends AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.Invoice>
		implements IdentifiableWithXid, IInvoice {
	
	public static final String REJECTED = "Zurückgewiesen";
	
	private ExtInfoHandler extInfoHandler;
	
	public Invoice(ch.elexis.core.jpa.entities.Invoice entity){
		super(entity);
		extInfoHandler = new ExtInfoHandler(this);
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
	public InvoiceState getState(){
		return getEntity().getState();
	}
	
	@Override
	public void setState(InvoiceState value){
		getEntityMarkDirty().setState(value);
	}
	
	@Override
	public String getNumber(){
		return getEntity().getNumber();
	}
	
	@Override
	public IMandator getMandator(){
		return ModelUtil.getAdapter(getEntity().getMandator(), IMandator.class);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void setMandator(IMandator value){
		if (value != null) {
			getEntityMarkDirty().setMandator(((AbstractIdModelAdapter<Kontakt>) value).getEntity());
		} else {
			getEntityMarkDirty().setMandator(null);
		}
	}
	
	@Override
	public ICoverage getCoverage(){
		return ModelUtil.getAdapter(getEntity().getFall(), ICoverage.class);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void setCoverage(ICoverage value){
		if (value != null) {
			getEntityMarkDirty().setFall(((AbstractIdModelAdapter<Fall>) value).getEntity());
		} else {
			getEntityMarkDirty().setFall(null);
		}
	}
	
	@Override
	public List<IEncounter> getEncounters(){
		CoreModelServiceHolder.get().refresh(this);
		return getEntity().getEncounters().parallelStream().filter(b -> !b.isDeleted())
			.map(b -> ModelUtil.getAdapter(b, IEncounter.class, true))
			.collect(Collectors.toList());
	}
	
	@Override
	public List<IBilled> getBilled(){
		CoreModelServiceHolder.get().refresh(this);
		return getEntity().getInvoiceBilled().parallelStream().filter(b -> !b.isDeleted())
			.map(b -> ModelUtil.getAdapter(b, IInvoiceBilled.class, true))
			.collect(Collectors.toList());
	}
	
	@Override
	public void addTrace(String name, String value){
		List<String> trace = getTrace(name);
		trace.add(new TimeTool().toString(TimeTool.FULL_GER) + ": " + value);
		extInfoHandler.setExtInfo(name, StringTool.pack(trace));
	}
	
	@Override
	public List<String> getTrace(String name){
		byte[] raw = (byte[]) extInfoHandler.getExtInfo(name);
		List<String> trace = null;
		if (raw != null) {
			trace = StringTool.unpack(raw);
		}
		if (trace == null) {
			trace = new ArrayList<String>();
		}
		return trace;
	}
	
	@Override
	public LocalDate getDate(){
		return getEntity().getInvoiceDate();
	}
	
	@Override
	public void setDate(LocalDate value){
		getEntityMarkDirty().setInvoiceDate(value);
	}
	
	@Override
	public LocalDate getDateFrom(){
		return getEntity().getInvoiceDateFrom();
	}
	
	@Override
	public void setDateFrom(LocalDate value){
		getEntityMarkDirty().setInvoiceDateFrom(value);
	}
	
	@Override
	public LocalDate getDateTo(){
		return getEntity().getInvoiceDateTo();
	}
	
	@Override
	public void setDateTo(LocalDate value){
		getEntityMarkDirty().setInvoiceDateTo(value);
	}
	
	@Override
	public Money getTotalAmount(){
		return ch.elexis.core.model.util.ModelUtil.getMoneyForCentString(getEntity().getAmount())
			.orElse(null);
	}
	
	@Override
	public void setTotalAmount(Money value){
		if (value != null) {
			getEntityMarkDirty().setAmount(value.getCentsAsString());
		} else {
			getEntityMarkDirty().setAmount(null);
		}
	}
	
	@Override
	public Money getOpenAmount(){
		List<IPayment> payments = getPayments();
		Money total = getTotalAmount();
		for (IPayment payment : payments) {
			Money paymentAmount = payment.getAmount();
			total.subtractMoney(paymentAmount);
		}
		return new Money(total);
	}
	
	@Override
	public Money getPayedAmount(){
		List<IPayment> payments = getPayments();
		Money total = new Money();
		for (IPayment payment : payments) {
			Money paymentAmount = payment.getAmount();
			if (!paymentAmount.isNegative()) {
				total.addMoney(paymentAmount);
			}
		}
		return total;
	}
	
	@Override
	public List<IPayment> getPayments(){
		CoreModelServiceHolder.get().refresh(this);
		return getEntity().getPayments().parallelStream().filter(p -> !p.isDeleted())
			.map(p -> ModelUtil.getAdapter(p, IPayment.class, true)).collect(Collectors.toList());
	}
	
	@Override
	public String getRemark(){
		return (String) getExtInfo(ch.elexis.core.jpa.entities.Invoice.REMARK);
	}
	
	@Override
	public void setRemark(String value){
		setExtInfo(ch.elexis.core.jpa.entities.Invoice.REMARK, value);
	}
	
	/**
	 * Since different ouputters can use different rules for rounding, the sum of the bill that an
	 * outputter created might be different from the sum, the Rechnung#build method calculated. So
	 * an outputter should always use setBetrag to correct the final amount. If the difference
	 * between the internal calculated amount and the outputter's result is more than 5 currency
	 * units or more than 2% of the sum, this method will return false an will not set the new
	 * value. Otherwise, the new value will be set, the account will be adjusted and the method
	 * returns true
	 * 
	 * @param betrag
	 *            new new sum
	 * @return true on success
	 */
	@Override
	public boolean adjustAmount(Money value){
		int oldVal = getTotalAmount().getCents();
		if (oldVal != 0) {
			int newVal = value.getCents();
			int diff = Math.abs(oldVal - newVal);
			
			if ((diff > 500) || ((diff * 50) > oldVal)) {
				return false;
			}
			INamedQuery<IAccountTransaction> query =
				CoreModelServiceHolder.get().getNamedQuery(IAccountTransaction.class, "invoice");
			List<IAccountTransaction> openTransactions =
				query.executeWithParameters(query.getParameterMap("invoice", this));
			// filter open transactions
			openTransactions =
				openTransactions.stream().filter(transaction -> transaction.getPayment() == null)
					.collect(Collectors.toList());
			
			if ((openTransactions != null) && (openTransactions.size() == 1)) {
				IAccountTransaction openTransaction = openTransactions.get(0);
				Money negBetrag = new Money(value);
				negBetrag.negate();
				openTransaction.setAmount(negBetrag);
			}
		}
		setTotalAmount(value);
		return true;
	}
	
	@Override
	public void reject(REJECTCODE rejectCode, String message){
		setState(InvoiceState.DEFECTIVE);
		addTrace(REJECTED, rejectCode.toString() + ", " + message);
	}
	
	@Override
	public Money getDemandAmount(){
		Money ret = new Money(0);
		for (IPayment payment : getPayments()) {
			String comment = payment.getRemark();
			if (comment.equals(ch.elexis.core.l10n.Messages.Rechnung_Mahngebuehr1)
				|| comment.equals(ch.elexis.core.l10n.Messages.Rechnung_Mahngebuehr2)
				|| comment.equals(ch.elexis.core.l10n.Messages.Rechnung_Mahngebuehr3)) {
				ret.addMoney(payment.getAmount());
			}
		}
		return ret.isNegative() ? ret.multiply(-1d) : ret;
	}

	@Override
	public LocalDate getStateDate(){
		return getEntity().getStatusDate();
	}

	@Override
	public void setStateDate(LocalDate value){
		getEntityMarkDirty().setStatusDate(value);
	}
}
