package ch.elexis.core.ui.tasks.parts.controls;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.services.IVirtualFilesystemService;
import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemHandle;
import ch.elexis.core.utils.OsgiServiceUtil;

public class RunContextTextWithDefaultValue extends Text {

	/**
	 * @wbp.parser.entryPoint
	 */
	public RunContextTextWithDefaultValue(Composite compAssisted, AbstractTaskDescriptorConfigurationComposite atdcc,
			String key, String defaultValue, String configuredValue) {
		super(compAssisted, SWT.BORDER);

		setMessage(defaultValue != null ? defaultValue : "");
		setText(configuredValue != null ? configuredValue : "");

		addModifyListener(event -> {
			if (StringUtils.isNotBlank(getText())) {
				atdcc.taskDescriptor.setRunContextParameter(key, getText().trim());
			} else {
				atdcc.taskDescriptor.setRunContextParameter(key, null);
			}
			atdcc.saveTaskDescriptor();
		});

		addMenuDetectListener(evmdl -> {
			IVirtualFilesystemService vfsService = OsgiServiceUtil.getService(IVirtualFilesystemService.class)
					.orElse(null);
			try {
				final IVirtualFilesystemHandle vfsHandle = vfsService.of(getText());
				evmdl.doit = false; // prevent system to handle this event
				Menu menu = new Menu(evmdl.display.getActiveShell(), SWT.POP_UP);
				MenuItem exit = new MenuItem(menu, SWT.NONE);
				exit.setText("Test URL");
				exit.addListener(SWT.Selection, event -> {

					boolean canRead = false;
					boolean isDirectory = false;
					try {
						canRead = vfsHandle.canRead();
						isDirectory = vfsHandle.isDirectory();

					} catch (IOException e) {
						MessageDialog.openError(getShell(), "VFS Test", e.getLocalizedMessage());
						return;
					}

					MessageDialog.openInformation(getShell(), "VFS Test",
							"canRead: " + canRead + ", isDirectory: " + isDirectory);
				});
				menu.setVisible(true);

			} catch (IOException ex) {
				return;
			}

		});
	}

	@Override
	protected void checkSubclass() {
	}

}
