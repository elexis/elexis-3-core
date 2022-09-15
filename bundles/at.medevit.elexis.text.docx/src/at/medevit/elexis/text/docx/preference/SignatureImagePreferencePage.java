package at.medevit.elexis.text.docx.preference;

import java.util.List;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.model.IUser;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.CoreModelServiceHolder;

public class SignatureImagePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private Composite parentComposite;

	@Override
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub

	}

	@Override
	protected Control createContents(Composite parent) {
		parentComposite = new Composite(parent, SWT.NONE);
		parentComposite.setLayout(new GridLayout(2, true));
		for (IUser user : getUsers()) {
			if (user.isActive()) {
				SignatureImageComposite signatureImageComposite = new SignatureImageComposite(user, parentComposite);
				signatureImageComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			}
		}

		return parentComposite;
	}

	private List<IUser> getUsers() {
		IQuery<IUser> userQuery = CoreModelServiceHolder.get().getQuery(IUser.class);
		userQuery.and(ModelPackage.Literals.IUSER__ASSIGNED_CONTACT, COMPARATOR.NOT_EQUALS, null);
		return userQuery.execute();
	}
}
