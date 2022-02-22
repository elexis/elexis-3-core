package ch.elexis.core.findings.util.fhir.transformer;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.r4.model.ChargeItem;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Invoice;
import org.hl7.fhir.r4.model.Invoice.InvoiceLineItemComponent;
import org.hl7.fhir.r4.model.Invoice.InvoiceLineItemPriceComponentComponent;
import org.hl7.fhir.r4.model.Invoice.InvoicePriceComponentType;
import org.hl7.fhir.r4.model.Invoice.InvoiceStatus;
import org.hl7.fhir.r4.model.Reference;
import org.osgi.service.component.annotations.Component;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.transformer.helper.FhirUtil;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.services.IModelService;

/**
 * Transforms an {@link IEncounter} into an {@link Invoice} of {@link Invoice#TYPE=encounter-only}.
 * This Invoice does not really exist, it is only a depiction of the current "billing state" of an
 * Encounter. The resp. Encounter is referenced via {@link Invoice#getSubject()}.
 */
@Component
public class InvoiceIEncounterTransformer implements IFhirTransformer<Invoice, IEncounter> {
	
	@org.osgi.service.component.annotations.Reference(target = "(" + IModelService.SERVICEMODELNAME
		+ "=ch.elexis.core.model)")
	private IModelService coreModelService;
	
	@org.osgi.service.component.annotations.Reference(target = "(" + IFhirTransformer.TRANSFORMERID
		+ "=ChargeItem.IBilled)")
	private IFhirTransformer<ChargeItem, IBilled> chargeItemTransformer;
	
	private final CodeableConcept TYPE_VIRTUAL =
		new CodeableConcept(new Coding("", "encounter-only", ""));
	
	@Override
	public Optional<Invoice> getFhirObject(IEncounter localObject, SummaryEnum summaryEnum,
		Set<Include> includes){
		
		Invoice invoice = new Invoice();
		invoice.setId(new IdDt("Invoice", "virtual." + localObject.getId()));
		
		invoice.setStatus(InvoiceStatus.DRAFT);
		invoice.setType(TYPE_VIRTUAL);
		invoice.setSubject(FhirUtil.getReference(localObject.getPatient()));
		
		ch.rgw.tools.Money sumTotal = new ch.rgw.tools.Money(0);
		
		List<IBilled> billed = localObject.getBilled();
		for (IBilled iBilled : billed) {
			invoice.addLineItem(toInvoiceLineItemComponent(iBilled, includes, sumTotal));
		}
		
		invoice.setTotalGross(FhirUtil.toFhir(sumTotal));
		
		return Optional.of(invoice);
	}
	
	private InvoiceLineItemComponent toInvoiceLineItemComponent(IBilled iBilled,
		Set<Include> includes, ch.rgw.tools.Money sum){
		
		InvoiceLineItemComponent ilic =
			new InvoiceLineItemComponent(FhirUtil.getReference(iBilled));
		
		if (includes.contains(new Include("Invoice.lineItem.chargeItem"))) {
			ChargeItem chargeItem = chargeItemTransformer.getFhirObject(iBilled).get();
			((Reference) ilic.getChargeItem()).setResource(chargeItem);
		}
		
		InvoiceLineItemPriceComponentComponent ilipcc =
			new InvoiceLineItemPriceComponentComponent();
		ilipcc.setType(InvoicePriceComponentType.BASE);
		
		// VerrechnungsDisplay#updateBilledLabel
		ch.rgw.tools.Money total = iBilled.getTotal();
		sum.addMoney(total);
		ilipcc.setAmount(FhirUtil.toFhir(total));
		ilipcc.setFactor(iBilled.getFactor());
		
		ilic.setPriceComponent(Collections.singletonList(ilipcc));
		
		return ilic;
	}
	
	@Override
	public Optional<IEncounter> getLocalObject(Invoice fhirObject){
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Optional<IEncounter> updateLocalObject(Invoice fhirObject, IEncounter localObject){
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Optional<IEncounter> createLocalObject(Invoice fhirObject){
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz){
		return Invoice.class.equals(fhirClazz) && IEncounter.class.equals(localClazz);
	}
	
}
