package ch.elexis.core.findings.util.fhir.transformer;

import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.ChargeItem;
import org.hl7.fhir.r4.model.ChargeItem.ChargeItemStatus;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Quantity;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.transformer.helper.CodeSystemUtil;
import ch.elexis.core.findings.util.fhir.transformer.helper.FhirUtil;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IBillable;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.services.IModelService;

@Component
public class ChargeItemIBilledTransformer implements IFhirTransformer<ChargeItem, IBilled> {
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService coreModelService;
	
	@Override
	public Optional<ChargeItem> getFhirObject(IBilled localObject, SummaryEnum summaryEnum,
		Set<Include> includes){
		
		ChargeItem chargeItem = new ChargeItem();
		chargeItem.setId(new IdDt("ChargeItem", localObject.getId()));
		
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
			//			chargeItem.setProduct(code);
		}
		
		String codeSystemCodeValue = localObject.getCode();
		String displayName = localObject.getText();
		String codeSystemCode = (billable != null) ? billable.getCodeSystemCode() : null;
		String codeSystemName = (billable != null) ? billable.getCodeSystemName() : null;
		code.addCoding(CodeSystemUtil.getElexisCodeSystemCoding(codeSystemCode, codeSystemName,
			codeSystemCodeValue, displayName));
		
		chargeItem.setQuantity(new Quantity(localObject.getAmount()));
		
		return Optional.of(chargeItem);
	}
	
	@Override
	public Optional<IBilled> getLocalObject(ChargeItem fhirObject){
		String id = fhirObject.getIdElement().getIdPart();
		if (id != null && !id.isEmpty()) {
			return coreModelService.load(id, IBilled.class);
		}
		return Optional.empty();
	}
	
	@Override
	public Optional<IBilled> updateLocalObject(ChargeItem fhirObject, IBilled localObject){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Optional<IBilled> createLocalObject(ChargeItem fhirObject){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz){
		return ChargeItem.class.equals(fhirClazz) && IBilled.class.equals(localClazz);
	}
	
}
