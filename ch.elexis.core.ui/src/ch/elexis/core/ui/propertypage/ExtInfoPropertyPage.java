package ch.elexis.core.ui.propertypage;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

import ch.elexis.data.PersistentObject;
import ch.elexis.data.Xid;

public class ExtInfoPropertyPage extends PropertyPage implements
		IWorkbenchPropertyPage {

	private PersistentObject po;

	@Override
	protected Control createContents(Composite parent) {
		noDefaultAndApplyButton();
		init();
		Composite comp = new Composite(parent, SWT.None);
		comp.setLayout(new GridLayout(2, false));
		
		Label header = new Label(comp, SWT.None);
		header.setText("Definierte Felder in ExtInfo:");
		header.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false, 2, 1));

		Map<Object, Object> extinfo = null;
		try {
			extinfo = po.getMap(PersistentObject.FLD_EXTINFO);
		} catch (Exception e) {
			// we ignore any exception here, as it might be coming
			// from a missing ExtInfo (like in Leistunbsblock) or
			// something else
		}
		
		if (extinfo == null || extinfo.size() == 0) {
			Label lab = new Label(comp, SWT.None);
			lab.setText("Keine.");
			lab.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false,
					1, 1));
			return comp;
		}
		
		Set<Entry<Object, Object>> eis = extinfo.entrySet();
		
		for (Iterator<Entry<Object, Object>> iterator = eis.iterator(); iterator.hasNext();) {
			Entry e = (Entry) iterator.next();
			Label lab = new Label(comp, SWT.None);
			lab.setText(e.getKey().toString());
			lab.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false,
					1, 1));

			Text txt = new Text(comp, SWT.None);
			txt.setText(e.getValue().toString());
			txt.setEditable(false);
			txt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
					1, 1));
		}

		return comp;
	}
	
	private void init() {
		IAdaptable adapt = getElement();
		po = (PersistentObject) adapt.getAdapter(PersistentObject.class);
	}

}
