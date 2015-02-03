package ch.elexis.core.ui.views.rechnung;

import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

public interface IRechnungsdetail {
	public ExpandableComposite getExpandableComposite(FormToolkit toolkit, ScrolledForm form);
}
