package ch.elexis.core.findings.util.fhir.transformer;

import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.r4.model.Invoice;
import org.osgi.service.component.annotations.Component;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.model.IInvoice;
import ch.elexis.core.services.IModelService;

@Component
public class InvoiceIInvoiceTransformer implements IFhirTransformer<Invoice, IInvoice> {

	@org.osgi.service.component.annotations.Reference(target = "(" + IModelService.SERVICEMODELNAME
			+ "=ch.elexis.core.model)")
	private IModelService modelService;

	@Override
	public Optional<Invoice> getFhirObject(IInvoice localObject, SummaryEnum summaryEnum, Set<Include> includes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<IInvoice> getLocalObject(Invoice fhirObject) {
		String id = fhirObject.getIdElement().getIdPart();
		if (id != null && !id.isEmpty()) {
			return modelService.load(id, IInvoice.class);
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
