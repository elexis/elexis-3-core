package ch.elexis.core.ui.contacts.views;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.IDiagnosis;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.EncounterServiceHolder;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.dialogs.DiagnoseSelektor;

public class BillingDiagnosisComposite extends Composite {

	private IPatient patient;

	private List<IDiagnosis> currentDiagnosis;

	public BillingDiagnosisComposite(Composite parent, int style) {
		super(parent, SWT.NONE);

		currentDiagnosis = new ArrayList<>();

		setBackground(parent.getBackground());
		createContent();
	}

	public void setPatient(IPatient patient) {
		this.patient = patient;
		if (patient != null) {
			currentDiagnosis = new ArrayList<>(EncounterServiceHolder.get().getBillingDiagnosis(patient));
			updateContent();
		} else {
			clear();
			createDiagnosisLine(null);
			parentLayout();
		}
	}

	private void updateContent() {
		clear();
		if (currentDiagnosis.isEmpty()) {
			createDiagnosisLine(null);
		} else {
			for (IDiagnosis iDiagnosis : currentDiagnosis) {
				createDiagnosisLine(iDiagnosis);
			}
		}
		parentLayout();
	}

	private void parentLayout() {
		layout();
		ScrolledForm parentScrolledForm = null;
		Composite parent = getParent();
		while (parent != null) {
			if (parent instanceof ScrolledForm) {
				parentScrolledForm = (ScrolledForm) parent;
				break;
			}
			parent = parent.getParent();
		}
		if (parentScrolledForm != null && !parentScrolledForm.isDisposed()) {
			parentScrolledForm.getBody().requestLayout();
		} else if (!getParent().isDisposed()) {
			getParent().requestLayout();
		}
	}

	private void clear() {
		for (Control control : getChildren()) {
			control.setVisible(false);
			control.dispose();
		}
	}

	private void createContent() {
		GridLayout layout = new GridLayout(3, false);
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		setLayout(layout);
		createDiagnosisLine(null);
	}

	private void createDiagnosisLine(IDiagnosis diagnosis) {
		Hyperlink openDiagnosisLink = new Hyperlink(this, SWT.NONE);
		openDiagnosisLink.setUnderlined(true);
		openDiagnosisLink.setText(Messages.Core_BillingDiagnosis);
		openDiagnosisLink.setForeground(UiDesk.getColorRegistry().get(UiDesk.COL_BLUE));
		openDiagnosisLink.setBackground(getBackground());
		openDiagnosisLink.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				DiagnoseSelektor dsl = new DiagnoseSelektor(getShell());
				if (dsl.open() == Dialog.OK) {
					Object[] sel = dsl.getResult();
					if (sel != null && sel.length > 0) {
						IDiagnosis selDiagnosis = (IDiagnosis) e.widget.getData(IDiagnosis.class.getSimpleName());
						if (selDiagnosis != null) {
							currentDiagnosis.remove(selDiagnosis);
						}
						IDiagnosis diagnose = (IDiagnosis) sel[0];
						currentDiagnosis.add(diagnose);
						EncounterServiceHolder.get().setBillingDiagnosis(currentDiagnosis, patient);
						CoreModelServiceHolder.get().save(patient);
						updateContent();
					} else {
						IDiagnosis selDiagnosis = (IDiagnosis) e.widget.getData(IDiagnosis.class.getSimpleName());
						if (selDiagnosis != null) {
							currentDiagnosis.remove(selDiagnosis);
							EncounterServiceHolder.get().setBillingDiagnosis(currentDiagnosis, patient);
							CoreModelServiceHolder.get().save(patient);
							updateContent();							
						}
					}
				}
			}
		});
		openDiagnosisLink.setData(IDiagnosis.class.getSimpleName(), diagnosis);
		openDiagnosisLink.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		Label diagnosisLabel = new Label(this, SWT.NONE);
		if (diagnosis != null) {
			diagnosisLabel.setText(diagnosis.getLabel());
		} else {
			diagnosisLabel.setText(StringUtils.EMPTY);
		}
		if (isFirstLine()) {
			diagnosisLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

			Hyperlink addDiagnosisLink = new Hyperlink(this, SWT.NONE);
			addDiagnosisLink.setUnderlined(true);
			addDiagnosisLink.setText(Messages.Core_Add);
			addDiagnosisLink.setForeground(UiDesk.getColorRegistry().get(UiDesk.COL_BLUE));
			addDiagnosisLink.setBackground(getBackground());
			addDiagnosisLink.addHyperlinkListener(new HyperlinkAdapter() {
				@Override
				public void linkActivated(HyperlinkEvent e) {
					DiagnoseSelektor dsl = new DiagnoseSelektor(getShell());
					if (dsl.open() == Dialog.OK) {
						Object[] sel = dsl.getResult();
						if (sel != null && sel.length > 0) {
							IDiagnosis diagnose = (IDiagnosis) sel[0];
							currentDiagnosis.add(diagnose);
							EncounterServiceHolder.get().setBillingDiagnosis(currentDiagnosis, patient);
							CoreModelServiceHolder.get().save(patient);
							updateContent();
						}
					}
				}
			});
			addDiagnosisLink.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		} else {
			diagnosisLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		}
	}

	private boolean isFirstLine() {
		int counter = 0;
		for (Control control : getChildren()) {
			if (control instanceof Hyperlink) {
				if (++counter > 1) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);

		for (Control control : getChildren()) {
			if (control instanceof Hyperlink) {
				if (enabled) {
					control.setForeground(UiDesk.getColor(UiDesk.COL_BLUE));
				} else {
					control.setForeground(UiDesk.getColor(UiDesk.COL_GREY));
				}
			}
		}
	}
}
