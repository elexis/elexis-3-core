package ch.elexis.core.ui.preferences;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.status.ElexisStatus;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.views.IDetailDisplay;

public class ServiceDiagnosePrefs extends PreferencePage implements IWorkbenchPreferencePage {
	private static final String FAVORITES = "Favoriten";
	private ComboViewer cmbViewer;
	private TableViewer viewer, viewerAvailable;
	private List<String> input, aInput;

	enum ViewType {
		Leistungen, Diagnose, Codes
	}

	public ServiceDiagnosePrefs() {
		super("Leistungen u. Diagnosen");
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayout(new GridLayout(3, true));

		Label lblView = new Label(ret, SWT.NONE);
		lblView.setText(Messages.ServiceDiagnosis_View);
		lblView.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

		cmbViewer = new ComboViewer(ret, SWT.READ_ONLY);
		Combo combo = cmbViewer.getCombo();
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		cmbViewer.setContentProvider(new ArrayContentProvider());
		cmbViewer.setLabelProvider(new LabelProvider());
		cmbViewer.setInput(ViewType.values());
		cmbViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				ViewType view = (ViewType) selection.getFirstElement();
				loadInput(view);
			}
		});

		int operations = DND.DROP_MOVE | DND.DROP_TARGET_MOVE;
		Transfer[] transferTypes = new Transfer[] { TextTransfer.getInstance() };

		new Label(ret, SWT.NONE);
		new Label(ret, SWT.NONE);
		new Label(ret, SWT.NONE);

		Label lblDisplayed = new Label(ret, SWT.NONE);
		lblDisplayed.setText(Messages.ServiceDiagnosis_Displayed);
		new Label(ret, SWT.NONE);
		Label lblAvailable = new Label(ret, SWT.NONE);
		lblAvailable.setText(Messages.ServiceDiagnosis_Available);

		Label label = new Label(ret, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

		viewer = new TableViewer(ret, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		Table table = viewer.getTable();
		table.setHeaderVisible(false);
		table.setLinesVisible(false);
		GridData gd_v = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		gd_v.widthHint = 150;
		gd_v.heightHint = 200;
		gd_v.minimumWidth = 150;
		gd_v.minimumHeight = 200;
		table.setLayoutData(gd_v);
		createColumn(ret, viewer);
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setInput(input);
		viewer.addDragSupport(operations, transferTypes, new DragListener(viewer, true));
		viewer.addDropSupport(operations, transferTypes, new DropListener(viewer, true));

		Composite btnComposite = new Composite(ret, SWT.NONE);
		btnComposite.setLayout(new GridLayout(1, false));
		btnComposite.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false));

		Button btnUp = new Button(btnComposite, SWT.PUSH);
		btnUp.setImage(Images.IMG_ARROWUP.getImage());
		btnUp.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		btnUp.setToolTipText(Messages.ServiceDiagnosis_UpTooltip);
		btnUp.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				String value = (String) selection.getFirstElement();
				int idx = input.indexOf(value);

				if (idx > 0) {
					input.remove(value);
					input.add(idx - 1, value);
					viewer.refresh();
				}
			}
		});

		Button btnDown = new Button(btnComposite, SWT.PUSH);
		btnDown.setImage(Images.IMG_ARROWDOWN.getImage());
		btnDown.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		btnDown.setToolTipText(Messages.ServiceDiagnosis_DownTooltip);
		btnDown.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				String value = (String) selection.getFirstElement();
				int idx = input.indexOf(value);

				if (idx != -1 && idx < input.size() - 1) {
					input.remove(value);
					input.add(idx + 1, value);
					viewer.refresh();
				}
			}
		});

		viewerAvailable = new TableViewer(ret, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		table = viewerAvailable.getTable();
		table.setHeaderVisible(false);
		table.setLinesVisible(false);
		GridData gd_va = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		gd_va.widthHint = 150;
		gd_va.heightHint = 200;
		gd_va.minimumWidth = 150;
		gd_va.minimumHeight = 200;
		table.setLayoutData(gd_va);
		createColumn(ret, viewerAvailable);
		viewerAvailable.setContentProvider(new ArrayContentProvider());
		viewerAvailable.setInput(aInput);

		viewerAvailable.addDragSupport(operations, transferTypes, new DragListener(viewerAvailable, false));
		viewerAvailable.addDropSupport(operations, transferTypes, new DropListener(viewerAvailable, false));

		cmbViewer.setSelection(new StructuredSelection(ViewType.Leistungen));
		return ret;
	}

	private void createColumn(final Composite parent, final TableViewer viewer) {
		TableViewerColumn tvCol = new TableViewerColumn(viewer, SWT.NONE);
		tvCol.setLabelProvider(new ColumnLabelProvider());
		TableColumn column = tvCol.getColumn();
		column.setText(StringUtils.EMPTY);
		column.setWidth(150);
		column.setResizable(false);
		column.setMoveable(false);
	}

	private void loadInput(ViewType view) {
		String[] settings = new String[] {};
		input = new ArrayList<>();
		aInput = new ArrayList<>();

		switch (view) {
		case Leistungen:
			aInput.addAll(findPagesFor(ExtensionPointConstantsUi.VERRECHNUNGSCODE, null));
			aInput.add(FAVORITES);

			settings = ConfigServiceHolder.getUser(Preferences.USR_SERVICES_DIAGNOSES_SRV, getListAsString(aInput))
					.split(","); //$NON-NLS-1$
			break;
		case Diagnose:
			aInput.addAll(findPagesFor(ExtensionPointConstantsUi.DIAGNOSECODE, null));
			settings = ConfigServiceHolder.getUser(Preferences.USR_SERVICES_DIAGNOSES_DIAGNOSE, getListAsString(aInput))
					.split(","); //$NON-NLS-1$
			break;
		case Codes:
			aInput.addAll(findPagesFor(ExtensionPointConstantsUi.VERRECHNUNGSCODE, "Artikel"));
			aInput.addAll(findPagesFor(ExtensionPointConstantsUi.DIAGNOSECODE, "Artikel"));
			aInput.addAll(findPagesFor(ExtensionPointConstantsUi.GENERICCODE, "Artikel"));
			aInput.add(FAVORITES);
			settings = ConfigServiceHolder.getUser(Preferences.USR_SERVICES_DIAGNOSES_CODES, getListAsString(aInput))
					.split(","); //$NON-NLS-1$
			break;
		default:
			break;
		}

		for (String s : settings) {
			input.add(s);
			aInput.remove(s);
		}

		viewer.setInput(input);
		viewerAvailable.setInput(aInput);
	}

	private String getListAsString(List<String> list) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < list.size(); i++) {
			sb.append(list.get(i));

			if (i != (list.size() - 1)) {
				sb.append(","); //$NON-NLS-1$
			}
		}
		return sb.toString();
	}

	private List<String> findPagesFor(String point, String ignore) {
		List<String> pageNames = new ArrayList<>();
		List<IConfigurationElement> list = Extensions.getExtensions(point);
		for (IConfigurationElement ce : list) {
			try {
				if (ignore != null && ignore.equals(ce.getName())) {
					continue;
				}
				IDetailDisplay d = (IDetailDisplay) ce.createExecutableExtension("CodeDetailDisplay"); //$NON-NLS-1$

				pageNames.add(d.getTitle().trim());
			} catch (Exception ex) {
				new ElexisStatus(ElexisStatus.WARNING, Hub.PLUGIN_ID, ElexisStatus.CODE_NONE,
						"Fehler beim Laden von " + ce.getName(), ex, ElexisStatus.LOG_WARNINGS);
			}
		}
		return pageNames;
	}

	@Override
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void performApply() {
		IStructuredSelection selection = (IStructuredSelection) cmbViewer.getSelection();
		ViewType type = (ViewType) selection.getFirstElement();

		if (type != null) {
			String listString = getListAsString(input);

			switch (type) {
			case Leistungen:
				ConfigServiceHolder.setUser(Preferences.USR_SERVICES_DIAGNOSES_SRV, listString);
				break;
			case Diagnose:
				ConfigServiceHolder.setUser(Preferences.USR_SERVICES_DIAGNOSES_DIAGNOSE, listString);
				break;
			case Codes:
				ConfigServiceHolder.setUser(Preferences.USR_SERVICES_DIAGNOSES_CODES, listString);
				break;
			default:
				break;
			}
		}
		super.performApply();
	}

	class DropListener extends ViewerDropAdapter {
		private final TableViewer viewer;
		private boolean isUserSet;

		protected DropListener(Viewer viewer, boolean isUserSet) {
			super(viewer);
			this.viewer = (TableViewer) viewer;
			this.isUserSet = isUserSet;
		}

		@Override
		public boolean performDrop(Object data) {
			if (isUserSet) {
				input.add(data.toString());
			} else {
				aInput.add(data.toString());
			}
			viewer.refresh();

			return true;
		}

		@Override
		public boolean validateDrop(Object target, int operation, TransferData transferType) {
			return true;
		}
	}

	class DragListener implements DragSourceListener {
		private final TableViewer viewer;
		private boolean isUserSet;
		private String movedValue;

		public DragListener(Viewer viewer, boolean isUserSet) {
			this.viewer = (TableViewer) viewer;
			this.isUserSet = isUserSet;
		}

		@Override
		public void dragSetData(DragSourceEvent event) {
			IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
			String value = (String) selection.getFirstElement();

			if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
				event.data = value;
				movedValue = value;
			}
		}

		public void dragStart(DragSourceEvent event) {
		}

		@Override
		public void dragFinished(DragSourceEvent event) {
			if (isUserSet) {
				input.remove(movedValue);
			} else {
				aInput.remove(movedValue);
			}
			viewer.refresh();
		}

	}
}
