
package ch.elexis.core.pdfbox.ui.parts;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.ui.e4.events.ElexisUiEventTopics;
import ch.elexis.core.ui.e4.util.CoreUiUtil;

public class PdfPreviewPart {
	
	@Inject
	private IConfigService configService;
	
	private Composite previewComposite;
	private ScrolledComposite scrolledComposite;
	private PdfPreviewPartLoadHandler pdfPreviewPartLoadHandler;
	
	@PostConstruct
	public void postConstruct(Composite parent) throws IOException{
		scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		previewComposite = new Composite(scrolledComposite, SWT.NONE);
		scrolledComposite.setContent(previewComposite);
		
		previewComposite.setLayout(new GridLayout(1, false));
		
		Label label = new Label(previewComposite, SWT.None);
		label.setText(Messages.PdfPreview_NoPDFSelected);
		
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setMinSize(previewComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}
	
	@Inject
	@Optional
	void activePatient(IPatient patient) throws IOException{
		if (pdfPreviewPartLoadHandler != null) {
			pdfPreviewPartLoadHandler.unloadDocument();
			updatePreview(null);
		}
	}
	
	@Inject
	@Optional
	void updatePreview(@UIEventTopic(ElexisUiEventTopics.EVENT_PREVIEW_MIMETYPE_PDF)
	InputStream pdfInputStream){
		
		if (pdfPreviewPartLoadHandler != null) {
			if (pdfInputStream == null) {
				try {
					pdfPreviewPartLoadHandler.unloadDocument();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			pdfPreviewPartLoadHandler.close();
		}
		
		String zoomLevel = configService.getActiveUserContact(Constants.PREFERENCE_USER_ZOOMLEVEL,
			Constants.PREFERENCE_USER_ZOOMLEVEL_DEFAULT);
		
		pdfPreviewPartLoadHandler = new PdfPreviewPartLoadHandler(pdfInputStream,
			Float.valueOf(zoomLevel), previewComposite, scrolledComposite);
		
	}
	
	public void changeScalingFactor(Float _zoomLevel){
		pdfPreviewPartLoadHandler.changeScalingFactor(_zoomLevel);
	}
	
	@Optional
	@Inject
	public void setFixLayout(MPart part, @Named(Preferences.USR_FIX_LAYOUT)
	boolean currentState){
		CoreUiUtil.updateFixLayout(part, currentState);
	}
	
}