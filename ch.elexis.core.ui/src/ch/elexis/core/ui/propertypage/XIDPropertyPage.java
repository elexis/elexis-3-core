package ch.elexis.core.ui.propertypage;

import java.util.Iterator;
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

public class XIDPropertyPage extends PropertyPage implements IWorkbenchPropertyPage {
	
	private PersistentObject po;
	
	public XIDPropertyPage(){
		super();
	}
	
	@Override
	protected Control createContents(Composite parent){
		noDefaultAndApplyButton();
		init();
		Composite comp = new Composite(parent, SWT.None);
		comp.setLayout(new GridLayout(2, false));
		
		Label header = new Label(comp, SWT.None);
		header.setText("Für dieses Objekt definierte XIDs:");
		header.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false, 2, 1));
		
		Set<String> domains = Xid.getXIDDomains();
		
		if (domains.size() == 0) {
			Label lab = new Label(comp, SWT.None);
			lab.setText("Keine XIDs gefunden.");
			lab.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			return comp;
		}
		for (Iterator<String> iterator = domains.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();
			Label lab = new Label(comp, SWT.None);
			lab.setText(Xid.getSimpleNameForXIDDomain(string));
			lab.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			
			Text txt = new Text(comp, SWT.None);
			txt.setText(po.getXid(string));
			txt.setEditable(false);
			txt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		}
		
		return comp;
	}
	
	private void init(){
		IAdaptable adapt = getElement();
		po = (PersistentObject) adapt.getAdapter(PersistentObject.class);
	}
	
}
