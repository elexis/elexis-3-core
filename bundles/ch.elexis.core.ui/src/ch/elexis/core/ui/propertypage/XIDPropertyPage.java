package ch.elexis.core.ui.propertypage;

import java.util.List;

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

import ch.elexis.core.model.IXid;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IXidService.IXidDomain;
import ch.elexis.core.services.holder.XidServiceHolder;

public class XIDPropertyPage extends PropertyPage implements IWorkbenchPropertyPage {
	
	private Identifiable po;
	
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
		
		List<IXidDomain> domains = XidServiceHolder.get().getDomains();
		
		if (domains.size() == 0) {
			Label lab = new Label(comp, SWT.None);
			lab.setText("Keine XIDs gefunden.");
			lab.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			return comp;
		}
		for (IXidDomain domain : domains) {
			Label lab = new Label(comp, SWT.None);
			lab.setText(domain.getSimpleName());
			lab.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			
			Text txt = new Text(comp, SWT.None);
			IXid xid = XidServiceHolder.get().getXid(po, domain.getDomainName());
			if (xid != null) {
				txt.setText(xid.getDomainId());
			}
			txt.setEditable(false);
			txt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		}
		
		return comp;
	}
	
	private void init(){
		IAdaptable adapt = getElement();
		po = (Identifiable) adapt.getAdapter(Identifiable.class);
	}
	
}
