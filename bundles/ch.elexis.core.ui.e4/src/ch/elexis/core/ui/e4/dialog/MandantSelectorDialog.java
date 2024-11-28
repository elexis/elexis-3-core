package ch.elexis.core.ui.e4.dialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.data.util.NoPoUtil;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.data.Mandant;

public class MandantSelectorDialog extends TitleAreaDialog {
	private List<Mandant> mandantsList;
	private ListViewer uiList;
	private Mandant selMandant;

	private List<Mandant> selMandants;
	private boolean multi;
	private boolean noEmpty;
	private boolean nonActive;
	private boolean saveFilter;

	private static final String CFG_MANDATORFILTER = "rechnungsliste/mandantenfiltered";

	public MandantSelectorDialog(Shell parentShell, boolean multi, boolean noEmpty, boolean nonActive,
			boolean saveFilter) {
		super(parentShell);
		this.multi = multi;
		this.noEmpty = noEmpty;
		this.nonActive = nonActive;
		this.saveFilter = saveFilter;
		selMandant = (Mandant) NoPoUtil.loadAsPersistentObject(
				(Identifiable) ContextServiceHolder.get().getActiveUser().get().getAssignedContact(), Mandant.class);
	}

	public MandantSelectorDialog(Shell parentShell, boolean multi) {
		this(parentShell, multi, true, false, false);
	}

	@Override
	public Control createDialogArea(final Composite parent) {

		Composite radioComposite = new Composite(parent, SWT.NONE);
		radioComposite.setLayout(new GridLayout(2, false));
		Button btnAlle = new Button(radioComposite, SWT.NONE);
		btnAlle.setText("alle");
		btnAlle.addListener(SWT.Selection, e -> {
			uiList.getList().selectAll();
		});
		Button btnKeine = new Button(radioComposite, SWT.NONE);
		btnKeine.setText("keine");
		btnKeine.addListener(SWT.Selection, e -> {
			uiList.getList().deselectAll();
		});

		if (multi) {
			setTitle("Mandanten Auswahl");
			StringBuilder msg = new StringBuilder("Bitte wählen Sie die Mandanten aus.");
			if (noEmpty) {
				msg.append("\nKeine Auswahl selektiert den aktiven Mandanten");
			}
			setMessage(msg.toString());
			uiList = new ListViewer(parent, SWT.BORDER | SWT.MULTI);
		} else {
			setTitle("Mandant ändern");
			setMessage("Bitte wählen Sie einen Mandanten");
			uiList = new ListViewer(parent, SWT.BORDER | SWT.SINGLE);
		}
		uiList.getList().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		mandantsList = getMandantors();

		for (Mandant m : mandantsList) {
			StringBuilder label = new StringBuilder(m.getLabel());
			if (!m.getKuerzel().isBlank()) {
				label.append(" (" + m.getKuerzel() + ")");
			}
			uiList.add(label.toString());
		}
		String idList = ConfigServiceHolder.get().get(ContextServiceHolder.get().getActiveUserContact().get(),
				CFG_MANDATORFILTER, StringUtils.EMPTY);
		for (String id : Arrays.stream(idList.split(",")).toList()) {
			for (int i = 0; i < mandantsList.size(); i++) {
				if (id.equalsIgnoreCase(mandantsList.get(i).getId())) {
					uiList.getList().select(i);
				}
			}
		}
		return uiList.getList();
	}

	@Override
	protected void okPressed() {
		int idx = uiList.getList().getSelectionIndex();
		if (idx > -1) {
			selMandant = mandantsList.get(idx);
		}

		int[] idxs = uiList.getList().getSelectionIndices();
		selMandants = new ArrayList<Mandant>();
		if (idxs != null && idxs.length > 0) {
			for (int i : idxs) {
				selMandants.add(mandantsList.get(i));
			}
		}
		if (saveFilter) {
			List<String> idList = new ArrayList<String>();
			for (Mandant m : selMandants) {
				idList.add(m.getId());
			}
			String r = String.join(",", idList);
			ConfigServiceHolder.get().set(ContextServiceHolder.get().getActiveUserContact().get(), CFG_MANDATORFILTER,
					r);
		}

		super.okPressed();
	}

	public Mandant getSelectedMandant() {
		return selMandant;
	}
	
	public List<Mandant> getSelectedMandants() {
		return selMandants;
	}

	private List<Mandant> getMandantors() {
		IQuery<IUser> userQuery = CoreModelServiceHolder.get().getQuery(IUser.class);
		userQuery.and(ModelPackage.Literals.IUSER__ASSIGNED_CONTACT, COMPARATOR.NOT_EQUALS, null);
		List<IUser> users = userQuery.execute();

		List<Mandant> mandantsList = users.stream()
				.filter(u -> (nonActive || isActive(u)) && isMandator(u))
				.map(u -> Mandant.load(u.getAssignedContact().getId())).collect(Collectors.toList());
		mandantsList.sort(Comparator.comparing(Mandant::getLabel));

		return mandantsList;
	}

	private boolean isMandator(IUser user) {
		return user.getAssignedContact() != null && user.getAssignedContact().isMandator();
	}

	private boolean isActive(IUser user) {
		if (user == null || user.getAssignedContact() == null) {
			return false;
		}
		if (!user.isActive()) {
			return false;
		}
		if (user.getAssignedContact() != null && user.getAssignedContact().isMandator()) {
			IMandator mandator = CoreModelServiceHolder.get().load(user.getAssignedContact().getId(), IMandator.class)
					.orElse(null);
			if (mandator != null && !mandator.isActive()) {
				return false;
			}
		}
		return true;
	}
}
