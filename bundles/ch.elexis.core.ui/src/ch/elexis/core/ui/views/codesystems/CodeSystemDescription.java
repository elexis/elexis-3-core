package ch.elexis.core.ui.views.codesystems;

import java.util.Optional;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.slf4j.LoggerFactory;

import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.util.SWTHelper;

public class CodeSystemDescription {

	private String configName;

	private CodeSelectorFactory codeSelectorFactory;

	private String system;
	private String elexisClassName;

	public static Optional<CodeSystemDescription> of(IConfigurationElement configuration) {
		CodeSystemDescription ret = new CodeSystemDescription();

		try {
			ret.configName = configuration.getName() + " - " + configuration.getAttribute("name"); //$NON-NLS-1$ //$NON-NLS-2$
			ret.codeSelectorFactory = (CodeSelectorFactory) configuration
					.createExecutableExtension(ExtensionPointConstantsUi.VERRECHNUNGSCODE_CSF);
			if (ret.codeSelectorFactory == null) {
				String error = "No CodeSelectorFactory [" + configuration.getClass().getName() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
				SWTHelper.alert("Error", error); //$NON-NLS-1$
				LoggerFactory.getLogger(CodeSystemDescription.class).error(error);
				return Optional.empty();
			} else {
				CoreUiUtil.injectServicesWithContext(ret.codeSelectorFactory);
			}

				String system = configuration.getAttribute("system"); //$NON-NLS-1$
				if (system != null && !system.isEmpty()) {
					ret.system = system;
				} else {
					String error = "No system or factory [" + ret.configName + "]"; //$NON-NLS-1$ //$NON-NLS-2$
					SWTHelper.alert("Error", error); //$NON-NLS-1$
					LoggerFactory.getLogger(CodeSystemDescription.class).error(error);
					return Optional.empty();
				}
				ret.elexisClassName = configuration.getAttribute("elexisClassName"); //$NON-NLS-1$
		} catch (CoreException ex) {
			LoggerFactory.getLogger(CodeSystemDescription.class).error("Error creating config", ex); //$NON-NLS-1$
			return Optional.empty();
		}
		return Optional.of(ret);
	}

	public String getCodeSystemName() {
		if (system != null) {
			return system;
		}
		throw new IllegalStateException("No system and no code element present [" + configName + "]"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public CodeSelectorFactory getCodeSelectorFactory() {
		return codeSelectorFactory;
	}

	public SelectionDialog getSelectionDialog(Shell parent, Object data) {
		return codeSelectorFactory.getSelectionDialog(parent, data);
	}

	public String getElexisClassName() {
		return elexisClassName;
	}
}
