
package ch.elexis.core.pdfbox.ui.parts;

import java.io.File;
import java.io.FileInputStream;
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
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.pdfbox.ui.parts.handlers.DocumentConverterServiceHolder;
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.services.IDocumentConverter;
import ch.elexis.core.ui.e4.events.ElexisUiEventTopics;
import ch.elexis.core.ui.e4.util.CoreUiUtil;

public class PdfPreviewPart {

	@Inject
	private IConfigService configService;

	private Composite previewComposite;
	private ScrolledComposite scrolledComposite;
	private PdfPreviewPartLoadHandler pdfPreviewPartLoadHandler;

	private IDocument currentDocument;

	@PostConstruct
	public void postConstruct(Composite parent) throws IOException {
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
	void activePatient(IPatient patient) throws IOException {
		// do not unload if document has no connection to a patient
		if (currentDocument != null && currentDocument.getPatient() == null) {
			return;
		}

		if (pdfPreviewPartLoadHandler != null) {
			pdfPreviewPartLoadHandler.unloadDocument();
			updatePreview((InputStream) null);
		}
	}

	@Inject
	@Optional
	void updatePreview(@UIEventTopic(ElexisUiEventTopics.EVENT_PREVIEW_MIMETYPE_PDF) IDocument pdfIDocument) {
		currentDocument = pdfIDocument;
		updatePreview(pdfIDocument != null ? pdfIDocument.getContent() : null);
	}

	private void updatePreview(InputStream pdfInputStream) {
		if (pdfPreviewPartLoadHandler != null) {
			if (pdfInputStream == null) {
				try {
					pdfPreviewPartLoadHandler.unloadDocument();
				} catch (IOException e) {
					LoggerFactory.getLogger(getClass()).warn("Error unloading document", e);
				}
			}
			pdfPreviewPartLoadHandler.close();
		}
		java.util.Optional<IDocumentConverter> converterService = DocumentConverterServiceHolder.get();
		if (converterService.isPresent() && converterService.get().isAvailable() && currentDocument != null) {
			boolean isSupported = converterService.get().isSupportedFile(currentDocument);
			if (isSupported) {
			try {
				java.util.Optional<File> pdfFile = converterService.get().convertToPdf(currentDocument);
				if (pdfFile.isPresent()) {
					 pdfInputStream = new FileInputStream(pdfFile.get());
						currentDocument = null;
				}
			} catch (IOException e) {
				LoggerFactory.getLogger(getClass()).error("Error converting document [" + currentDocument + "]", e);
			}
	}
		
		String zoomLevel = configService.getActiveUserContact(Constants.PREFERENCE_USER_ZOOMLEVEL,
				Constants.PREFERENCE_USER_ZOOMLEVEL_DEFAULT);

		pdfPreviewPartLoadHandler = new PdfPreviewPartLoadHandler(pdfInputStream, Float.valueOf(zoomLevel),
				previewComposite, scrolledComposite);
	}
	}

	public void changeScalingFactor(Float _zoomLevel) {
		pdfPreviewPartLoadHandler.changeScalingFactor(_zoomLevel);
	}

	@Optional
	@Inject
	public void setFixLayout(MPart part, @Named(Preferences.USR_FIX_LAYOUT) boolean currentState) {
		CoreUiUtil.updateFixLayout(part, currentState);
	}

}