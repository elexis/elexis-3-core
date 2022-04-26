package ch.elexis.core.findings.ui.views;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.LabelProvider;

import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.findings.util.ModelUtil;

public class ObservationLabelProvider extends LabelProvider {

	@Override
	public String getText(Object element) {
		if (element instanceof IObservation) {
			return getLabelText((IObservation) element);
		}
		return super.getText(element);
	}

	/**
	 * Get text value of the observation without leading title.
	 *
	 * @param observation
	 * @return
	 */
	private String getLabelText(IObservation observation) {
		String ret = observation.getText().orElse("").trim();

		Optional<ICoding> coding = ModelUtil.getCodeBySystem(observation.getCoding(),
				CodingSystem.ELEXIS_LOCAL_CODESYSTEM);
		String title = coding.isPresent() ? coding.get().getDisplay() : "";
		if (StringUtils.isNotBlank(title) && ret.startsWith(title)) {
			ret = ret.substring(title.length(), ret.length()).trim();
			if (ret.startsWith(": ")) {
				ret = ret.substring(": ".length(), ret.length());
			}
		}
		return ret;
	}
}
