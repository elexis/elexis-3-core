package ch.elexis.core.pdfbox.ui.parts;

import java.io.IOException;
import java.io.InputStream;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.ui.e4.events.ElexisUiEventTopics;

public class PdfPreviewPart {
	
	@Inject
	private IConfigService configService;
	private Composite previewComposite;
	private ScrolledComposite scrolledComposite;
	private PdfPreviewPartLoadHandler pdfPreviewPartLoadHandler;
	private Label label;
	
	@PostConstruct
	public void postConstruct(Composite parent) throws IOException{
		scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		previewComposite = new Composite(scrolledComposite, SWT.NONE);
		label = new Label(previewComposite, SWT.None);
		
		previewComposite.setLayout(new GridLayout(1, false));
		label.setText(Messages.PdfPreview_NoPDFSelected);
		scrolledComposite.setContent(previewComposite);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setMinSize(previewComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}
	
	
	/*
	 * wird nur mit null ausgeführt wenn bereits im PDFPreviewPartLoadhandler im PDFInputStream schon null ist.
	 */
	@Inject
	@Optional
	void updatePreview(
		@UIEventTopic(ElexisUiEventTopics.EVENT_PREVIEW_MIMETYPE_PDF) InputStream pdfInputStream){
		
		
		if (pdfPreviewPartLoadHandler != null) {
			if(pdfInputStream == null) {
				try {
					pdfPreviewPartLoadHandler.unLoadDocument();
				}catch (IOException e) {
					e.printStackTrace();
				}
			}
			pdfPreviewPartLoadHandler.close();
		}
	
		String zoomLevel = configService.getActiveUserContact(Constants.PREFERENCE_USER_ZOOMLEVEL,
			Constants.PREFERENCE_USER_ZOOMLEVEL_DEFAULT);
		
		pdfPreviewPartLoadHandler = new PdfPreviewPartLoadHandler(pdfInputStream,
			new Float(zoomLevel), previewComposite, scrolledComposite);	
	}
	
	public void changeScalingFactor(Float _zoomLevel){
		pdfPreviewPartLoadHandler.changeScalingFactor(_zoomLevel);
	}
	
	/*
	 * if we change patient the PDF view should refresh 
	 */
	
//	@Inject
//	void activePatient(@Optional
//	IPatient patient){
//		Display.getDefault().asyncExec(() -> {
//			updatePreview(null);
//		});
//	}
	
	@Inject
	@Optional
	void activePatient(IPatient patient){
			updatePreview(null);
	}
}