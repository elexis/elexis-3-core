package ch.elexis.core.findings.util.fhir.transformer;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.ChargeItem;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Invoice;
import org.hl7.fhir.r4.model.Invoice.InvoiceStatus;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.fhir.mapper.r4.IInvoiceInvoiceAttributeMapper;
import ch.elexis.core.fhir.mapper.r4.util.FhirUtil;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.services.IModelService;

/**
 * Transforms an {@link IEncounter} into an {@link Invoice} of
 * {@link Invoice#TYPE=encounter-only}. This Invoice does not really exist, it
 * is only a depiction of the current "billing state" of an Encounter. The resp.
 * Encounter is referenced via {@link Invoice#getSubject()}.
 */
@Component
public class InvoiceIEncounterTransformer implements IFhirTransformer<Invoice, IEncounter> {

	@org.osgi.service.component.annotations.Reference(target = "(" + IModelService.SERVICEMODELNAME
			+ "=ch.elexis.core.model)")
	private IModelService coreModelService;

	@org.osgi.service.component.annotations.Reference(target = "(" + IFhirTransformer.TRANSFORMERID
			+ "=ChargeItem.IBilled)")
	private IFhirTransformer<ChargeItem, IBilled> chargeItemTransformer;

	private IInvoiceInvoiceAttributeMapper attributeMapper;

	public InvoiceIEncounterTransformer() {
	}

	@Activate
	public void activate() {
		attributeMapper = new IInvoiceInvoiceAttributeMapper();
	}

	private final CodeableConcept TYPE_VIRTUAL = new CodeableConcept(
			new Coding(StringUtils.EMPTY, "encounter-only", StringUtils.EMPTY));

	@Override
	public Optional<Invoice> getFhirObject(IEncounter localObject, SummaryEnum summaryEnum, Set<Include> includes) {

		Invoice invoice = new Invoice();
		invoice.setId(new IdDt("Invoice", "virtual." + localObject.getId()));

		invoice.setStatus(InvoiceStatus.DRAFT);
		invoice.setType(TYPE_VIRTUAL);
		invoice.setSubject(FhirUtil.getReference(localObject.getPatient()));

		ch.rgw.tools.Money sumTotal = new ch.rgw.tools.Money(0);

		List<IBilled> billed = localObject.getBilled();
		for (IBilled iBilled : billed) {
			invoice.addLineItem(attributeMapper.toInvoiceLineItemComponent(iBilled, sumTotal));
		}

		invoice.setTotalGross(FhirUtil.toFhir(sumTotal));

		return Optional.of(invoice);
	}

	@Override
	public Optional<IEncounter> getLocalObject(Invoice fhirObject) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Optional<IEncounter> updateLocalObject(Invoice fhirObject, IEncounter localObject) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Optional<IEncounter> createLocalObject(Invoice fhirObject) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return Invoice.class.equals(fhirClazz) && IEncounter.class.equals(localClazz);
	}

}
