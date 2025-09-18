package ch.elexis.core.ui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.l10n.Messages;
import ch.elexis.core.ui.icons.Images;

public class PatientPhotoDialog extends Dialog {
	private final String title;
	private final ImageData sourceData;
	private Image displayImage;
	private Label imageLabel;
	private ScrolledComposite scroller;
	private Composite content;
	private double zoom = 1.0;

	public PatientPhotoDialog(Shell parentShell, String title, ImageData imageData) {
		super(parentShell);
		this.title = (title != null && !title.isBlank()) ? title : Messages.PatientPhotoDialog_Title_Default;
		this.sourceData = imageData;
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(this.title);
		shell.setMinimumSize(520, 620);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayout(new GridLayout(1, false));
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		scroller = new ScrolledComposite(container, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		scroller.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		scroller.setExpandHorizontal(true);
		scroller.setExpandVertical(true);
		content = new Composite(scroller, SWT.NONE);
		content.setLayout(new GridLayout(1, false));
		imageLabel = new Label(content, SWT.NONE);
		imageLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));

		scroller.setContent(content);

		applyZoom();
		content.addListener(SWT.MouseWheel, ev -> {
			boolean ctrlDown = (ev.stateMask & (SWT.CTRL | SWT.COMMAND)) != 0;
			if (!ctrlDown)
				return;
			if (ev.count > 0)
				zoomIn();
			else
				zoomOut();
		});

		imageLabel.addListener(SWT.MouseDoubleClick, ev -> fitToWidth());

		return area;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		GridLayout layout = new GridLayout(2, true);
		layout.marginWidth = 10;
		layout.marginHeight = 5;
		parent.setLayout(layout);

		Composite leftGroup = new Composite(parent, SWT.NONE);
		leftGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		GridLayout leftLayout = new GridLayout(3, false);
		leftLayout.marginWidth = 5;
		leftLayout.horizontalSpacing = 5;
		leftGroup.setLayout(leftLayout);

		int leftButtonWidth = 55;

		Button outBtn = new Button(leftGroup, SWT.PUSH);
		outBtn.setImage(Images.IMG_ZOOM_OUT.getImage());
		outBtn.setToolTipText(Messages.PatientPhotoDialog_ZoomOut);
		GridData gdOut = new GridData(SWT.CENTER, SWT.CENTER, false, false);
		gdOut.widthHint = leftButtonWidth;
		outBtn.setLayoutData(gdOut);
		outBtn.addListener(SWT.Selection, e -> zoomOut());

		Button resetBtn = new Button(leftGroup, SWT.PUSH);
		resetBtn.setText(Messages.PatientPhotoDialog_ZoomReset);
		GridData gdReset = new GridData(SWT.CENTER, SWT.CENTER, false, false);
		gdReset.widthHint = leftButtonWidth;
		resetBtn.setLayoutData(gdReset);
		resetBtn.addListener(SWT.Selection, e -> {
			zoom = 1.0;
			applyZoom();
		});

		Button inBtn = new Button(leftGroup, SWT.PUSH);
		inBtn.setImage(Images.IMG_ZOOM_IN.getImage());
		inBtn.setToolTipText(Messages.PatientPhotoDialog_ZoomIn);
		GridData gdIn = new GridData(SWT.CENTER, SWT.CENTER, false, false);
		gdIn.widthHint = leftButtonWidth;
		inBtn.setLayoutData(gdIn);
		inBtn.addListener(SWT.Selection, e -> zoomIn());

		Composite rightGroup = new Composite(parent, SWT.NONE);
		rightGroup.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false));
		GridLayout rightLayout = new GridLayout(3, false);
		rightLayout.marginWidth = 0;
		rightLayout.horizontalSpacing = 10;
		rightGroup.setLayout(rightLayout);

		Label spacer = new Label(rightGroup, SWT.NONE);
		spacer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		int rightButtonWidth = 110;

		Button fitBtn = new Button(rightGroup, SWT.PUSH);
		fitBtn.setText(Messages.PatientPhotoDialog_FitToWidth);
		GridData gdFit = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
		gdFit.widthHint = rightButtonWidth;
		fitBtn.setLayoutData(gdFit);
		fitBtn.addListener(SWT.Selection, e -> fitToWidth());

		Button closeBtn = createButton(rightGroup, IDialogConstants.CLOSE_ID, IDialogConstants.CLOSE_LABEL, true);
		GridData gdClose = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
		gdClose.widthHint = rightButtonWidth;
		closeBtn.setLayoutData(gdClose);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.CLOSE_ID) {
			close();
			return;
		}
		super.buttonPressed(buttonId);
	}

	private void zoomIn() {
		zoom = Math.min(zoom * 1.25, 8.0);
		applyZoom();
	}

	private void zoomOut() {
		zoom = Math.max(zoom / 1.25, 0.1);
		applyZoom();
	}

	private void fitToWidth() {
		if (scroller == null || scroller.isDisposed() || sourceData == null)
			return;
		int imgW = sourceData.width;
		int clientW = scroller.getClientArea().width;
		if (imgW > 0 && clientW > 0) {
			zoom = Math.max(0.1, (double) clientW / imgW);
			applyZoom();
		}
	}

	private void applyZoom() {
		if (sourceData == null || imageLabel == null || imageLabel.isDisposed())
			return;

		if (displayImage != null && !displayImage.isDisposed()) {
			displayImage.dispose();
			displayImage = null;
		}

		int w = Math.max(1, (int) Math.round(sourceData.width * zoom));
		int h = Math.max(1, (int) Math.round(sourceData.height * zoom));
		ImageData scaled = sourceData.scaledTo(w, h);
		displayImage = new Image(getShell().getDisplay(), scaled);
		imageLabel.setImage(displayImage);

		if (content != null && !content.isDisposed()) {
			content.layout(true, true);
			org.eclipse.swt.graphics.Point pref = content.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			pref.x = Math.max(pref.x, w);
			pref.y = Math.max(pref.y, h);
			scroller.setMinSize(pref);
		}
	}

	@Override
	public boolean close() {
		if (displayImage != null && !displayImage.isDisposed())
			displayImage.dispose();
		displayImage = null;
		return super.close();
	}
}
