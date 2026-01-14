package ch.elexis.core.findings.util.fhir.transformer;

import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.r4.model.ChargeItem;
import org.hl7.fhir.r4.model.Invoice;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.fhir.mapper.r4.IInvoiceInvoiceAttributeMapper;
import ch.elexis.core.fhir.mapper.r4.util.FhirUtil;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.IInvoice;
import ch.elexis.core.services.IModelService;

@Component
public class InvoiceIInvoiceTransformer implements IFhirTransformer<Invoice, IInvoice> {

	@org.osgi.service.component.annotations.Reference(target = "(" + IModelService.SERVICEMODELNAME
			+ "=ch.elexis.core.model)")
	private IModelService coreModelService;

	@org.osgi.service.component.annotations.Reference(target = "(" + IFhirTransformer.TRANSFORMERID
			+ "=ChargeItem.IBilled)")
	private IFhirTransformer<ChargeItem, IBilled> chargeItemTransformer;

	private IInvoiceInvoiceAttributeMapper attributeMapper;

	@Activate
	private void activate() {
		attributeMapper = new IInvoiceInvoiceAttributeMapper();
	}

	@Override
	public Optional<Invoice> getFhirObject(IInvoice localObject, SummaryEnum summaryEnum, Set<Include> includes) {
		Invoice invoice = new Invoice();
		attributeMapper.elexisToFhir(localObject, invoice, summaryEnum);
		return Optional.of(invoice);
	}

	@Override
	public Optional<IInvoice> getLocalObject(Invoice fhirObject) {
		if (fhirObject != null && fhirObject.getId() != null) {
			Optional<String> localId = FhirUtil.getLocalId(fhirObject.getId());
			if (localId.isPresent()) {
				return coreModelService.load(localId.get(), IInvoice.class);
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<IInvoice> updateLocalObject(Invoice fhirObject, IInvoice localObject) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Optional<IInvoice> createLocalObject(Invoice fhirObject) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return Invoice.class.equals(fhirClazz) && IInvoice.class.equals(localClazz);
	}

}
