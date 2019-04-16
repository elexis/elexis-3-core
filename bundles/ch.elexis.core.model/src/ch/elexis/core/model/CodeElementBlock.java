package ch.elexis.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.jpa.entities.Kontakt;
import ch.elexis.core.jpa.entities.Leistungsblock;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;
import ch.elexis.core.jpa.model.adapter.mixin.IdentifiableWithXid;
import ch.elexis.core.model.util.internal.ModelUtil;
import ch.elexis.core.services.ICodeElementService;
import ch.elexis.core.services.holder.CodeElementServiceHolder;

public class CodeElementBlock extends AbstractIdDeleteModelAdapter<Leistungsblock>
		implements IdentifiableWithXid, ICodeElementBlock {
	
	private static final String SEPARATOR = ":=:";
	
	public CodeElementBlock(Leistungsblock entity){
		super(entity);
	}
	
	@Override
	public String getCodeSystemName(){
		return "Block";
	}
	
	@Override
	public String getCode(){
		return getEntity().getName();
	}
	
	@Override
	public void setCode(String value){
		getEntity().setName(value);
	}
	
	@Override
	public String getText(){
		return getEntity().getText();
	}
	
	@Override
	public void setText(String value){
		getEntity().setName(value);
	}
	
	@Override
	public List<ICodeElement> getElements(IEncounter encounter){
		ICodeElementService service = CodeElementServiceHolder.get();
		List<ICodeElement> ret = new ArrayList<>();
		if (service != null) {
			String codeelements = getEntity().getServices();
			if (StringUtils.isNotBlank(codeelements)) {
				String[] parts = codeelements.split("\\" + SEPARATOR);
				for (String part : parts) {
					Optional<ICodeElement> created =
						service.loadFromString(part,
							CodeElementServiceHolder.createContext(encounter));
					created.ifPresent(c -> ret.add(c));
				}
			}
		}
		return ret;
	}
	
	@Override
	public List<ICodeElement> getElements(){
		ICodeElementService service = CodeElementServiceHolder.get();
		List<ICodeElement> ret = new ArrayList<>();
		if (service != null) {
			String codeelements = getEntity().getServices();
			if (StringUtils.isNotBlank(codeelements)) {
				String[] parts = codeelements.split("\\" + SEPARATOR);
				for (String part : parts) {
					Optional<ICodeElement> created =
						service.loadFromString(part, CodeElementServiceHolder.createContext());
					created.ifPresent(c -> ret.add(c));
				}
			}
		}
		return ret;
	}
	
	@Override
	public List<ICodeElement> getElementReferences(){
		ICodeElementService service = CodeElementServiceHolder.get();
		List<ICodeElement> ret = new ArrayList<>();
		if (service != null) {
			String codeelements = getEntity().getServices();
			if (StringUtils.isNotBlank(codeelements)) {
				String[] parts = codeelements.split("\\" + SEPARATOR);
				for (String part : parts) {
					String[] elementParts = service.getStoreToStringParts(part);
					if (elementParts != null && elementParts.length > 1) {
						CodeElementReference reference =
							new CodeElementReference(elementParts[0], elementParts[1]);
						if (elementParts.length > 2) {
							reference.setText(elementParts[2]);
						}
						ret.add(reference);
					}
				}
			}
		}
		return ret;
	}
	
	@Override
	public IMandator getMandator(){
		if (getEntity().getMandator() != null) {
			return ModelUtil.getAdapter(getEntity().getMandator(), IMandator.class);
		}
		return null;
	}
	
	@Override
	public void setMandator(IMandator value){
		if (value instanceof AbstractIdModelAdapter) {
			getEntity().setMandator((((AbstractIdModelAdapter<Kontakt>) value).getEntity()));
		} else if (value == null) {
			getEntity().setMandator(null);
		}
	}
	
	@Override
	public String getMacro(){
		return getEntity().getMacro();
	}
	
	@Override
	public void setMacro(String value){
		getEntity().setMacro(value);
	}
	
	@Override
	public List<ICodeElement> getDiffToReferences(List<ICodeElement> elements){
		List<ICodeElement> references = getElementReferences();
		if (references.size() > elements.size()) {
			// use copy to iterate 
			for (ICodeElement reference : references.toArray(new ICodeElement[references.size()])) {
				for (ICodeElement element : elements) {
					if (isMatchingCodeElement(element, reference)) {
						references.remove(reference);
					}
				}
			}
		} else {
			references.clear();
		}
		return references;
	}
	
	@Override
	public void addElement(ICodeElement element){
		if (element != null) {
			List<ICodeElement> elements = getElementReferences();
			int index = getIndexOf(elements, element);
			if (index != -1) {
				elements.add(index, element);
			} else {
				elements.add(element);
			}
			storeElements(elements);
		}
	}
	
	@Override
	public void removeElement(ICodeElement element){
		if (element != null) {
			List<ICodeElement> elements = getElementReferences();
			int index = getIndexOf(elements, element);
			if (index != -1) {
				elements.remove(index);
			}
			storeElements(elements);
		}
	}
	
	@Override
	public void moveElement(ICodeElement element, boolean up){
		if (element != null) {
			groupElements();
			
			List<ICodeElement> elements = getElementReferences();
			long count = getNumberOf(element);
			int index = getIndexOf(elements, element);
			if (up) {
				int offset = -1;
				if (index + offset >= 0) {
					ICodeElement pervElement = elements.get((int) (index + offset));
					long nextElementCount = getNumberOf(pervElement);
					if (nextElementCount > 1) {
						offset = ((int) nextElementCount) * -1;
					}
					Collections.rotate(elements.subList(index + offset, (int) ((index + count))),
						offset);
					storeElements(elements);
				}
			} else {
				int offset = 1;
				if (index + count + offset <= elements.size()) {
					if (offset == 1) {
						ICodeElement nextElement =
							elements.get((int) (index + (count - 1) + offset));
						long nextElementCount = getNumberOf(nextElement);
						if (nextElementCount > 1) {
							offset = (int) nextElementCount;
						}
					}
					Collections.rotate(elements.subList(index, (int) (index + count + offset)),
						offset);
					storeElements(elements);
				}
			}
		}
	}
	
	private long getNumberOf(ICodeElement element){
		List<ICodeElement> elements = getElementReferences();
		return elements.stream().filter(e -> isMatchingCodeElement(e, element)).count();
	}
	
	/**
	 * Group the elements of this {@link Leistungsblock} by combination of
	 * {@link ICodeElement#getCodeSystemName()} and {@link ICodeElement#getCode()}.
	 * 
	 * First occurrence of such a combination results in index of the grouped elements in the
	 * resulting elements order.
	 */
	private void groupElements(){
		List<List<ICodeElement>> order = new ArrayList<>();
		Map<String, List<ICodeElement>> group = new HashMap<>();
		List<ICodeElement> elements;
		elements = getElementReferences();
		for (ICodeElement iCodeElement : elements) {
			String key = iCodeElement.getCodeSystemName() + iCodeElement.getCode();
			List<ICodeElement> list = group.get(key);
			if (list == null) {
				list = new ArrayList<>();
				list.add(iCodeElement);
				group.put(key, list);
				order.add(list);
			} else {
				list.add(iCodeElement);
			}
		}
		List<ICodeElement> sortedGrouped = new ArrayList<>();
		for (List<ICodeElement> groupedList : order) {
			sortedGrouped.addAll(groupedList);
		}
		storeElements(sortedGrouped);
	}
	
	private int getIndexOf(List<ICodeElement> elements, ICodeElement element){
		if (element != null && elements != null) {
			for (int i = 0; i < elements.size(); i++) {
				if (isMatchingCodeElement(element, elements.get(i))) {
					return i;
				}
			}
		}
		return -1;
	}
	
	private void storeElements(List<ICodeElement> elements){
		ICodeElementService service = CodeElementServiceHolder.get();
		if (service != null) {
			StringBuilder sb = new StringBuilder();
			for (ICodeElement element : elements) {
				if (sb.length() > 0) {
					sb.append(SEPARATOR);
				}
				sb.append(service.storeToString(element));
			}
			getEntity().setServices(sb.toString());
		}
	}
	
	private boolean isMatchingCodeElement(ICodeElement left, ICodeElement right){
		String lCodeSystemName = left.getCodeSystemName();
		String rCodeSystemName = right.getCodeSystemName();
		String lCode = left.getCode();
		String rCode = right.getCode();
		if (lCodeSystemName != null && rCodeSystemName != null && lCode != null && rCode != null) {
			if (lCodeSystemName.equals(rCodeSystemName) && lCode.equals(rCode)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String getLabel(){
		String name = getEntity().getName();
		String macro = getEntity().getMacro();
		if (macro == null || macro.length() == 0 || macro.equals(name))
			return name;
		return name + " [" + macro + "]";
	}
	
	private class CodeElementReference implements ch.elexis.core.model.ICodeElement {
		
		private String codeSystemName;
		private String code;
		
		private String text;
		
		public CodeElementReference(String system, String code){
			this.codeSystemName = system;
			this.code = code;
		}
		
		@Override
		public String getCodeSystemName(){
			return codeSystemName;
		}
		
		@Override
		public String getCode(){
			return code;
		}
		
		@Override
		public void setCode(String value){
			this.code = value;
		}
		
		@Override
		public String getText(){
			return text;
		}
		
		@Override
		public void setText(String value){
			this.text = value;
		}
	}
}
