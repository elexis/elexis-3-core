package ch.elexis.core.ui.reminder.dialogs;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IReminder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.dialogs.Messages;
import ch.elexis.core.ui.icons.ImageSize;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.reminder.composites.ReminderComposite;

public class ReminderDetailDialog extends TitleAreaDialog {

	private IReminder reminder;

	private ReminderComposite reminderComposite;

	private Composite composite;

	public ReminderDetailDialog(IReminder reminder, Shell parentShell) {
		super(parentShell);
		this.reminder = reminder;
		setShellStyle(SWT.DIALOG_TRIM | SWT.MODELESS | SWT.RESIZE);
	}

	@Override
	public void create() {
		super.create();
		String shelltitle = Messages.EditReminderDialog_reminderShellTitle; // $NON-NLS-1$
		setTitle(Messages.EditReminderDialog_editReminder); // $NON-NLS-1$
		IContact o = reminder.getCreator();
		if (o == null) {
			shelltitle += Messages.EditReminderDialog_unknown; // $NON-NLS-1$
		} else {
			shelltitle += " (" + o.getLabel() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		getShell().setText(shelltitle);

		setMessage(Messages.EditReminderDialog_enterDataForReminder);
		setTitleImage(Images.lookupImage("tick_banner.png", ImageSize._75x66_TitleDialogIconSize)); //$NON-NLS-1$
		reminderComposite = new ReminderComposite(composite, SWT.NONE);
		reminderComposite.setReminder(reminder);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(new FillLayout());
		return composite;
	}

	@Override
	protected void okPressed() {
		reminder = reminderComposite.getReminder();
		CoreModelServiceHolder.get().save(reminder);
		ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, reminder);
		super.okPressed();
	}
}
