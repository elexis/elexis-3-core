package ch.elexis.core.ui.tasks.parts.controls;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class RunContextTextWithDefaultValue extends Text {

	public RunContextTextWithDefaultValue(Composite compAssisted, AbstractTaskDescriptorConfigurationComposite atdcc,
			String key, String defaultValue, String configuredValue) {
		super(compAssisted, SWT.BORDER);

		setMessage(defaultValue != null ? defaultValue : StringUtils.EMPTY);
		setText(configuredValue != null ? configuredValue : StringUtils.EMPTY);

		addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				if (StringUtils.isNotBlank(getText())) {
					atdcc.taskDescriptor.setRunContextParameter(key, getText().trim());
				} else {
					atdcc.taskDescriptor.setRunContextParameter(key, null);
				}
				atdcc.saveTaskDescriptor();
			}
		});
	}

	@Override
	protected void checkSubclass() {
	}

}
