package ch.elexis.core.ui.services;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.LoggerFactory;

import ch.elexis.core.services.ILocalDocumentService;
import ch.elexis.core.services.ILocalDocumentService.ILoadHandler;
import ch.elexis.core.services.ILocalDocumentService.ISaveHandler;
import ch.elexis.data.Brief;

/**
 * Service component for {@link LocalDocumentService} access. <br/>
 * <br/>
 * Component annotation on views leads to startup problems. The reason is that on DS start a display
 * is created in the context of the resolving Thread.
 * 
 * @author thomas
 *
 */
@Component(service = {})
public class LocalDocumentServiceHolder {
	private static Optional<ILocalDocumentService> localDocumentService;
	
	@Reference
	public void bind(ILocalDocumentService service){
		LocalDocumentServiceHolder.localDocumentService = Optional.ofNullable(service);
		
		service.registerSaveHandler(Brief.class, new ISaveHandler() {
			@Override
			public boolean save(Object documentSource, ILocalDocumentService service){
				Brief brief = (Brief) documentSource;
				Optional<InputStream> content = service.getContent(brief);
				if (content.isPresent()) {
					try {
						brief.save(IOUtils.toByteArray(content.get()), brief.getMimeType());
						return true;
					} catch (IOException e) {
						LoggerFactory.getLogger(getClass()).error("Error saving document", e);
					} finally {
						try {
							content.get().close();
						} catch (IOException e) {
							// ignore
						}
					}
				}
				return false;
			}
		});
		
		service.registerLoadHandler(Brief.class, new ILoadHandler() {
			@Override
			public InputStream load(Object documentSource){
				Brief brief = (Brief) documentSource;
				
				try {
					byte[] bytes = brief.loadBinary();
					if (bytes != null) {
						return new ByteArrayInputStream(bytes);
					}
					LoggerFactory.getLogger(getClass())
						.warn("Document is empty - id: " + brief.getId());
					return new ByteArrayInputStream(new byte[0]);
				} catch (Exception e) {
					LoggerFactory.getLogger(getClass()).error("Error loading document", e);
				}
				return null;
			}
		});
	}
	
	public static void unbind(ILocalDocumentService service){
		LocalDocumentServiceHolder.localDocumentService = Optional.empty();
	}
	
	public static Optional<ILocalDocumentService> getService(){
		return localDocumentService;
	}
}
