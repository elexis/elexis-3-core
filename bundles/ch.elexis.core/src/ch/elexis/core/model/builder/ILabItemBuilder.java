package ch.elexis.core.model.builder;

import java.util.ArrayList;
import java.util.List;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ILabItem;
import ch.elexis.core.model.ILabMapping;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.types.LabItemTyp;

public class ILabItemBuilder extends AbstractBuilder<ILabItem> {
	
	private List<ILabMapping> mappingList;
	
	public ILabItemBuilder(IModelService modelService, String code, String name, String refMale,
		String refFemale, String unit, LabItemTyp type, String group, int seq){
		super(modelService);
		object = modelService.create(ILabItem.class);
		object.setCode(code);
		object.setName(name);
		object.setReferenceMale(refMale);
		object.setReferenceFemale(refFemale);
		object.setUnit(unit);
		object.setTyp(type);
		object.setGroup(group);
		object.setPriority(Integer.toString(seq));
		object.setVisible(true);
	}
	
	public ILabItemBuilder origin(IContact origin, String itemName, boolean charge){
		if(mappingList == null) {
			mappingList = new ArrayList<>();
		}
		ILabMapping mapping = modelService.create(ILabMapping.class);
		mapping.setItem(object);
		mapping.setOrigin(origin);
		mapping.setItemName(itemName);
		mapping.setCharge(charge);
		mappingList.add(mapping);
		return this;
	}
	
	@Override
	public ILabItem buildAndSave(){
		modelService.save(object);
		if (mappingList != null) {
			modelService.save(mappingList);
		}
		return object;
	}
	
	@Override
	public ILabItem build(){
		if(mappingList != null) {
			throw new IllegalStateException("Will loose mapping on lazy save operation");
		}
		return super.build();
	}
}
