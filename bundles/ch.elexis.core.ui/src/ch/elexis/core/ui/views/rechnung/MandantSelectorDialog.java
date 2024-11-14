package ch.elexis.core.ui.views.rechnung;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IUser;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.UserServiceHolder;
import ch.elexis.core.ui.util.SWTHelper;

public class MandantSelectorDialog extends TitleAreaDialog {
	private List<IMandator> mandatorsList;
	private org.eclipse.swt.widgets.List lMandators;
	private List<IMandator> selMandators;

	private static final String CFG_MANDATORFILTER = "rechnungsliste/mandantenfiltered";

	public MandantSelectorDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	public Control createDialogArea(final Composite parent) {
		setTitle("Mandanten Auswahl");
		setMessage("Bitte wählen Sie die Mandanten aus.\n");

		Composite radioComposite = new Composite(parent, SWT.NONE);
		radioComposite.setLayout(new GridLayout(2, false));
		Button btnAlle = new Button(radioComposite, SWT.NONE);
		btnAlle.setText("alle");
		btnAlle.addListener(SWT.Selection, e -> {
			lMandators.selectAll();
		});
		Button btnKeine = new Button(radioComposite, SWT.NONE);
		btnKeine.setText("keine");
		btnKeine.addListener(SWT.Selection, e -> {
			lMandators.deselectAll();
		});

		lMandators = new org.eclipse.swt.widgets.List(parent, SWT.BORDER | SWT.MULTI);
		lMandators.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));

		mandatorsList = getUserMandators();
		for (IMandator m : mandatorsList) {
			lMandators.add(m.getLabel());
		}
		String idList = ConfigServiceHolder.get().get(ContextServiceHolder.get().getActiveUserContact().get(),
				CFG_MANDATORFILTER, StringUtils.EMPTY);
		for (String id : Arrays.stream(idList.split(",")).toList()) {
			for (int i = 0; i < mandatorsList.size(); i++) {
				if (id.equalsIgnoreCase(mandatorsList.get(i).getId())) {
					lMandators.select(i);
				}
			}
		}
		return lMandators;
	}

	@Override
	protected void okPressed() {
		int[] idxs = lMandators.getSelectionIndices();
		selMandators = new ArrayList<IMandator>();
		if (idxs != null && idxs.length > 0) {
			for (int i : idxs) {
				selMandators.add(mandatorsList.get(i));
			}
		}
		List<String> idList = new ArrayList<String>();
		for (IMandator m : selMandators) {
			idList.add(m.getId());
		}
		String r = String.join(",", idList);
		ConfigServiceHolder.get().set(ContextServiceHolder.get().getActiveUserContact().get(), CFG_MANDATORFILTER, r);

		super.okPressed();
	}

	private List<IMandator> getUserMandators() {
		Optional<IUser> activeUser = ContextServiceHolder.get().getActiveUser();
		Set<IMandator> set = UserServiceHolder.get().getExecutiveDoctorsWorkingFor(activeUser.get(), false);

		List<IMandator> mandatorsList = new ArrayList<>(set);
		mandatorsList.sort(Comparator.comparing(IMandator::getLabel));
		return mandatorsList;
	}
}