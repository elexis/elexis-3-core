package ch.elexis.core.model.builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

import ch.elexis.core.model.ILabItem;
import ch.elexis.core.model.ILabOrder;
import ch.elexis.core.model.ILabResult;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.IModelService;

public class ILabResultBuilder extends AbstractBuilder<ILabResult> {
	
	private ILabOrder labOrder;
	
	public ILabResultBuilder(IModelService modelService, ILabItem labItem, IPatient patient){
		super(modelService);
		object = modelService.create(ILabResult.class);
		object.setItem(labItem);
		object.setPatient(patient);
		object.setDate(LocalDate.now());
	}
	
	public ILabResultBuilder result(String result){
		object.setResult(result);
		return this;
	}
	
	public ILabResultBuilder buildLabOrder(String orderId){
		labOrder = modelService.create(ILabOrder.class);
		labOrder.setResult(object);
		labOrder.setItem(object.getItem());
		labOrder.setOrderId(orderId);
		labOrder.setTimeStamp(LocalDateTime.now());
		return this;
	}
	
	@Override
	public ILabResult buildAndSave(){
		if (labOrder != null) {
			modelService.save(labOrder);
		}
		modelService.save(object);
		return object;
	}
	
}
