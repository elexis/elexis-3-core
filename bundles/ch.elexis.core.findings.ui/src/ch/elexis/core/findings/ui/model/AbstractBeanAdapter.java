package ch.elexis.core.findings.ui.model;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.ui.services.FindingsServiceComponent;
import ch.elexis.core.services.holder.ContextServiceHolder;

public abstract class AbstractBeanAdapter<T extends IFinding> {

	protected T finding;

	protected boolean autoSave;

	protected void init() {
		if (ContextServiceHolder.get().getActivePatient().isPresent()) {
			finding.setPatientId(ContextServiceHolder.get().getActivePatient().get().getId());
		}
	}

	public AbstractBeanAdapter<T> autoSave(boolean value) {
		autoSave = value;
		return this;
	}

	public String getText() {
		return finding.getText().orElse(StringUtils.EMPTY);
	}

	public void setText(String text) {
		finding.setText(text);
		autoSave();
	}

	public void addStringExtension(String theUrl, String theValue) {
		finding.addStringExtension(theUrl, theValue);
		autoSave();
	}

	public Map<String, String> getStringExtensions() {
		return finding.getStringExtensions();
	}

	public String getLabel() {
		return finding.getLabel();
	}

	public IFinding getFinding() {
		return finding;
	}

	protected void autoSave() {
		if (autoSave) {
			save();
		}
	}

	protected void save() {
		FindingsServiceComponent.getService().saveFinding(finding);
	}
}
