package ch.elexis.core.findings.util.fhir.transformer;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.ChargeItem;
import org.hl7.fhir.r4.model.ChargeItem.ChargeItemPerformerComponent;
import org.hl7.fhir.r4.model.ChargeItem.ChargeItemStatus;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Quantity;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.fhir.mapper.r4.helper.CodeSystemUtil;
import ch.elexis.core.fhir.mapper.r4.util.FhirUtil;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.IFhirTransformerException;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IBillable;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.services.IBillingService;
import ch.elexis.core.services.ICodeElementService;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.status.StatusUtil;
import ch.rgw.tools.Result;

@Component(property = IFhirTransformer.TRANSFORMERID + "=ChargeItem.IBilled")
public class ChargeItemIBilledTransformer implements IFhirTransformer<ChargeItem, IBilled> {

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService coreModelService;

	@Reference
	private IBillingService billingService;

	@Reference
	private ICodeElementService codeElementService;

	@Reference
	private IContextService contextService;

	@Override
	public Optional<ChargeItem> getFhirObject(IBilled localObject, SummaryEnum summaryEnum, Set<Include> includes) {

		ChargeItem chargeItem = new ChargeItem();
		chargeItem.setId(new IdDt("ChargeItem", localObject.getId(), Long.toString(localObject.getLastupdate())));

		chargeItem.setStatus(ChargeItemStatus.BILLED);

		chargeItem.setContext(FhirUtil.getReference(localObject.getEncounter()));

		CodeableConcept code = new CodeableConcept();
		chargeItem.setCode(code);

		IBillable billable = localObject.getBillable();
		if (billable instanceof IArticle) {
			IArticle article = ((IArticle) billable);
			String gtin = article.getGtin();
			if (StringUtils.isNotBlank(gtin)) {
				code.addCoding(CodeSystemUtil.getGtinCoding(gtin));
			}
			// chargeItem.setProduct(code);
		}

		code.addCoding(CodeSystemUtil.getCodeElementCoding(codeElementService, billable));

		chargeItem.setQuantity(new Quantity(localObject.getAmount()));

		return Optional.of(chargeItem);
	}

	@Override
	public Optional<IBilled> getLocalObject(ChargeItem fhirObject) {
		String id = fhirObject.getIdElement().getIdPart();
		if (id != null && !id.isEmpty()) {
			return coreModelService.load(id, IBilled.class);
		}
		return Optional.empty();
	}

	@Override
	public Optional<IBilled> updateLocalObject(ChargeItem fhirObject, IBilled localObject) {

		assertMandator(fhirObject, localObject.getEncounter().getMandator());

		Quantity quantity = fhirObject.getQuantity();
		IStatus status = billingService.changeAmountValidated(localObject, quantity.getValue().doubleValue());
		if (!status.isOK()) {
			throw new IFhirTransformerException(StatusUtil.getSeverityString(status.getSeverity()), status.getMessage(),
					status.getCode());
		}

		// TODO what is updatable?
		// TODO lock

		return Optional.of(localObject);
	}

	@Override
	public Optional<IBilled> createLocalObject(ChargeItem fhirObject) {

		IEncounter encounter = assertEncounter(fhirObject);
		assertMandator(fhirObject, encounter.getMandator());
		IBillable billable = assertBillable(fhirObject);

		// TODO locking?
		Result<IBilled> result = billingService.bill(billable, encounter,
				fhirObject.getQuantity().getValue().doubleValue());
		if (!result.isOK()) {
			throw new IFhirTransformerException(result.getSeverity().name(), result.getCombinedMessages(),
					result.getCode());
		}

		return Optional.of(result.get());
	}

	private void assertMandator(ChargeItem fhirObject, IMandator encounterMandator) {
		List<ChargeItemPerformerComponent> performers = fhirObject.getPerformer();
		if (performers.isEmpty()) {
			if (encounterMandator != null) {
				contextService.setActiveMandator(encounterMandator);
				return;
			} else {
				throw new IFhirTransformerException("WARNING", "No performer set or available via encounter", 0);
			}
		}

		String mandatorId = performers.get(0).getActor().getReferenceElement().getIdPart();
		IMandator mandator = coreModelService.load(mandatorId, IMandator.class).orElse(null);
		if (mandator == null) {
			throw new IFhirTransformerException("WARNING", "Unresolvable mandator", 0);
		}
		contextService.setActiveMandator(mandator);
	}

	private IBillable assertBillable(ChargeItem fhirObject) {
		Optional<ICodeElement> iCodeElement = CodeSystemUtil.loadCodeElementEntryInCodeableConcept(codeElementService,
				fhirObject.getCode());
		if (iCodeElement.isEmpty()) {
			throw new IFhirTransformerException("WARNING", "No codeElement found", 412);
		}

		ICodeElement _iCodeElement = iCodeElement.get();
		if (!(_iCodeElement instanceof IBillable)) {
			throw new IFhirTransformerException("WARNING", "Non-billable codeElement found", 412);
		}
		return (IBillable) _iCodeElement;
	}

	private IEncounter assertEncounter(ChargeItem fhirObject) {
		IIdType referenceElement = fhirObject.getContext().getReferenceElement();
		String encounterId = referenceElement.getIdPart();
		if (StringUtils.isBlank(encounterId)) {
			// currently context is encounter only
			throw new IFhirTransformerException("WARNING", "Missing encounter parameter", 412);
		}

		IEncounter encounter = coreModelService.load(encounterId, IEncounter.class).orElse(null);
		if (encounter == null) {
			throw new IFhirTransformerException("WARNING", "Invalid encounter", 412);
		}
		return encounter;
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return ChargeItem.class.equals(fhirClazz) && IBilled.class.equals(localClazz);
	}

}
