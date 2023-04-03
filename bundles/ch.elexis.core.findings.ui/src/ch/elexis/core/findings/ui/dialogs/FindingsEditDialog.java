package ch.elexis.core.findings.ui.dialogs;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.data.service.LocalLockServiceHolder;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.IObservation.ObservationType;
import ch.elexis.core.findings.IObservationLink.ObservationLinkType;
import ch.elexis.core.findings.ObservationComponent;
import ch.elexis.core.findings.ui.action.DateAction;
import ch.elexis.core.findings.ui.composites.CompositeBoolean;
import ch.elexis.core.findings.ui.composites.CompositeDate;
import ch.elexis.core.findings.ui.composites.CompositeGroup;
import ch.elexis.core.findings.ui.composites.CompositeTextUnit;
import ch.elexis.core.findings.ui.composites.ICompositeSaveable;
import ch.elexis.core.findings.ui.util.FindingsUiUtil;
import ch.elexis.core.findings.util.ModelUtil;
import ch.elexis.core.findings.util.commands.UpdateFindingTextCommand;

public class FindingsEditDialog extends TitleAreaDialog {

	private final IFinding iFinding;
	private ICompositeSaveable iCompositeSaveable;

	private List<IFinding> lockedFindings = new ArrayList<>();

	public FindingsEditDialog(Shell parentShell, IFinding iFinding) {
		super(parentShell);
		this.iFinding = iFinding;
		lockedFindings.clear();
	}

	/**
	 * Create contents of the dialog.
	 *
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		String title = FindingsUiUtil.getTypeAsText(iFinding);
		setTitle("Befund");
		setMessage(title);
		int depth = 0;
		iCompositeSaveable = new CompositeGroup(parent, iFinding, false, false, 10, 10, depth++);
		try {
			iCompositeSaveable.getChildReferences().add(createDynamicContent(iFinding, iCompositeSaveable, depth));
		} catch (ElexisException e) {
			MessageDialog.openError(getShell(), "Fehler", e.getMessage());
			cancelPressed();
		}
		return (Control) iCompositeSaveable;
	}

	private ICompositeSaveable createDynamicContent(IFinding iFinding, ICompositeSaveable current, int depth)
			throws ElexisException {
		if (!LocalLockServiceHolder.get().acquireLock(iFinding).isOk()) {
			throw new ElexisException("Die Editierung ist nicht möglich, kein Lock erhalten.");
		}
		lockedFindings.add(iFinding);
		if (iFinding instanceof IObservation) {
			IObservation item = (IObservation) iFinding;
			List<IObservation> refChildrens = item.getTargetObseravtions(ObservationLinkType.REF);
			StringBuilder sb = new StringBuilder();
			refChildrens.stream()
					.forEach(o -> sb.append(o.getCoding().get(0).getDisplay()).append((o).isDeleted()).append(","));
			List<ObservationComponent> compChildrens = item.getComponents();
			if (refChildrens.isEmpty() && compChildrens.isEmpty()) {
				current = createComposite((Composite) current, item, null);
			} else {
				if (!refChildrens.isEmpty()) {
					current = new CompositeGroup((Composite) current, item, true, false, 0, 10, depth);
					for (IObservation child : refChildrens) {
						ICompositeSaveable childComposite = createDynamicContent(child, current, ++depth);
						current.getChildReferences().add(childComposite);
					}
				}
				if (!compChildrens.isEmpty()) {
					// show as component
					current = new CompositeGroup((Composite) current, item, false, false, 0, 5, depth);

					Group group = new Group((Composite) current, SWT.NONE);
					group.setText(StringUtils.EMPTY);

					GridLayout gd = new GridLayout(2, false);
					gd.marginHeight = 0;
					gd.marginBottom = 10;
					gd.verticalSpacing = 0;
					gd.marginTop = 0;
					group.setLayout(gd);
					group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
					Composite groupComposite = new Composite(group, SWT.NONE);
					GridLayout gd2 = new GridLayout(2, false);
					gd2.marginHeight = 0;
					gd2.marginBottom = 5;
					gd2.verticalSpacing = 0;
					gd2.marginTop = 0;
					groupComposite.setLayout(gd2);
					groupComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
					Label lblTitle = new Label(groupComposite, SWT.NONE);
					lblTitle.setText(current.getTitle());
					lblTitle.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1));
					current.setToolbarActions(FindingsUiUtil.createToolbarSubComponents(groupComposite, item, 1));

					boolean allUnitsSame = ModelUtil.getExactUnitOfComponent(compChildrens) != null;
					int i = 0;

					for (ObservationComponent child : compChildrens) {
						i++;
						ICompositeSaveable childComposite = createComposite(group, iFinding, child);
						current.getChildComponents().add(childComposite);
						if (allUnitsSame) {
							if (childComposite instanceof CompositeTextUnit) {
								childComposite.hideLabel(i < compChildrens.size());
							}
						}

					}

				}
			}
		} else {
			current = createComposite((Composite) current, iFinding, null);
		}

		return current;
	}

	private ICompositeSaveable createComposite(Composite parent, IFinding iFinding,
			ObservationComponent backboneComponent) {
		if (iFinding instanceof IObservation) {
			ObservationType type = ((IObservation) iFinding).getObservationType();
			switch (type) {
			case COMP:
			case NUMERIC:
			case TEXT:
				return new CompositeTextUnit(parent, iFinding, backboneComponent);
			case BOOLEAN:
				return new CompositeBoolean(parent, iFinding, backboneComponent);
			case DATE:
				return new CompositeDate(parent, iFinding, backboneComponent);
			default:
				throw new IllegalStateException(
						"No composite for observation type [" + type + "] of [" + iFinding + "]");
			}
		}
		throw new IllegalStateException("No composite for finding [" + iFinding + "]");
	}

	/**
	 * Create contents of the button bar.
	 *
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		super.buttonPressed(buttonId);
	}

	@Override
	protected void okPressed() {
		if (iCompositeSaveable != null) {
			LocalDateTime localDateTime = LocalDateTime.now();
			List<Action> actions = iCompositeSaveable.getToolbarActions();
			if (actions != null) {
				for (Action action : actions) {
					if (action instanceof DateAction) {
						localDateTime = ((DateAction) action).getLocalDateTime();
					}
				}
			}
			// set the values
			FindingsUiUtil.saveGroup(iCompositeSaveable);
			// set time and comment, including all sub components
			iCompositeSaveable.saveContents(localDateTime);
			try {
				new UpdateFindingTextCommand(iFinding).execute();
			} catch (ElexisException e) {
				MessageDialog.openError(getShell(), "Fehler", "Fehler bei der Generierung des Texts der Beobachtung.");
			}
		}
		super.okPressed();
	}

	public void releaseAllLocks() {
		for (IFinding iFinding : lockedFindings) {
			LocalLockServiceHolder.get().releaseLock(iFinding);
		}
	}

	public List<IFinding> getLockedFindings() {
		return lockedFindings;
	}
}
