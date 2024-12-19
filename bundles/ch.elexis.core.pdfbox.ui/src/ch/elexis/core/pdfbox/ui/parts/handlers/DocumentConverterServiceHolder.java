package ch.elexis.core.pdfbox.ui.parts.handlers;

import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import ch.elexis.core.documents.DocumentStore;
import ch.elexis.core.services.IDocumentConverter;

@Component(service = {})
public class DocumentConverterServiceHolder {
	private static IDocumentConverter documentConverter;

	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policyOption = ReferencePolicyOption.GREEDY)
	public void bind(IDocumentConverter service) {
		DocumentConverterServiceHolder.documentConverter = service;
	}

	public static void unbind(DocumentStore service) {
		DocumentConverterServiceHolder.documentConverter = null;
	}

	public static Optional<IDocumentConverter> get() {
		return Optional.ofNullable(documentConverter);
	}
}
