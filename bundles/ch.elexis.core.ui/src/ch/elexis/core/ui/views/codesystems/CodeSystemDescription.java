package ch.elexis.core.ui.views.codesystems;

import java.util.List;
import java.util.Optional;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.interfaces.ICodeElement;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.PersistentObjectFactory;

public class CodeSystemDescription {
	
	private String configName;
	
	private ICodeElement poCodeElement;
	
	private CodeSelectorFactory codeSelectorFactory;
	
	private String system;
	private String elexisClassName;
	
	public static Optional<CodeSystemDescription> of(IConfigurationElement configuration){
		CodeSystemDescription ret = new CodeSystemDescription();
		
		try {
			ret.configName =
				configuration.getName() + " - " + configuration.getAttribute("name");
			ret.codeSelectorFactory = (CodeSelectorFactory) configuration
				.createExecutableExtension(ExtensionPointConstantsUi.VERRECHNUNGSCODE_CSF);
			if (ret.codeSelectorFactory == null) {
				String error =
					"No CodeSelectorFactory [" + configuration.getClass().getName() + "]";
				SWTHelper.alert("Error", error); //$NON-NLS-1$
				LoggerFactory.getLogger(CodeSystemDescription.class).error(error);
				return Optional.empty();
			}
			
			String factoryName =
				configuration.getAttribute(ExtensionPointConstantsUi.VERRECHNUNGSCODE_ELF);
			if (factoryName != null && !factoryName.isEmpty()) {
				PersistentObjectFactory poFactory = (PersistentObjectFactory) configuration
					.createExecutableExtension(ExtensionPointConstantsUi.VERRECHNUNGSCODE_ELF);
				ret.poCodeElement =
					(ICodeElement) poFactory
						.createTemplate(ret.codeSelectorFactory.getElementClass());
			} else {
				String system = configuration.getAttribute("system");
				if (system != null && !system.isEmpty()) {
					ret.system = system;
				} else {
					String error =
						"No system or factory [" + ret.configName + "]";
					SWTHelper.alert("Error", error); //$NON-NLS-1$
					LoggerFactory.getLogger(CodeSystemDescription.class).error(error);
					return Optional.empty();
				}
				ret.elexisClassName = configuration.getAttribute("elexisClassName");
			}
		} catch (CoreException ex) {
			LoggerFactory.getLogger(CodeSystemDescription.class)
				.error("Error creating config", ex);
			return Optional.empty();
		}
		return Optional.of(ret);
	}
	
	public String getCodeSystemName(){
		if (poCodeElement != null) {
			return poCodeElement.getCodeSystemName();
		} else if (system != null) {
			return system;
		}
		throw new IllegalStateException(
			"No system and no code element present [" + configName + "]");
	}
	
	public CodeSelectorFactory getCodeSelectorFactory(){
		return codeSelectorFactory;
	}
	
	public SelectionDialog getSelectionDialog(Shell parent, Object data){
		return codeSelectorFactory.getSelectionDialog(parent, data);
	}
	
	public List<Object> getActions(Object context){
		if (poCodeElement != null) {
			return poCodeElement.getActions(context);
		}
		return null;
	}
	
	public String getElexisClassName(){
		if (poCodeElement != null) {
			return poCodeElement.getClass().getName();
		}
		return elexisClassName;
	}
}
