package ch.elexis.core.ui.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.PlatformUI;

import ch.elexis.core.ac.EvACE;
import ch.elexis.core.ac.Right;
import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.builder.IEncounterBuilder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.CoverageServiceHolder;
import ch.elexis.core.services.holder.EncounterServiceHolder;
import ch.elexis.core.services.holder.LocalLockServiceHolder;
import ch.elexis.core.ui.actions.RestrictedAction;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.icons.Images;
import jakarta.inject.Inject;

public class SelectOrCreateOpenKonsDialog extends TitleAreaDialog {

	private IPatient patient;
	private ComboViewer fallCombo;
	private ComboViewer openKonsCombo;

	private IEncounter konsultation;
	private String title;

	public SelectOrCreateOpenKonsDialog(IPatient patient) {
		super(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		this.patient = patient;
		CoreUiUtil.injectServicesWithContext(this);
	}

	public SelectOrCreateOpenKonsDialog(IPatient patient, String title) {
		this(patient);
		this.title = title;
		CoreUiUtil.injectServicesWithContext(this);
	}

	@Override
	public void create() {
		super.create();
		getShell().setText("Konsultation auswählen"); //$NON-NLS-1$
		setTitle(title); // $NON-NLS-1$
		setMessage(String.format("Erstellen bzw. wählen Sie eine Konsultation für den Patienten %s aus\n", //$NON-NLS-1$
				patient.getLabel()));
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);

		Composite areaComposite = new Composite(composite, SWT.NONE);
		areaComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));

		areaComposite.setLayout(new FormLayout());

		Label lbl = new Label(areaComposite, SWT.NONE);
		lbl.setText("Konsultation erstellen");

		ToolBarManager tbManager = new ToolBarManager(SWT.FLAT | SWT.HORIZONTAL | SWT.WRAP);
		tbManager.add(new RestrictedAction(EvACE.of(IEncounter.class, Right.CREATE), Messages.Core_New_Consultation) {
			{
				setImageDescriptor(Images.IMG_NEW.getImageDescriptor());
				setToolTipText(Messages.Core_Create_new_consultation); // $NON-NLS-1$
			}

			@Override
			public void doRun() {
				IEncounter kons = null;
				ICoverage fall = null;
				IStructuredSelection selection = fallCombo.getStructuredSelection();
				if (selection.isEmpty()) {
					List<ICoverage> openFall = getOpenFall();
					if (openFall.isEmpty()) {
						fall = CoverageServiceHolder.get().createDefaultCoverage(patient);
					}
				} else {
					fall = (ICoverage) selection.getFirstElement();
				}

				if (fall != null) {
					kons = new IEncounterBuilder(CoreModelServiceHolder.get(), fall,
							ContextServiceHolder.get().getActiveMandator().orElse(null)).buildAndSave();
					EncounterServiceHolder.get().addDefaultDiagnosis(kons);
				}

				if (kons != null) {
					LocalLockServiceHolder.get().acquireLock(kons);
					LocalLockServiceHolder.get().releaseLock(kons);
				}
				fallCombo.setInput(getOpenFall());
				if (!selection.isEmpty()) {
					fallCombo.setSelection(selection);
				}
				openKonsCombo.setInput(getOpenKons());
			}
		});
		ToolBar toolbar = tbManager.createControl(areaComposite);

		FormData fd = new FormData();
		fd.top = new FormAttachment(0, 5);
		fd.left = new FormAttachment(0, 5);
		lbl.setLayoutData(fd);

		fd = new FormData();
		fd.top = new FormAttachment(0, 5);
		fd.left = new FormAttachment(30, 5);
		toolbar.setLayoutData(fd);

		fallCombo = new ComboViewer(areaComposite);

		fallCombo.setContentProvider(new ArrayContentProvider());

		fallCombo.setInput(getOpenFall());
		fallCombo.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((ICoverage) element).getLabel();
			}
		});
		fallCombo.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				StructuredSelection selection = (StructuredSelection) fallCombo.getSelection();
				if (!selection.isEmpty()) {
					ContextServiceHolder.get().setTyped(selection.getFirstElement());
				}
			}
		});
		ICoverage selectedFall = ContextServiceHolder.get().getTyped(ICoverage.class).orElse(null);
		if (selectedFall != null) {
			fallCombo.setSelection(new StructuredSelection(selectedFall));
		}

		fd = new FormData();
		fd.top = new FormAttachment(0, 5);
		fd.left = new FormAttachment(toolbar, 5);
		fallCombo.getControl().setLayoutData(fd);

		lbl = new Label(areaComposite, SWT.NONE);
		lbl.setText("Konsultation auswählen");

		openKonsCombo = new ComboViewer(areaComposite);

		openKonsCombo.setContentProvider(new ArrayContentProvider());

		openKonsCombo.setInput(getOpenKons());
		openKonsCombo.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((IEncounter) element).getLabel();
			}
		});

		fd = new FormData();
		fd.top = new FormAttachment(toolbar, 5);
		fd.left = new FormAttachment(0, 5);
		lbl.setLayoutData(fd);

		fd = new FormData();
		fd.top = new FormAttachment(toolbar, 5);
		fd.left = new FormAttachment(30, 5);
		fd.right = new FormAttachment(100, -5);
		openKonsCombo.getControl().setLayoutData(fd);

		return areaComposite;
	}

	private List<ICoverage> getOpenFall() {
		ArrayList<ICoverage> ret = new ArrayList<>();
		List<ICoverage> faelle = patient.getCoverages();
		for (ICoverage f : faelle) {
			if (f.isOpen()) {
				ret.add(f);
			}
		}
		return ret;
	}

	protected List<IEncounter> getOpenKons() {
		ArrayList<IEncounter> ret = new ArrayList<>();
		List<ICoverage> faelle = patient.getCoverages();
		for (ICoverage f : faelle) {
			if (f.isOpen()) {
				List<IEncounter> consultations = f.getEncounters();
				for (IEncounter konsultation : consultations) {
					if (EncounterServiceHolder.get().isEditable(konsultation)) {
						ret.add(konsultation);
					}
				}
			}
		}
		return ret;
	}

	@Override
	public void okPressed() {
		Object obj = ((IStructuredSelection) openKonsCombo.getSelection()).getFirstElement();
		if (obj instanceof IEncounter) {
			konsultation = (IEncounter) obj;
			super.okPressed();
		}
		if (this.getShell() != null && !this.getShell().isDisposed())
			setErrorMessage("Keine Konsultation ausgewählt.");
		return;
	}

	public IEncounter getKonsultation() {
		return konsultation;
	}

	@Optional
	@Inject
	private void createCoverage(@UIEventTopic(ElexisEventTopics.EVENT_CREATE) ICoverage iCoverage) {
		CoreUiUtil.runAsyncIfActive(() -> {
			openKonsCombo.setInput(getOpenKons());
			openKonsCombo.refresh();
		}, openKonsCombo);
	}

	@Optional
	@Inject
	private void updateCoverage(@UIEventTopic(ElexisEventTopics.EVENT_UPDATE) ICoverage iCoverage) {
		CoreUiUtil.runAsyncIfActive(() -> {
			openKonsCombo.setInput(getOpenKons());
			openKonsCombo.refresh();
		}, openKonsCombo);
	}
}
