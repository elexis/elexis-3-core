package ch.elexis.core.ui.mediorder.internal;

import java.time.format.DateTimeFormatter;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.LoggerFactory;

import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.holder.CodeElementServiceHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.OrderServiceHolder;
import ch.elexis.core.services.holder.StockServiceHolder;

public class MediorderHistoryDialog extends Dialog {

	private final IPatient patient;
	private final String blobId;

	public MediorderHistoryDialog(Shell parentShell, IPatient patient, String blobId) {
		super(parentShell);
		this.patient = patient;
		this.blobId = blobId;
		setShellStyle(SWT.SHELL_TRIM | SWT.MODELESS | getDefaultOrientation());
		setBlockOnOpen(false);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.Mediorder_history + " - " + patient.getLabel()); //$NON-NLS-1$
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		area.setLayout(layout);

		try {
			Browser browser = new Browser(area, SWT.NONE);
			GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
			layoutData.widthHint = 800;
			layoutData.heightHint = 500;
			browser.setLayoutData(layoutData);
			MediorderHistoryBuilder historyBuilder = new MediorderHistoryBuilder(CoreModelServiceHolder.get(),
					OrderServiceHolder.get(), CodeElementServiceHolder.get(), ContextServiceHolder.get(),
					DateTimeFormatter.ofPattern("dd.MM.yyyy"), DateTimeFormatter.ofPattern("HH:mm")); //$NON-NLS-1$ //$NON-NLS-2$
			browser.addLocationListener(LocationListener
					.changingAdapter(new MediorderHistoryLinkListener(historyBuilder)::handleHistoryLink));
			browser.setText(new MediorderHistoryRenderer().renderSections(historyBuilder.buildSections(patient,
					StockServiceHolder.get().getPatientStock(patient).orElse(null), blobId)));
		} catch (SWTError e) {
			LoggerFactory.getLogger(getClass()).error("Failed to create the browser widget for the history.", e);
			Label message = new Label(area, SWT.NONE);
			message.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			message.setText("Historie konnte nicht dargestellt werden.");
		}
		return area;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.CLOSE_ID, IDialogConstants.CLOSE_LABEL, true);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (IDialogConstants.CLOSE_ID == buttonId) {
			close();
			return;
		}
		super.buttonPressed(buttonId);
	}
}
