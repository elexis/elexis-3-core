package ch.elexis.core.findings.templates.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.osgi.service.component.annotations.Component;

import ch.elexis.core.findings.templates.model.FindingsTemplates;
import ch.elexis.core.findings.templates.model.ModelFactory;
import ch.elexis.data.NamedBlob;

@Component(service = FindingsTemplateService.class)
public class FindingsTemplateService {
	
	private static final String FINDINGS_TEMPLATE_ID = "Findings_Template_1";
	
	public FindingsTemplateService(){
		
	}
	
	public FindingsTemplates getFindingsTemplates(){
		
		NamedBlob namedBlob = NamedBlob.load(FINDINGS_TEMPLATE_ID);
		if (namedBlob.exists() && namedBlob.getString() != null
			&& !namedBlob.getString().isEmpty()) {
			try {
				
				Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
				Map<String, Object> m = reg.getExtensionToFactoryMap();
				m.put("xmi", new XMIResourceFactoryImpl());
				// Obtain a new resource set
				ResourceSet resSet = new ResourceSetImpl();
				
				// Get the resource
				Resource resource = resSet.createResource(URI.createURI("findingsTemplate.xml"));
				resource.load(new URIConverter.ReadableInputStream(namedBlob.getString()), null);
				return (FindingsTemplates) resource.getContents().get(0);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		ModelFactory factory = ModelFactory.eINSTANCE;
		FindingsTemplates findingsTemplates = factory.createFindingsTemplates();
		findingsTemplates.setId(FINDINGS_TEMPLATE_ID);
		findingsTemplates.setTitle("Vorlagen");
		return findingsTemplates;
	}
	
	public String createXMI(FindingsTemplates findingsTemplates){
		Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
		Map<String, Object> m = reg.getExtensionToFactoryMap();
		m.put("xmi", new XMIResourceFactoryImpl());
		ResourceSet resSet = new ResourceSetImpl();
		Resource resource = resSet.createResource(URI.createURI("findingsTemplate.xml"));
		resource.getContents().add(findingsTemplates);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		
		try {
			resource.save(os, Collections.EMPTY_MAP);
			os.flush();
			String aString = new String(os.toByteArray(), "UTF-8");
			os.close();
			return aString;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public void saveFindingsTemplates(Optional<FindingsTemplates> findingsTemplates){
		if (findingsTemplates.isPresent()) {
			String result = createXMI(findingsTemplates.get());
			if (result != null) {
				NamedBlob namedBlob = NamedBlob.load(findingsTemplates.get().getId());
				namedBlob.putString(result);
			}
			else {
				//cannot save
			}
		}
		
	}
}
