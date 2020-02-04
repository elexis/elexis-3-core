package ch.elexis.core.findings.fhir.model.service;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.IDocument;
import ch.elexis.core.services.IDocumentStore;

@Component
public class DocumentStoreHolder {
	
	private static ConcurrentMap<String, IDocumentStore> services = new ConcurrentHashMap<>();
	
	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	void addDocumentStore(IDocumentStore store){
		services.put(store.getId(), store);
	}
	
	void removeDocumentStore(IDocumentStore store){
		services.remove(store.getId());
	}
	
	public static Optional<IDocument> getDocument(String documentStoreId, String documentId){
		if (StringUtils.isNotBlank(documentStoreId) && StringUtils.isNotBlank(documentId)) {
			IDocumentStore store = services.get(documentStoreId);
			if (store != null) {
				return store.loadDocument(documentId);
			} else {
				LoggerFactory.getLogger(DocumentStoreHolder.class)
					.warn("Could not get store for id [" + documentStoreId + "]");
			}
		}
		return Optional.empty();
	}
}
