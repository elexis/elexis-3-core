package ch.elexis.core.findings.util.fhir.transformer.mapper;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.hl7.fhir.r4.model.ChargeItem;
import org.hl7.fhir.r4.model.Invoice;
import org.hl7.fhir.r4.model.Invoice.InvoiceLineItemComponent;
import org.hl7.fhir.r4.model.Invoice.InvoiceLineItemPriceComponentComponent;
import org.hl7.fhir.r4.model.Invoice.InvoicePriceComponentType;
import org.hl7.fhir.r4.model.Invoice.InvoiceStatus;
import org.hl7.fhir.r4.model.Reference;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.transformer.helper.FhirUtil;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IInvoice;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.InvoiceState;
import ch.elexis.core.time.TimeUtil;

/**
 * @see https://hl7.org/fhir/R4/invoice.html
 */
public class IInvoiceInvoiceAttributeMapper implements IdentifiableDomainResourceAttributeMapper<IInvoice, Invoice> {

	private IFhirTransformer<ChargeItem, IBilled> chargeItemBilledTransformer;

	public IInvoiceInvoiceAttributeMapper(IFhirTransformer<ChargeItem, IBilled> chargeItemBilledTransformer) {
		this.chargeItemBilledTransformer = chargeItemBilledTransformer;
	}

	@SuppressWarnings("unused")
	private IInvoiceInvoiceAttributeMapper() {
	};

	@Override
	public void elexisToFhir(IInvoice source, Invoice target, SummaryEnum summaryEnum, Set<Include> includes) {
		target.setId(new IdDt(Invoice.class.getSimpleName(), source.getId()));
		mapMetaData(source, target);

		target.setStatus(toInvoiceStatus(source.getState()));
		// cancelledReason ?
		// type ?
		ICoverage coverage = source.getCoverage();
		IPatient patient = coverage != null ? coverage.getPatient() : null;
		target.setSubject(FhirUtil.getReference(patient));
		// recipient ? depends on insurance?
		target.setDate(TimeUtil.toDate(source.getDate()));
		// participant
		// issuer -> self
		// account ?

		ch.rgw.tools.Money sumTotal = new ch.rgw.tools.Money(0);
		List<IBilled> billed = source.getBilled();
		for (IBilled iBilled : billed) {
			target.addLineItem(toInvoiceLineItemComponent(iBilled, includes, sumTotal));
		}
		// totalPriceComponent
		// totalNet
		target.setTotalGross(FhirUtil.toFhir(sumTotal));
		// paymentGerms
		// note
	}

	private InvoiceStatus toInvoiceStatus(InvoiceState state) {
		// TODO more
		switch (state) {
		case CANCELLED:
			return InvoiceStatus.CANCELLED;
		case BILLED:
		case OPEN:
		case OPEN_AND_PRINTED:
			return InvoiceStatus.ISSUED;
		case PAID:
			return InvoiceStatus.BALANCED;
		default:
			return InvoiceStatus.DRAFT;
		}
	}

	public InvoiceLineItemComponent toInvoiceLineItemComponent(IBilled iBilled, Set<Include> includes,
			ch.rgw.tools.Money sum) {

		InvoiceLineItemComponent ilic = new InvoiceLineItemComponent(FhirUtil.getReference(iBilled));

		if (includes.contains(new Include("Invoice.lineItem.chargeItem"))) {
			ChargeItem chargeItem = chargeItemBilledTransformer.getFhirObject(iBilled).get();
			((Reference) ilic.getChargeItem()).setResource(chargeItem);
		}

		InvoiceLineItemPriceComponentComponent ilipcc = new InvoiceLineItemPriceComponentComponent();
		ilic.setPriceComponent(Collections.singletonList(ilipcc));

		ilipcc.setType(InvoicePriceComponentType.BASE);
		// code
		ilipcc.setFactor(iBilled.getFactor());

		// VerrechnungsDisplay#updateBilledLabel
		ch.rgw.tools.Money total = iBilled.getTotal();
		ilipcc.setAmount(FhirUtil.toFhir(total));

		sum.addMoney(total);
		return ilic;
	}

	@Override
	public void fhirToElexis(Invoice source, IInvoice target) {
		throw new UnsupportedOperationException();
	}

}
