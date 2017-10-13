package ch.elexis.core.findings.templates.ui.actions;

import java.time.LocalDateTime;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.ui.dialogs.DateTimeSelectorDialog;
import ch.elexis.core.ui.icons.Images;
import ch.rgw.tools.TimeTool;

public class DateAction extends Action {
	private LocalDateTime localDateTime;
	private final Shell shell;
	private Label lblDateText;
	
	public DateAction(Shell shell, LocalDateTime localDateTime, Composite composite){
		super("", Action.AS_PUSH_BUTTON);
		Assert.isNotNull(shell);
		this.shell = shell;
		this.localDateTime = localDateTime == null ? LocalDateTime.now() : localDateTime;
		this.lblDateText = new Label(composite, SWT.NONE);
		this.lblDateText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		
		init();
	}
	
	@Override
	public void run(){
		DateTimeSelectorDialog inputDialog =
			new DateTimeSelectorDialog(shell, new TimeTool(localDateTime), true);
		if (inputDialog.open() == MessageDialog.OK) {
			TimeTool timeTool = inputDialog.getSelectedDate();
			if (timeTool != null) {
				this.localDateTime = timeTool.toLocalDateTime();
				init();
			}
		}
		super.run();
	}
	
	@Override
	public String getToolTipText(){
		return "Datum ändern";
	}
	
	private void init(){
		if (lblDateText != null) {
			lblDateText.setText(new TimeTool(localDateTime).toString(TimeTool.FULL_GER));
		}
	}
	
	@Override
	public ImageDescriptor getImageDescriptor(){
		return Images.IMG_CALENDAR.getImageDescriptor();
	}
	
	public LocalDateTime getLocalDateTime(){
		return localDateTime;
	}
}